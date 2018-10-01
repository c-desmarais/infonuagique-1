package ca.polymtl.inf8480.tp1.client;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import ca.polymtl.inf8480.tp1.shared.ServerInterface;

public class Client {
	
	private static int testLength = 0;
	
	public static void main(String[] args) {
		String distantHostname = null;

		if (args.length > 0) {
			distantHostname = args[0];
			testLength = 0;
		}
		if (args.length > 1) {
			testLength = Integer.parseInt(args[1]);
		}

		Client client = new Client(distantHostname);
		client.run();
	}

	FakeServer localServer = null; // Pour tester la latence d'un appel de
									// fonction normal.
	private ServerInterface localServerStub = null;
	private ServerInterface distantServerStub = null;

	public Client(String distantServerHostname) {
		super();

		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}

		localServer = new FakeServer();
		localServerStub = loadServerStub("127.0.0.1");

		if (distantServerHostname != null) {
			distantServerStub = loadServerStub(distantServerHostname);
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
