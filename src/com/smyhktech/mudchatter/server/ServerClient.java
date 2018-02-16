package com.smyhktech.mudchatter.server;

import java.net.InetAddress;

public class ServerClient {
	public String name;
	public InetAddress address;
	public int port;
	private final int ID;
	public int attempt = 0;
	
	// Constructor
	public ServerClient(String name, InetAddress address, int port, final int ID) {
		this.name = name;
		this.address = address;
		this.port = port;
		this.ID = ID;
	}
	
	public int getID() {
		return ID;
	}
	
	public String getName() {
		return name;
	}
	
	public InetAddress getAddress() {
		return address;
	}
	
	public int getPort() {
		return port;
	}
}
