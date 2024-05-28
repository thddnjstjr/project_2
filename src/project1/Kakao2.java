package project1;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import lombok.Data;

@Data
public class Kakao2 extends JFrame implements ActionListener,ListSelectionListener{

	private Vector<Socket>socket;
	private ArrayList<Integer> portNum;
	private JLabel background1;
	private JLabel background2;
	private JLabel background3;
	private JPanel roomarea;
	private TextField chat;
	private TextField search;
	private TextField nickname;
	private TextArea chatarea;
	private JButton send;
	private JButton join;
	private JButton makeRoom;
	private JButton roomCreat;
	private JButton back;
	private String[] serverName;
	private String name; // 사용자 닉네임
	private boolean login;
	private boolean wait;
	private boolean chatting;
	private int count;
	private static final int WAITROOM = 0; // 0으로 고정 (대기실)
	private DefaultListModel model;
	private JList roomList;
	private BufferedReader readMsg;
	private BufferedReader userMsg;
	private PrintWriter sendMsg;
	private String[] roomName;
	
	public Kakao2 () {
		initData();
		initLayoutData();
		actionListener();
		try {
			socket = new Vector<>(20);
			
			socket.add(new Socket("localhost",5000));
			System.out.println("서버 접속 완료");
			
			readMsg = new BufferedReader(new InputStreamReader(socket.get(WAITROOM).getInputStream()));
			sendMsg = new PrintWriter(socket.get(WAITROOM).getOutputStream(), true);
			
			Thread readThread = new Thread (() -> {
				try {
					String serverMsg;
					while( (serverMsg = readMsg.readLine()) != null) {
						System.out.println("서버에서 보냄 : " + serverMsg);
						if(serverMsg.startsWith("createroom")) {
							String roomName[] = serverMsg.split(":");
							model.addElement(roomName[1] + "채팅방 " + "           인원수 : " + "  / " + 30);
							repaint();
						}
						if(serverMsg.startsWith("portNum")) {
							String port[] = serverMsg.split(":");
							try {
								Integer ports = Integer.valueOf(port[1]);
								portNum.add(ports);
							} catch (Exception e) {
							}
						}
					}
				} catch (Exception e) {
				}
			});
			
			
			readThread.start();
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
	}
	
	private void initData() {
		portNum = new ArrayList<>(20);
		roomName = new String[100];
		Font font2 = new Font("나눔고딕",Font.BOLD,25);
		serverName = new String[10];
		model = new DefaultListModel();
		roomList =  new JList(model);
		roomList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		roomList.setFont(font2);
		background1 = new JLabel(new ImageIcon("images/login.jpg"));
		background2 = new JLabel(new ImageIcon("images/openchat.png"));
		background3 = new JLabel(new ImageIcon("images/kakao.jpg"));
		roomarea = new JPanel();
		chat = new TextField();
		search = new TextField();
		nickname = new TextField();
		chatarea = new TextArea();
		roomCreat = new JButton();
		back = new JButton();
		makeRoom = new JButton("방 만들기");
		setTitle("카카오톡");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setContentPane(background1);
		setSize(500,840);
		login = true;
	}
	
	public void actionListener() {
		makeRoom.addActionListener(this);
		roomList.addListSelectionListener(this);
		back.addActionListener(this);
		nickname.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				name = nickname.getText();
				waitRoom();
			}
		});
		chat.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				sendMsg.println(name + " : " + chat.getText());
				chat.setText("");
			}
		});
	}
	
	private void initLayoutData() {
		setLayout(null);
		setResizable(false);
		setLocationRelativeTo(null);
		setVisible(true);
		nickname.setBounds(75, 400, 335, 30);
		add(nickname);
	}
	// 대기실 화면
	private void waitRoom() {
		login = false;
		wait = true;
		getContentPane().removeAll();
		setContentPane(background2);
		setSize(500,841);
		roomarea.setBounds(40, 100, 400, 500);
		roomarea.setBorder(new TitledBorder(new LineBorder(Color.BLACK)));
		makeRoom.setBounds(350, 635, 100, 50);
		add(roomarea);
		add(makeRoom);
		roomarea.add(roomList);
		repaint();
	}
	// 채팅방 화면
	private void chatRoom() {
		
		wait = false;
		chatting = true;
		getContentPane().removeAll();
		setContentPane(background3);
		setSize(500,842);
		add(chat);
		add(chatarea);
		add(back);
		back.setBounds(17, 34, 40, 40);
		back.setBorderPainted(false);
		back.setContentAreaFilled(false);
		back.setFocusPainted(false);
		chatarea.setEditable(false);
		chatarea.setLocation(40, 100);
		chatarea.setSize(400,620);
		chat.setSize(400,40);
		chat.setLocation(40, 750);
		repaint();
		
	}
	
	// 방 만들기
	public void creatServer(String name) {
		sendMsg.println("createroom:" + name);
		System.out.println("작동");
		count++;
	}
	
	
	private Thread chatRead() {
		return new Thread(() -> {
			try {
				String msg;
				while( (msg = readMsg.readLine()) != null) {
					chatarea.append("\n" + msg);
					repaint();
				}
			} catch (Exception e) {
			}
		});
	}
	
	
	public static void main(String[] args) {
		new Kakao2();
	}
	
	
	@Override
	public void actionPerformed(ActionEvent e) {
		JButton selectedButton = (JButton)e.getSource();
		if(selectedButton == makeRoom) {
			serverName[count] = JOptionPane.showInputDialog("방 만들기 ");
			if(!serverName.equals(null)) {
				creatServer(serverName[count]);
			} 
		}
		if(selectedButton == back) {
			waitRoom();
			try {
				socket.get(1).close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		Font font1 = new Font("나눔고딕",Font.BOLD,25);
		if(login == true) {
			g.setFont(font1);
			g.drawString("이름을 입력해주세요", 80, 410);
		}
		if(wait) {
			g.setFont(font1);
			g.drawString(name+"", 10, 700);
		}
	}

	// 채팅방 선택했을때 이벤트 리스너
	@Override
	public void valueChanged(ListSelectionEvent e) {
		if(roomList.getSelectedIndex() == 0) {
			System.out.println("작동");
			chatRoom();
			try {
				socket.add(new Socket("localhost", portNum.get(0)));
				readMsg = new BufferedReader(new InputStreamReader(socket.get(1).getInputStream()));
				sendMsg = new PrintWriter(socket.get(1).getOutputStream(), true);
				chatRead().start();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
		}
	}
}
