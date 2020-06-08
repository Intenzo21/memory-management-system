import java.io.IOException;
import java.io.PrintWriter;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.net.Socket;
import java.util.*;

/**
 * Class 'ClientConnection' that is instantiated by the server
 * whenever a client connects to the server and acts as the “receiver” 
 * for any messages received from such a client.
 */
public class ClientConnection extends Thread {

	private Socket clientSocket;
	private MessageServer server;
	private PrintWriter out;
	private String clientName; // To hold the name of the user
	
	// Constructor
	public ClientConnection( Socket clientSocket, MessageServer server ) 
		throws IOException, NullPointerException {

		this.clientSocket = clientSocket;
		this.server = server;
        	setPriority( NORM_PRIORITY - 1 );
	}

	public synchronized void run() {   
			
		try {
			// Read the input from server
			BufferedReader in = new BufferedReader( new InputStreamReader( this.clientSocket.getInputStream() ) );
			
			// Writer to send an output to server
			out = new PrintWriter( new OutputStreamWriter( this.clientSocket.getOutputStream() ), true );
			
			// Show currently registered users
			printUsers();

			// The first input from the server is the client name so we 
			// assign it to a variable for later use
			clientName = in.readLine();

			// Prompt the user for a new name if the previously picked 
			// one is already in the client list
			while (server.isInClients(clientName) || clientName.isEmpty()){
				printMessage("Please try again: ");
				clientName = in.readLine();
			}

			// Add the user to the client list
			server.addUser(clientName, this);
			
			// Broadcast a message stating that a new user has connected successfully
			String serverMsg = "New user connected: " + clientName;
			server.broadcast(serverMsg, clientName);
			
			// While the socket is open
			while ( ! clientSocket.isClosed() ) {

				// Read from the server and assign it to a new variable
				String clientMsg = in.readLine();

				// Prompt the user for input if no text (command) is provided
				while (clientMsg.isEmpty()){
					printMessage("Please use a command from the command list!");
					clientMsg = in.readLine();
				}

				// Create a scanner to scan the input message from the server
				Scanner sc = new Scanner(clientMsg);

				// Assign the first scanned word to a variable so we can check 
				// whether it is a command from the command list
				String first = (sc.next()).toLowerCase();

				// Second scanned word to be used for the commands
				String second = "";
				if(sc.hasNext()) second = sc.next();
				
				// Default message string
				String message = "[" + clientName + "]:";
				
				// If the first word is '/all' broadcast the message to all users
				// else check for commands
				if ( first.equals("/all") ) {
					message += clientMsg.substring(4);
					server.broadcast(message, clientName);
				}
				else {
					/**
					 * Scanner to scan the client message for topics.
					 * If a topic is found then send the message to all
					 * users subscribed to the found topic.
					 * If topic is mentioned while using the '/all' command the
					 * message is sent only once. If a user is subscribed to two
					 * of the mentioned topics then the message will be printed out twice...
					 * (The message sent when scanning for a topic is the whole line 
					 * including the command (if typed). I left it out because I 
					 * thought it wouldn't cause a issue...)
					 */		
					Scanner tp = new Scanner(clientMsg);
					String word;
					while(tp.hasNext()) {
						word = tp.next();
						if ( server.isInTopics(word) ) server.sendToTopic(word, clientMsg, clientName);
					}

					// Switch statements to decide which method to call from the server
					// according to the command the user has chosen.
					switch ( first ) { // Check if the first word is a command
					case "/exit": // Handled by the try-catch statements but just in case
						server.unregister(clientName);
						break;
					case "/users": // Print available users
						printUsers();
						break;
					case "/create": // Create a new group
						server.createGroup(second, this);
						break;
					case "/join": // Allows the user to join a group
						server.joinGroup(second,clientName, this);
						break;
					case "/remove": // Remove a group
						server.removeGroup(second, this);
						break;
					case "/groups": // Print available groups
						printMessage("Current groups: " + server.getGroups());
						break;
					case "/leave": // Leave a group
						server.leaveGroup(second, clientName, this);
						break;
					case "/topic": // Create a topic 
						server.createTopic(second, this);
						break;
					case "/topics": // Print current topics
						printMessage("Current topics: " + (server.getTopics()).keySet());
						break;
					case "/sub": // Subscribe to topic
						server.subscribe(second, clientName, this);
						break;
					case "/unsub": // Unsubscribe from topic
						server.unsubscribe(second, clientName, this);
						break;
					case "/pm": // Send personal message
						if ( !second.isEmpty() ) {
							message += clientMsg.substring( 4 + second.length() );
							server.personalMsg(message, second, this);
						}
						else {
							printMessage("/pm <name> <text>");
						}
						break;
					case "/gm": // Send group message
						if ( !second.isEmpty() ) {
							message += clientMsg.substring( 4 + second.length() );
							server.sendToGroup(second, message, clientName, this);
						}					
						else {
							printMessage("/gm <group name> <text>");
						}
						break;
					default:
						// Print invalid command message if the first word starts with 
						// a slash but is not a command
						if ( first.charAt(0) == '/' ) { 
							printMessage("Invalid command!");
							break;
						}
					}
					// Close scanners
					tp.close();
				}
				sc.close();
			}
			// Close resources
			in.close();
			out.close();
			clientSocket.close();
		}
        	catch ( IOException e ) { // Handle IO exceptions
            		System.out.println( "I/O Error: " + e );
        	}
		catch ( NullPointerException np ) { // Handle NullPointer exceptions
			server.broadcast("User '" + clientName + "' has quit.", clientName);
			server.unregister(clientName); // Unregister 
			System.out.println( "Client has disconnected" );
		}
	}

	/**
	 * Sends a list of online users to the newly connected user.
	 */
	public void printUsers() {
		if (!(server.getClientList()).isEmpty()) {
	    		printMessage("Connected users: " + (server.getClientList()).keySet());
		} else {
	    		printMessage("No other users connected!");
		}
	}	
	/**
	 * Sends a message to the client.
	 */
	public void printMessage(String message) {
		out.println(message);
	}
}
