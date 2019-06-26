package server;

import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.rmi.*;
import java.rmi.server.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.*;

import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import DEMS_CORBA.DEMSInterfaceCorbaPOA;


/**
 * This class implements the remote interface server.DEMSInterface.
 */

public class DEMSImpl extends DEMSInterfaceCorbaPOA {

	// in sub-HashMap, key is event ID, value is a integer ArrayList, 
	// element 0 is the booking capacity, element 1 is number already booked
	private ConcurrentHashMap<String, ArrayList<Integer>> conferencesSubHashMap = new ConcurrentHashMap<String, ArrayList<Integer>>();
	private ConcurrentHashMap<String, ArrayList<Integer>> seminarsSubHashMap = new ConcurrentHashMap<String, ArrayList<Integer>>();
	private ConcurrentHashMap<String, ArrayList<Integer>> tradeShowsSubHashMap = new ConcurrentHashMap<String, ArrayList<Integer>>();

	//<eventType, <eventID, <eventCapacity, spacedUsed>>>
	private ConcurrentHashMap<String, ConcurrentHashMap<String, ArrayList<Integer>>> mainHashMap = new ConcurrentHashMap<String, ConcurrentHashMap<String, ArrayList<Integer>>>();
	
	// A customer can not book more than one event with the same event id and same event type
	// record of customerID, eventType+eventID (e.g. CTORA100519, first letter is event type), to make sure unique
	// MTLC2222<CTORA100519, SMTLA100519, ...>
	private ConcurrentHashMap<String, ArrayList<String>> cBookingRecord = new ConcurrentHashMap<String, ArrayList<String>>();	
	public ConcurrentHashMap<String, ArrayList<String>> getcBookingRecord() {
		return cBookingRecord;
	}

	// a customer can book at most 3 events from other cities overall in a month. <CustomerID, <monthYear, numberOfBooking>
	private ConcurrentHashMap<String, HashMap<String, Integer>> cBookingOtherCity = new ConcurrentHashMap<String, HashMap<String, Integer>> ();

	//remote udp port for request other servers
	private int firstRemoteUDPPort;
	private int secondRemoteUDPPort;
	
	private int MTLRemoteUDPPortNumber;
	private int TORRemoteUDPPortNumber;
	private int OTWRemoteUDPPortNumber;

	private static FileHandler fh;
	private static Logger serverLogger;

	private ORB orb;

	public void setORB(ORB orb_val){orb = orb_val;}

	private Any returnAny(Object obj){
		Any any = orb.create_any();
		any.insert_Value((Serializable)obj);
		return any;
	}

	public DEMSImpl(int firstRemoteUDPPort,int secondRemoteUDPPort,String serverName) throws Exception {
		super();
		mainHashMap.put("Conferences",conferencesSubHashMap);
		mainHashMap.put("Seminars",seminarsSubHashMap);
		mainHashMap.put("TradeShows",tradeShowsSubHashMap);

		this.firstRemoteUDPPort = firstRemoteUDPPort;
		this.secondRemoteUDPPort = secondRemoteUDPPort;

		this.MTLRemoteUDPPortNumber = DEMSInterface.UDP_PORT_MTL;
		this.OTWRemoteUDPPortNumber = DEMSInterface.UDP_PORT_OTW;
		this.TORRemoteUDPPortNumber = DEMSInterface.UDP_PORT_TOR;

		serverLogger = Logger.getLogger(serverName);

		serverLogger.setUseParentHandlers(true);
		fh = new FileHandler("src/server/server_log/"+serverName+".log",true);
		fh.setFormatter(new SimpleFormatter());
		serverLogger.addHandler(fh);

		serverLogger.info("log start"+"\n");		
	}
	
	public synchronized Any addEvent(String MID,String eventID, String eventType, int bookingCapacity){
		ArrayList<String> returnMessage = new ArrayList<String>();		

		serverLogger.info("request: add event"+"\n");
		serverLogger.info("manager id: "+MID+" event id: "+eventID+" event type: "+eventType+" capacity: "+ bookingCapacity+"\n");

		if (!mainHashMap.get(eventType).containsKey(eventID)) { //If an event does not exist in the database for that event type, then add it.
			ConcurrentHashMap<String, ArrayList<Integer>> tempSubHashMap = mainHashMap.get(eventType);
			ArrayList<Integer> tempCapArrayList = new ArrayList<Integer>();
			tempCapArrayList.add(bookingCapacity);
			tempCapArrayList.add(0);
			tempSubHashMap.put(eventID, tempCapArrayList);			
			mainHashMap.put(eventType, tempSubHashMap);				
			returnMessage.add("Added");
			returnMessage.add("New event added.");
			serverLogger.info("New event added."+"\n");
			return returnAny(returnMessage);
		} else { // If an event already exists for same event type, the event manager can't add it again for the same event type but the new bookingCapacity is updated
			ArrayList<Integer> tempCapArrayList = mainHashMap.get(eventType).get(eventID);
			if (bookingCapacity < tempCapArrayList.get(1)) {
				returnMessage.add("Fail");
				returnMessage.add("Event already exist and the new booking capacity you entered is less than space already booked, updating event fails.");
				serverLogger.info("Fail. Event already exist and the new booking capacity you entered is less than space already booked, updating event fails."+"\n");
				return returnAny(returnMessage) ;
			}
			tempCapArrayList.set(0, bookingCapacity); //update the element 0 as the input new capacity
			mainHashMap.get(eventType).put(eventID, tempCapArrayList);	
			returnMessage.add("Updated");
			returnMessage.add("Event exists, no new event added. Event capacity updated.");
			serverLogger.info("Updated. Event exists, no new event added. Event capacity updated."+"\n");
			return returnAny(returnMessage) ;
		}
	}

