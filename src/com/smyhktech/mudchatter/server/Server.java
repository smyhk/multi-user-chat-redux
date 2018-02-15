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
					
					// Quick test for ServerClient
					clients.add(new ServerClient("Temp", packet.getAddress(), packet.getPort(), 50));
					System.out.println(clients.get(0).address.toString() + ":" + clients.get(0).port);
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
	
	private void send(final byte[] data, InetAddress address, int port) {
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
	
	private void process(DatagramPacket packet) {
		String rcvString = new String(packet.getData());
		if (rcvString.startsWith("/c/")) {
			// UUID id = UUID.randomUUID();
			int id = UniqueIdentifier.getIdentifier();
			clients.add(new ServerClient(rcvString.substring(3, rcvString.length()), packet.getAddress(), packet.getPort(), id));
			// testing only
			System.out.println(rcvString.substring(3, rcvString.length()) + " identifier: " + id);
		} else if (rcvString.startsWith("/m/")) {
			//String message = rcvString.substring(3, rcvString.length());
			sendToAll(rcvString);
		} else {
			
		}
	}
}
