package ca.polymtl.inf8480.tp1.server;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

import ca.polymtl.inf8480.tp1.shared.ServerInterface;

public class Server implements ServerInterface {

	Map<String, String> users = new HashMap<String, String>();
	
	public static void main(String[] args) {
		Server server = new Server();
		server.run();
	}

	public Server() {
		super();
	}

	private void run() {
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}

		try {
			ServerInterface stub = (ServerInterface) UnicastRemoteObject
					.exportObject(this, 0);

			Registry registry = LocateRegistry.getRegistry();
			registry.rebind("server", stub);
			System.out.println("Server ready.");
		} catch (ConnectException e) {
			System.err
					.println("Impossible de se connecter au registre RMI. Est-ce que rmiregistry est lancé ?");
			System.err.println();
			System.err.println("Erreur: " + e.getMessage());
		} catch (Exception e) {
			System.err.println("Erreur: " + e.getMessage());
		}
	}

	/*
	 * Méthode accessible par RMI. Additionne les deux nombres passés en
	 * paramètre.
	 */
	@Override
	public int execute(int a, int b) throws RemoteException {
		return a + b;
	}
	
	/*
	 * Méthode accessible par RMI. Retour 1.
	 */
	@Override
	public int testArrayLengthImpact(byte[] value) throws RemoteException {
		return 1;
	}

	/*
	 * Méthode accessible par RMI. Return false if the user already exists.
	 */
	@Override
	public boolean newUser(String login, String password) throws RemoteException {
		if(users.get(login) == null)
		{
			users.put(login, password);
			return true;
		}
		return false;
	}

	@Override
	public boolean verify(String login, String password) throws RemoteException {
		return password.equals(users.get(login));
	}

	@Override
	public boolean create(String Name) throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}
}