	public synchronized Any removeEvent(String MID , String eventID, String eventType){
		ArrayList<String> returnMessage = new ArrayList<String>();

		serverLogger.info("request: remove event"+"\n");
		serverLogger.info("manager id: "+MID+" event id: "+eventID+" event type: "+eventType+"\n");

		if (!mainHashMap.get(eventType).containsKey(eventID)) { //If an event does not exist, there is no deletion performed
			returnMessage.add("NoExist");
			returnMessage.add("No such event exist. Nothing is removed.");
			serverLogger.info("No such event exist. Nothing is removed."+"\n");
			return returnAny(returnMessage) ;
		} else { //if an event exists
			// TODO if need to inform related customer, write here, otherwise no action needed
			mainHashMap.get(eventType).remove(eventID);
			
			String eventTypeAbb = eventType.substring(0,1).toUpperCase();
			String eventTypeAndID = eventTypeAbb + "" + eventID.trim();
			
			// also delete the corresponding cBookingRecord		
			Set<String> keySet = cBookingRecord.keySet();			
			for (String CID : keySet) {				
				if (cBookingRecord.get(CID).contains(eventTypeAndID)) {
					cBookingRecord.get(CID).remove(eventTypeAndID);
				}
			}
			
			returnMessage.add("Success");
			returnMessage.add("Event " + eventID + " of " + eventType + " has been removed.");
			serverLogger.info("Success. "+"Event " + eventID + " of " + eventType + " has been removed."+"\n");
			return returnAny(returnMessage) ;
		}
	}
	public synchronized Any listEventAvailability(String MID, String eventType){
		ArrayList<String> returnMessage = new ArrayList<String>(); // only return when combine info in all cities
		
		ArrayList<String> returnMessageOwnCity = new ArrayList<String>();
		ArrayList<String> returnMessageFirstOtherCity = new ArrayList<String>();
		ArrayList<String> returnMessageSecondOtherCity = new ArrayList<String>();

		serverLogger.info("request: list event availability"+"\n");
		serverLogger.info("manager id: "+MID+" event type: "+eventType+"\n");

		returnMessage.add("Number of spaces available for each event:");
		String lineFormated = String.format("%-15s %-18s %-15s %-15s", "Event ID", "Total Capacity", "Booked Space", "Available Space");
		returnMessage.add(lineFormated);
		
		//--------- begin of adding the event in own city---------------		
		String ownCity = MID.substring(0,3).toUpperCase();
		ConcurrentHashMap<String, ArrayList<Integer>> tempSubHashMap = mainHashMap.get(eventType);		
		Set<String> keySet = tempSubHashMap.keySet();
			
		for (String s : keySet) {
			String eID = s;
			int totalCap = tempSubHashMap.get(s).get(0);
			int bookedCap = tempSubHashMap.get(s).get(1);
			int availableCap = totalCap-bookedCap;
			lineFormated = String.format("%-15s %-18s %-15s %-15s", eID, totalCap, bookedCap, availableCap);
			returnMessageOwnCity.add(lineFormated);	
		}
		//--------- end of adding the event in own city---------------
		
		//--------- begin of adding the event in first other city---------------
		returnMessageFirstOtherCity = UDPCommunicationlistEventAvailability(MID, eventType, firstRemoteUDPPort);
		
		returnMessageSecondOtherCity = UDPCommunicationlistEventAvailability(MID, eventType, secondRemoteUDPPort);
		
		
		returnMessage.addAll(returnMessageOwnCity);
		returnMessage.addAll(returnMessageFirstOtherCity);
		returnMessage.addAll(returnMessageSecondOtherCity);
		serverLogger.info("information showed"+"\n");
		return returnAny(returnMessage) ;
	}
	
