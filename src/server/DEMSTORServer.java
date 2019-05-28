package server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class DEMSTORServer{

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub

		int localRMIPortNumber = 1002;
		DEMSImpl stub = new DEMSImpl();
		Registry registry = LocateRegistry.createRegistry(localRMIPortNumber);
		registry.bind("TORServer",stub);

		System.out.println("tor server online");
	}

}
