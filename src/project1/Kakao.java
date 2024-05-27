package project1;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
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
public class Kakao extends JFrame implements ActionListener,ListSelectionListener{

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
	private int port;
	private Vector<Server> server;
	private String[] serverName;
	private String name; // 사용자 닉네임
	private boolean login;
	private boolean wait;
	private int count;
	private DefaultListModel model;
	private JList roomList;
	
	public Kakao () {
		initData();
		initLayoutData();
		actionListener();
	}
	
	private void initData() {
		Font font2 = new Font("나눔고딕",Font.BOLD,25);
		serverName = new String[10];
		model = new DefaultListModel();
		roomList =  new JList(model);
		roomList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		roomList.setFont(font2);
		roomList.addListSelectionListener(this);
		server = new Vector<>(10);
		port = 5000;
		background1 = new JLabel(new ImageIcon("images/login.jpg"));
		background2 = new JLabel(new ImageIcon("images/openchat.png"));
		background3 = new JLabel(new ImageIcon("images/kakao.jpg"));
		roomarea = new JPanel();
		chat = new TextField();
		search = new TextField();
		nickname = new TextField();
		chatarea = new TextArea();
		makeRoom = new JButton("방 만들기");
		roomCreat = new JButton();
		setTitle("카카오톡");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setContentPane(background1);
		setSize(500,840);
		login = true;
		nickname.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				name = nickname.getText();
				waitRoom();
			}
		});
	}
	
	public void actionListener() {
		makeRoom.addActionListener(this);
	}
	
	private void initLayoutData() {
		setLayout(null);
		setResizable(false);
		setLocationRelativeTo(null);
		setVisible(true);
		nickname.setBounds(75, 400, 335, 30);
		add(nickname);
	}
	
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
		repaint();
	}
	
	public void creatServer(String name) {
		System.out.println(port);
		server.add(new Server(this,port,this.name,name));
		new Thread(server.get(count)).start();
		roomarea.add(roomList);
		model.addElement(name + " 채팅방 " + "        인원수 : " + "  / " + 30);
		port++;
		count++;
		repaint();
	}
	
	public static void main(String[] args) {
		new Kakao();
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
		if(selectedButton == join) {
			
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
			try {
				System.out.println("작동");
				Socket socket = new Socket("localhost",5000);
				System.out.println("서버접속 완료");
			} catch (UnknownHostException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
}
