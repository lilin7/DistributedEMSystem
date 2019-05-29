package server;

import java.rmi.*;
import java.rmi.server.*;

/**
 * This class implements the remote interface server.DEMSInterface.
 */

public class DEMSImpl extends UnicastRemoteObject implements DEMSInterface {
	//hashmap
	//hashmap(id, number)

	public DEMSImpl() throws RemoteException {
		super( );
	}
	
	public boolean addEvent(String MID,String eventID, String eventType, int bookingCapacity) throws RemoteException {
		
		return true; //return true if action success
	}

	public boolean removeEvent(String MID , String eventID, String eventType) throws RemoteException{

		return true;//return true if action success
	}
	public boolean listEventAvailability(String EventType) throws RemoteException{


		return true;//return true if action success
	}
	public boolean bookEvent(String customerID,String eventID, String eventType) throws RemoteException{


		return true;//return true if action success
	}

	public boolean getBookingSchedule(String customerID)throws RemoteException{


		return true;//return true if action success
	}
	public boolean cancelEvent(String customerID,String eventID)throws RemoteException{


		return true;//return true if action success
	}

}
