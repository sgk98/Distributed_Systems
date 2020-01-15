import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.io.* ;
import java.net.* ;
import java.util.* ;
import java.awt.* ;
import java.util.Queue; 
import java.util.LinkedList; 

public class Server extends UnicastRemoteObject implements GraphInterface {
    public static final long serialVersionUID = 1L ;
    public ArrayList<ArrayList<Integer> > adj; 
    public Vector<GraphInterface> clientList;
    public int counter;
    public static void main(String[] args) throws RemoteException, MalformedURLException {
        System.setProperty("java.rmi.server.hostname","10.2.130.130");
        Naming.rebind("RMIServer" ,new Server());
    }
    public Server() throws RemoteException
    {
        counter=0;
        clientList = new Vector<GraphInterface>();
        adj = new ArrayList<ArrayList<Integer> >(10000); 
        for(int i=0;i<10000;i++)
        {
            adj.add(new ArrayList<Integer>()); 
        }

    }

    public void updateGraph(int i,int j,int id) throws RemoteException, IOException
    {
        adj.get(i).add(j);
        adj.get(j).add(i);
        GraphInterface client = clientList.get(id);
        client.sendMessage("Graph updated");
    }

    public void findGraph(int i, int j,int id) throws RemoteException, IOException
    {
        //System.out.println("called");
        Queue<Integer> q = new LinkedList<>(); 
        int visited[] = new int[10000]; 
        int distance[] = new int[10000];
        visited[i]=1;
        distance[i]=0;
        q.add(i);
        while(q.size()!=0)
        {
            int ver = q.peek();
            q.remove();
            //distance[ver]=distance[i]+1;
            //visited[ver] = 1;
            Iterator<Integer> it = adj.get(ver).listIterator();
            while (it.hasNext()) 
            { 
                int n = it.next(); 
                if (visited[n]==0) 
                { 
                    visited[n] = 1;
                    distance[n] = distance[ver]+1; 
                    q.add(n); 
                } 
            }  
            i=ver;

        }

        int result=-1;
        if(visited[j]==0)
        {
            result=-1;
        }
        else
        {
            result=distance[j];
        }
        String res="Shortest distance is "+result; 
        GraphInterface client = clientList.firstElement();
        System.out.println(res);
        client.sendMessage(res);
    }
    public void getGraph(int id) throws RemoteException, IOException
    {
        String message = "The edges are follows \n";
        for(int i=0;i<10000;i++)
        {
            Iterator <Integer> it = adj.get(i).listIterator();
            while(it.hasNext())
            {
                int n1=it.next();
                String sn1 = n1 +"";
                String sn2 = i+"";
                if(n1>i)
                {
                    message = message + sn2 + " "+sn1+" \n";
                }
                
            }
        }
        System.out.println("id is "+id);
        GraphInterface client = clientList.get(id);
        client.sendMessage(message);
    }

    public int join(GraphInterface client){
        clientList.add(client);
        System.out.println("counter"+counter);
        return counter++;
    }


    public int sendMessage (String message) throws RemoteException, IOException {return 0;}

}

