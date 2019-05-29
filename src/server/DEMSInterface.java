package server;

import java.rmi.*;
/**
 * This is a remote interface.
 */
public interface DEMSInterface extends Remote {
	
	   public boolean addEvent(String MID,String eventID, String eventType, int bookingCapacity) throws java.rmi.RemoteException;

	   public boolean removeEvent(String MID , String eventID, String eventType) throws java.rmi.RemoteException;

	   public boolean listEventAvailability(String EventType) throws java.rmi.RemoteException;

	   public boolean bookEvent (String customerID,String eventID, String eventType)throws java.rmi.RemoteException;

	   public boolean getBookingSchedule(String customerID) throws java.rmi.RemoteException;

	   public boolean cancelEvent(String customerID,String eventID) throws java.rmi.RemoteException;
}
