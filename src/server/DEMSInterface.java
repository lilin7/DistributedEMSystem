package server;

import java.rmi.*;
/**
 * This is a remote interface.
 */
public interface DEMSInterface extends Remote {
	
	   public void addEvent(String eventID, String eventType, int bookingCapacity) 
	      throws java.rmi.RemoteException;


	   
}
