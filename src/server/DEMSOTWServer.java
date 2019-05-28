package server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class DEMSOTWServer {

	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub

		int localRMIPortNumber = 1001;
		DEMSImpl stub = new DEMSImpl();
		Registry registry = LocateRegistry.createRegistry(localRMIPortNumber);
		registry.bind("OTWServer",stub);

		System.out.println("otw server online");
	}

}
