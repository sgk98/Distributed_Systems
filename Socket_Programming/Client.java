import java.net.*;
import java.io.*;
import java.util.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import static java.nio.file.StandardOpenOption.APPEND;
import java.net.InetAddress;


public class Client
{
	public static DataInputStream sInput;
	public static DataOutputStream sOutput;
	public Socket socket;
	public String server;
	public static String username;
	public int port;


	Client(String server, int port, String username)
	{
		this.server = server;
		this.port = port;
		this.username = username;
	}

	public boolean start()
	{
		try {
			socket = new Socket(server, port);
		}
		catch(Exception ec) {
			System.out.println("Error, can't connect to the server:" + ec);
			return false;
		}

		String msg = "Connection accepted " + socket.getInetAddress() + ":" + socket.getPort();
		System.out.println(msg);

		try
		{
			sInput  = new DataInputStream(socket.getInputStream());
			sOutput = new DataOutputStream(socket.getOutputStream());
		}
		catch (IOException eIO) {
			System.out.println("Exception creating new Input/output Streams: " + eIO);
			return false;
		}
		try
		{
			sOutput.writeUTF(username);
		}
		catch (IOException eIO) {
			System.out.println("Exception doing login : " + eIO);
			return false;
		}
		ListenFromServer masterListener = new ListenFromServer();
		masterListener.start();
		return true;
	}
	void sendMessage(String msg) {
		try {
			sOutput.writeUTF(msg);
		}
		catch(IOException e) {
			System.out.println("Exception writing to server: " + e);
		}
	}

	public static void main(String[] args)
	{

		int portNumber=3000;
		String serverAddress = "localhost";
		Scanner scan = new Scanner(System.in);

		System.out.println("Enter the username: ");
		String userName = scan.nextLine();

		// Read port number, server address
		// Example 6006, localhost
		serverAddress = args[1];
		try {
			portNumber = Integer.parseInt(args[0]);
		}
		catch(Exception e) {
			System.out.println("Give a proper port number");
			return;
		}
		Client client = new Client(serverAddress, portNumber, userName);

		if(!client.start())
			return;

		while(true)
		{
			System.out.print(">> ");
			String message = scan.nextLine();
			String[] elements = message.split(" ");
			client.sendMessage(message);
			BufferedInputStream bis = null;


			if(elements[0].equalsIgnoreCase("upload"))
			{
				
				try{
					File sendFile = new File(elements[1]);

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


					}
					catch( Exception e){
						System.out.println(e);
					}


				}
				catch( Exception e){
					System.out.println(e);
				}

			}

			else if(elements[0].equalsIgnoreCase("move_file"))
			{
				try{
					File sendFile = new File(elements[1]);

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
				        System.out.println("deleting file");
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
			else if(elements[0].equalsIgnoreCase("get_file")){

				BufferedOutputStream bos = null;
				byte[] fileData = new byte[1024];
				String fileName = elements[1].split("/")[2];
				try{
					InetAddress serverIP = InetAddress.getByName(serverAddress);
					System.out.println(serverAddress);
					Socket fileSocket = new Socket(serverIP, 6000);
					FileOutputStream fos = new FileOutputStream(fileName+"_copied");
					bos = new BufferedOutputStream(fos);
					int amountRead=0;
					InputStream is = fileSocket.getInputStream();

					while((amountRead=is.read(fileData))!=-1)
					{
						//System.out.println(fileData);
			            bos.write(fileData, 0, amountRead);
					}

			        bos.flush();
					fileSocket.close();
			        System.out.println("File saved successfully!");
							// bos.write(mybytearray, 0, bytesRead);
	    			bos.close();
					fos.close();
				}
				catch(Exception e) {
					System.out.println(e);
				}
			}
			else if(elements[0].equalsIgnoreCase("upload_udp"))
			{
				byte[] getData = new byte[1024];
				try{
					byte[] data= Files.readAllBytes(Paths.get(elements[1]));
					int mSize=1;
					int count = data.length/mSize;
					if(data.length % mSize !=0){
						++count;
					}
					int cur=0;
					sOutput.writeUTF(Integer.toString(count));
					sOutput.flush();
					byte[] sendData = new byte[1];
					DatagramSocket serverSocket = new DatagramSocket(9000);
					while( cur < count)
					{
						int j=0;
						for(int i=cur*mSize;i< data.length && i< (cur +1)*mSize;i++)
						{
							sendData[j]=data[i];
							++j;
						}
						++cur;

						//get ack from server
						DatagramPacket recPacket = new DatagramPacket(getData, getData.length);
						serverSocket.receive(recPacket);

						String a = new String(recPacket.getData());

						InetAddress IPAddress = recPacket.getAddress();
		            	int port = recPacket.getPort();
		            	DatagramPacket sendPack = new DatagramPacket(sendData, sendData.length, IPAddress, port);
		            	serverSocket.send(sendPack);

		            	sendData[0]=0;

		            	

					}
					serverSocket.close();
				}
				catch(Exception e){
					System.out.println(e);
				}
			}
		}

	}

	public static class ListenFromServer extends Thread{

		public void run(){
			while(true){
				try{
					String message = sInput.readUTF();
					System.out.println(message);
					System.out.print(">> ");
				}
				catch (IOException e){
					System.out.println(e);
				}
			}
		}
	}




}