import java.net.*;
import java.io.*;
import java.util.*;
/**
 * Class 'MessageClient' starts the client program, connects to a server specified by 
 * hostname/IP address and port number. Once the connection is made, it creates and starts 
 * two threads MessageReadThread and MessageWriteThread. Type '/exit' to terminate.
 */
public class MessageClient {
	private String hostname;
	private int port;
	
	// Constructor
	public MessageClient(String hostname, int port) {
		this.hostname = hostname;
		this.port = port;
	}

	public void execute() { 
		try {
			// Create new socket
			Socket socket = new Socket(hostname, port);
			
			// Print out welcome message and command list
			System.out.println("Connected to the MessageServer!\n");
			System.out.println("Welcome to MessageServer!\n");
			System.out.println("\t=========COMMAND LIST=========:\n\n"
				+ "\t<command> ::= <keyword> | <keyword><text>\n"
				+ "\t<users> ::= /users\n"
				+ "\t<all> ::= /all <text>\n"
				+ "\t<create> ::= /create <group name>\n"
				+ "\t<join> ::= /join <group name>\n"
				+ "\t<leave> ::= /leave <group name>\n"
				+ "\t<remove> ::= /remove <group name>\n"
				+ "\t<personal message> ::= /pm <name><text>\n"
				+ "\t<group message> ::= /gm <group name><text>\n"
				+ "\t<groups> ::= /groups prints out available groups\n"
				+ "\t<topic> ::= /topic <topic name>\n"
				+ "\t<topics> ::= /topics prints out available topics\n"
				+ "\t<subscribe> ::= /sub <topic name>\n"
				+ "\t<unsubscribe> ::= /unsub <topic name>\n"
				+ "\t<text> ::= any text . . .\n"
				+ "\t<exit> ::= /exit exits MessageServer\n");
			
			// Start the reader and writer threads
			new MessageReadThread(socket).start();
			new MessageWriteThread(socket).start();

		} 
		// Catch exceptions
		catch (UnknownHostException ex) {
			System.out.println("Server not found: " + ex.getMessage());
		} 
		catch (IOException ex) {
			System.out.println("I/O Error: " + ex.getMessage());
		}
	}

	public static void main(String[] args) {
		if (args.length < 2) return;

		String hostname = args[0];
		int port = Integer.parseInt(args[1]);
		
		// Instantiate the MessageClient class and execute
		MessageClient client = new MessageClient(hostname, port);
		client.execute();
	}
}
