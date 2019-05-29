package server;

import java.rmi.*;
import java.rmi.server.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class implements the remote interface server.DEMSInterface.
 */

public class DEMSImpl extends UnicastRemoteObject implements DEMSInterface {
	//hashmap
	//hashmap(id, number)

	private HashMap<String,Integer> conferencesSubHashMap;
	private HashMap<String,Integer> seminarsSubHashMap;
	private HashMap<String,Integer> tradeShowsSubHashMap;

	private HashMap<String, HashMap<String,Integer>> mainHashMap;

	private HashMap<String, ArrayList<String>> CEOtherCity;

	public DEMSImpl() throws RemoteException {
		super( );
		conferencesSubHashMap = new HashMap<String,Integer>();
		seminarsSubHashMap = new HashMap<String,Integer>();
		tradeShowsSubHashMap = new HashMap<String,Integer>();

		mainHashMap = new HashMap<String,HashMap<String,Integer>>();

		mainHashMap.put("Conferences",conferencesSubHashMap);
		mainHashMap.put("Seminars",seminarsSubHashMap);
		mainHashMap.put("TradeShows",tradeShowsSubHashMap);

		CEOtherCity = new HashMap<String,ArrayList<String>>();

	}
	
	public boolean addEvent(String MID,String eventID, String eventType, int bookingCapacity) throws RemoteException {
		
		return true; //return true if action success
	}

	public boolean removeEvent(String MID , String eventID, String eventType) throws RemoteException{

		return true;//return true if action success
	}
	public boolean listEventAvailability(String EventType) throws RemoteException{

		//TODO:print
		return true;//return true if action success
	}
	public boolean bookEvent(String customerID,String eventID, String eventType) throws RemoteException{


		return true;//return true if action success
	}

	public boolean getBookingSchedule(String customerID)throws RemoteException{

		//TODO:print
		return true;//return true if action success
	}
	public boolean cancelEvent(String customerID,String eventID)throws RemoteException{


		return true;//return true if action success
	}

}
