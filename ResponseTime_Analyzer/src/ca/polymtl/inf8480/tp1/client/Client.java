package ca.polymtl.inf8480.tp1.client;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import ca.polymtl.inf8480.tp1.shared.ServerInterface;

public class Client {

	private final static String CREDENTIALS_FILE_NAME = "localAuthFile.txt";
	private final static String FILES_DIRECTORY_NAME = "./FilesDirectory/";
	private final static String DISTANT_HOSTNAME = "132.207.12.114";

	private static ServerInterface distantServerStub = null;

	public static void main(String[] args) throws RemoteException {
		Client client = new Client(DISTANT_HOSTNAME);
		if (args.length == 3) {
			switch (args[0]) {
			case "new":
				client.newUser(args[1], args[2]);
				break;
			default:
				client.printArgErrorMsg();
				break;
			}
		} else if (args.length == 2) {
			switch (args[0]) {
			case "create":
				client.create(args[1]);
				break;
			case "lock":
				client.lock(args[1]);
				break;
			case "get":
				client.get(args[1]);
				break;
			case "push":
				client.push(args[1]);
				break;
			default:
				client.printArgErrorMsg();
				break;
			}
		} else if (args.length == 1) {
			switch (args[0]) {
			case "list":
				client.printlist();
				break;
			case "syncLocalDirectory":
				client.syncLocalDirectory();
				break;
			default:
				client.printArgErrorMsg();
				break;
			}
		} else {
			client.printArgErrorMsg();
		}
	}

	private void printArgErrorMsg() {
		System.out.println("Veuillez specifier des arguments parmi la liste suivante:");
		System.out.println("new <login> <password>");
		System.out.println("create <file name>");
		System.out.println("list");
		System.out.println("lock <file name>");
		System.out.println("push <file name>");		
	}

	public Client(String distantServerHostname) {
		super();

		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}

