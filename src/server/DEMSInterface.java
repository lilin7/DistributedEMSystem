package server;

import java.rmi.*;
/**
 * This is a remote interface.
 */
public interface DEMSInterface extends Remote {
	
	   public void addEvent(String MID,String eventID, String eventType, int bookingCapacity) throws java.rmi.RemoteException;

	   public void removeEvent(String MID , String eventID, String eventType) throws java.rmi.RemoteException;

	   public void listEventAvailability(String EventType) throws java.rmi.RemoteException;

	   public void bookEvent (String customerID,String eventID, String eventType)throws java.rmi.RemoteException;

	   public void getBookingSchedule(String customerID) throws java.rmi.RemoteException;

	   public void cancelEvent(String customerID,String eventID) throws java.rmi.RemoteException;
}
