import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

/**
 * Class 'MessageServer' that starts the server, listening on a specific port.
 * Whenever a client gets connected an instance of 'ClientConnection' is created 
 * to serve the client. The server is able to hadle multiple clients at the same 
 * time since each connection is processed in a separate thread.
 */
public class MessageServer {
	private int port;
	
	// Hashmaps to keep track of registered clients, client groups and topics respectively
	private Map<String,ClientConnection> clientList = new HashMap<>() ;
	private Map<String, Map<String, ClientConnection>> groups = new HashMap<>() ;
	private Map<String, Map<String, ClientConnection>> topics = new HashMap<>() ;
	
	// Constructor
	public MessageServer ( int serverPort ) 
		throws IOException {
		this.port = serverPort;

	}
	
	public void execute() {
        	try (ServerSocket server = new ServerSocket(port)) { // Start a new server that listens at the given port
			
			// System message to indicate that the server has been started successfully 
			// and is waiting for connections 			
            		System.out.println ( "MessageServer: started, listening for client connect ...");
 			
			// Loop that waits for (multiple) client connections.
			// A new ClientConnection instance is created and started for each 
			// accepted client connection.
		    	while (true) {
		        	Socket socket = server.accept();
				System.out.println ( "MessageServer: client connected!");
				ClientConnection client = new ClientConnection(socket, this);
				client.start();
		    	}
		} catch (IOException ex) { // Handle IO exceptions
		    System.out.println("Error in the server: " + ex.getMessage());
		    ex.printStackTrace();
		}
	    }
	
