import java.io.*;
import java.net.*;
 
/**
 * Class 'MessageReadThread' for reading server's input and printing it
 * to the console. It runs in an infinite loop until the client disconnects 
 * from the server.
 */
public class MessageReadThread extends Thread {
	private BufferedReader reader;
	private Socket socket;
	private InputStream input;
	private String response;

	public MessageReadThread(Socket socket) {
		this.socket = socket;

		try {
			// Obtain input stream
			input = socket.getInputStream();
			reader = new BufferedReader(new InputStreamReader(input));
		} 
		catch (IOException ex) { // Handle IO exceptions
			System.out.println("Error getting input stream: " + ex.getMessage());
			ex.printStackTrace();
		}
	}

	public void run() {
		// Loop to print the server response to the client(s)
		while (true) {
			try {
				// Assign the server input stream to the response variable
				response = reader.readLine();

				// If the server closes then break the loop
				if (response == null) {
					System.out.println("Server has been closed!");					
					break;
				}
				// Print out server response with a '>>' prefix
				System.out.println("\n>> " + response + "\n");
			} 
			catch (IOException ex) { // Handle IO exceptions
				System.out.println("You have unregistered from the chat! Exiting...");
				System.out.println("Error reading from server: " + ex.getMessage());
				break;
			}
		}
	}
}
