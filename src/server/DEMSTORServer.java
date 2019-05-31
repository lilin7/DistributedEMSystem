package server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class DEMSTORServer{
	public static void main(String[] args) throws Exception {
		int localRMIPortNumber = DEMSInterface.PORT_TOR;

		int localUDPPortNumber = DEMSInterface.UDP_PORT_TOR;

		int firstRemoteUDPPortNumber = DEMSInterface.UDP_PORT_MTL;
		int secondRemoteUDPPortNumber = DEMSInterface.UDP_PORT_OTW;

		DEMSImpl stub = new DEMSImpl(firstRemoteUDPPortNumber,secondRemoteUDPPortNumber);
		Registry registry = LocateRegistry.createRegistry(localRMIPortNumber);
		registry.bind("TORServer",stub);

		System.out.println("Toronto server online");

		DEMSThread demsThread = new DEMSThread(stub,localUDPPortNumber);
		demsThread.start();
	}

}
