/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.dublineventserver;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 *
 * @author aaron
 */
public class DublinEventServer {
    private static ServerSocket servSock; // Server socket to listen for incoming connections
    private static final int PORT = 1234; // Port number for the server (client has to be same as this to connect)
    private static final List<String> events = new ArrayList<>(); // List to store event details

    public static void main(String[] args) {
        System.out.println("Opening port...");
        // Attempt to create the server socket
        try {
            servSock = new ServerSocket(PORT); // Bind the server socket to the specified port
        } catch (IOException e) {
            System.out.println("Unable to attach to port!");
            System.exit(1); // Exit the program if socket creation fails
        }
        // Continuously handles incoming client connections
        do {
            handleClient(); // Calls the method to handle client connections
        } while (true);
    }

    private static void handleClient() {
        Socket link = null; // Socket for client connection
        try {
            link = servSock.accept(); // Wait for a client to connect
            new Thread(new ClientHandler(link)).start(); // Start a new thread to handle the client
        } catch (IOException e) {
            e.printStackTrace(); // Print any exceptions that occur during client handling
        }
    }

    // ClientHandler class to manage individual client requests
    private static class ClientHandler implements Runnable {
        private Socket client; // Client socket connection
        private BufferedReader in; // Input stream to read data from client
        private PrintWriter out; // Output stream to send data back to client

        public ClientHandler(Socket socket) {
            this.client = socket; // Assign the client socket to the class variable
            try {
                // Initialize input and output streams for client communication
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                out = new PrintWriter(client.getOutputStream(), true);
            } catch (IOException e) {
                e.printStackTrace(); // Print any exceptions during stream initialization
            }
        }

        public void run() {
            String message; // Variable to hold incoming messages from the client
            try {
                // Continuously reads messages from client until the connection is closed
                while ((message = in.readLine()) != null) {
                    if (message.equals("STOP")) { // Check for termination command
                        out.println("TERMINATE"); // Inform the client of termination
                        break; // Exit the loop if STOP is received
                    }

                    StringTokenizer tokenizer = new StringTokenizer(message, ";"); // Tokenize the incoming message
                    if (tokenizer.countTokens() != 4) { // Check if there are exactly 4 tokens (correct format)
                        out.println("Error: Invalid input format, please try again using 'add/remove; date; time; description'.");
                        continue; 
                    }
                    // Extract tokens from the message
                    String action = tokenizer.nextToken().trim(); // Action (add/remove)
                    String date = tokenizer.nextToken().trim(); // Date 
                    String time = tokenizer.nextToken().trim(); // Time 
                    String description = tokenizer.nextToken().trim(); // Description
                    String event = date + "; " + time + "; " + description; // Construct event string

                    synchronized (events) { // Synchronize access to the events list for thread safety
                        try {
                            // Handle the action specified by the client (add/remove)
                            if (action.equals("add")) {
                                // Add event to the list snd then confirm it
                                events.add(event); 
                                out.println("Added event: " + event); 
                                printEvents();
                            } else if (action.equals("remove")) {
                                // Attempt to remove the event from the list and then confirm it
                                if (events.remove(event)) {
                                    out.println("Removed event: " + event); 
                                } else {
                                    out.println("Event not found to remove: " + event); 
                                }
                                printEvents(); // Print the updated list of events to server
                            } else {
                                // Throw an exception for invalid action (any action other than add/remove)
                                throw new IncorrectActionException("Incorrect action specified, please use add/remove.");
                            }
                        } catch (IncorrectActionException e) {
                            out.println(e.getMessage()); 
                        } catch (Exception e) { // General exception handling for debugging
                            out.println("Error processing request: " + e.getMessage());
                            e.printStackTrace(); // Log the stack trace for debugging
                        }
                    }
                }
            } catch (IOException e) {
                // Handle connection errors
                System.out.println("Connection error: " + e.getMessage());
                e.printStackTrace(); 
            } finally {
                try {
                    client.close(); // Close the client connection
                } catch (IOException e) {
                    // Log any errors that occur during disconnection
                    System.out.println("Unable to disconnect!");
                    e.printStackTrace(); // Print stack trace for disconnection errors
                }
            }
        }
    }

    // Prints the current list of events to the server (helps with testing)
    private static void printEvents() {
        System.out.println("Current Events List:");
        for (String event : events) {
            System.out.println(" - " + event); 
        }
        System.out.println("----------"); // Separator for clarity/style
    }

    // Custom exception class for throwing exception when incorrect actions are requested
    private static class IncorrectActionException extends Exception {
        public IncorrectActionException(String message) {
            super(message); 
        }
    }
}