		if (distantServerHostname != null) {
			distantServerStub = loadServerStub(distantServerHostname);
		}
		else
		{
			System.out.println("Invalid distantServerHostname");
		}
	}

	private void newUser(String login, String password) {
		try {
			if (distantServerStub.newUser(login, password)) {
				createLocalAuthFile(login, password);
				System.out.println("Bravo le user " + login + " a ete cree!");
			} else {
				System.out.println("Erreur le user " + login + " existe deja!");
			}
		} catch (RemoteException e) {
			System.out.println("Erreur: " + e.getMessage());
		}
	}

	private Path createLocalAuthFile(String login, String password) {
		try {
			return Files.write(Paths.get(CREDENTIALS_FILE_NAME), Arrays.asList(login, password),
					Charset.forName("UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void create(String fileName) {
		List<String> credentials = getSavedCredentials();

		try {
			if (distantServerStub.create(fileName, credentials)) {
				System.out.println(fileName + " ajoute.");
			} else {
				System.out.println(fileName + " existe deja.");
			}
		} catch (RemoteException e) {
			System.out.println("Erreur: " + e.getMessage());
		}
	}

	private List<String> getSavedCredentials() {
		List<String> credentials = new ArrayList<String>();
		try {
			credentials = Files.readAllLines(Paths.get(CREDENTIALS_FILE_NAME));
		} catch (NoSuchFileException e) {
			System.out.println("Vous devez vous enregistrer d'abord avec la commande new <id> <password>.");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return credentials;
	}

	private void printlist() {
		List<String> credentials = getSavedCredentials();

		try {
			Map<String, String> filesAndLocks = distantServerStub.list(credentials);
			for (Map.Entry<String, String> entry : filesAndLocks.entrySet()) {
				System.out.println("* " + entry.getKey() + "    "
						+ (entry.getValue().equals("") ? "non verouille" : entry.getValue()));
			}
		} catch (RemoteException e) {
			System.out.println("Erreur: " + e.getMessage());
		}
	}

	private void syncLocalDirectory() {
		List<String> credentials = getSavedCredentials();

		File directory = new File(FILES_DIRECTORY_NAME);
		if (!directory.exists()) {
			directory.mkdir();
		}

		try {
			Map<String, String> filesAndContent = distantServerStub.syncLocalDirectory(credentials);
			for (Map.Entry<String, String> entry : filesAndContent.entrySet()) {
				Files.write(Paths.get(FILES_DIRECTORY_NAME + entry.getKey()),
						entry.getValue().getBytes(StandardCharsets.UTF_8));
			}
		} catch (RemoteException e) {
			System.out.println("Erreur: " + e.getMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void get(String fileName) {
		List<String> credentials = getSavedCredentials();

		try {
			byte[] b = Files.readAllBytes(Paths.get(FILES_DIRECTORY_NAME + fileName));
			byte[] checksum = MessageDigest.getInstance("MD5").digest(b);
			String file = distantServerStub.get(fileName, checksum, credentials);
			if (file != null) {
				Files.write(Paths.get(FILES_DIRECTORY_NAME + fileName), file.getBytes(StandardCharsets.UTF_8));
			}
		} catch (RemoteException e) {
			System.out.println("Erreur: " + e.getMessage());
		} catch (NoSuchFileException e) {
			try {
				String file = distantServerStub.get(fileName, null, credentials);
				if (file != null) {
					Files.write(Paths.get(FILES_DIRECTORY_NAME + fileName), file.getBytes(StandardCharsets.UTF_8));
				}
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void lock(String fileName) {
		List<String> credentials = getSavedCredentials();

		try {
			// check if file exists
			File f = new File(FILES_DIRECTORY_NAME + fileName);
			
			byte[] checksum = null;
			
			// if the file exists, get the appropriate checksum
			if (f.exists()) {
				byte[] b = Files.readAllBytes(Paths.get(FILES_DIRECTORY_NAME + fileName));
				checksum = MessageDigest.getInstance("MD5").digest(b);
			} 
			
			Map<String,String> idAndContent = distantServerStub.lock(fileName, checksum, credentials);
			Map.Entry<String, String> entry = idAndContent.entrySet().iterator().next();
			// The file is used by me
			if (credentials.get(0).equals(entry.getKey())) {
				// Imprimer message a lutilisateur
				System.out.println(fileName + " verouille .");

				if(entry.getValue()!=null)
				{
					// Mettre a jour le fichier
					Files.write(Paths.get(FILES_DIRECTORY_NAME + fileName), entry.getValue().getBytes(StandardCharsets.UTF_8));
					System.out.println(" (Modifs a " + fileName + " )");
				}
				
			} else { // The file is locked by someone else
				System.out.println(fileName + " est deja verouille par " + entry.getKey());
			}
			
			
		} catch (RemoteException e) {
			System.out.println(e.getMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void push(String fileName) {
		List<String> credentials = getSavedCredentials();

		try {
			Map<String, String> filesAndLocks = distantServerStub.list(credentials);
			String userLock = filesAndLocks.get(fileName);
			if(userLock==null)
			{
				System.out.println("operation refusee : vous devez verouiller le fichier d'abord. ");
			}
			else if (userLock.equals(credentials.get(0)))
			{
				// check if file exists
				File f = new File(FILES_DIRECTORY_NAME + fileName);
				if (f.exists() && !f.isDirectory()) {
					// read the content of the file and send it to server
					String content = new String(Files.readAllBytes(Paths.get(FILES_DIRECTORY_NAME + fileName)));
					distantServerStub.push(fileName, content, credentials);
					System.out.println(fileName + " a ete envoye au serveur.");
				} else {
					System.out.println("Le fichier " + fileName + " n'existe pas.");
				}
			}
			else
			{
				System.out.println(fileName +" est deja verouille par " + userLock );
			}
		} catch (RemoteException e) {
			System.out.println("Erreur: " + e.getMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private ServerInterface loadServerStub(String hostname) {
		ServerInterface stub = null;

		try {
			Registry registry = LocateRegistry.getRegistry(hostname);
			stub = (ServerInterface) registry.lookup("server");
		} catch (NotBoundException e) {
			System.out.println("Erreur: Le nom '" + e.getMessage() + "' n'est pas défini dans le registre.");
		} catch (AccessException e) {
			System.out.println("Erreur: " + e.getMessage());
		} catch (RemoteException e) {
			System.out.println("Erreur: " + e.getMessage());
		}

		return stub;
	}

}
