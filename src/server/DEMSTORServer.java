package server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class DEMSTORServer{
	public static void main(String[] args) throws Exception {
		int localRMIPortNumber = DEMSInterface.PORT_TOR;
		
		DEMSImpl stub = new DEMSImpl();
		Registry registry = LocateRegistry.createRegistry(localRMIPortNumber);
		registry.bind("TORServer",stub);

		System.out.println("Toronto server online");
	}

}
