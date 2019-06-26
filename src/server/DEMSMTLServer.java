package server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import DEMS_CORBA.DEMSInterfaceCorba;
import DEMS_CORBA.DEMSInterfaceCorbaHelper;



public class DEMSMTLServer {
	public static void main(String[] args) throws Exception{
		int localRMIPortNumber = DEMSInterface.PORT_MTL;

		int localUDPPortNumber = DEMSInterface.UDP_PORT_MTL;

		int firstRemoteUDPPortNumber = DEMSInterface.UDP_PORT_OTW;
		int secondRemoteUDPPortNumber = DEMSInterface.UDP_PORT_TOR;
		
		int MTLRemoteUDPPortNumber = DEMSInterface.UDP_PORT_MTL;
		int TORRemoteUDPPortNumber = DEMSInterface.UDP_PORT_TOR;
		int OTWRemoteUDPPortNumber = DEMSInterface.UDP_PORT_OTW;

		ORB orb = ORB.init(args, null);

		// get reference to rootpoa &amp; activate
		POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
		rootpoa.the_POAManager().activate();



		DEMSImpl DEMSobj = new DEMSImpl(firstRemoteUDPPortNumber,secondRemoteUDPPortNumber,"MTL");
		DEMSobj.setORB(orb);

		org.omg.CORBA.Object ref = rootpoa.servant_to_reference(DEMSobj);
		// and cast the reference to a CORBA reference
		DEMSInterfaceCorba href = DEMSInterfaceCorbaHelper.narrow(ref);
		// get the root naming context
		// NameService invokes the transient name service
		org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
		// Use NamingContextExt, which is part of the
		// Interoperable Naming Service (INS) specification.
		NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

		// bind the Object Reference in Naming
		NameComponent path[] = ncRef.to_name("MTLServer");
		ncRef.rebind(path, href);

		//Registry registry = LocateRegistry.createRegistry(localRMIPortNumber);
		//registry.bind("MTLServer",stub);

		System.out.println("Montreal server online");


		DEMSThread demsThread = new DEMSThread(DEMSobj,localUDPPortNumber);
		demsThread.start();
		for (;;) {
			orb.run();
		}
	}

}