	public synchronized Any bookEvent(String customerID, String eventID, String eventType){
		ArrayList<String> returnMessage = new ArrayList<String>();

		serverLogger.info("request: book event"+"\n");
		serverLogger.info("customer id: "+customerID+" event id: "+eventID+" event type: "+eventType+"\n");

		String eventTypeAndID = eventType.substring(0,1) + "" + eventID;		
		
		if (customerID.substring(0,3).toUpperCase().equals(eventID.substring(0,3).toUpperCase())) {// if this customer is booking for his/her own city
			if (!mainHashMap.get(eventType).containsKey(eventID)) { // if the event doesn't exist
				returnMessage.add("NoExist");
				returnMessage.add("The event you attampt to book doesn't exist.");
				serverLogger.info("The event you attampt to book doesn't exist."+"\n");
				return returnAny(returnMessage) ;
			} else { // if the event exists
				if (! (mainHashMap.get(eventType).get(eventID).get(0) 
						> mainHashMap.get(eventType).get(eventID).get(1))) { // if the capacity left is not enough
					returnMessage.add("Full");
					returnMessage.add("This event is fully booked.");
					serverLogger.info("This event is fully booked."+"\n");
					return returnAny(returnMessage);
				} else { // if there is still space to book	
					if (!cBookingRecord.containsKey(customerID)) { //if this customer never booked before and doesn't exist in database
						// add to total booking record
						ArrayList<String> tempEventTypeAndIDAL =  new ArrayList<String> ();
						tempEventTypeAndIDAL.add(eventTypeAndID);
						cBookingRecord.put(customerID, tempEventTypeAndIDAL);
						
						//update space available of this event
						int usedSpace = mainHashMap.get(eventType).get(eventID).get(1);
						mainHashMap.get(eventType).get(eventID).set(1, usedSpace+1);
						
						returnMessage.add("Success");
						returnMessage.add("You have successfully booked a space in:  \n"
								+ "Event type: " + eventType + "; Event ID: " + eventID + ".");
						serverLogger.info("Success"+"\n");
						serverLogger.info("You have successfully booked a space in:  \n"
								+ "Event type: " + eventType + "; Event ID: " + eventID + "."+"\n");
						return returnAny(returnMessage) ;
					} else { //if this customer booked before and exists
						if (cBookingRecord.get(customerID).contains(eventTypeAndID)) { // if the event type and ID is not unique
							returnMessage.add("NotUnique");
							returnMessage.add("A customer can not book more than one event with the same event id and same event type.");
							serverLogger.info("A customer can not book more than one event with the same event id and same event type."+"\n");
							return returnAny(returnMessage) ;
						} else { // if the input event type and ID doesn't exist for this customer
							// update total booking record (by adding this event)
							cBookingRecord.get(customerID).add(eventTypeAndID);		
							
							//update space available of this event
							int usedSpace = mainHashMap.get(eventType).get(eventID).get(1);
							mainHashMap.get(eventType).get(eventID).set(1, usedSpace+1);
							
							returnMessage.add("Success");
							returnMessage.add("You have successfully booked a space in:  \n"
									+ "Event type: " + eventType + "; Event ID: " + eventID + ".");
							serverLogger.info("Success"+"\n");
							serverLogger.info("You have successfully booked a space in:  \n"
									+ "Event type: " + eventType + "; Event ID: " + eventID + "."+"\n");
							return returnAny(returnMessage);
						}
					}
				}
			}
		} else { // this is to write if this customer wants to book in other cities, add UDP communication
			
			// ------ begin communicate with the target city:------
			// send message to target city, get reply, put reply to returnMessage		
			String result;
			String monthYear = eventID.substring(6,10); // target booking month and year, e.g. "0519"
			
			// validate if this customer is eligible for booking in other cities 
			if (cBookingOtherCity.containsKey(customerID)) { //if this customer has ever booked in other cities
				if (cBookingOtherCity.get(customerID).containsKey(monthYear)) { // if this customer has booked in the target month
					if (! (cBookingOtherCity.get(customerID).get(monthYear) <3) ) { // if booking time exceeding limitation
						returnMessage.add("Exceed3LimitInOtherCity");
						returnMessage.add("A customer can only book at most 3 events from other cities overall in a month.");
						serverLogger.info("A customer can only book at most 3 events from other cities overall in a month."+"\n");
						return returnAny(returnMessage) ;
					} else { // if booking time less than 3, go UDP communicate with the target city
						result = UDPCommunicationBookEvent(customerID, eventID, eventType);
													
						// add 1 to the number of booking of this customer of this month
						if (result.equals("Success")) {
							int currentBookingOtherCities = cBookingOtherCity.get(customerID).get(monthYear);
							cBookingOtherCity.get(customerID).put(monthYear, currentBookingOtherCities+1);
							serverLogger.info("Success"+"\n");
						}
					}
				} else { //if customer exists in cBookingOtherCity but never booked this month, also can book
					result = UDPCommunicationBookEvent(customerID, eventID, eventType).trim();
				
					// create this month, put 1
					if (result.equals("Success")) {
						HashMap<String, Integer> monthRecord = cBookingOtherCity.get(customerID);
						monthRecord.put(monthYear, 1);
						cBookingOtherCity.put(customerID, monthRecord);
						serverLogger.info("Success"+"\n");
					}
				}
			} else { //if this customer has never booked in other cities, can book
				result = UDPCommunicationBookEvent(customerID, eventID, eventType);
				
				// if booking successes, need to record in the cBookingOtherCity. As this customer doesn't exist, create
				if (result.equals("Success")) {
					HashMap<String, Integer> tempDateNumber =  new HashMap<String, Integer> ();
					tempDateNumber.put(monthYear, 1);
					cBookingOtherCity.put(customerID, tempDateNumber);
					serverLogger.info("Success"+"\n");
				}
			}
			// ------ end communicate with target other city:------	
			returnMessage.add(result);			
			return returnAny(returnMessage) ;
		}
	}

