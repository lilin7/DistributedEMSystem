package server;

import java.rmi.*;
import java.rmi.server.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

/**
 * This class implements the remote interface server.DEMSInterface.
 */

public class DEMSImpl extends UnicastRemoteObject implements DEMSInterface {
	//hashmap
	//hashmap(id, number)
	
	// in sub-HashMap, key is event ID, value is a integer ArrayList, 
	// element 0 is the booking capacity, element 1 is number already booked
	private HashMap<String, ArrayList<Integer>> conferencesSubHashMap = new HashMap<String, ArrayList<Integer>>();
	private HashMap<String, ArrayList<Integer>> seminarsSubHashMap = new HashMap<String, ArrayList<Integer>>();
	private HashMap<String, ArrayList<Integer>> tradeShowsSubHashMap = new HashMap<String, ArrayList<Integer>>();

	private HashMap<String, HashMap<String, ArrayList<Integer>>> mainHashMap = new HashMap<String,HashMap<String, ArrayList<Integer>>>();
	
	// A customer can not book more than one event with the same event id and same event type
	// record of customerID, eventType, eventID, to make sure unique
	private HashMap<String, ArrayList<String>> cBookingRecord = new HashMap<String, ArrayList<String>>();
	
	// a customer can book at most 3 events from other cities overall in a month
	private HashMap<String, HashMap<String, Integer>> cBookingOtherCity = new HashMap<String, HashMap<String, Integer>> ();

	public DEMSImpl() throws RemoteException {
		super();
		mainHashMap.put("Conferences",conferencesSubHashMap);
		mainHashMap.put("Seminars",seminarsSubHashMap);
		mainHashMap.put("TradeShows",tradeShowsSubHashMap);
		
		// TODO start a UDP socket (use unused port like 1031-3) in accept, in another thread
        // TODO open UPD socket on others
		
		// you should start a processing thread and define a message class that is processed in order to ensure synchronization		
		
		/* example of the kind of message class
		class message
		{		    
			wait () { aquire lock }
			
			constructor : push message specifics
			
			process message : do the things you need to do, then unlock.
			either create a message processing method or take the data out and do it in a switch case
		}
		*/
	}
	// processing thread should wait that the message queue contains a message
	// consume it
	// unlock the message to notify the Method invoked that this is over
	// for the message, do the appropriate action over the maps of data
	// (add, remove, create a list, respond to the other servers)
	
	public boolean addEvent(String MID,String eventID, String eventType, int bookingCapacity){
		// push a add event message to the processing queue.
		// wait that the message is processed
		
		if (!mainHashMap.get(eventType).containsKey(eventID)) { //If an event does not exist in the database for that event type, then add it.
			HashMap<String, ArrayList<Integer>> tempSubHashMap = mainHashMap.get(eventType);
			ArrayList<Integer> tempCapArrayList = new ArrayList<Integer>();
			tempCapArrayList.add(bookingCapacity);
			tempCapArrayList.add(0);
			tempSubHashMap.put(eventID, tempCapArrayList);			
			mainHashMap.put(eventType, tempSubHashMap);
			System.out.println("New event added.");			
			//TODO write log into this manager
			return true;
		} else { // If an event already exists for same event type, the event manager can¡¯t add it again for the same event type but the new bookingCapacity is updated
			ArrayList<Integer> tempCapArrayList = mainHashMap.get(eventType).get(eventID);
			if (bookingCapacity < tempCapArrayList.get(1)) {
				System.out.println("Event already exist and the new booking capacity you entered is less than space already booked, updating event fails.");
				//TODO write log into this manager
				return false;
			}
			tempCapArrayList.set(0, bookingCapacity); //update the element 0 as the input new capacity
			mainHashMap.get(eventType).put(eventID, tempCapArrayList);	
			System.out.println("Event capacity updated.");
			//TODO write log into this manager
			return true;
		}
	}

