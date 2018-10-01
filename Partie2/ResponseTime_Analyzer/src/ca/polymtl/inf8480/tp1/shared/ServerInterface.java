package ca.polymtl.inf8480.tp1.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

public interface ServerInterface extends Remote {
	boolean newUser(String login, String password) throws RemoteException;
	boolean create(String fileName, List<String> credentials) throws RemoteException;
	Map<String, String> list(List<String> credentials) throws RemoteException;
	Map<String, String> syncLocalDirectory(List<String> credentials) throws RemoteException;
	String get(String fileName, byte[] checksum, List<String> credentials) throws RemoteException;
	Map<String, String> lock(String fileName, byte[] checksum, List<String> credentials) throws RemoteException;
	void push(String fileName, String content, List<String> credentials) throws RemoteException;
}
