package ucbang.network;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

public class Server extends Thread{
	protected HashMap<String,LinkedList<String>> messages = new HashMap<String,LinkedList<String>>();	
	static int numPlayers;
	ServerSocket me;
        boolean gameInProgress = false;
	
	void print(Object stuff){
    	System.out.println("Server:"+stuff);
    }
	public Server(int port){
		try{
			me=new ServerSocket(port);
		}
		catch(IOException e){
			System.err.println("Server Socket Error!\n"+e);
                        e.printStackTrace();
		}
		print("Game server is listening to port "+port);
		this.start();
	}

	public static void main(String Args[]){
		new Server(12345);
	}
	public void run(){
		while(true) {
			try {
				Socket client = me.accept();
				new ServerThread(client, this);
				numPlayers++;
			}
			catch(Exception e) {e.printStackTrace();}
		}
	}
	void addChat(String string) {
		Iterator<String> keyter = messages.keySet().iterator();
		while(keyter.hasNext()){
			messages.get(keyter.next()).add("Chat:"+string);
		}
	}
	void playerJoin(String player){
		Iterator<String> keyter = messages.keySet().iterator();
		while(keyter.hasNext()){
			messages.get(keyter.next()).add("PlayerJoin:"+player);
		}		
	}
	void playerLeave(String player){
		messages.remove(player);
		Iterator<String> keyter = messages.keySet().iterator();
		while(keyter.hasNext()){
			messages.get(keyter.next()).add("PlayerLeave:"+player);
		}		
	}	
	void startGame(){
                gameInProgress = true;
		Iterator<String> keyter = messages.keySet().iterator();
		while(keyter.hasNext()){
			messages.get(keyter.next()).add("Prompt:Player");
		}
	}
}

class ServerThread extends Thread{
	//sends HashMap of stuff to clients, gets client's updated positions
	Socket client;
	BufferedReader in;
	BufferedWriter out;
	
	Server server;
	String name="";
	String buffer;
	boolean connected=false;
	LinkedList<String> newMsgs = new LinkedList<String>();
	void print(Object stuff){
    	System.out.println("Server:"+stuff);
    }
	public ServerThread(Socket theClient, Server myServer){
		client=theClient;

		this.server=myServer;
		try {
      		in= new BufferedReader(new InputStreamReader(client.getInputStream()));
      		out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
     	}
     	catch(Exception e1) {
     		
     		e1.printStackTrace();
        	try {
           		client.close();
        	}
        	catch(Exception e) {
           		e.printStackTrace();
         	}
     	}
		try
		{


		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
     	this.start();
	}
	public synchronized void run(){		
		while(!client.isClosed()){
			try {
				if(in.ready()){
					buffer=(String)in.readLine();
					System.out.println("Server received "+buffer);
					String[] temp = buffer.split(":",2);
					if(temp[0].equals("Name")){
						if(!connected){//player was never connected
				            if(server.messages.containsKey(temp[1])){
				                out.write("Connection:Name taken!");
				                out.newLine();
				                out.flush();
				                print(client.getInetAddress()+" Attempting joining with taken name.");

				            }
							else{
								name=temp[1];
				                print(name+"("+client.getInetAddress()+") has joined the game.");
				    			server.playerJoin(name);
				    			server.messages.put(name, newMsgs);
				    			out.write("Connection:Successfully connected.");
				    			out.newLine();
				    			out.flush();
				    			Iterator<String> players = server.messages.keySet().iterator();
				    			out.write("Players:");
				    			while(players.hasNext()){//give player list of current players
				    				out.write(players.next()+",");
				    			}
				    			out.newLine();
				    			out.flush();
				            }
						}
					}
					else if(temp[0].equals("Chat")){
						if(temp[1].charAt(0)=='/'){
							//TODO: Send commands
							if(temp[1].equals("/start")&&client.getInetAddress().toString().equals("/127.0.0.1")&&!server.gameInProgress) server.startGame();//TODO: needs to make sure game isn't already in progress
							else if(temp[1].startsWith("/rename")){
                                                            if(temp[1].length()>7&&temp[1].charAt(7)==' '){
                                                                String temp1=temp[1].split(" ",2)[1];
                                                                if(server.messages.containsKey(temp1)){
                                                                    out.write("Connection:Name taken!");
                                                                    out.newLine();
                                                                    out.flush();
                                                                    print(name+"("+client.getInetAddress()+") Attempting renaming to taken name.");
                                                                }
                                                                else{
                                                                    print(name+"("+client.getInetAddress()+") is now known as "+temp1);
                                                                    server.messages.remove(name);
                                                                    server.messages.put(temp1, newMsgs);
                                                                    server.playerLeave(name);
                                                                    server.playerJoin(temp1);
                                                                    name=temp1;
                                                                    out.write("Connection:Successfully renamed.");
                                                                    out.newLine();
                                                                    out.flush();
                                                                }
                                                            }
                                                            else{
                                                                //TODO: (Optional) create /help RENAME
                                                            }
							}
						}else
							server.addChat(name+": "+temp[1]);
					}
				}
	         	if(!newMsgs.isEmpty()){
	         		Iterator<String> iter = ((LinkedList<String>) newMsgs.clone()).iterator();
	         		while(iter.hasNext()){
		         		out.write(iter.next());
		         		out.newLine();
		         		iter.remove();
	         		}
                                newMsgs.clear(); //will this still produce CME?
	         	}
	         	out.flush();

	      }
	      catch(Exception e) {
	      	if(e!=null&&e.getMessage()!=null&&e.getMessage().equals("Connection reset"))
	      		try{finalize();}catch(Throwable t){}
			else
	      		e.printStackTrace();
	      }
		}
	}
	protected void finalize() throws Throwable{
		print(name+"("+client.getInetAddress()+") has left the game.");
		server.playerLeave(name);
		try{in.close();
	    out.close();
		client.close(); }
		catch(IOException e){
			e.printStackTrace();
		}
	}
}