	public boolean removeEvent(String MID , String eventID, String eventType){
		// push a add event message to the processing queue.
		// wait that the message is processed
		if (!mainHashMap.get(eventType).containsKey(eventID)) { //If an event does not exist, there is no deletion performed
			System.out.println("No such event exist.");
			return false;			
		} else { //if an event exists
			// TODO if need to inform related customer, write here, otherwise no action needed
			mainHashMap.get(eventType).remove(eventID);
			System.out.println("Event " + eventID + " of " + eventType + " has been removed.");
			//TODO write log into this manager
			return true;
		}
	}
	public boolean listEventAvailability(String MID, String eventType){
		// push a add event message to the processing queue.
		// wait that the message is processed
		
		// TODO UDP to communicate with other cities, maybe need to adjust method.
		//below is for printing only the city of the manager. 		
		String ownCity = MID.substring(0,3).toUpperCase();
		System.out.println("Number of spaces available for each event:");
		System.out.println(ownCity + ":");
		
		HashMap<String, ArrayList<Integer>> tempSubHashMap = mainHashMap.get(eventType);		
		Set<String> keySet = tempSubHashMap.keySet();
		
		System.out.printf("%-15s %-18s %-15s %-15s", "Event ID", "Total Capacity", "Booked Space", "Available Space");
		System.out.println();
		
		for (String s : keySet) {
			String eID = s;
			int totalCap = tempSubHashMap.get(s).get(0);
			int bookedCap = tempSubHashMap.get(s).get(1);
			int availableCap = totalCap-bookedCap;
			System.out.printf("%-15s %-18s %-15s %-15s", eID, totalCap, bookedCap, availableCap);
			System.out.println();		
		}
		return true;//return true if action success
	}
	
