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

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import lombok.Data;

@Data
public class Kakao2 extends JFrame implements ActionListener,ListSelectionListener{

	private Vector<Socket>serviceSocket;
	private Vector<Socket>socket;
	private ArrayList<Integer> portNum;
	private ArrayList<String> roomData;
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
	private JButton delete;
	private String[] serverName;
	private String name; // 사용자 닉네임
	private boolean login;
	private boolean wait;
	private boolean chatting;
	private int count;
	private int chatRoomNum;
	private static final int WAITROOM = 0; // 0으로 고정 (대기실)
	private DefaultListModel model;
	private JList roomList;
	private BufferedReader readMsg; // 채팅방 전용 읽는 스트림
	private BufferedReader userMsg; // 채팅방 전용 쓰는 스트림
	private PrintWriter sendMsg;
	private BufferedReader broadCast; // 대기실 서버 (WAITROOM) 명령어를 계속 받기위해 만듬
	private PrintWriter orderMsg; // 마찬가지로 (WAITROOM)에 계속 명령어를 내리기 위해 만듬
	
	public Kakao2 () {
		initData();
		initLayoutData();
		actionListener();
		try {
			serviceSocket = new Vector<>(2); // 서비스 소켓
			socket = new Vector<>(2); // 채팅 소켓
			roomData = new ArrayList<>(5);
			for(int i = 0; i < socket.capacity(); i++) {
				socket.add(i,null); // 빈 소켓 찾기 위해서 처음에 백터 공간을 전부 null로 만들어줌
			}
			serviceSocket.add(WAITROOM,new Socket("localhost",5000));
			System.out.println("서버 접속 완료");
			
			broadCast = new BufferedReader(new InputStreamReader(serviceSocket.get(WAITROOM).getInputStream()));
			orderMsg = new PrintWriter(serviceSocket.get(WAITROOM).getOutputStream(), true);
			
			Thread readThread = new Thread (() -> {
				try {
					String serverMsg;
					while( (serverMsg = broadCast.readLine()) != null) {
						System.out.println("서버에서 보냄 : " + serverMsg);
						if(serverMsg.startsWith("createroom")) {
							String roomName[] = serverMsg.split(":");
							model.addElement(roomName[1] + " 방 " + "     인원수 : " + "  / " + 30);
							roomData.add(roomName[1]);
							repaint();
						}
						if(serverMsg.startsWith("portNum")) {
							String port[] = serverMsg.split(":"); // : 를 기준으로 문자를 명령어 portNum과 받을 서버 포트넘버로 나눔
							try {
								Integer ports = Integer.valueOf(port[1]); // 문자'포트넘버'를 정수형으로 변환
								System.out.println(ports + "서버 넘버");
								portNum.add(ports); // 받은 서버 포트 넘버를 저장
							} catch (Exception e) {
							}
						}
						if(serverMsg.startsWith("deleteroom")) {
							String deleteMsg[] = serverMsg.split(":");
							try {
								int indexNum = Integer.valueOf(deleteMsg[1]);
								System.out.println(socket.get(chatRoomNum).getPort() + " : 현재 포트번호 " + portNum.get(indexNum) + " : 방 포트번호");
								if(socket.get(chatRoomNum).getPort() == portNum.get(indexNum)) {
									JOptionPane.showMessageDialog(null, "방이 삭제되었습니다.","알림",JOptionPane.ERROR_MESSAGE);
									socket.get(chatRoomNum).close();
									waitRoom();
								}
								model.remove(indexNum);
								roomData.remove(indexNum);
								portNum.remove(indexNum);
								repaint();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						if(serverMsg.startsWith("error:wrongUser")) {
							JOptionPane.showMessageDialog(null, "권한이 없습니다.","경고",JOptionPane.ERROR_MESSAGE);
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
		delete = new JButton("방 삭제");
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
		delete.addActionListener(this);
		back.addActionListener(this);
		
		nickname.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				name = nickname.getText();
				orderMsg.println("userName:" + name); // 서버에게 사용자 이름을 전송
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
		roomList.setBorder(BorderFactory.createLineBorder(Color.black,1));
		DefaultListCellRenderer renderer = (DefaultListCellRenderer) roomList.getCellRenderer();
		renderer.setHorizontalAlignment(SwingConstants.RIGHT);
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
		requestFocus();
		repaint();
	}
	// 채팅방 화면
	private void chatRoom() {
		wait = false;
		chatting = true;
		getContentPane().removeAll();
		setContentPane(background3);
		setSize(500,839);
		add(chat);
		add(chatarea);
		add(back);
		add(delete);
		delete.setBounds(360,40,100,50);
		back.setBounds(17, 34, 40, 40);
		back.setBorderPainted(false);
		back.setContentAreaFilled(false);
		back.setFocusPainted(false);
		chatarea.setEditable(false);
		chatarea.setLocation(40, 100);
		chatarea.setSize(400,620);
		chat.setSize(400,40);
		chat.setLocation(40, 750);
		requestFocus();
		repaint();
		
	}
	
	// 방 만들기
	public void creatServer(String name) {
		if(count > 4) {
			System.out.println("방을 더이상 만들수 없습니다");
			JOptionPane.showMessageDialog(null, "더 이상 방을 만들수 없습니다", " 경고" , JOptionPane.ERROR_MESSAGE);
			return;
		}
		else {
			orderMsg.println("createroom:" + name + ":" + this.name);
			System.out.println("작동");
			count++;
		}
	}
	
	// 방 삭제하기
	public void deleteServer() {
		System.out.println("서버 삭제 작동");
		orderMsg.println("deleteroom:"+roomData.get(chatRoomNum) + ":" + this.name);
	}
	
	private Thread chatRead() {
		return new Thread(() -> {
			try {
				String msg;
				while( (msg = readMsg.readLine()) != null) {
					System.out.println("채팅 스레드에서 보낸 메세지 : " + msg);
					System.out.println(socket.get(chatRoomNum).getPort() + " : 연결된 포트 넘버");
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
			if(serverName[count].length() > 5) {
				System.out.println("방 제목은 5글자 이하로만 작성가능합니다");
				JOptionPane.showMessageDialog(null,"방 제목은 5글자까지 작성가능합니다","경고",JOptionPane.ERROR_MESSAGE);
				return;
			}
			if(serverName[count] != null) {
				creatServer(serverName[count]);
			}
		}
		if(selectedButton == delete) {
			System.out.println("삭제 버튼 작동");
			deleteServer();
		}
		if(selectedButton == back) {
			sendMsg.println("quit:" + name + " 님이 나가셨습니다.");
			waitRoom();
			chatarea.setText("");
			try {
				socket.get(chatRoomNum).close();
				socket.remove(chatRoomNum);
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
			chatRoom();
			chatRoomNum = 0;
			System.out.println("0번방 입장");
			try {
				socket.add(chatRoomNum,new Socket("localhost", portNum.get(0)));
				readMsg = new BufferedReader(new InputStreamReader(socket.get(chatRoomNum).getInputStream()));
				sendMsg = new PrintWriter(socket.get(chatRoomNum).getOutputStream(), true);
				chatRead().start();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
		}
		if(roomList.getSelectedIndex() == 1) {
			chatRoom();
			chatRoomNum = 1;
			System.out.println(chatRoomNum + "번방 입장");
			try {
				socket.add(chatRoomNum,new Socket("localhost", portNum.get(1)));
				readMsg = new BufferedReader(new InputStreamReader(socket.get(chatRoomNum).getInputStream()));
				sendMsg = new PrintWriter(socket.get(chatRoomNum).getOutputStream(), true);
				chatRead().start();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
		}
		if(roomList.getSelectedIndex() == 2) {
			chatRoom();
			chatRoomNum = 2;
			System.out.println(chatRoomNum + "번방 입장");
			try {
				socket.add(chatRoomNum,new Socket("localhost", portNum.get(2)));
				readMsg = new BufferedReader(new InputStreamReader(socket.get(chatRoomNum).getInputStream()));
				sendMsg = new PrintWriter(socket.get(chatRoomNum).getOutputStream(), true);
				chatRead().start();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
		}
		if(roomList.getSelectedIndex() == 3) {
			chatRoom();
			chatRoomNum = 3;
			System.out.println(chatRoomNum + "번방 입장");
			try {
				socket.add(chatRoomNum,new Socket("localhost", portNum.get(3)));
				readMsg = new BufferedReader(new InputStreamReader(socket.get(chatRoomNum).getInputStream()));
				sendMsg = new PrintWriter(socket.get(chatRoomNum).getOutputStream(), true);
				chatRead().start();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		if(roomList.getSelectedIndex() == 4) {
			chatRoom();
			chatRoomNum = 4;
			System.out.println(chatRoomNum + "번방 입장");
			try {
				socket.add(chatRoomNum,new Socket("localhost", portNum.get(4)));
				readMsg = new BufferedReader(new InputStreamReader(socket.get(chatRoomNum).getInputStream()));
				sendMsg = new PrintWriter(socket.get(chatRoomNum).getOutputStream(), true);
				chatRead().start();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
		}
	}
}
