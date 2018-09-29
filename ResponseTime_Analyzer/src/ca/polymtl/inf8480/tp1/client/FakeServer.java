package ca.polymtl.inf8480.tp1.client;

import java.rmi.RemoteException;

public class FakeServer {
	int execute(int a, int b) {
		return a + b;
	}
	int testArrayLengthImpact(byte[] value) {
		return 1;
	}
}
