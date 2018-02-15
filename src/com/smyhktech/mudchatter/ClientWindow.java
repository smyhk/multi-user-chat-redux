package com.smyhktech.mudchatter;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultCaret;

public class ClientWindow extends JFrame implements Runnable {
	
	private static final long serialVersionUID = 1L;

	private JPanel contentPane;

	private JTextField txtMessage;
	private JTextArea chatHistory;
	private DefaultCaret caret;
	private Thread run, listen;
	private boolean running = false;
	
	private Client client;
	
	/**
	 * Create the frame.
	 */
	public ClientWindow(String name, String address, int port) {
		client = new Client(name, address, port);
		setTitle("MudChatter Client");
		
		boolean connect = client.openConnection(address);
		if (!connect) {
			String msg = "Connection failed!";
			System.err.println(msg);
			console(msg);
		}
		
		createWindow();
		console("Attempting connection to: " + address + ":" + port + ", user: " + name);
		String connection = "/c/" + name + "/e/";
		client.send(connection.getBytes());
		running = true;
		run = new Thread(this, "Running");
		run.start();
	}
	
	private void createWindow() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(880, 600);
		setLocationRelativeTo(null);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[]{28, 815, 30, 7};
		gbl_contentPane.rowHeights = new int[]{30, 520, 50};
		gbl_contentPane.columnWeights = new double[]{1.0, 1.0};
		gbl_contentPane.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		contentPane.setLayout(gbl_contentPane);
		
		chatHistory = new JTextArea();
		chatHistory.setEditable(false);
		JScrollPane scroll = new JScrollPane(chatHistory);
		caret = (DefaultCaret) chatHistory.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		GridBagConstraints scrollConstraints = new GridBagConstraints();
		scrollConstraints.insets = new Insets(0, 5, 5, 5);
		scrollConstraints.fill = GridBagConstraints.BOTH;
		scrollConstraints.gridx = 0;
		scrollConstraints.gridy = 0;
		scrollConstraints.gridwidth = 3;
		scrollConstraints.gridheight = 2;
		contentPane.add(scroll, scrollConstraints);
		
		txtMessage = new JTextField();
		txtMessage.addKeyListener(new KeyAdapter() {
			// Send text to the chat when Enter key is pressed
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					send(txtMessage.getText());
				}
			}
		});
		GridBagConstraints gbc_txtMessage = new GridBagConstraints();
		gbc_txtMessage.insets = new Insets(0, 0, 0, 5);
		gbc_txtMessage.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtMessage.gridx = 0;
		gbc_txtMessage.gridy = 2;
		gbc_txtMessage.gridwidth = 2;
		contentPane.add(txtMessage, gbc_txtMessage);
		txtMessage.setColumns(10);
		
		JButton btnSend = new JButton("Send");
		btnSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				send(txtMessage.getText());
			}
		});
		GridBagConstraints gbc_btnSend = new GridBagConstraints();
		gbc_btnSend.insets = new Insets(0, 0, 0, 5);
		gbc_btnSend.gridx = 2;
		gbc_btnSend.gridy = 2;
		contentPane.add(btnSend, gbc_btnSend);
		
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				String disconnect = "/d/" + client.getId() + "/e/";
				send(disconnect);
			}
		});
		
		setVisible(true);
		
		// Automatically give focus to the text entry window
		txtMessage.requestFocusInWindow();
	}
	
	public void run() {
		System.out.println("I am in the run method.");
		listen();
	}
	
	private void send(String message) {
		// Ignore blank messages
		if (message.equals("")) return;
		message = client.getName() + ": " + message;

		// Send message to the server
		message = "/m/" + message;
		client.send(message.getBytes());
		txtMessage.setText("");
	}
	
	public void listen() {
		listen = new Thread("Listen") {
			public void run() {
				System.out.println("I am listening.");
				while(running) {
					String message = client.receive();
					if (message.startsWith("/c/")) {
						System.out.println("In window listen: " + message);
						client.setId(Integer.parseInt(message.split("/c/|/e/")[1]));
						console("Successfully connected to server. ID: " + client.getId());
					} else if (message.startsWith("/m/")) {
						String text = message.split("/m/|/e/")[1];
						console(text);
					} else {
						System.out.println("I am not getting any data!!");
					}
				}
			}
		};
		listen.start();
	}
	
	/**
	 * Writes to the chat history in the JFrame
	 * @param message
	 */
	public void console(String message) {
		chatHistory.append(message + "\n\r");
		// TODO: research this...
		//chatHistory.setCaretPosition();
	}
}
