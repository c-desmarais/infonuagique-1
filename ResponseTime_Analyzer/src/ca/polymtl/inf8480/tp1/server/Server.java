package ca.polymtl.inf8480.tp1.server;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.polymtl.inf8480.tp1.shared.ServerInterface;

public class Server implements ServerInterface {

	Map<String, String> users = new HashMap<String, String>();
	Map<String, String> filesAndLocks = new HashMap<String, String>();
	
	private final static String FILES_DIRECTORY_NAME = "./FilesDirectory/";
	private final static String METADATA_DIRECTORY_NAME = "./MetaDataDirectory/";
	private final static String CREDENTIALS_METADATA_FILE = "credentials.txt";
	private final static String LOCK_METADATA_FILE = "lock.txt";
	
	public static void main(String[] args) {
		Server server = new Server();
		server.run();
	}

	/*
	 * Constructeur du serveur. 
	 * Se charge de l'initialisation des differentes donnees ainsi que de la creation du repertoire
	 * pour les fichiers stockes sur le serveur.
	 */
	public Server() {
		super();
		
		// initialiser les donnees concernant les noms dutilisateurs et mot de passe (recuperation des donnees).
		initializeMeta(CREDENTIALS_METADATA_FILE);
		
		// initialiser les donnees concernant les locks sur chaque fichier.
		File f = new File(METADATA_DIRECTORY_NAME + LOCK_METADATA_FILE);
		
		// si certaines donnees peuvent etre recuperees, les recuperer.
		if (f.exists() && f.length()>0) {
			initializeMeta(LOCK_METADATA_FILE);
		} else {  // initialiser la structure de donnees pour les locks comme si personne navait de lock sur aucun fichier.
			initializeFilesAndLocks();
		}
		
		// Creation du repertoire pour les fichiers stockes sur le serveur.
		File directory = new File(FILES_DIRECTORY_NAME);
		if(!directory.exists())
		{
			directory.mkdir();
		}
	}
	
	/*
	 * Initialisation de la structure de donnees contenant les fichiers et leurs locks respectifs.
	 * Cette initialisation sassure qu'aucun lock nest mis sur les fichiers (empty string). 
	 */
	private void initializeFilesAndLocks()
	{
		File dir = new  File(FILES_DIRECTORY_NAME);
		File[] files = dir.listFiles();
		if(files!=null)
		{
			for(File aFile : files) {
				filesAndLocks.put(aFile.getName(), "");
			}
		}
	}
	
