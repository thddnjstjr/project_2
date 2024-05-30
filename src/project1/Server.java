package project1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Vector;

import lombok.Data;
import lombok.Locked.Write;

@Data
public class Server implements Runnable{
	
	private static Vector<Socket>socket;
	private static Vector<Socket>chatSocket;
	private static ArrayList<String>roomName;
	private static ArrayList<String>userId;
	private static ArrayList<String>createUser;
	private static ArrayList<Integer>serialId;
	private static Kakao kakao;
	private static PrintWriter writer;
	private static BufferedReader client;
	private static int number;
	private static int socketNum;
	private String name;
	private String serverName;
	private ServerFrame serverFrame;
	private static int port = 5001;
	
	public Server() {
		
		new ServerFrame(this); // 서버 프레임 화면
		socket = new Vector<>(30); // 인원 30명으로 설정
		createUser = new ArrayList<>(5);
		roomName = new ArrayList<>(20);
		userId = new ArrayList<>(30);
		chatSocket = new Vector<>(100);
		serialId = new ArrayList<>(100);
		
		try (ServerSocket serverSocket = new ServerSocket(5000)){
			System.out.println("서버 구동완료");
			while(true) {
				Socket blank = serverSocket.accept();
				socket.add(blank);
				new Service(socket.get(socketNum)).start();
				socketNum++;
				System.out.println(socketNum);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Server(int port,String servername) {
		this.serverName = servername;
		this.port = port;
		port++;
	}
	
	@Override
	public void run() {
		System.out.println(serverName + "서버 작동");
		System.out.println(port + " : 서버 포트번호");
		try (ServerSocket serversocket = new ServerSocket(port)){
			port++;
			int many = 0;
			
			// 새로운 인원이 들어올때마다 소켓 추가
				while(true) {
					try {
						Socket blank = serversocket.accept(); // 담아놓을 소켓 없으면 오류가 생김
						chatSocket.add(blank);
						many++;
						System.out.println(number + "첫번째 손님 입장");
						System.out.println(chatSocket.size() + " 소켓 사이즈");
						System.out.println(serialId.size() + " 시리얼 넘버");
						new Client(chatSocket.get(serialId.size())).start();
						broadCast(many ,port-1);
						serialId.add(number);
						number++;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// 전체에게 명령이 들어가는 메소드 (생성 명령)
	private static void broadCast(String message,int port) {
		for(Socket socket : socket) {
			try {
				writer = new PrintWriter(socket.getOutputStream(), true);
				writer.println("createroom:"+message);
				writer.println("portNum:"+port);
				System.out.println(message + "방이름 전송");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	// 전체에게 명령이 들어가는 메소드 (삭제 명령)
	private static void broadCast(int indexNum) {
		for(Socket socket : socket) {
			try {
				writer = new PrintWriter(socket.getOutputStream(), true);
				writer.println("deleteroom:"+indexNum);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	// 전체에게 명령이 들어가는 메소드 (방 인원수 전달)
	private static void broadCast(int many,int port) {
		for(Socket socket : socket) {
			try {
					writer.println("howMany:" + port+":"+ many);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	// 전체에게 메세지가 들어가는 메소드 
	private static void broadCastChat(int port,String message) {
		for(Socket socket : chatSocket) {
			try {
				if(socket.getLocalPort() == port) {
					writer = new PrintWriter(socket.getOutputStream(), true);
					writer.println(message);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	// 채팅 서버 스레드
	private static class Client extends Thread{
		
		private Socket socket;
		private BufferedReader msgin;
		
		public Client(Socket socket) {
			this.socket = socket;
		}
		
		@Override
		public void run() {
			try {
				System.out.println("스레드 작동");
				msgin = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				writer = new PrintWriter(socket.getOutputStream());
				String message;
				while( (message = msgin.readLine()) != null) {
					if(message.startsWith("quit")){ // 나갔음을 알림 
						String exit[] = message.split(":");
						int portNum = Integer.valueOf(exit[1]);
						broadCastChat(portNum,exit[2]);
					}
					else if(message.startsWith("enter")) { // 들어옴을 알림
						String enter[] = message.split(":");
						int portNum = Integer.valueOf(enter[1]);
						broadCastChat(portNum,enter[2]);
					} else if(message.startsWith("img")) { // 이모티콘 보내기
						String img[] = message.split(":");
						int portNum = Integer.valueOf(img[1]);
						broadCastChat(portNum, "img:"+img[2]);
						System.out.println(img[2]);
					} else {
						System.out.println(message);
						String broadCast[] = message.split(":");
						int portNum = Integer.valueOf(broadCast[0]);
						String chat = broadCast[1] + " : " + broadCast[2];
						broadCastChat(portNum,chat);
					}
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
	}

	// 서비스 서버 스레드
	private static class Service extends Thread {
		
		private Socket socket;
		private BufferedReader order;
		private PrintWriter service;
		
		public Service(Socket socket) {
			this.socket = socket;
			System.out.println("서비스 시작");
		}
		
		@Override
		public void run() {
			try {
				order = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				service = new PrintWriter(socket.getOutputStream(), true);
				String orderMsg;
				for(int i = 0; i < roomName.size(); i++) { // 처음 들어온 사용자에게 있는 방 리스트를 전송
					service.println("createroom:" + roomName.get(i));
					System.out.println(roomName.get(i));
				}
				while( (orderMsg = order.readLine()) != null) {
					System.out.println(orderMsg + "확인");
					if(orderMsg.startsWith("createroom")) { // 방 생성 하라는 명령어
						String[] serverName = orderMsg.split(":");
						broadCast(serverName[1],port); // 대기실 서버 사용자들에게 해당 이름의 방을 만들라고 명령 , 포트 주소도 보냄
						roomName.add(serverName[1]); // 서버에 방 추가
						createUser.add(serverName[2]); // 만든유저 정보 저장
						new Thread(new Server(port, serverName[1])).start(); 
					}
					if(orderMsg.startsWith("userName")) { // 유저 정보를 받아오는 명령어
						String[] userName = orderMsg.split(":");
						userId.add(userName[1]);
						System.out.println(userId.get(0));
					}
					if(orderMsg.startsWith("deleteroom")) { // 방 삭제 하라는 명령어
						String[] orderData = orderMsg.split(":");
						System.out.println("서버 삭제 명령 받음");
						for(int i = 0; i < roomName.size();i++) {
							if(roomName.get(i).equals(orderData[1]) && createUser.get(i).equals(orderData[2])) {
								System.out.println(i + " 번째 방 삭제");
								broadCast(i);
								roomName.remove(i); // i 번째 방 삭제
								createUser.remove(i); // i 번째 방 만든 유저 정보 삭제
							} else if(!createUser.get(i).equals(orderData[2])){
								service.println("error:wrongUser");
							}
						}
					}
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	
	
	public static void main(String[] args) {
		new Server();
	}
	
	
	
}
