package com.smyhktech.mudchatter.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Server implements Runnable {
	
	protected static final int MAX_ATTEMPTS = 5;

	// Store connected clients
	private List<ServerClient> clients = new ArrayList<>();
	
	private List<Integer> clientResponse = new ArrayList<>();
	
	private DatagramSocket socket;
	private int port;
	private boolean running = false;
	private Thread run, manage, send, receive;

	private boolean raw = false;
	
	public Server(int port) {
		this.port = port;
		try {
			socket = new DatagramSocket(port);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		run = new Thread(this, "Server");
		run.start();
	}
	
	public void run() {
		running = true;
		System.out.println("Server started on port " + port);
		manageClients();
		receive();
		
		// Server commands
		Scanner scanner = new Scanner(System.in);
		while (running) {
			String text = scanner.nextLine();
			if (!text.startsWith("/")) {
				sendToAll("/m/Server: " + text + "/e/");
				continue;
			}
			text = text.split("/")[1];
			// Enable/disable raw mode
			if (text.equalsIgnoreCase("raw")) {
				raw = !raw;
			} else if (text.equalsIgnoreCase("who")) {
				System.out.println("Connected Clietns:");
				System.out.println("=================");
				for (int i = 0; i < clients.size(); i++) {
					ServerClient c = clients.get(i);
					System.out.println(c.name + " (" + c.getID() + ") " + "@ " + c.address.toString() + ":" + c.port);
				}
				System.out.println("=================");
			} else if (text.startsWith("kick")) {
				String name = text.split(" ")[1];  // Extract the name or id
				int id = -1;
				boolean number = true;
				try {
					id = Integer.parseInt(name);
				} catch (NumberFormatException e) {
					number = false;
				}
				if (number) {
					boolean exists = false;
					for (int i = 0; i < clients.size(); i++) {
						if (clients.get(i).getID() == id) {
							exists = true;
							break;
						}
					}
					if (exists) disconnect(id, true);
					else System.out.println("Client with ID: " + id + " does not exist. Check ID number.");
				} else {
					for (int i = 0; i < clients.size(); i++) {
						ServerClient c = clients.get(i);
						if (name.equals(c.name)) {
							disconnect(c.getID(), true);
							break;
						}
					}
				}
			}
			if (!running) scanner.close();
		}
	}
	
	private void manageClients() {
		manage = new Thread("Manage") {
			public void run() {
				while(running) {
					sendToAll("/i/server");
					sendStatus();
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					for (int i = 0; i < clients.size(); i++) {
						ServerClient c = clients.get(i);
						if (!clientResponse.contains(c.getID())) {
							if (c.attempt >= MAX_ATTEMPTS) {
								disconnect(c.getID(), false);
							} else {
								c.attempt++;
							}
						} else {
							clientResponse.remove(new Integer(c.getID()));
							c.attempt = 0;
						}
					}
				}
			}
		};
		manage.start();
	}
	
	private void sendStatus() {
		if (clients.size() <= 0) return;
		String users = "/u/";
		for (int i = 0; i < clients.size() - 1; i++) {
			users += clients.get(i).getName() + "/n/";
		}
		users += clients.get(clients.size() - 1).name + "/e/";
		sendToAll(users);
	}

	private void receive() {
		receive = new Thread("Receive") {
			public void run() {
				while(running) {
					byte[] data = new byte[1024];
					DatagramPacket packet = new DatagramPacket(data, data.length);
					try {
						socket.receive(packet);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					// Interprets the received packet, i.e., connection, data, disconnect...
					process(packet);
				}
			}
		};
		receive.start();
	}
	
	private void sendToAll(String message) {
		if (message.startsWith("/m/")) {
			String text = message.substring(3).split("/e/")[0];
			System.out.println(text);
		}
		
		for (ServerClient client : clients) {
			send(message.getBytes(), client.address, client.port);
		}
	}
	
	private void send(final byte[] data, final InetAddress address, final int port) {
		send = new Thread("Server Send") {
			public void run() {
				DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
				try {
					socket.send(packet);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		send.start();
	}
	
	/**
	 * Packages the message with delimiters before passing to network send
	 */
	private void send(String message, InetAddress address, int port) {
		// Indicates end of message
		message += "/e/";
		send(message.getBytes(), address, port);
	}
	
	private void process(DatagramPacket packet) {
		String rcvString = new String(packet.getData());
		if (raw) System.out.println(rcvString);
		
		if (rcvString.startsWith("/c/")) {
			// UUID id = UUID.randomUUID(); possible alternative client unique id
			
			String name = rcvString.split("/c/|/e/")[1];
			int id = UniqueIdentifier.getIdentifier();
			System.out.println(name + " (" + id + ") connected!"); 
			
			clients.add(new ServerClient(name, packet.getAddress(), packet.getPort(), id));
			
			// Notify client of successful connection
			String msg = "/c/" + id;
			send(msg, packet.getAddress(), packet.getPort());
			
		} else if (rcvString.startsWith("/m/")) {
			// Send message to all connected clients
			sendToAll(rcvString);
		} else if (rcvString.startsWith("/d/")) {
			String id = rcvString.split("/d/|/e/")[1];
			disconnect(Integer.parseInt(id), true);
		} else if (rcvString.startsWith("/i/")) {
			clientResponse.add(Integer.parseInt(rcvString.split("/i/|/e/")[1]));
		}
	}
	
	private void disconnect(int id, boolean status) {
		ServerClient c = null;
		boolean existed = false;
		for (int i = 0; i < clients.size(); i++) {
			if (clients.get(i).getID() == id) {
				c = clients.get(i);
				clients.remove(i);
				existed = true;
				break;
			}
		}
		if (!existed) return;
		String message = "";
		if (status) {
			message = "Client: " + c.name + " (" + c.getID() + ") " +  "@ " + c.address.toString() + ":" + c.port + " disconnected.";
		} else {
			message = "Client: " + c.name + " (" + c.getID() + ") " + "@ " + c.address.toString() + ":" + c.port +" timed out.";
		}
		System.out.println(message);
	}
}
