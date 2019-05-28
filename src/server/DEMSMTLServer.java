package server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class DEMSMTLServer {

	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub

		int localRMIPortNumber = 1000;

		DEMSImpl stub = new DEMSImpl();
		Registry registry = LocateRegistry.createRegistry(localRMIPortNumber);
		registry.bind("MTLServer",stub);

		System.out.println("mtl server online");
	}

}
