package socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * 服务器端
 * @author hualei
 * @date Sep 3, 2017 4:26:51 PM
 * 
 */
public class Server {
	
	private ServerSocket serverSocket;
	// private volatile int count;
	private List<PrintWriter> allOut = new ArrayList<PrintWriter>();
	private ExecutorService threadPool = Executors.newFixedThreadPool(10);
	private Map<String, PrintWriter> aliasOuts = new HashMap<String, PrintWriter>();
	
	public Server(){
		try {
			serverSocket = new ServerSocket(8088);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void start(){
		// System.out.println("enter server ...");
		try {
			// 循环监听 socket
			while(true){
				System.out.println("等待客户端连接...");
				Socket socket = serverSocket.accept();
				System.out.println("客户端连接成功");
				//count ++;
				
				// 未抽取处理类之前代码
				/*System.out.println("服务端与客户端 "+count+" 建立连接");
				InputStream inputStream = socket.getInputStream();
				BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
				
				// 接收多行数据
				while(true){
					System.out.println("客户端" + count + "说：" + br.readLine());
				}*/
				ClientHandler clientHandler = new ClientHandler(socket);
				threadPool.execute(clientHandler);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		Server server = new Server();
		server.start();
	}
	
	private class ClientHandler implements Runnable{
		// private int ccount;
		private Socket socket;
		
		public ClientHandler(Socket socket /*, int count*/){
			this.socket = socket;
			//this.ccount = count;
		}

		@Override
		public void run() {
			// System.out.println("服务端与客户端 "+ccount+" 建立连接");
			PrintWriter pw = null;
			try {
				// 接收客户端输出流
				OutputStream outputStream = socket.getOutputStream();
				pw = new PrintWriter(new OutputStreamWriter(outputStream, "utf-8"), true);
				add(pw);
				
				InputStream	inputStream = socket.getInputStream();
				BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
				
				String content = "";
				while((content = br.readLine()) != null){
					if(!content.contains(":")){
						aliasOuts.put(content.trim(), pw);
					}
					
					if(content.startsWith("@") && content.contains(":")){	
						String pwName = content.substring(content.indexOf("@"), content.indexOf(":"));
						aliasOuts.get(pwName).println(content.substring(content.indexOf(":")+1));
					}else{
						foreach(content);
					}
					
				}
				
				// 支持多个客户发送数据，服务器端只接收数据并展示
				/*InputStream	inputStream = socket.getInputStream();
				BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
				String content = "";
				// 接收多行数据
				while((content = br.readLine()) != null){
					System.out.println("客户端" + ccount + "说：" + content);
				}*/
				
			} catch (IOException e) {
				e.printStackTrace();
			}finally{
				remove(pw);
				if(socket != null){
					try {
						socket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	private synchronized void add(PrintWriter pw){
		allOut.add(pw);
	}
	private synchronized void remove(PrintWriter pw){
		allOut.remove(pw);
	}
	private synchronized void foreach(String content){
		Set<Entry<String, PrintWriter>> entrySet = aliasOuts.entrySet();
		Iterator<Entry<String, PrintWriter>> iterator = entrySet.iterator();
		
		while(iterator.hasNext()){
			Entry<String, PrintWriter> next = iterator.next();
			String key = next.getKey();
			if(!key.contains(" 上线了")){
				next.getValue().println(content);
			}
		}
	}
	
	
}
