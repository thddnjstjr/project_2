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
	private static Vector<ServerSocket>chatServer;
	private static Kakao kakao;
	private static PrintWriter writer;
	private static BufferedReader client;
	private static int number;
	private static int socketNum;
	private String name;
	private String serverName;
	private ServerFrame serverFrame;
	private static int port = 5001;
	private static ArrayList<String>roomName;
	
	public Server() {
		
		socket = new Vector<>(30); // 인원 30명으로 설정
		chatSocket = new Vector<>(30);
		chatServer = new Vector<>(20);
		roomName = new ArrayList<>(20);
		
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
		System.out.println(port);
		try (ServerSocket serversocket = new ServerSocket(port)){
			port++;
			chatServer.add(serversocket);
			// 새로운 인원이 들어올때마다 소켓 추가
				while(true) {
					try {
						chatSocket.add(serversocket.accept());
						System.out.println(number);
						new Client(chatSocket.get(number), number).start();
						number++;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// 전체에게 명령이 들어가는 메소드
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
	// 전체에게 메세지가 들어가는 메소드
	private static void broadCastChat(String message) {
		for(Socket realsocket : chatSocket) {
			try {
				writer = new PrintWriter(realsocket.getOutputStream(), true);
				writer.println(message);
				System.out.println(message);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static class Client extends Thread{
		
		private Socket socket;
		private BufferedReader msgin;
		private int serialNumber;
		
		public Client(Socket socket, int number) {
			this.socket = socket;
			this.serialNumber = number;
		}
		
		@Override
		public void run() {
			try {
				System.out.println("스레드 작동");
				msgin = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				writer = new PrintWriter(socket.getOutputStream());
				String message;
				while( (message = msgin.readLine()) != null) {
					System.out.println(message + "채팅방");
					broadCastChat(message);
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
	}

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
				for(int i = 0; i < roomName.size(); i++) {
					service.println(roomName.get(i));
					System.out.println(roomName.get(i));
				}
				while( (orderMsg = order.readLine()) != null) {
					System.out.println(orderMsg + "확인");
					if(orderMsg.startsWith("createroom")) {
						String[] serverName = orderMsg.split(":");
						broadCast(serverName[1],port);
						roomName.add(serverName[1]);
						new Thread(new Server(port, serverName[1])).start(); 
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
