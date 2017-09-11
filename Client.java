package socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * 客户端
 * @author hualei
 * @date Sep 3, 2017 4:26:34 PM
 * 
 */
public class Client {

    private Socket socket;
    private String alias;

    public Client(){
        try {
            // socket = new Socket("127.0.0.1", 8088);
            socket = new Socket("10.20.24.103", 8088);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start(){
    	// System.out.println("enter client ...");
    	Scanner scanner = null;
    	try {
    		
    		// 接收服务端消息
    		ServerHandler serverHandler = new ServerHandler(socket);
    		Thread thread = new Thread(serverHandler);
    		thread.setDaemon(true);
    		thread.start();
    		
			OutputStream outputStream = socket.getOutputStream();
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(outputStream, "utf-8"), true);
			// 发送单行数据 pw.println("Hello, server!");
			// 发送多行数据
			scanner = new Scanner(System.in);
			System.out.println("请输入昵称：");
			alias = scanner.nextLine();
			System.out.println("你好!" + alias + ", 开始聊天吧!");
			pw.println(alias + " 上线了");
			
			while(true){
				pw.println(alias + "对你说:" + scanner.nextLine());
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			if(socket != null){
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			scanner.close();
		}
    }
    
    public static void main(String[] args) {
    	Client client = new Client();
    	client.start();
	}
    
    private class ServerHandler implements Runnable{
    	private Socket socket;
    	public ServerHandler(Socket socket){
    		this.socket = socket;
    	}
		@Override
		public void run() {
			InputStream inputStream;
			try {
				inputStream = socket.getInputStream();
				BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
				while(true){
					System.out.println(br.readLine());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
    	
    }
}
