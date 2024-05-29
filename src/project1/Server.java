package project1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

import lombok.Data;
import lombok.Locked.Write;

@Data
public  class Server implements Runnable{
	
	private static Vector<Socket>socket;
	private static Kakao kakao;
	private static PrintWriter writer;
	private static int number;
	private String name;
	private String serverName;
	private int port;
	
	public Server(Kakao kakao,int port,String name,String servername) {
		this.name = name;
		this.serverName = servername;
		socket = new Vector<>(30); // 인원 30명으로 설정
		this.kakao = kakao;
		this.port = port;
	}
	
	@Override
	public void run() {
		System.out.println(serverName + "서버 작동");
		System.out.println(port);
		try (ServerSocket serversocket = new ServerSocket(port)){
			
			// 새로운 인원이 들어올때마다 소켓 추가
				while(true) {
					try {
						socket.add(serversocket.accept());
						new Thread(new Client(socket.get(number),number)).start();
						for(int i = 0; i < socket.size(); i++) {
							if(socket.get(i) == null) {
								number = i;
								break;
							}
						}
					} catch (IOException e) {
						e.printStackTrace();
					}1
				}
		
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// 전체에게 메세지가 들어가는 메소드
	private static void broadCast(String message,PrintWriter writer) {
		for(Socket socket : socket) {
			writer.println(message);
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
				msgin = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				writer = new PrintWriter(socket.getOutputStream());
				String message;
				while( (message = msgin.readLine()) != null) {
					broadCast(message,writer);
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
	}

	
	
	
}
