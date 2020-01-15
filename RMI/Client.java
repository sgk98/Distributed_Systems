import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.net.MalformedURLException;
import java.util.Scanner;
import java.io.*;

public class Client extends UnicastRemoteObject implements GraphInterface{
    private static final long serialVersionUID = 1L;
    public GraphInterface server;
    private int clientID;
    //private static BufferedReader input_ ;
    //public static boolean canplaymore=false;

    public static void main(String[] args) throws MalformedURLException, RemoteException, NotBoundException, IOException {
        
        System.out.println("The client is now running");
        
        //String userName = scan.nextLine();
        new Client().handleLoop();
        //Client.handleLoop();
    }

    protected Client() throws MalformedURLException, RemoteException, NotBoundException, IOException  {
        System.out.println("Connecting to Server");
        this.server = (GraphInterface) Naming.lookup("rmi://10.2.130.130/RMIServer");
        this.clientID = this.server.join(this);
        System.out.println("clientID"+clientID);
        System.out.println("Conected to Server");
        System.out.println("Enter Your Queries");

        //Scanner scan = new Scanner(System.in);
        //this.server.joinGame(this);
    }

    public int sendMessage(String message) throws RemoteException, IOException {
        System.out.println(message);
        //System.out.print(">>>");
        return 0;
    }

    public void handleLoop() throws RemoteException, IOException{
        while(true)
        {


            System.out.print(">>>");
            Scanner scan = new Scanner(System.in);
            String message = scan.nextLine();
            System.out.println("message is "+message);
            String[] elements = message.split(" ");
            if(elements[0].equalsIgnoreCase("add_edge"))
            {
                //int g = Integer.parseInt(elements[1])
                int n1 = Integer.parseInt(elements[1]);
                int n2 = Integer.parseInt(elements[2]);
                this.server.updateGraph(n1,n2,this.clientID);
            }
            else if(elements[0].equalsIgnoreCase("shortest_distance"))
            {
                //System.out.println(this.clientID);
                //int g = Integer.parseInt(elements[1])
                int n1 = Integer.parseInt(elements[1]);
                int n2 = Integer.parseInt(elements[2]);
                this.server.findGraph(n1,n2,this.clientID);
            }
            else if(elements[0].equalsIgnoreCase("get_graph"))
            {
                //System.out.println("clientID"+this.clientID);
                //int g = Integer.parseInt(elements[1]);
                this.server.getGraph(this.clientID);
            }
        }
    }

    public void updateGraph(int i,int j, int id) throws RemoteException, IOException {}
    public void findGraph(int i, int j,int id) throws RemoteException, IOException {}
    public int join(GraphInterface client) throws RemoteException {return 0;}
    public void getGraph(int id) throws RemoteException, IOException {}
}