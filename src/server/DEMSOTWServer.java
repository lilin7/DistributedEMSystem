package server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class DEMSOTWServer {
	public static void main(String[] args) throws Exception{
		int localRMIPortNumber = DEMSInterface.PORT_OTW;

		int localUDPPortNumber = DEMSInterface.UDP_PORT_OTW;

		int firstRemoteUDPPortNumber = DEMSInterface.UDP_PORT_MTL;
		int secondRemoteUDPPortNumber = DEMSInterface.UDP_PORT_TOR;

		DEMSImpl stub = new DEMSImpl(firstRemoteUDPPortNumber,secondRemoteUDPPortNumber);
		Registry registry = LocateRegistry.createRegistry(localRMIPortNumber);
		registry.bind("OTWServer",stub);

		System.out.println("Ottawa server online");

		DEMSThread demsThread = new DEMSThread(stub,localUDPPortNumber);
		demsThread.start();
	}

}
