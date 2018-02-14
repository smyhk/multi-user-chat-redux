package com.smyhktech.mudchatter;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultCaret;

import java.awt.GridBagLayout;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import java.awt.GridBagConstraints;
import javax.swing.JButton;
import java.awt.Insets;
import javax.swing.JTextField;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class Client extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	
	private String name, address;
	private int port;
	private JTextField txtMessage;
	private JTextArea chatHistory;
	private DefaultCaret caret;
	
	/**
	 * Create the frame.
	 */
	public Client(String name, String address, int port) {
		setTitle("MudChatter Client");
		this.name = name;
		this.address = address;
		this.port = port;
		
		createWindow();
		
		console("Attempting connection to: " + address + ":" + port + ", user: " + name);
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
		setVisible(true);
		
		// Automatically give focus to the text entry window
		txtMessage.requestFocusInWindow();
	}
	
	private void send(String message) {
		// Ignore blank messages
		if (message.equals("")) return;
		message = name + ": " + message;
		console(message);
		txtMessage.setText("");
	}
	
	public void console(String message) {
		chatHistory.append(message + "\n\r");
		// TODO: research this...
		//chatHistory.setCaretPosition();
	}

}