	// Main method
	public static synchronized void main(String[] args) {
	        if (args.length < 1) {
		    System.out.println("Syntax: java MessageServer <port-number>");
		    System.exit(0);
		}
		System.out.println ( "Messenger> start");
		try {
			// Get the port from console, create a MessageServer instance
			// and run the server.
			int port = Integer.parseInt( args[0] );
			MessageServer server = new MessageServer ( port );
			server.execute();
		}
		catch ( IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println ( "Messenger> finished");
	} 
	
	/**
	 * Delivers a message from one user to others (broadcasting)
	 */
	public synchronized void broadcast(String message, String excludeClient) {
		// Loop through the client list and broadcast the message
		clientList
			.forEach( (k, v) -> {
				if (!k.equals(excludeClient)) { // exclude sender client
					v.printMessage(message);	
				}
			}
		);
	}
	
	/**
	 * Send personal message to a user (including yourself like in Messenger)
	 */
	public synchronized void personalMsg(String message, String clientName, ClientConnection client) {
		if (isInClients(clientName)) { // Check if the client name is in the client list
			(clientList.get(clientName)).printMessage(message);
		}
		else {
			client.printMessage("User with such name does not exist!");
		}
	}

	/**
	 * Creates a group (if not already created) and notifies the users
	 */
	public synchronized void createGroup(String groupName, ClientConnection client) {
		if ( ! groups.containsKey(groupName) ) { // If the group is not already created
			groups.put(groupName, new HashMap<String, ClientConnection>()) ;
			broadcast("Group: " + groupName + " created!", null);
		}
		else {
			client.printMessage("Group already exists!");
		}
	}
	
	/**
	 * Removes a group (if exists) and notifies the users
	 */
	public synchronized void removeGroup(String groupName, ClientConnection client) {
		if ( groups.containsKey(groupName) ) { 
			groups.remove(groupName);
			broadcast("Group: " + groupName + " removed!", null);
		}		
		else {
			client.printMessage("Group does not exists!");
		}
	}
	
	/**
	 * Lets clients join a group and notifies the group about the new member
	 */
	public synchronized void joinGroup(String groupName,String clientName, ClientConnection client) {
		if ( groups.containsKey(groupName) ) {
			if (!(groups.get(groupName)).containsKey(clientName)) {
				(groups.get(groupName)).put(clientName, client);
				client.printMessage("You have joined the group!");
				sendToGroup(groupName, "User '" + clientName + "' has joined the group!", clientName, client);
			}
			else {
				client.printMessage("You are already in the group");
			}
		}		
		else {
			client.printMessage("Such group does not exist!");
		}
	}

	/**
	 * Sends given message to a particular group
	 */
	public synchronized void sendToGroup(String groupName,String message, String clientName, ClientConnection client) {
		if ( groups.containsKey(groupName) ) { // If the group exists
			if ( (groups.get(groupName)).containsKey(clientName) ) { // If the sender is actually a group member
				(groups.get(groupName))
					.forEach( (k, v) -> {
						if (!k.equals(clientName)) v.printMessage(message); //exclude sender client
					}
				);
			}
			else {
				client.printMessage("You have to be in the group to send messages to the group members!");
			}
		}
		else {
			client.printMessage("Such group does not exist!");
		}	
	}	

	/**
	 * Adds client to the list of clients
	 */
	public synchronized void addUser(String clientName, ClientConnection client) {
		if (!clientName.isEmpty()) {
			clientList.put(clientName, client);
			client.printMessage("You have registered successfully!");
		}
		else {
			client.printMessage("Please provide a name!");
		}
	}
	
	/**
	 * Removes client from group and notifies the group members
	 */
	public synchronized void leaveGroup(String groupName, String clientName, ClientConnection client) {
		if (groups.containsKey(groupName)) { // If the group exists
			if ((groups.get(groupName)).containsKey(clientName)) { // If the user is a member
				sendToGroup(groupName, "User '" + clientName + "' has left the group!", clientName, client);
				(groups.get(groupName)).remove(clientName);
				client.printMessage("You have left the group!");
			}		
			else {
				client.printMessage("You cannot leave a group u haven't joined!");
			}
		}
		else {
			client.printMessage("Such group does not exist!");
		}
	}
	
	/**
	 * Removes client from the list of clients (no if-else statements 
	 * because every user needs to be registered so that they can send messages)
	 */
	public void unregister(String clientName) {
		clientList.remove(clientName);
	}
	
	/**
	 * Creates new topic in topics hashmap
	 */
	public synchronized void createTopic(String topic, ClientConnection client) {
		if ( !topic.isEmpty()) { // If provided with topic name
			if (!isInTopics(topic)) { // If the topic is not already created
				topics.put(topic, new HashMap<String, ClientConnection>());
				broadcast("There is a new topic: '" + topic +"'", null);
			}
			else {
				client.printMessage("Topic '" + topic + "' already created!");
			}
		}
		else {
			client.printMessage("/topic <topic name>");
		}
	}
	
	/**
	 * Subscribes a client to a given topic and notifies the already subscribed users
	 */
	public synchronized void subscribe(String topic, String clientName, ClientConnection client) {
		if ( isInTopics(topic) ) { // If topic exists
			if ( !topics.get(topic).containsKey(clientName) ) { // If the user is subscribed to the topic
				sendToTopic(topic, "User '" + clientName + "' has subscribed to the '" + topic + "' topic!", clientName);
				(topics.get(topic)).put(clientName, client);
				client.printMessage("You have successfully subscribed to topic '" + topic +"'!");
			}
			else {
				client.printMessage("You have already subscribed to the topic!");
			}
		}
		else {
			client.printMessage("Such topic does not exist!");
		}
	}

	/**
	 * Unsubscribes a client from a given topic and notifies the remaining subscribers
	 */
	public synchronized void unsubscribe(String topic, String clientName, ClientConnection client) {
		if ( isInTopics(topic) ) { // If topic exists
			if ( topics.get(topic).containsKey(clientName) ) { // If user is subscribed to the topic
				sendToTopic(topic, "User '" + clientName + "' has unsubscribed from the '" + topic + "' topic!", clientName);
				(topics.get(topic)).remove(clientName);
				client.printMessage("You have unsubscribed from the topic!");
			}
			else {
				client.printMessage("You aren't subscribed to this topic!");
			}
		}
		else {
			client.printMessage("Such topic does not exist!");
		}
			
	}
	
	/**
	 * Sends message to clients subscribed to the topic given
	 */
	public synchronized void sendToTopic(String topic, String message, String clientName) {
		(topics.get(topic))
			.forEach( (k, v) -> {
				if(!k.equals(clientName)) v.printMessage("Topic '" + topic + "' was mentioned: [" + clientName + "]: " + message);
			}
		);
	}

	/**
	 * Returns groups hashmap
	 */
	public synchronized Map getTopics() {
		return topics;
	}
		
	/**
	 * Returns the list of registered clients (users)
	 */
	public synchronized Map<String, ClientConnection> getClientList() {
		return clientList;
	}

	/**
	 * Returns groups hashmap
	 */
	public synchronized Set<String> getGroups() {
		return groups.keySet();
	}

	/**
	 * Returns true if the client is already in the client list
	 */
	public synchronized boolean isInClients(String clientName) {
		return clientList.containsKey(clientName);
	}

	/**
	 * Returns true if the given topic is found in the topics
	 */
	public synchronized boolean isInTopics(String topic) {
		return topics.containsKey(topic);
	}
}
