package server;

import java.rmi.*;
import java.util.ArrayList;
/**
 * This is a remote interface.
 */
public interface DEMSInterface extends Remote {
	// port number need to start from 1025
	   public final int PORT_MTL = 1025;
	   public final int PORT_OTW = 1026;
	   public final int PORT_TOR = 1027;

	   public final int UDP_PORT_MTL = 5050;
	   public final int UDP_PORT_OTW = 6060;
	   public final int UDP_PORT_TOR = 7070;
	
	   public ArrayList<String> addEvent(String MID,String eventID, String eventType, int bookingCapacity) throws java.rmi.RemoteException;

	   public ArrayList<String> removeEvent(String MID, String eventID, String eventType) throws java.rmi.RemoteException;

	   public ArrayList<String> listEventAvailability(String MID, String eventType) throws java.rmi.RemoteException;

	   public ArrayList<String> bookEvent (String customerID,String eventID, String eventType) throws java.rmi.RemoteException;

	   public boolean getBookingSchedule(String customerID) throws java.rmi.RemoteException;

	   public boolean cancelEvent(String customerID,String eventID) throws java.rmi.RemoteException;

	   //TODO:change return value and parameters if necessary

		public String listEventAvailabilityForUDP()throws Exception;

		public String bookEventForUDP(String customerID,String eventID,String eventType) throws Exception;

		public String getBookingScheduleForUDP(String customerID) throws Exception;

		public String cancelEventForUDP(String customerID,String eventID) throws Exception;

}