	/*
	 * Cette fonction recupere les fichiers contenant les meta donnees sils existent et contiennent des meta donnees.
	 * Par la suite, stocke les informations dans les structures de donnees appropriees.
	 */
	private void initializeMeta(String filename) 
	{
		try {
			File f = new File(METADATA_DIRECTORY_NAME + filename);
			if (f.exists() && !f.isDirectory() && f.length() > 0) {
				String fileContent = new String(Files.readAllBytes(Paths.get(METADATA_DIRECTORY_NAME + filename)));
				Map<String, String> map = parseMetaMap(fileContent);
				if (filename.equals(CREDENTIALS_METADATA_FILE)) {
					users = map;
				} else if (filename.equals(LOCK_METADATA_FILE)) {
					filesAndLocks = map;
				}
			} 
		} catch (IOException e) {
			e.printStackTrace();
		}
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
	 * Méthode accessible par RMI. Return false if the user already exists.
	 */
	@Override
	public boolean newUser(String login, String password) throws RemoteException {
		if(users.get(login) == null)
		{
			users.put(login, password);
			saveMetaToFile(users, CREDENTIALS_METADATA_FILE);
			return true;
		}
		return false;
	}

	public boolean verify(List<String> credentials) throws RemoteException {
		return credentials.get(1).equals(users.get(credentials.get(0)));
	}

	@Override
	public boolean create(String fileName, List<String> credentials) throws RemoteException {
		if(!verify(credentials))
		{
			throw new RemoteException("Invalid credentials for user " + credentials.get(0));
		}
		
		try
		{
			File file = new File(FILES_DIRECTORY_NAME+fileName);
					
			if(file.createNewFile())
			{
				filesAndLocks.put(fileName, "");
				saveMetaToFile(filesAndLocks, LOCK_METADATA_FILE);
				return true;
			}
			return false;
		}
		catch(IOException e)
		{
			System.out.println("Error Empty file not created : " + fileName);
			e.printStackTrace();
			throw new RemoteException(e.getMessage());
		}
	}

	@Override
	public Map<String, String> list(List<String> credentials) throws RemoteException {
		if(!verify(credentials))
		{
			throw new RemoteException("Invalid credentials for user " + credentials.get(0));
		}
		
		return filesAndLocks;
	}

	@Override
	public Map<String, String> syncLocalDirectory(List<String> credentials) throws RemoteException {
		if(!verify(credentials))
		{
			throw new RemoteException("Invalid credentials for user " + credentials.get(0));
		}
		
		Map<String, String> filesAndContent = new HashMap<String, String>();
		for(Map.Entry<String, String> entry: filesAndLocks.entrySet())
		{
				String content;
				try {
					content = new String(Files.readAllBytes(Paths.get(FILES_DIRECTORY_NAME+entry.getKey())));
					filesAndContent.put(entry.getKey(), content);
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		return filesAndContent;
	}

	@Override
	public String get(String fileName, byte[] checksum, List<String> credentials) throws RemoteException {
		if(!verify(credentials))
		{
			throw new RemoteException("Invalid credentials for user " + credentials.get(0));
		}
		
		try {
			byte[] b = Files.readAllBytes(Paths.get(FILES_DIRECTORY_NAME+fileName));
			byte[] hash = MessageDigest.getInstance("MD5").digest(b);
			
			if(Arrays.equals(hash, checksum))
			{
				return null;			
			}
			else
			{
				return new String(b);
			}
			
			
		} catch (IOException | NoSuchAlgorithmException e) {
			// TODO gerer le cas ou le nom du fichier est inexistant
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Map<String, String> lock(String fileName, byte[] checksum, List<String> credentials) throws RemoteException {
		if(!verify(credentials))
		{
			throw new RemoteException("Invalid credentials for user " + credentials.get(0));
		}
		
		// check if file exists
		File f = new File(FILES_DIRECTORY_NAME + fileName);
		if (f.exists() && !f.isDirectory()) {
			// check if file is already locked by another client
			String currentUser = filesAndLocks.get(fileName);
			Map<String, String> infos = new HashMap<String, String>();
			if (currentUser.equals("")) {
				filesAndLocks.put(fileName, credentials.get(0));
				infos.put(credentials.get(0), get(fileName, checksum, credentials));
			}
			else // dont update file content if the file is locked by other user
			{
				infos.put(currentUser, null);
			}
			
			// sauvegarder un fichier de metadonnees des locks respectifs des fichiers
			saveMetaToFile(filesAndLocks, LOCK_METADATA_FILE);
			return infos;
				
		} else { 
			throw new RemoteException(fileName + " existe pas.");
		}		
	}
	
	@Override
	public void push(String fileName, String content, List<String> credentials) throws RemoteException {
		if(!verify(credentials))
		{
			throw new RemoteException("Invalid credentials for user " + credentials.get(0));
		}
		
		// TODO check if we need to verify more here instead than in the client (lock)
		try {
		
			// check if file exists
			File f = new File(FILES_DIRECTORY_NAME + fileName);
			if (f.exists() && !f.isDirectory()) {
				// check if file is already locked by another client
				String currentUser = filesAndLocks.get(fileName);
				if (currentUser.equals(credentials.get(0))) {
					System.out.println("writing content to file" + content + "content.len" + content.length() );
					Files.write(Paths.get(FILES_DIRECTORY_NAME + fileName), content.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);

					filesAndLocks.put(fileName, "");
				}
			} else { 
				throw new RemoteException(fileName + " existe pas.");
			}
			
			// sauvegarder un fichier de metadonnees des locks respectifs des fichiers
			saveMetaToFile(filesAndLocks, LOCK_METADATA_FILE);
		} 
		catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	/*
	 * Methode permettant de sauvegarder des donnees provenant dune hashmap dans un fichier
	 * contenant les meta donnees (soit les informations des credentials ou celles des fichiers
	 * et de leurs locks respectifs. 
	 */
	private void saveMetaToFile(Map<String, String> meta, String filename) throws RemoteException {
		File directory = new File(METADATA_DIRECTORY_NAME);
		if(!directory.exists())
		{
			directory.mkdir();
		}
		
		String stringifiedMap = stringifyMetaMap(meta);
		
		try {
			Files.write(Paths.get(METADATA_DIRECTORY_NAME + filename), stringifiedMap.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/*
	 * Methode permettant de convertir une Map<String, String> en String afin de pouvoir
	 * eventuellement sauvegarder cette structure de donnees dans des fichiers de metadonnees.
	 */
	private String stringifyMetaMap(Map<String, String> map) {
		String stringifiedMap = "";
		for (Map.Entry<String, String> entry : map.entrySet()) {
			stringifiedMap += entry.getKey() + "," + entry.getValue() + "-";
		}
		return stringifiedMap;
	}
	
	/*
	 * Methode permettant de construire une map a partir dune String donnee (parse). 
	 * Utilise dans le cas ou lon souhaite lire des fichiers de donnees afin de recuperer les donnees
	 * appropriees (credentials ou locks de fichiers).
	 */
	private Map<String, String> parseMetaMap(String metaMap) {
		Map<String, String> meta = new HashMap<String, String>();
		String[] metaStrArray = metaMap.split("-");
		for (String el : metaStrArray) {
			String[] data = el.split(",");
			if (data.length == 2) {
				meta.put(data[0], data[1]);	
			}
		}
		return meta;
	}
	
}
