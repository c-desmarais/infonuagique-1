package ca.polymtl.inf8480.tp1.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

public interface ServerInterface extends Remote {
	int execute(int a, int b) throws RemoteException;
	int testArrayLengthImpact(byte[] value) throws RemoteException;

	boolean newUser(String login, String password) throws RemoteException;
	boolean verify(List<String> credentials) throws RemoteException;
	//List<> list()
	boolean create(String fileName, List<String> credentials) throws RemoteException;
	Map<String, String> list(List<String> credentials) throws RemoteException;
	Map<String, String> syncLocalDirectory(List<String> credentials) throws RemoteException;
	String get(String fileName, byte[] checksum, List<String> credentials) throws RemoteException;
}