	public synchronized Any getBookingSchedule(String customerID){
		serverLogger.info("request: get booking schedule"+"\n");
		serverLogger.info("customer id: "+customerID+"\n");
		ArrayList<String> returnMessage = new ArrayList<String>(); // only return when combine info in all cities
		
		ArrayList<String> returnMessageOwnCity = new ArrayList<String>();
		ArrayList<String> returnMessageFirstOtherCity = new ArrayList<String>();
		ArrayList<String> returnMessageSecondOtherCity = new ArrayList<String>();
		
		if (cBookingRecord.containsKey(customerID)) {
			returnMessageOwnCity = cBookingRecord.get(customerID);
		}		
		
		// ------ begin communicate with the first other city:------
		// send message to target city 1, get reply, put reply to returnMessageFirstOtherCity
		returnMessageFirstOtherCity = UDPCommunicationGetBookingSchedule(customerID, firstRemoteUDPPort);
		
		// ------ begin communicate with the second other city:------
		// send message to target city 2, get reply, put reply to returnMessageFirstOtherCity
		returnMessageSecondOtherCity = UDPCommunicationGetBookingSchedule(customerID, secondRemoteUDPPort);
			
		// combine info in all 3 cities, and reply to client (return a ArrayList<String>, safe	
		returnMessage.addAll(returnMessageOwnCity);
		returnMessage.addAll(returnMessageFirstOtherCity);
		returnMessage.addAll(returnMessageSecondOtherCity);
		
		System.out.println("Return booking schedules below:");
		for (String s : returnMessage) {
			System.out.print(s + " ");
		}
		serverLogger.info("information showed"+"\n");
		return returnAny(returnMessage) ;//return a ArrayList<String> to client, safe
	}
	
	public synchronized String cancelEvent(String customerID, String eventID, String eventType) {
		customerID = customerID.trim();
		eventID = eventID.trim();
		eventType = eventType.trim();

		serverLogger.info("request: cancel event"+"\n");
		serverLogger.info("customer id: "+customerID+" event id: "+eventID+" event type: "+eventType+"\n");

		String eventTypeAndID = eventType.substring(0,1) + "" + eventID; //create the info like "CTORA100519"
		String monthYear = eventID.substring(6,10);
		String returnMessage = "";
		String cityOfCustomer = customerID.substring(0, 3).trim();
		String cityOfEvent = eventID.substring(0, 3).trim();
	
		// if this is to cancel the event in the own city
		if (cityOfCustomer.equals(cityOfEvent)) { 
			if (! (mainHashMap.get(eventType).containsKey(eventID))) { // if this event id of this type doesn't exist
				returnMessage = "EventNotExist";
				serverLogger.info("EventNotExist"+"\n");
			} else { // if this event id of this type exists
				if ( ! cBookingRecord.containsKey(customerID)) { // if this customer never booked in own city (doesn't exist in cBookingRecord)
					returnMessage = "CustomerNeverBooked";
					serverLogger.info( "CustomerNeverBooked"+"\n");
				} else {								
					if ( ! cBookingRecord.get(customerID).contains(eventTypeAndID)) { // if this customer never booked this event in this type
						returnMessage = "ThisCustomerHasNotBookedThis";
						serverLogger.info("ThisCustomerHasNotBookedThis"+"\n");
					} else { // if everything is ok
						cBookingRecord.get(customerID).remove(eventTypeAndID);
						if (mainHashMap.get(eventType).get(eventID).get(1)>0) { //validate in mainHashMap
							int newUsedSpace = mainHashMap.get(eventType).get(eventID).get(1)-1;
							mainHashMap.get(eventType).get(eventID).set(1, newUsedSpace);
							returnMessage = "Success";
							serverLogger.info("Success"+"\n");
						} else {
							returnMessage = "Capacity Error";
							serverLogger.info("Capacity Error"+"\n");
						}
					}
				}
				
			}
		} else { // if this customer wants to cancel an event in other cities
			//communicate by UDP with the target city
			returnMessage = UDPCommunicationCancelEvent(customerID, eventID, eventType);
			//if successfully cancelled an event in other city, need to update the record of booking in other cities, which is stored in customer's own city
			if (returnMessage.equals("Success")) {
				if (!cBookingOtherCity.containsKey(customerID)) {
					returnMessage = "SuccessButNoSuchCustomerIncBookingOtherCity";
					serverLogger.info("SuccessButNoSuchCustomerIncBookingOtherCity"+"\n");
				} else {
					if ( ! cBookingOtherCity.get(customerID).containsKey(monthYear)) {
						returnMessage = "SuccessButNoSuchMonthIncBookingOtherCity";
						serverLogger.info("SuccessButNoSuchMonthIncBookingOtherCity"+"\n");
					} else {
						if ( ! (cBookingOtherCity.get(customerID).get(monthYear) >0)) { // the number of booking in this month in other cities should be at least 1
							returnMessage = "SuccessButWrongNumberOfBookingIncBookingOtherCity";
							serverLogger.info("SuccessButWrongNumberOfBookingIncBookingOtherCity"+"\n");
						} else { // everything is perfect
							int newBookingNumber = cBookingOtherCity.get(customerID).get(monthYear) -1;
							cBookingOtherCity.get(customerID).put(monthYear, newBookingNumber);
							returnMessage = "Success";
							serverLogger.info("Success"+"\n");
						}
					}
				}
			}			
		}		
		System.out.println(returnMessage);
		return returnMessage;
	}
	
