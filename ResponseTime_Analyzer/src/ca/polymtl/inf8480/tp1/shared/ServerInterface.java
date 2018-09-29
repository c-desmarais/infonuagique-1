package ca.polymtl.inf8480.tp1.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;
//import java.util.List;

public interface ServerInterface extends Remote {
	int execute(int a, int b) throws RemoteException;
	int testArrayLengthImpact(byte[] value) throws RemoteException;

	boolean newUser(String login, String password) throws RemoteException;
	boolean verify(String login, String password) throws RemoteException;
	boolean create(String Name) throws RemoteException;
	//List<> list()
}
