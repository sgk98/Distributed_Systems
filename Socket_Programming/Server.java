import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import java.net.InetAddress;
import java.util.HashMap; 
import java.util.Map; 

public class Server {
	private static int uniqueId = 0;
	private ArrayList<ClientThread> al;
	private int port;
	private boolean keepGoing;
	private String[] chatRooms= new String[20];
	private String[] inWhichRoom= new String[20];
	private int roomCounter = 0;
	public static HashMap<String,List<String> > Groups = new HashMap<String,List<String> >();
	public static HashMap<String,List<String> > allFiles = new HashMap<String,List<String> >();


	public Server(int port) {
		this.port = port;
		al = new ArrayList<ClientThread>();
	}

	public void start() {

		//Groups.put("calculus",new int[] {1,2});
		keepGoing = true;
		try
		{
			ServerSocket serverSocket = new ServerSocket(port);
			while(keepGoing)
			{
				System.out.println("Server waiting for Clients on port " + port + ".");
				Socket socket = serverSocket.accept();
				if(!keepGoing)
					break;
				ClientThread t = new ClientThread(socket);
				al.add(t);
				t.start();
			}
			// stop server
			try {
				serverSocket.close();
				for(int i = 0; i < al.size(); ++i) {
					ClientThread tc = al.get(i);
					try {
					tc.sInput.close();
					tc.sOutput.close();
					tc.socket.close();
					}
					catch(IOException ioE) {
					}
				}
			}
			catch(Exception e) {
				System.out.println("Exception closing the server and clients: " + e);
			}
		}
		catch (IOException e) {
			System.out.println(e);
		}
	}

	public static void main(String[] args) {
		int portNumber = 3000;
		try {
			portNumber = Integer.parseInt(args[0]);
		}
		catch(Exception e) {
			System.out.println("Give a proper port number");
			return;
		}

		Server server = new Server(portNumber);
		server.start();
	}

	private synchronized boolean broadcast(String broadcast_message,String groupname) {
		List<String> users=Groups.get(groupname);
		System.out.println("in function"+broadcast_message);
		for(int i = al.size(); --i >= 0;) {
			ClientThread ct = al.get(i);
			for(int j=0;j<users.size();j++){
				if(ct.username.equalsIgnoreCase(users.get(j)))
				{
					ct.sendMessage(broadcast_message);
				}
			}
			//if(!ct.sendMessage(message)) {
			//	al.remove(i);
			//	System.out.println("Disconnected Client " + ct.username + " removed from list.");
			//}
		}
		return true;
	}


	class ClientThread extends Thread
	{
		Socket socket;
		DataInputStream sInput;
		DataOutputStream sOutput;
		int id;
		String username;

		ClientThread(Socket socket) {
			id = ++uniqueId;
			inWhichRoom[id] = "Lobby";
			this.socket = socket;
			try
			{
				sOutput = new DataOutputStream(socket.getOutputStream());
				sInput  = new DataInputStream(socket.getInputStream());
				username = sInput.readUTF();
				System.out.println(username + " has joined Lobby");
				ArrayList<String> userFiles = new ArrayList<String>();
				allFiles.put(username,userFiles);
				//broadcast(username + " has joined Lobby");
			}
			catch (IOException e) {
				System.out.println(e);
				return;
			}
		}