	public synchronized Any swapEvent(String customerID, String newEventID, String newEventType, String oldEventID, String oldEventType) {
		ArrayList<String> returnMessage = new ArrayList<String>();
		
		serverLogger.info("request: swap two events"+"\n");
		serverLogger.info("customer id: "+customerID+" new event id: "+newEventID+" new event type: "+newEventType
				+" old event id: "+ oldEventID+" old event type: "+oldEventType +"\n");
		
		//check if inputs are two identical events, if so, no action, return
		if ( (newEventID.equals(oldEventID) ) && ( newEventType.equals(oldEventType) )){
			returnMessage.add("IdenticalEvents");
			returnMessage.add("IdenticalEvents");
			serverLogger.info("You are trying to swap two identical events, no action is taken."+"\n");
			return returnAny(returnMessage) ;
		}
		
		boolean possibleConflictCBookingOtherCity = true;
		
		String customerCity = customerID.substring(0,3);
		String newEventCity = newEventID.substring(0,3);
		String oldEventCity = oldEventID.substring(0,3);
		
		String newMonthYear = newEventID.substring(6,10);
		String oldMonthYear = oldEventID.substring(6,10);
		
		if (  (customerCity.equals(newEventCity) && customerCity.equals(oldEventCity) ) //case 1: if old event is local, new event is local
				|| (customerCity.equals(newEventCity) && (!customerCity.equals(oldEventCity))) //case 2: if new event is local, old event is other city
				|| ((!customerCity.equals(newEventCity)) && customerCity.equals(oldEventCity)) //case 3: if new event is other city, old event is local		
			) {
			possibleConflictCBookingOtherCity = false;
		} else if ((!customerCity.equals(newEventCity)) && (!customerCity.equals(oldEventCity)))  { //case 4: if new event is other city, old event is other city
			if ( ! newMonthYear.equals(oldMonthYear)) { // different monthYear in old and new event, no confliction in cBookingRecord
				possibleConflictCBookingOtherCity = false;
			} else { //same monthYear, possible confliction
				if ( ! cBookingOtherCity.containsKey(customerID)) { //if this customer has ever booked in other cities
					possibleConflictCBookingOtherCity = false;
				} else {
					if ( ! cBookingOtherCity.get(customerID).containsKey(newMonthYear)) { //newMonthYear=oldMonthYear
						possibleConflictCBookingOtherCity = false;						
					} else { // if this customer ever booked in this monthYear
						if (cBookingOtherCity.get(customerID).get(newMonthYear) == 3) {
							possibleConflictCBookingOtherCity = true;							
						}		
					}
				}		
			}
		}	

		ArrayList<String> bookEventResult = new ArrayList<String>();
		Any any;
		String cancelEventResult = "";
		
		if (possibleConflictCBookingOtherCity) { //if 
			int currentBookingOtherCities = cBookingOtherCity.get(customerID).get(newMonthYear);
			cBookingOtherCity.get(customerID).put(newMonthYear, currentBookingOtherCities-1);			
		}
		any = bookEvent(customerID, newEventID, newEventType);
		bookEventResult = (ArrayList<String>)any.extract_Value();
		if ( ! bookEventResult.get(0).equals("Success")) { //can't book the new event
			returnMessage.add(bookEventResult.get(0));
			returnMessage.add("");
			serverLogger.info("Fail in swaping, because can't book new event. New event id: " + newEventID +" new event type: "+newEventType+"\n");
			if (possibleConflictCBookingOtherCity) { //if has - 1
				int currentBookingOtherCities = cBookingOtherCity.get(customerID).get(newMonthYear);
				cBookingOtherCity.get(customerID).put(newMonthYear, currentBookingOtherCities+1);			
			}			
			return returnAny(returnMessage) ;
		} else { //if book event is success
			returnMessage.add(bookEventResult.get(0)); // the first element is "success"
			cancelEventResult = cancelEvent(customerID, oldEventID, oldEventType);
			
			if (cancelEventResult.equals("Success")) { //book new and cancel old both succeed
				returnMessage.add(cancelEventResult);
				serverLogger.info("Succeed in booking new event and canceling old event."+"\n");
				if (possibleConflictCBookingOtherCity) { //if has - 1
					int currentBookingOtherCities = cBookingOtherCity.get(customerID).get(newMonthYear);
					cBookingOtherCity.get(customerID).put(newMonthYear, currentBookingOtherCities+1);			
				}	
				return returnAny(returnMessage) ;
			} else { // booked new, but failed in canceling old
				returnMessage.add(cancelEventResult);
				serverLogger.info("Fail in swaping, because can't cancel old event. Old event id: " + oldEventID +" old event type: "+oldEventType+"\n");
				cancelEvent(customerID, newEventID, newEventType); //TODO: need to verify if succeed here? receive message???
				if (possibleConflictCBookingOtherCity) { //if has - 1
					int currentBookingOtherCities = cBookingOtherCity.get(customerID).get(newMonthYear);
					cBookingOtherCity.get(customerID).put(newMonthYear, currentBookingOtherCities+1);			
				}
				return returnAny(returnMessage) ;
			}
		}			
	}


	public ConcurrentHashMap<String, ArrayList<Integer>> listEventAvailabilityForUDP(String eventType) throws Exception {
		serverLogger.info("request: list event availability for other server"+"\n");
		ConcurrentHashMap<String, ArrayList<Integer>> eventSubHashMap = mainHashMap.get(eventType);
		return eventSubHashMap;
	}


