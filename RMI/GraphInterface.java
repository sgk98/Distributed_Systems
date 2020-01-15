import java.rmi.RemoteException;
import java.io.IOException;

public interface GraphInterface extends java.rmi.Remote {
   
    public int sendMessage(String message) throws RemoteException, IOException ;
    public void updateGraph(int i,int j,int id) throws RemoteException, IOException;
    public void findGraph(int i, int j,int id) throws RemoteException, IOException;
    public void getGraph(int id) throws RemoteException, IOException;
    public int join(GraphInterface client) throws RemoteException ;
}