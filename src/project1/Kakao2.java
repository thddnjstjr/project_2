package project1;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
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
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
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
	private ArrayList<Integer> imgNum;
	private ArrayList<String> roomData;
	private JLabel background1;
	private JLabel background2;
	private JLabel background3;
	private JPanel roomarea;
	private TextField chat;
	private TextField search;
	private TextField nickname;
	private JTextArea chatarea;
	private JButton send;
	private JButton join;
	private JButton makeRoom;
	private JButton roomCreat;
	private JButton back;
	private JButton delete;
	private JButton sendImg;
	private JButton img1;
	private JButton img2;
	private JButton img3;
	private JButton closeImg;
	private String[] serverName;
	private String name; // 사용자 닉네임
	private boolean login;
	private boolean wait;
	private boolean chatting;
	private boolean imoticon;
	private int count;
	private int chatRoomNum;
	private int many; // 서버에서 받은 방 인원수
	private ArrayList<String>image;
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
			serviceSocket = new Vector<>(30); // 서비스 소켓
			socket = new Vector<>(30); // 채팅 소켓
			roomData = new ArrayList<>(5);
			for(int i = 0; i < socket.capacity(); i++) {
				socket.add(i,null); // 빈 소켓 찾기 위해서 처음에 백터 공간을 전부 null로 만들어줌
			}
			serviceSocket.add(WAITROOM,new Socket("localhost",5000));
			System.out.println("서비스 서버 접속 완료");
			
			broadCast = new BufferedReader(new InputStreamReader(serviceSocket.get(WAITROOM).getInputStream()));
			orderMsg = new PrintWriter(serviceSocket.get(WAITROOM).getOutputStream(), true);
			
			// 서버에서 명령 받음
			Thread readThread = new Thread (() -> {
				try {
					String serverMsg;
					while( (serverMsg = broadCast.readLine()) != null) {
						System.out.println("서버에서 보냄 : " + serverMsg);
						if(serverMsg.startsWith("createroom")) {
							String roomName[] = serverMsg.split(":");
							model.addElement(roomName[1] + " 방 " + "     인원수 : " + 0 + "  / " + 30);
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
		image = new ArrayList<>(100);
		imgNum = new ArrayList<>(100);
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
		roomCreat = new JButton();
		sendImg = new JButton("이모티콘");
		closeImg = new JButton("닫기");
		img1 = new JButton(new ImageIcon("images/img1.jpg"));
		img2 = new JButton(new ImageIcon("images/img2.jpg"));
		img3 = new JButton(new ImageIcon("images/img3.jpg"));
		delete = new JButton("방 삭제");
		back = new JButton();
		makeRoom = new JButton("방 만들기");
		chatarea = new JTextArea() {
			@Override
			protected void paintComponent(Graphics g) {
				// 이모티콘 그리기
					if(imoticon) {
						int imgCount = 0; // 이미지 저장해놓은거 불러오기
						g.drawImage(new ImageIcon(image.get(imgCount)).getImage(),58,imgNum.get(imgCount) * 20 - 14,this);
						if(imgNum.size() > 1) {
							g.drawImage(new ImageIcon(image.get(imgCount+1)).getImage(),58,imgNum.get(imgCount+1) * 20 - 14,this);
						}
						if(imgNum.size() > 2) {
							g.drawImage(new ImageIcon(image.get(imgCount+2)).getImage(),58,imgNum.get(imgCount+2) * 20 - 14,this);
						}
						if(imgNum.size() > 3) {
							g.drawImage(new ImageIcon(image.get(imgCount+3)).getImage(),58,imgNum.get(imgCount+3) * 20 - 14,this);
						}
						if(imgNum.size() > 4) {
							g.drawImage(new ImageIcon(image.get(imgCount+4)).getImage(),58,imgNum.get(imgCount+4) * 20 - 14,this);
						}
						if(imgNum.size() > 5) {
							g.drawImage(new ImageIcon(image.get(imgCount+5)).getImage(),58,imgNum.get(imgCount+5) * 20 - 14,this);
						}
						if(imgNum.size() > 6) {
							g.drawImage(new ImageIcon(image.get(imgCount+6)).getImage(),58,imgNum.get(imgCount+6) * 20 - 14,this);
						}
						if(imgNum.size() > 7) {
							g.drawImage(new ImageIcon(image.get(imgCount+7)).getImage(),58,imgNum.get(imgCount+7) * 20 - 14,this);
						}
						if(imgNum.size() > 8) {
							g.drawImage(new ImageIcon(image.get(imgCount+8)).getImage(),58,imgNum.get(imgCount+8) * 20 - 14,this);
						}
						if(imgNum.size() > 9) {
							g.drawImage(new ImageIcon(image.get(imgCount+9)).getImage(),58,imgNum.get(imgCount+9) * 20 - 14,this);
						}
						if(imgNum.size() > 10) {
							g.drawImage(new ImageIcon(image.get(imgCount+10)).getImage(),58,imgNum.get(imgCount+10) * 20 - 14,this);
						}
						if(imgNum.size() > 11) {
							g.drawImage(new ImageIcon(image.get(imgCount+11)).getImage(),58,imgNum.get(imgCount+11) * 20 - 14,this);
						}
					}
					super.paintComponent(g);
			}
		};
		setTitle("카카오톡");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setContentPane(background1);
		setSize(500,840);
		login = true;
		Font chatFont = new Font("망고보드 또박체 B",Font.BOLD,20);
		chatarea.setFont(chatFont);
		chatarea.setOpaque(false); // 배경색 투명으로 만듬
		chatarea.setLineWrap(true); // 자동으로 줄바꿈
		chatarea.setEditable(false); // 글자 수정 불가
		chat.setFont(chatFont);
	}
	
	public void actionListener() {
		makeRoom.addActionListener(this);
		roomList.addListSelectionListener(this);
		delete.addActionListener(this);
		back.addActionListener(this);
		sendImg.addActionListener(this);
		closeImg.addActionListener(this);
		img1.addActionListener(this);
		img2.addActionListener(this);
		img3.addActionListener(this);
		
		nickname.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				name = nickname.getText();
				orderMsg.println("userName:" + name); // 서버에게 사용자 이름을 전송
				waitRoom();
			}
		});
		// 채팅 보내는 이벤트
		chat.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!chat.getText().equals("")) {
					System.out.println(chat.getText() + " : 보낸메세지");
					sendMsg.println(socket.get(chatRoomNum).getPort() + ":" + name + ":"+ chat.getText() + "");
					chat.setText("");
				}
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
		img1.setBounds(50,550,120,120);
		img2.setBounds(200,550,120,120);
		img3.setBounds(350,550,120,120);
		closeImg.setBounds(365,690,100,50);
		sendImg.setBounds(365,690,100,50);
		delete.setBounds(375,40,100,50);
		back.setBounds(17,34,40,40);
	}
	// 대기실 화면
	private void waitRoom() {
		login = false;
		wait = true;
		chatting = false;
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
		add(back);
		add(delete);
		add(sendImg,0);
		back.setBorderPainted(false);
		back.setContentAreaFilled(false);
		back.setFocusPainted(false);
		chat.setSize(400,40);
		chat.setLocation(40, 750);
		JScrollPane scroll = new JScrollPane(chatarea) { // 스크롤 pane 테두리가 없도록 오버라이딩
			@Override
			public void setBorder(Border border) {
			}
		};
		scroll.getViewport().setOpaque(false); // 스크롤 배경 투명하게만들기
		scroll.setOpaque(false); // 스크롤 pane 배경 투명하게 만들기
		scroll.getVerticalScrollBar().setValue(scroll.getVerticalScrollBar().getMaximum());
		add(scroll);
		scroll.setLocation(40, 100);
		scroll.setSize(400,620);
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
			count++;
		}
	}
	
	// 방 삭제하기
	public void deleteServer() {
		orderMsg.println("deleteroom:"+roomData.get(chatRoomNum) + ":" + this.name);
	}
	
	// 서버에서 보낸 메세지를 읽는 스레드
	private Thread chatRead() {
		return new Thread(() -> {
			try {
				String msg;
				while( (msg = readMsg.readLine()) != null && chatting) {
					if(msg.startsWith("img")) {
						System.out.println("이미지 그리기 명령 들어옴");
						String img[] = msg.split(":");
						image.add(img[1]);
						imgNum.add(chatarea.getLineCount());
						imoticon = true;
						chatarea.paintComponents(getGraphics());
						chatarea.append("\n");
						chatarea.append("\n");
						chatarea.append("\n");
						chatarea.append("\n");
						chatarea.append("\n");
						chatarea.append("\n");
					} else if (msg.startsWith("howMany")){
						return;
					} else {
						System.out.println("채팅 스레드에서 보낸 메세지 : " + msg);
						System.out.println(socket.get(chatRoomNum).getPort() + " : 연결된 포트 넘버");
						chatarea.append("\n" + msg);
					}
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
			// 방 만들기 액션
			serverName[count] = JOptionPane.showInputDialog("방 만들기 ");
			if(serverName[count].length() > 5) {
				System.out.println("방 제목은 5글자 이하로만 작성가능합니다");
				JOptionPane.showMessageDialog(null,"방 제목은 5글자까지 작성가능합니다","경고",JOptionPane.ERROR_MESSAGE);
				return;
			}
			else if(serverName[count] != null) {
				creatServer(serverName[count]);
			}
		}
		if(selectedButton == delete) {
			deleteServer();
		}
		// 뒤로 가기 액션 
		if(selectedButton == back) {
			imoticon = false;
			imgNum.clear();
			image.clear();
			sendMsg.println("quit:"+ socket.get(chatRoomNum).getPort() + ":" + name + " 님이 나가셨습니다.");
			waitRoom();
			chatarea.setText(null);
			try {
				socket.get(chatRoomNum).close();
				socket.remove(chatRoomNum);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		if(selectedButton == sendImg) {
			requestFocus();
			add(img1,0);
			add(img2,0);
			add(img3,0);
			add(closeImg,0);
			remove(sendImg);
			repaint();
		}
		// 이모티콘 보내기
		if(selectedButton == img1) {
			sendMsg.println(socket.get(chatRoomNum).getPort()+":"+name+":"+" ");
			sendMsg.println("img:"+socket.get(chatRoomNum).getPort()+":"+"images/img1.gif");
			remove(img1);
			remove(img2);
			remove(img3);
			remove(closeImg);
			add(sendImg,0);
			repaint();
		}
		if(selectedButton == img2) {
			sendMsg.println(socket.get(chatRoomNum).getPort()+":"+name+":"+" ");
			sendMsg.println("img:"+socket.get(chatRoomNum).getPort()+":"+"images/img2.gif");
			remove(img1);
			remove(img2);
			remove(img3);
			remove(closeImg);
			add(sendImg,0);
			repaint();
		}
		if(selectedButton == img3) {
			sendMsg.println(socket.get(chatRoomNum).getPort()+":"+name+":"+" ");
			sendMsg.println("img:"+socket.get(chatRoomNum).getPort()+":"+"images/img3.gif");
			remove(img1);
			remove(img2);
			remove(img3);
			remove(closeImg);
			add(sendImg,0);
			repaint();
		}
		if(selectedButton == closeImg) {
			remove(img1);
			remove(img2);
			remove(img3);
			remove(closeImg);
			add(sendImg,0);
			repaint();
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
			g.drawString(name+"", 20, 700);
		}
		if(chatting == true) {
			g.setFont(font1);
			g.drawString(roomData.get(chatRoomNum)+" 방", 65,96);
		}
	}

	
	// 채팅방 선택했을때 이벤트 리스너
	@Override
	public void valueChanged(ListSelectionEvent e) {
		if(roomList.getSelectedIndex() == 0) {
			chatRoom();
			chatRoomNum = 0;
			System.out.println(chatRoomNum + "번방 입장");
			try {
				socket.add(chatRoomNum,new Socket("localhost", portNum.get(chatRoomNum)));
				readMsg = new BufferedReader(new InputStreamReader(socket.get(chatRoomNum).getInputStream()));
				sendMsg = new PrintWriter(socket.get(chatRoomNum).getOutputStream(), true);
				sendMsg.println("enter" + ":" + socket.get(chatRoomNum).getPort() +":"+ name + " 님이 입장하셨습니다.");
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
				socket.add(chatRoomNum,new Socket("localhost", portNum.get(chatRoomNum)));
				readMsg = new BufferedReader(new InputStreamReader(socket.get(chatRoomNum).getInputStream()));
				sendMsg = new PrintWriter(socket.get(chatRoomNum).getOutputStream(), true);
				sendMsg.println("enter" + ":" + socket.get(chatRoomNum).getPort() +":"+ name + " 님이 입장하셨습니다.");
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
				socket.add(chatRoomNum,new Socket("localhost", portNum.get(chatRoomNum)));
				readMsg = new BufferedReader(new InputStreamReader(socket.get(chatRoomNum).getInputStream()));
				sendMsg = new PrintWriter(socket.get(chatRoomNum).getOutputStream(), true);
				sendMsg.println("enter" + ":" + socket.get(chatRoomNum).getPort() +":"+ name + " 님이 입장하셨습니다.");
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
				socket.add(chatRoomNum,new Socket("localhost", portNum.get(chatRoomNum)));
				readMsg = new BufferedReader(new InputStreamReader(socket.get(chatRoomNum).getInputStream()));
				sendMsg = new PrintWriter(socket.get(chatRoomNum).getOutputStream(), true);
				sendMsg.println("enter" + ":" + socket.get(chatRoomNum).getPort() +":"+ name + " 님이 입장하셨습니다.");
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
				socket.add(chatRoomNum,new Socket("localhost", portNum.get(chatRoomNum)));
				readMsg = new BufferedReader(new InputStreamReader(socket.get(chatRoomNum).getInputStream()));
				sendMsg = new PrintWriter(socket.get(chatRoomNum).getOutputStream(), true);
				sendMsg.println("enter" + ":" + socket.get(chatRoomNum).getPort() +":"+ name + " 님이 입장하셨습니다.");
				chatRead().start();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
}
