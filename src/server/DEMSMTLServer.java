package server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class DEMSMTLServer {
	public static void main(String[] args) throws Exception{
		int localRMIPortNumber = DEMSInterface.PORT_MTL;

		int localUDPPortNumber = DEMSInterface.UDP_PORT_MTL;

		int firstRemoteUDPPortNumber = DEMSInterface.UDP_PORT_OTW;
		int secondRemoteUDPPortNumber = DEMSInterface.UDP_PORT_TOR;

		DEMSImpl stub = new DEMSImpl(firstRemoteUDPPortNumber,secondRemoteUDPPortNumber);
		Registry registry = LocateRegistry.createRegistry(localRMIPortNumber);
		registry.bind("MTLServer",stub);

		System.out.println("Montreal server online");

		DEMSThread demsThread = new DEMSThread(stub,localUDPPortNumber);
		demsThread.start();
	}

}
