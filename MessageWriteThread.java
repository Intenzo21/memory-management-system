import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Class 'MessageWriteThread' responsible for reading user's input and sending it
 * to the server. It runs in an infinite loop until the user uses the '/exit' command to quit.
 */
public class MessageWriteThread extends Thread {
	private PrintWriter writer;
	private Socket socket;
	private String text;

	// Constructor
	public MessageWriteThread(Socket socket) {
		this.socket = socket;
		
		// Obtain the output stream 
		try {
	    		OutputStream output = socket.getOutputStream();
	    		writer = new PrintWriter(output, true);
		} 
		catch (IOException ex) {
	    		System.out.println("Error getting output stream: " + ex.getMessage());
	    		ex.printStackTrace();
		}
	}

	public void run() {
		
		// Scanner for the user input
		Scanner scanner = new Scanner(System.in);
		
		// Give time for printing available users message to the client (in ClientConnection)
		try {
			Thread.sleep(1);
		} 
		catch (InterruptedException ex) {
			System.out.println(ex);
		}

		// Get the username (client name) of the user (client)
		System.out.print("Enter your name: ");
		String userName = scanner.nextLine();
		writer.println(userName);
		
		// Get input from user until '/exit' command is typed
		do {
	    		text = scanner.nextLine();
	    		writer.println(text);

		} while (!text.equals("/exit"));

		// Close the socket and the scanner as the user exits
		try {
	    		socket.close();
			scanner.close();
		} 
		catch (IOException ex) { // Handle IO exceptions
	    		System.out.println("Error writing to server: " + ex.getMessage());
		}
	}
}