	//book event in my city upon request of other cities, no record needed in cBookingOtherCites in target city, it is managed by its own city
	public synchronized ArrayList<String> bookEventForUDP(String customerID, String eventID, String eventType) throws Exception {
		ArrayList<String> returnMessage = new ArrayList<String>();
		customerID = customerID.trim();
		eventType = eventType.trim();
		eventID = eventID.trim();

		serverLogger.info("request: book event for other server"+"\n");

		String eventTypeAndID = eventType.substring(0,1) + "" + eventID;
		
		//validate, if book for own city, should not use this method
		if (customerID.substring(0,3).toUpperCase().equals(eventID.substring(0,3).toUpperCase())) { 
			returnMessage.add("Fail");
			returnMessage.add("City confusion");
			serverLogger.info("Fail"+"\n");
			serverLogger.info("City confusion"+"\n");
			System.out.println(returnMessage.get(0)); System.out.println(returnMessage.get(1));
			return returnMessage;
		}

		if (!(mainHashMap.get(eventType).containsKey(eventID))) { // if the event doesn't exist
			returnMessage.add("NoExist");
			returnMessage.add("The event you attampt to book doesn't exist.");
			serverLogger.info("NoExist"+"\n");
			serverLogger.info("The event you attampt to book doesn't exist."+"\n");
			System.out.println(returnMessage.get(0)); System.out.println(returnMessage.get(1));
			return returnMessage;
		} else { // if the event exists
			if (! (mainHashMap.get(eventType).get(eventID).get(0) > mainHashMap.get(eventType).get(eventID).get(1))) { // if the capacity left is not enough
				returnMessage.add("Full");
				returnMessage.add("This event is fully booked.");
				serverLogger.info("Full"+"\n");
				serverLogger.info("This event is fully booked."+"\n");
				System.out.println(returnMessage.get(0)); System.out.println(returnMessage.get(1));
				return returnMessage;
			} else { // if there is still space to book	
				if (!cBookingRecord.containsKey(customerID)) { // if this customer never booked before and doesn't exist in database	
					// add to total booking record
					ArrayList<String> tempEventTypeAndIDAL =  new ArrayList<String> ();
					tempEventTypeAndIDAL.add(eventTypeAndID);
					cBookingRecord.put(customerID, tempEventTypeAndIDAL);
					
					//update space available of this event
					int usedSpace = mainHashMap.get(eventType).get(eventID).get(1);
					mainHashMap.get(eventType).get(eventID).set(1, usedSpace+1);
					
					returnMessage.add("Success");
					returnMessage.add("You have successfully booked a space in:  \n"
							+ "Event type: " + eventType + "; Event ID: " + eventID + ".");
					serverLogger.info("Success"+"\n");
					serverLogger.info("You have successfully booked a space in:  \n"
							+ "Event type: " + eventType + "; Event ID: " + eventID + "."+"\n");
					System.out.println(returnMessage.get(0)); System.out.println(returnMessage.get(1));
					return returnMessage;
				} else { // if this customer booked before and exists
					if (cBookingRecord.get(customerID).contains(eventTypeAndID)) { // if the event type and ID is not unique
						returnMessage.add("NotUnique");
						returnMessage.add("A customer can not book more than one event with the same event id and same event type.");
						serverLogger.info("NotUnique"+"\n");
						serverLogger.info("A customer can not book more than one event with the same event id and same event type."+"\n");
						System.out.println(returnMessage.get(0));
						System.out.println(returnMessage.get(1));
						return returnMessage;
					} else { // if the event type and ID is unique
						// update total booking record (by adding this event)
						cBookingRecord.get(customerID).add(eventTypeAndID);
						
						//update space available of this event
						int usedSpace = mainHashMap.get(eventType).get(eventID).get(1);
						mainHashMap.get(eventType).get(eventID).set(1, usedSpace+1);
						
						returnMessage.add("Success");
						returnMessage.add("You have successfully booked a space in:  \n"
								+ "Event type: " + eventType + "; Event ID: " + eventID + ".");
						serverLogger.info("Success"+"\n");
						serverLogger.info("You have successfully booked a space in:  \n"
								+ "Event type: " + eventType + "; Event ID: " + eventID + "."+"\n");
						System.out.println(returnMessage.get(0)); System.out.println(returnMessage.get(1));
						return returnMessage;		
					}
				}
			}
		}			
	}


	// done this method
	public synchronized ArrayList<String> getBookingScheduleForUDP(String customerID) throws Exception {
		//get a ArrayList<String>, elements are: CTORA100519, CTORE100519, ... (first letter is event type)
		serverLogger.info("request: get booking schedule for other server"+"\n");
		ArrayList<String> returnMessageThisCity = cBookingRecord.get(customerID); 
		return returnMessageThisCity;
	}


	// cancel the event happens in this city for a customer in other city
	public synchronized String cancelEventForUDP(String customerID, String eventID, String eventType) throws Exception {
		String returnMessage;
		customerID = customerID.trim();
		eventID = eventID.trim();
		eventType = eventType.trim();		
		String eventTypeAndID = eventType.substring(0,1) + "" + eventID; //create the info like "CTORA100519"
		serverLogger.info("request: cancel event for other server"+"\n");
		 
		if (! mainHashMap.get(eventType).containsKey(eventID)) { // if this event id of this type doesn't exist
			returnMessage = "EventNotExist";
			serverLogger.info("EventNotExist"+"\n");
		} else { // if this event id of this type exists
			if ( ! cBookingRecord.containsKey(customerID)) { // if this customer never booked in own city (doesn't exist in cBookingRecord)
				returnMessage = "CustomerNeverBooked";
				serverLogger.info("CustomerNeverBooked"+"\n");
			} else {								
				if ( ! cBookingRecord.get(customerID).contains(eventTypeAndID)) { // if this customer never booked this event in this type
					returnMessage = "ThisCustomerHasNotBookedThis";
					serverLogger.info("ThisCustomerHasNotBookedThis"+"\n");
				} else { // if everything is ok
					cBookingRecord.get(customerID).remove(eventTypeAndID); //update the cBookingRecord of target city
					if (mainHashMap.get(eventType).get(eventID).get(1)>0) { //validate in mainHashMap's capacity record
						int newUsedSpace = mainHashMap.get(eventType).get(eventID).get(1)-1;
						mainHashMap.get(eventType).get(eventID).set(1, newUsedSpace);
						returnMessage = "Success";
						serverLogger.info("Success"+"\n");
					} else {
						returnMessage = "Capacity Error";
						serverLogger.info("Capacity Error"+"\n");
					}
				}
			}		
		}	
		return returnMessage.trim();
	}
	
