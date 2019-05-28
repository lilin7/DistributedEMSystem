package server;

import java.rmi.*;
import java.rmi.server.*;

/**
 * This class implements the remote interface server.DEMSInterface.
 */

public class DEMSImpl extends UnicastRemoteObject implements DEMSInterface {
	public DEMSImpl() throws RemoteException {
		super( );
	}
	
	public void addEvent(String eventID, String eventType, int bookingCapacity) throws RemoteException {
		
		
	}
}
