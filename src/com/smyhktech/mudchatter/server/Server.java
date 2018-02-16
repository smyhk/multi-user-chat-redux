package com.smyhktech.mudchatter.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class Server implements Runnable {
	
	// Store connected clients
	private List<ServerClient> clients = new ArrayList<>();
	
	private DatagramSocket socket;
	private int port;
	private boolean running = false;
	private Thread run, manage, send, receive;
	
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
	}
	
	private void manageClients() {
		manage = new Thread("Manage") {
			public void run() {
				while(running) {
					
				}
			}
		};
		manage.start();
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
		
		if (rcvString.startsWith("/c/")) {
			// UUID id = UUID.randomUUID(); possible alternative client unique id
			
			rcvString = rcvString.split("/e/")[0];
			int id = UniqueIdentifier.getIdentifier();
			clients.add(new ServerClient(rcvString.substring(3, rcvString.length()), packet.getAddress(), packet.getPort(), id));
			
			// Notify client of successful connection
			String msg = "/c/" + id;
			send(msg, packet.getAddress(), packet.getPort());
			
		} else if (rcvString.startsWith("/m/")) {
			// Send message to all connected clients
			sendToAll(rcvString);
		} else if (rcvString.startsWith("/d/")) {
			String id = rcvString.split("/d/|/e/")[1];
			disconnect(Integer.parseInt(id), true);
		}
	}
	
	private void disconnect(int id, boolean status) {
		ServerClient c = null;
		for (int i = 0; i < clients.size(); i++) {
			if (clients.get(i).getID() == id) {
				c = clients.get(i);
				clients.remove(i);
				break;
			}
		}
		String message = "";
		if (status) {
			message = "Client: " + c.name + " (" + c.getID() + ") " +  "@ " + c.address.toString() + ":" + c.port + " disconnected.";
		} else {
			message = "Client: " + c.name + " (" + c.getID() + ") " + "@ " + c.address.toString() + ":" + c.port +" timed out.";
		}
		System.out.println(message);
	}
}