	public String UDPCommunicationCancelEvent(String customerID, String eventID, String eventType) {
		// judge which is the target city
		String cityAbb = eventID.trim().substring(0, 3).trim();
		int targetUDPPortNumber = 0;
		if (cityAbb.equals("MTL")) {
			targetUDPPortNumber = MTLRemoteUDPPortNumber;
		} else if (cityAbb.equals("OTW")){
			targetUDPPortNumber = OTWRemoteUDPPortNumber;
		} else if (cityAbb.equals("TOR")){
			targetUDPPortNumber = TORRemoteUDPPortNumber;
		}
		System.out.println(targetUDPPortNumber);
		DatagramSocket aSocket = null;  //a buffer
		String result =""; //initialize
		
		try{
			System.out.println("asking request");
			aSocket = new DatagramSocket(); //reference of the original socket

			String messageToSend = "cancelEvent " + customerID + " " + eventID + " " + eventType;//the message you want to send, e.g. "bookEvent TORC1234 OTWA100519 Conferences"
			byte [] message = messageToSend.getBytes(); //message to be passed is stored in byte array
			InetAddress aHost = InetAddress.getByName("localhost");

			int serverPort = targetUDPPortNumber;// defined for every server already in server classes
			DatagramPacket request = new DatagramPacket(message, messageToSend.length(), aHost, serverPort);//request packet ready
			aSocket.send(request);//request sent out
			System.out.println("Request message sent : "+ new String(request.getData()));
			
			//from here to below: after sending request, receive feedback from target city
			byte [] buffer = new byte[1000];//to store the received data, it will be populated by what receive method returns
			DatagramPacket reply = new DatagramPacket(buffer, buffer.length);//reply packet ready but not populated.

			//Client waits until the reply is received-----------------------------------------------------------------------
			aSocket.receive(reply);//reply received and will populate reply packet now.
			result = new String(reply.getData());
			result = result.trim();
			System.out.println("Reply received from the server is: "+ result);//print reply message after converting it to a string from bytes		
		}
		catch(SocketException e){
			System.out.println("Socket: "+e.getMessage());
			serverLogger.warning(e.getMessage()+"\n");
		}
		catch(IOException e){
			e.printStackTrace();
			System.out.println("IO: "+e.getMessage());
			serverLogger.warning(e.getMessage()+"\n");
		} 
		finally{
			//if(aSocket != null) aSocket.close();//now all resources used by the socket are returned to the OS, so that there is no
			//resource leakage, therefore, close the socket after it's use is completed to release resources.
		}
		return result;
	}

	public String UDPCommunicationBookEvent(String customerID, String eventID, String eventType) {
		// judge which is the target city
		String cityAbb = eventID.substring(0, 3);
		int targetUDPPortNumber = 0;
		if (cityAbb.equals("MTL")) {
			targetUDPPortNumber = MTLRemoteUDPPortNumber;
		} else if (cityAbb.equals("OTW")){
			targetUDPPortNumber = OTWRemoteUDPPortNumber;
		} else if (cityAbb.equals("TOR")){
			targetUDPPortNumber = TORRemoteUDPPortNumber;
		}
		System.out.println(targetUDPPortNumber);
		DatagramSocket aSocket = null;  //a buffer
		String result =""; //initialize

		try{
			System.out.println("asking request");
			aSocket = new DatagramSocket(); //reference of the original socket

			String messageToSend = "bookEvent " + customerID + " " + eventID + " " + eventType;//the message you want to send, e.g. "bookEvent TORC1234 OTWA100519 Conferences"
			byte [] message = messageToSend.getBytes(); //message to be passed is stored in byte array
			InetAddress aHost = InetAddress.getByName("localhost");

			int serverPort = targetUDPPortNumber;// defined for every server already in server classes
			DatagramPacket request = new DatagramPacket(message, messageToSend.length(), aHost, serverPort);//request packet ready
			aSocket.send(request);//request sent out
			System.out.println("Request message sent : "+ new String(request.getData()));
			
			//from here to below: after sending request, receive feedback from target city
			byte [] buffer = new byte[1000];//to store the received data, it will be populated by what receive method returns
			DatagramPacket reply = new DatagramPacket(buffer, buffer.length);//reply packet ready but not populated.

			//Client waits until the reply is received-----------------------------------------------------------------------
			aSocket.receive(reply);//reply received and will populate reply packet now.
			result = new String(reply.getData());
			result = result.trim();
			System.out.println("Reply received from the server is: "+ result);//print reply message after converting it to a string from bytes		
		}
		catch(SocketException e){
			System.out.println("Socket: "+e.getMessage());
			serverLogger.warning(e.getMessage()+"\n");
		}
		catch(IOException e){
			e.printStackTrace();
			System.out.println("IO: "+e.getMessage());
			serverLogger.warning(e.getMessage()+"\n");
		} 
		finally{
			//if(aSocket != null) aSocket.close();//now all resources used by the socket are returned to the OS, so that there is no
			//resource leakage, therefore, close the socket after it's use is completed to release resources.
		}
		return result;
	}
	
