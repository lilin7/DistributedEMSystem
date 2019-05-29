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
	
	public void addEvent(String MID,String eventID, String eventType, int bookingCapacity) throws RemoteException {

		
	}

	public void removeEvent(String MID , String eventID, String eventType) throws RemoteException{


	}
	public void listEventAvailability(String EventType) throws RemoteException{



	}
	public void bookEvent (String customerID,String eventID, String eventType) throws RemoteException{



	}

	public void getBookingSchedule(String customerID)throws RemoteException{



	}
	public void cancelEvent(String customerID,String eventID)throws RemoteException{



	}

}
