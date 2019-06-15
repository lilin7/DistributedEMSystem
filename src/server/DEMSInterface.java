package server;

import java.rmi.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
/**
 * This is a remote interface.
 */
public interface DEMSInterface extends Remote {
	// port number need to start from 1025
	   public final int PORT_MTL = 1028;
	   public final int PORT_OTW = 1033;
	   public final int PORT_TOR = 1029;

	   public final int UDP_PORT_MTL = 5555;
	   public final int UDP_PORT_OTW = 6666;
	   public final int UDP_PORT_TOR = 7777;
	
	   public ArrayList<String> addEvent(String MID,String eventID, String eventType, int bookingCapacity) throws java.rmi.RemoteException;

	   public ArrayList<String> removeEvent(String MID, String eventID, String eventType) throws java.rmi.RemoteException;

	   public ArrayList<String> listEventAvailability(String MID, String eventType) throws java.rmi.RemoteException;

	   public ArrayList<String> bookEvent (String customerID,String eventID, String eventType) throws java.rmi.RemoteException;

	   public ArrayList<String> getBookingSchedule(String customerID) throws java.rmi.RemoteException;

	   public String cancelEvent(String customerID, String eventID, String eventType) throws java.rmi.RemoteException;
	   
	   public ArrayList<String> swapEvent(String customerID, String newEventID, String newEventType, String oldEventID, String oldEventType) throws java.rmi.RemoteException;

	   public ConcurrentHashMap<String, ArrayList<Integer>> listEventAvailabilityForUDP(String eventType) throws Exception;

	   public ArrayList<String> bookEventForUDP(String customerID,String eventID,String eventType) throws Exception;

	   public ArrayList<String> getBookingScheduleForUDP(String customerID) throws Exception;

	   public String cancelEventForUDP(String customerID, String eventID, String eventType) throws Exception;

}
