/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.dublineventclient;

import java.io.*;
import java.net.*;

/**
 *
 * @author aaron
 */
public class DublinEventClient {
    private static InetAddress host; // Variable to hold the servers host address
    private static final int PORT = 1234; // Port same as server or else it wont connect

    public static void main(String[] args) {
        // Attempt to get the local host address
        try {
            host = InetAddress.getLocalHost(); // Fetches the IP address of the local machine
        } catch (UnknownHostException e) {
            // If the host cannot be found display error message and exit
            System.out.println("Host ID not found!");
            System.exit(1);
        }
        run(); // Call the run method to start the client 
    }

    private static void run() {
        Socket link = null; // Initialize the socket connection
        try {
            // Create a socket connection to the specified host and port
            link = new Socket(host, PORT);
            // Set up input and output streams for communication with the server
            BufferedReader in = new BufferedReader(new InputStreamReader(link.getInputStream()));
            PrintWriter out = new PrintWriter(link.getOutputStream(), true);
            BufferedReader userEntry = new BufferedReader(new InputStreamReader(System.in)); // For user input

            String message; // Variable to hold user messages
            String response; // Variable to hold responses from the server

            // Loop to continuously accept user commands until STOP is entered
            do {
                // Prompt the user for input and explain the required format
                System.out.println("Enter action and event (add/remove; date; time; description) or STOP to quit:");
                message = userEntry.readLine(); // Read user input
                out.println(message); // Send the message to the server

                // If the user did not type STOP it will read and display the servers response
                if (!message.equalsIgnoreCase("STOP")) {
                    response = in.readLine(); // Get the response from the server
                    System.out.println("SERVER RESPONSE> " + response); // Display the servers response
                }
            } while (!message.equalsIgnoreCase("STOP")); // Continue until STOP is received

        } catch (IOException e) {
            // Handle IO exceptions indicating any problems with the connection
            System.out.println("Connection error: " + e.getMessage());
            e.printStackTrace(); // Print the stack trace for debugging purposes
        } finally {
            try {
                System.out.println("Closing connection..."); // Inform the user of the closure
                if (link != null) {
                    link.close(); // Attempt to close the socket connection
                }
            } catch (IOException e) {
                // Handle any errors that occur during disconnection
                System.out.println("Unable to disconnect!");
                e.printStackTrace(); // Print the stack trace for disconnection errors
            }
        }
    }
}