	public boolean bookEvent(String customerID, String eventID, String eventType){
		// push a add event message to the processing queue.
		// wait that the message is processed
		String eventTypeAndID = eventType.substring(0,1) + "" + eventID;		
		
		if (customerID.substring(0,3).toUpperCase().equals(eventID.substring(0,3).toUpperCase())) {// if this customer is booking for his/her own city
			if (!mainHashMap.get(eventType).containsKey(eventID)) { // if the event doesn't exist
				System.out.println("The event you attampt to book doesn't exist.");
				return false;
			} else { // if the event exists
				if (! (mainHashMap.get(eventType).get(eventID).get(0) 
						> mainHashMap.get(eventType).get(eventID).get(1))) { // if the capacity left is not enough
					System.out.println("This event is fully booked.");
					return false;
				} else { // if there is still space to book	
					if (!cBookingRecord.containsKey(customerID)) { //if this customer never booked before and doesn't exist in database
						// add to total booking record
						ArrayList<String> tempEventTypeAndIDAL =  new ArrayList<String> ();
						tempEventTypeAndIDAL.add(eventTypeAndID);
						cBookingRecord.put(customerID, tempEventTypeAndIDAL);
						
						//update space available of this event
						int usedSpace = mainHashMap.get(eventType).get(eventID).get(1);
						mainHashMap.get(eventType).get(eventID).set(1, usedSpace+1);
						
						System.out.println("You have successfully booked:  \n"
								+ "Event type: " + eventType + "; Event ID: " + eventID + ".");					
						return true;
					} else { //if this customer booked before and exists
						if (cBookingRecord.get(customerID).contains(eventTypeAndID)) { // if the event type and ID is not unique
							System.out.println("A customer can not book more than one event with the same event id and same event type.");
							return false;
						} else { // if the input event type and ID doesn't exist for this customer
							// update total booking record (by adding this event)
							cBookingRecord.get(customerID).add(eventTypeAndID);		
							
							//update space available of this event
							int usedSpace = mainHashMap.get(eventType).get(eventID).get(1);
							mainHashMap.get(eventType).get(eventID).set(1, usedSpace+1);
							
							System.out.println("You have successfully booked:  \n"
									+ "Event type: " + eventType + "; Event ID: " + eventID + ".");					
							return true;
						}
					}
				}
			}
		} else { // this is to write if this customer wants to book in other cities, need UDP
			if (!mainHashMap.get(eventType).containsKey(eventID)) { // if the event doesn't exist
				System.out.println("The event you attampt to book doesn't exist.");
				return false;
			} else { // if the event exists
				if (! (mainHashMap.get(eventType).get(eventID).get(0) > mainHashMap.get(eventType).get(eventID).get(1))) { // if the capacity left is not enough
					System.out.println("This event is fully booked.");
					return false;
				} else { // if there is still space to book	
					String monthYear = eventID.substring(6,10);
					if (!cBookingRecord.containsKey(customerID)) { // if this customer never booked before and doesn't exist in database	
						// add to total booking record
						ArrayList<String> tempEventTypeAndIDAL =  new ArrayList<String> ();
						tempEventTypeAndIDAL.add(eventTypeAndID);
						cBookingRecord.put(customerID, tempEventTypeAndIDAL);
						
						// add to booking record other cities
						HashMap<String, Integer> tempDateNumber =  new HashMap<String, Integer> ();
						tempDateNumber.put(monthYear, 1);
						cBookingOtherCity.put(customerID, tempDateNumber);
						
						//update space available of this event
						int usedSpace = mainHashMap.get(eventType).get(eventID).get(1);
						mainHashMap.get(eventType).get(eventID).set(1, usedSpace+1);
						
						System.out.println("You have successfully booked:  \n"
								+ "Event type: " + eventType + "; Event ID: " + eventID + ".");					
						return true;
					} else { // if this customer booked before and exists
						if (cBookingRecord.get(customerID).contains(eventTypeAndID)) { // if the event type and ID is not unique
							System.out.println("A customer can not book more than one event with the same event id and same event type.");
							return false;
						} else { // if the event type and ID is unique
							if (! cBookingOtherCity.containsKey(customerID)) { // if customer only booked in own city, never in other cities
								// update total booking record (by adding this event)
								cBookingRecord.get(customerID).add(eventTypeAndID);
								
								// add to booking record other cities
								HashMap<String, Integer> tempDateNumber =  new HashMap<String, Integer> ();
								tempDateNumber.put(monthYear, 1);
								cBookingOtherCity.put(customerID, tempDateNumber);
								
								//update space available of this event
								int usedSpace = mainHashMap.get(eventType).get(eventID).get(1);
								mainHashMap.get(eventType).get(eventID).set(1, usedSpace+1);
								
								System.out.println("You have successfully booked:  \n"
										+ "Event type: " + eventType + "; Event ID: " + eventID + ".");					
								return true;			
							} else { // customer already booked in other cities in the past
								int currentBookingOtherCities = cBookingOtherCity.get(customerID).get(monthYear);
								if (currentBookingOtherCities <3) { // if less than 3 times in the month of the input event in other cities
									// update total booking record (by adding this event)
									cBookingRecord.get(customerID).add(eventTypeAndID);
									
									// update booking record in other cities 
									cBookingOtherCity.get(customerID).put(monthYear, currentBookingOtherCities+1);
									
									//update space available of this event
									int usedSpace = mainHashMap.get(eventType).get(eventID).get(1);
									mainHashMap.get(eventType).get(eventID).set(1, usedSpace+1);
									
									System.out.println("You have successfully booked:  \n"
											+ "Event type: " + eventType + "; Event ID: " + eventID + ".");
									return true;
									
								} else { // if equals to 3 times or more in the month of the input event in other cities
									System.out.println("You can only book at most 3 events from other cities overall in a month.");
									return false;
								}
							}
						}
					}
				}
			}	
		}
	}

	public boolean getBookingSchedule(String customerID){
		// push a add event message to the processing queue.
		// wait that the message is processed
		
		//get the booking record in customer's own city
		ArrayList<String> tempEventTypeAndId = cBookingRecord.get(customerID);
		
		for (String s : tempEventTypeAndId) {			
		}
		//TODO:print
		return true;//return true if action success
	}
	public boolean cancelEvent(String customerID,String eventID){
		// push a add event message to the processing queue.
		// wait that the message is processed

		return true;//return true if action success
	}

}