		public void run(){

			boolean keepGoing = true;
			BufferedOutputStream bos = null;

			while(keepGoing)
			{
				String message;
				try{
					message = sInput.readUTF();
				}
				catch (IOException e){
					System.out.println(username + " unable to read streams: " + e);
					break;
				}

				String[] elements=message.split(" ");
				if ( elements[0].equalsIgnoreCase("create_group"))
				{
					System.out.println("create_group command received");
					ArrayList<String> userlist = new ArrayList<String>();
					System.out.println(elements[1]);
					Groups.put(elements[1],userlist);
				}

				else if(elements[0].equalsIgnoreCase("list_groups"))
				{
					String list_groups="";
					System.out.println("list_groups command received");
					for ( String key : Groups.keySet() ) {
    					list_groups += key+" ";
					}
					System.out.println(list_groups);
					sendMessage(list_groups);
				}

				else if(elements[0].equalsIgnoreCase("join_group"))
				{
					String groupname = elements[1];
					Groups.get(groupname).add(username);
					System.out.println("joined group");
					sendMessage(username + "joined group"+ groupname);
				}
				else if(elements[0].equalsIgnoreCase("leave_group"))
				{
					String groupname = elements[1];
					Groups.get(groupname).remove(username);
					System.out.println("left group");
					sendMessage(username + "left group" + groupname);
				}

				else if(elements[0].equalsIgnoreCase("share_msg"))
				{
					String broadcast_message="";
					for(int i=2;i<elements.length; i++)
					{
						broadcast_message+=elements[i]+" ";
						
					}
					System.out.println("broadcast_message is "+broadcast_message);
					broadcast(broadcast_message,elements[1]);
				}

				else if(elements[0].equalsIgnoreCase("list_detail"))
				{
					String groupname = elements[1];
					List<String> users = Groups.get(groupname);
					String send_message = "Users and Files: ";
					for(int i=0;i<users.size();i++)
					{
						send_message+=users.get(i)+" ";
						List<String> userFiles = allFiles.get(users.get(i));
						for(int j=0;j< userFiles.size();j++){
							send_message+=userFiles.get(j);
							System.out.println(userFiles.get(j));
						}
						send_message+="\n";
					}
					sendMessage(send_message);
				}
				else if(elements[0].equalsIgnoreCase("upload"))
				{
					byte[] fileData = new byte[1024];
					try{
						InetAddress clientIP = socket.getInetAddress();
						//Socket fileSocket = new Socket(InetAddress.getByName("localhost"), 6000);
						Socket fileSocket = new Socket(clientIP, 6000);
						FileOutputStream fos = new FileOutputStream(elements[1]);
						bos = new BufferedOutputStream(fos);
						int amountRead=0;
						InputStream is = fileSocket.getInputStream();

						while((amountRead=is.read(fileData))!=-1)
						{
							System.out.println(fileData);
			            	bos.write(fileData, 0, amountRead);
						}

			        	bos.flush();
						fileSocket.close();
			        	System.out.println("File saved successfully!");
			        	allFiles.get(username).add(elements[1]);
			        	System.out.println(elements[1]);
							// bos.write(mybytearray, 0, bytesRead);
	    				bos.close();
						fos.close();
						}
					catch(Exception e) {
						System.out.println(e);
					}
					
				}
				else if(elements[0].equalsIgnoreCase("create_folder"))
				{
					String dir=elements[1];
					new File(dir).mkdirs();
					System.out.println("Directory created");
					sendMessage("Directory created");
				}

				else if(elements[0].equalsIgnoreCase("move_file")){

					byte[] fileData = new byte[1024];
					try{
						InetAddress clientIP = socket.getInetAddress();
						Socket fileSocket = new Socket(clientIP, 6000);
						FileOutputStream fos = new FileOutputStream(elements[2]);
						bos = new BufferedOutputStream(fos);
						int amountRead=0;
						InputStream is = fileSocket.getInputStream();

						while((amountRead=is.read(fileData))!=-1)
						{
							System.out.println(fileData);
			            	bos.write(fileData, 0, amountRead);
						}

			        	bos.flush();
						fileSocket.close();
			        	System.out.println("File saved successfully!");
			        	allFiles.get(username).add(elements[1]);
							// bos.write(mybytearray, 0, bytesRead);
	    				bos.close();
						fos.close();
						}
					catch(Exception e) {
						System.out.println(e);
					}			
				}
				else if(elements[0].equalsIgnoreCase("get_file")){

					try{
						BufferedInputStream bis = null;
						String fileName = elements[1].split("/")[2];
						File sendFile = new File(fileName);

						ServerSocket ssock = new ServerSocket(6000);

			        	Socket socker = ssock.accept();
			        	System.out.println("lets send file");
			        	//InetAddress IA = InetAddress.getByName("localhost");//replace with localhost if needed
			        	System.out.println("socket ready");
			        	try {
							bis = new BufferedInputStream(new FileInputStream(sendFile));
						}
						catch(FileNotFoundException fe) {
							System.out.println(fe);
						}

						try{
							byte[] fileData;
							long  fileLength = sendFile.length();
							long  current_pos=0;
							//long start = System.nanoTime();
							System.out.println("going to send file");

							OutputStream send_stream = socker.getOutputStream();

							while(current_pos!=fileLength){
					            int size = 1024;
					            if(fileLength - current_pos >= size)
					                current_pos += size;
					            else{
					                size = (int)(fileLength - current_pos);
					                current_pos = fileLength;
					            }
					            fileData = new byte[size];
					            bis.read(fileData, 0, size);
					            send_stream.write(fileData);
					            System.out.print("Sending file ... "+(current_pos*100)/fileLength+"% complete!");
					        }
					        bis.close();
					        send_stream.flush();
					        socker.close();
					        ssock.close();
					        //System.out.println("deleting file");
					        sendFile.delete();


						}
						catch( Exception e){
							System.out.println(e);
						}
					}
					catch(Exception e){
						System.out.println(e);
					}


				}
				else if(elements[0].equalsIgnoreCase("upload_udp"))
				{
					String countlength;
					int count;
					try{
						DatagramSocket clientSocket = new DatagramSocket();
			        	InetAddress IPAddress = socket.getInetAddress();
			        	byte[] sendData = new byte[1];
			        	String sentence = "contacting";
			        	sendData = sentence.getBytes();
			        	DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress,9000);
			        	countlength = sInput.readUTF();
			        	count = Integer.parseInt(countlength);
			        	Files.write(Paths.get( elements[1]), "".getBytes());
			        	for(int i=0;i<count;i++)
			        	{
			        		byte[] receiveData = new byte[1];
			        		DatagramPacket getPacket = new DatagramPacket(receiveData, receiveData.length);
			        		clientSocket.send(sendPacket);
			            	clientSocket.receive(getPacket);
			            	String data = new String(getPacket.getData());
			            	Files.write(Paths.get( elements[1]), data.getBytes(), APPEND);
			        	}
			        	clientSocket.close();
			        	allFiles.get(username).add(elements[1]);
					}
					catch(Exception e){
						System.out.println(e);
					}
				}

			}
		}

		private void close() {
			try {
				if(sOutput != null) sOutput.close();
			}
			catch(Exception e) {}
			try {
				if(sInput != null) sInput.close();
			}
			catch(Exception e) {};
			try {
				if(socket != null) socket.close();
			}
			catch (Exception e) {}
		}

		private boolean sendMessage(String msg) {
			if(!socket.isConnected()) {
				close();
				return false;
			}
			try {
				sOutput.writeUTF(msg);
				System.out.println("message sent");
			}
			catch(IOException e) {
				System.out.println("Error sending message to " + username);
				System.out.println(e.toString());
			}
			return true;
		}

	}
}
