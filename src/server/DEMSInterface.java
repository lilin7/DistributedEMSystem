package server;

import java.rmi.*;
/**
 * This is a remote interface.
 */
public interface DEMSInterface extends Remote {
	// port number need to start from 1025
	   public final int PORT_MTL = 1025;
	   public final int PORT_OTW = 1026;
	   public final int PORT_TOR = 1027;
	
	   public boolean addEvent(String MID,String eventID, String eventType, int bookingCapacity) throws java.rmi.RemoteException;

	   public boolean removeEvent(String MID, String eventID, String eventType) throws java.rmi.RemoteException;

	   public boolean listEventAvailability(String MID, String eventType) throws java.rmi.RemoteException;

	   public boolean bookEvent (String customerID,String eventID, String eventType) throws java.rmi.RemoteException;

	   public boolean getBookingSchedule(String customerID) throws java.rmi.RemoteException;

	   public boolean cancelEvent(String customerID,String eventID) throws java.rmi.RemoteException;
}
