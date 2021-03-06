package com.smyhktech.mudchatter;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;

public class Login extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField txtName;
	private JLabel lblAddress;
	private JTextField txtAddress;
	private JLabel lblPort;
	private JTextField txtPort;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Login frame = new Login();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public Login() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		setResizable(false);
		setTitle("Login");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//setBounds(100, 100, 300, 380);
		setSize(300, 380);
		setLocationRelativeTo(null);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblName = new JLabel("Name:");
		lblName.setBounds(129, 38, 40, 16);
		contentPane.add(lblName);
		
		txtName = new JTextField();
		txtName.setText("smyhk");
		txtName.setBounds(61, 56, 177, 26);
		contentPane.add(txtName);
		txtName.setColumns(10);
		
		lblAddress = new JLabel("IP Address:");
		lblAddress.setBounds(114, 104, 70, 16);
		contentPane.add(lblAddress);
		
		txtAddress = new JTextField();
		txtAddress.setText("localhost");
		txtAddress.setToolTipText("IP Address");
		txtAddress.setColumns(10);
		txtAddress.setBounds(61, 122, 177, 26);
		contentPane.add(txtAddress);
		
		lblPort = new JLabel("Port:");
		lblPort.setBounds(135, 170, 29, 16);
		contentPane.add(lblPort);
		
		txtPort = new JTextField();
		txtPort.setText("8192");
		txtPort.setToolTipText("IP Address");
		txtPort.setColumns(10);
		txtPort.setBounds(61, 186, 177, 26);
		contentPane.add(txtPort);
		
		JButton btnLogin = new JButton("Login");
		btnLogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// TODO: add validations
				String name = txtName.getText();
				String address = txtAddress.getText();
				int port  = Integer.parseInt(txtPort.getText());
				
				login(name, address, port);
			}
		});
		btnLogin.setBounds(91, 251, 117, 29);
		contentPane.add(btnLogin);
	}
	
	/**
	 * Login stuff goes here
	 */
	private void login(String name, String address, int port) {
		dispose(); // closes the current window, not application
		new ClientWindow(name, address, port);
	}
}
