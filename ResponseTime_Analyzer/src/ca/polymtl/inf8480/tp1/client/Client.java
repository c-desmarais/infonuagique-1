package ca.polymtl.inf8480.tp1.client;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import ca.polymtl.inf8480.tp1.shared.ServerInterface;

public class Client {
	
	private static int testLength = 0;
	
	private final static String CREDENTIALS_FILE_NAME = "localAuthFile.txt";
	

	FakeServer  localServer = null; // Pour tester la latence d'un appel de
									// fonction normal.
	private static ServerInterface localServerStub = null;
	private static ServerInterface distantServerStub = null;
	
	public static void main(String[] args) throws RemoteException {
		String distantHostname = "132.207.12.114";
		Client client = new Client(distantHostname);
		//client.run();
		if (args.length == 3) {
			switch(args[0]) {
				case "new": client.newUser(args[1],args[2]);
							break;
				
				default: break;
			}
		}
		else if (args.length == 2) {
			switch(args[0]) {
				case "create": client.create(args[1]);
								break;
				default: break;
			}
		}
		else if (args.length == 1) {
			switch(args[0]) {
				case "List": client.printlist();
							break;
				
				default: break;
			}
		}
		else
		{
			client.PrintArgErrorMsg();
		}
	}

	private void PrintArgErrorMsg()
	{
		System.out.println("Veuillez specifier des arguments parmi la liste suivante:");
		System.out.println("new <login> <password>");
		System.out.println("create <name>");
		System.out.println("list <name>");
		System.out.println("list <name>");
	}

	public Client(String distantServerHostname) {
		super();

		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}

		//localServer = new FakeServer();
		localServerStub = loadServerStub("127.0.0.1");

		if (distantServerHostname != null) {
			distantServerStub = loadServerStub(distantServerHostname);
		}
	}
	
	private void newUser(String login, String password) {
		try {
			if( distantServerStub.newUser(login, password) )
			{
				createLocalAuthFile(login, password);
				System.out.println("Bravo le user " + login +" a ete cree!");
			}
			else
			{
				System.out.println("Erreur le user " + login +" existe deja!");
			}
		}
		catch (RemoteException e)
		{
			System.out.println("Erreur: " + e.getMessage());
		}
	}

	private Path createLocalAuthFile(String login, String password) {
		try {
			return Files.write(Paths.get(CREDENTIALS_FILE_NAME),Arrays.asList(login,password), Charset.forName("UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private void create(String fileName)
	{
		List<String> credentials = getSavedCredentials();
		
		try {
			if( distantServerStub.create(fileName, credentials) )
			{
				System.out.println(fileName +" ajoute.");
			}
			else
			{
				System.out.println(fileName +" existe deja.");
			}
		}
		catch (RemoteException e)
		{
			System.out.println("Erreur: " + e.getMessage());
		}
	}
	
	private List<String> getSavedCredentials()
	{
		List<String> credentials = new ArrayList<String>();
		try
		{
			credentials = Files.readAllLines(Paths.get(CREDENTIALS_FILE_NAME));
		}
		catch (NoSuchFileException e)
		{
			System.out.println("Vous devez vous enregistrer d'abord avec la commande new <id> <password>.");
		}
		catch (IOException e)
		{
			e.printStackTrace();	
		}
		return credentials;
	}
	
	private void printlist() {
		List<String> credentials = getSavedCredentials();
		
		try {
			Map<String,String> filesAndLocks = distantServerStub.list(credentials);
			for(Map.Entry<String, String> entry: filesAndLocks.entrySet())
			{
				System.out.println("* " + entry.getKey() + "    " + (entry.getValue() == "" ? "non verouille" : entry.getValue()));
			}
		}
		catch (RemoteException e)
		{
			System.out.println("Erreur: " + e.getMessage());
		}
	}

	private void run() {
		
		// Check if we are testing the effect of different array length on the RMI
		byte[] bytes = new byte[(int) Math.pow(10, testLength)];
		
		appelNormal(bytes);

		if (localServerStub != null) {
			appelRMILocal(bytes);
		}

		if (distantServerStub != null) {
			appelRMIDistant(bytes);
		}
	}

	private ServerInterface loadServerStub(String hostname) {
		ServerInterface stub = null;

		try {
			Registry registry = LocateRegistry.getRegistry(hostname);
			stub = (ServerInterface) registry.lookup("server");
		} catch (NotBoundException e) {
			System.out.println("Erreur: Le nom '" + e.getMessage()
					+ "' n'est pas défini dans le registre.");
		} catch (AccessException e) {
			System.out.println("Erreur: " + e.getMessage());
		} catch (RemoteException e) {
			System.out.println("Erreur: " + e.getMessage());
		}

		return stub;
	}

	private void appelNormal(byte[] bytes) {
		long start = System.nanoTime();
		int result = -1;
		if ( testLength == 0 ) {
			result = localServer.execute(4, 7);
		}
		else {
			result = localServer.testArrayLengthImpact(bytes);
		}
		long end = System.nanoTime();

		System.out.println("Temps écoulé appel normal: " + (end - start)
				+ " ns");
		System.out.println("Résultat appel normal: " + result);
	}

	private void appelRMILocal(byte[] bytes) {
		try {
			long start = System.nanoTime();
			int result = -1;
			if ( testLength == 0 ) {
				result = localServerStub.execute(4, 7);
			}
			else {
				result = localServerStub.testArrayLengthImpact(bytes);
			}
			long end = System.nanoTime();

			System.out.println("Temps écoulé appel RMI local: " + (end - start)
					+ " ns");
			System.out.println("Résultat appel RMI local: " + result);
		} catch (RemoteException e) {
			System.out.println("Erreur: " + e.getMessage());
		}
	}

	private void appelRMIDistant(byte[] bytes) {
		try {
			long start = System.nanoTime();
			int result = -1;
			if ( testLength == 0 ) {
				result = distantServerStub.execute(4, 7);
			}
			else {
				result = distantServerStub.testArrayLengthImpact(bytes);
			}
			long end = System.nanoTime();

			System.out.println("Temps écoulé appel RMI distant: "
					+ (end - start) + " ns");
			System.out.println("Résultat appel RMI distant: " + result);
		} catch (RemoteException e) {
			System.out.println("Erreur: " + e.getMessage());
		}
	}
}