	public ArrayList<String> UDPCommunicationGetBookingSchedule(String customerID, int remoteUDPPort) {		
		ArrayList<String> returnMessageThisCity = new ArrayList<String>();
		DatagramSocket aSocket = null;  //a buffer
		String result =""; //initialize
		try{
			System.out.println("asking request");
			aSocket = new DatagramSocket(); //reference of the original socket

			String messageToSend = "getBookingSchedule " + customerID;//the message you want to send, e.g. "getBookingSchedule TORC1234"
			byte [] message = messageToSend.getBytes(); //message to be passed is stored in byte array
			InetAddress aHost = InetAddress.getByName("localhost");

			int serverPort = remoteUDPPort;// defined for every server already in server classes
			DatagramPacket request = new DatagramPacket(message, messageToSend.length(), aHost, serverPort);//request packet ready
			aSocket.send(request);//request sent out
			System.out.println("Request message sent : "+ new String(request.getData()));
			
			//from here to below: after sending request, receive feedback from target city
			byte [] buffer = new byte[1000];//to store the received data, it will be populated by what receive method returns
			DatagramPacket reply = new DatagramPacket(buffer, buffer.length);//reply packet ready but not populated.

			//Client waits until the reply is received-----------------------------------------------------------------------
			aSocket.receive(reply);//reply received and will populate reply packet now.
			result = new String(reply.getData());
			result = result.trim();
			System.out.println("Reply received from the server is: "+ result);//print reply message after converting it to a string from bytes	
			if (!result.trim().equals("")) {						
				String[] replyArray = result.split("\\s+"); //split the received info (e.g. "CTORA100519 CTORE100519 ..." (first letter is event type)			
				for (String s : replyArray) {
					returnMessageThisCity.add(s.trim()); // each element in this ArrayList<String> is "CTORA100519" etc.
				}			
			}			
		}
		catch(SocketException e){
			System.out.println("Socket: "+e.getMessage());
			serverLogger.warning(e.getMessage()+"\n");
		}
		catch(IOException e){
			e.printStackTrace();
			System.out.println("IO: "+e.getMessage());
			serverLogger.warning(e.getMessage()+"\n");
		} 
		finally{
			//if(aSocket != null) aSocket.close();//now all resources used by the socket are returned to the OS, so that there is no
			//resource leakage, therefore, close the socket after it's use is completed to release resources.
		}
		return returnMessageThisCity;
	}

	public ArrayList<String> UDPCommunicationlistEventAvailability(String managerID, String eventType, int remoteUDPPort){
		ArrayList<String> returnMessageThisCity = new ArrayList<String>();
		DatagramSocket aSocket = null;  //a buffer
		String result =""; //initialize
		try{
			System.out.println("asking request");
			aSocket = new DatagramSocket(); //reference of the original socket

			String messageToSend = "listEventAvailability " + eventType;//the message you want to send, e.g. "getBookingSchedule TORC1234"
			byte [] message = messageToSend.getBytes(); //message to be passed is stored in byte array
			InetAddress aHost = InetAddress.getByName("localhost");

			int serverPort = remoteUDPPort;// defined for every server already in server classes
			DatagramPacket request = new DatagramPacket(message, messageToSend.length(), aHost, serverPort);//request packet ready
			aSocket.send(request);//request sent out
			System.out.println("Request message sent : "+ new String(request.getData()));
			
			//from here to below: after sending request, receive feedback from target city
			byte [] buffer = new byte[1000];//to store the received data, it will be populated by what receive method returns
			DatagramPacket reply = new DatagramPacket(buffer, buffer.length);//reply packet ready but not populated.

			//Client waits until the reply is received-----------------------------------------------------------------------
			aSocket.receive(reply);//reply received and will populate reply packet now.
			result = new String(reply.getData());
			result = result.trim();
			System.out.println("Reply received from the server is: "+ result);//print reply message after converting it to a string from bytes				
			
			if (!result.trim().equals("")) {						
				String[] replyArray = result.split("\\s+"); //split the received info (e.g. "CTORA100519 CTORE100519 ..." (first letter is event type)			
				
				for (int m = 0; m < replyArray.length; m=m+3) {
					String eID = replyArray[m].trim();
					String totalCap = replyArray[m+1].trim();
					String bookedCap = replyArray[m+2].trim();
					int availableCap = Integer.parseInt(totalCap) - Integer.parseInt(bookedCap);
					
					String lineFormated = String.format("%-15s %-18s %-15s %-15s", eID, totalCap, bookedCap, availableCap);
					returnMessageThisCity.add(lineFormated);
				}
			}			
		}
		catch(SocketException e){
			System.out.println("Socket: "+e.getMessage());
			serverLogger.warning(e.getMessage()+"\n");
		}
		catch(IOException e){
			e.printStackTrace();
			System.out.println("IO: "+e.getMessage());
			serverLogger.warning(e.getMessage()+"\n");
		} 
		finally{
			//if(aSocket != null) aSocket.close();//now all resources used by the socket are returned to the OS, so that there is no
			//resource leakage, therefore, close the socket after it's use is completed to release resources.
		}
		return returnMessageThisCity;
	}
}
