package server;

import java.io.IOException;
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
	// record of customerID, eventType+eventID (e.g. CTORA100519, first letter is event type), to make sure unique
	
	// TODO use concurrent hasmap
	//private ConcurrentHashMap<String, ArrayList<String>> cBookingRecord = new ConcurrentHashMap<String, ArrayList<String>>();
	
	private HashMap<String, ArrayList<String>> cBookingRecord = new HashMap<String, ArrayList<String>>();
	
	// a customer can book at most 3 events from other cities overall in a month. <CustomerID, <monthYear, numberOfBooking>
	private HashMap<String, HashMap<String, Integer>> cBookingOtherCity = new HashMap<String, HashMap<String, Integer>> ();


	//remote udp port for request other servers
	private int firstRemoteUDPPort;
	private int secondRemoteUDPPort;
	
	private int MTLRemoteUDPPortNumber;
	private int TORRemoteUDPPortNumber;
	private int OTWRemoteUDPPortNumber;

	public DEMSImpl(int firstRemoteUDPPort,int secondRemoteUDPPort) throws RemoteException {
		super();
		mainHashMap.put("Conferences",conferencesSubHashMap);
		mainHashMap.put("Seminars",seminarsSubHashMap);
		mainHashMap.put("TradeShows",tradeShowsSubHashMap);

		this.firstRemoteUDPPort = firstRemoteUDPPort;
		this.secondRemoteUDPPort = secondRemoteUDPPort;

		this.MTLRemoteUDPPortNumber = UDP_PORT_MTL;
		this.OTWRemoteUDPPortNumber = UDP_PORT_OTW;
		this.TORRemoteUDPPortNumber = UDP_PORT_TOR;

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
	
	public ArrayList<String> addEvent(String MID,String eventID, String eventType, int bookingCapacity){
		// push a add event message to the processing queue.
		// wait that the message is processed
		
		ArrayList<String> returnMessage = new ArrayList<String>();		
		
		if (!mainHashMap.get(eventType).containsKey(eventID)) { //If an event does not exist in the database for that event type, then add it.
			HashMap<String, ArrayList<Integer>> tempSubHashMap = mainHashMap.get(eventType);
			ArrayList<Integer> tempCapArrayList = new ArrayList<Integer>();
			tempCapArrayList.add(bookingCapacity);
			tempCapArrayList.add(0);
			tempSubHashMap.put(eventID, tempCapArrayList);			
			mainHashMap.put(eventType, tempSubHashMap);				
			returnMessage.add("Added");
			returnMessage.add("New event added.");
			//TODO write log into this manager
			return returnMessage;
		} else { // If an event already exists for same event type, the event manager can't add it again for the same event type but the new bookingCapacity is updated
			ArrayList<Integer> tempCapArrayList = mainHashMap.get(eventType).get(eventID);
			if (bookingCapacity < tempCapArrayList.get(1)) {
				returnMessage.add("Fail");
				returnMessage.add("Event already exist and the new booking capacity you entered is less than space already booked, updating event fails.");
				//TODO write log into this manager
				return returnMessage;
			}
			tempCapArrayList.set(0, bookingCapacity); //update the element 0 as the input new capacity
			mainHashMap.get(eventType).put(eventID, tempCapArrayList);	
			returnMessage.add("Updated");
			returnMessage.add("Event exists, no new event added. Event capacity updated.");
			//TODO write log into this manager
			return returnMessage;
		}
	}

	public ArrayList<String> removeEvent(String MID , String eventID, String eventType){
		// push a add event message to the processing queue.
		// wait that the message is processed
		
		ArrayList<String> returnMessage = new ArrayList<String>();
		
		if (!mainHashMap.get(eventType).containsKey(eventID)) { //If an event does not exist, there is no deletion performed
			returnMessage.add("NoExist");
			returnMessage.add("No such event exist. Nothing is removed.");
			return returnMessage;			
		} else { //if an event exists
			// TODO if need to inform related customer, write here, otherwise no action needed
			mainHashMap.get(eventType).remove(eventID);
			returnMessage.add("Success");
			returnMessage.add("Event " + eventID + " of " + eventType + " has been removed.");
			//TODO write log into this manager
			return returnMessage;
		}
	}
	public ArrayList<String> listEventAvailability(String MID, String eventType){
		// push a add event message to the processing queue.
		// wait that the message is processed
		
		// TODO UDP to communicate with other cities, maybe need to adjust method.
		
		ArrayList<String> returnMessage = new ArrayList<String>();
		String lineFormated;
		
		//below is for printing only the city of the manager. 		
		String ownCity = MID.substring(0,3).toUpperCase();
		returnMessage.add("Number of spaces available for each event:");
		returnMessage.add(ownCity + ":");
		
		HashMap<String, ArrayList<Integer>> tempSubHashMap = mainHashMap.get(eventType);		
		Set<String> keySet = tempSubHashMap.keySet();
		
		lineFormated = String.format("%-15s %-18s %-15s %-15s", "Event ID", "Total Capacity", "Booked Space", "Available Space");
		returnMessage.add(lineFormated);
		
		for (String s : keySet) {
			String eID = s;
			int totalCap = tempSubHashMap.get(s).get(0);
			int bookedCap = tempSubHashMap.get(s).get(1);
			int availableCap = totalCap-bookedCap;
			lineFormated = String.format("%-15s %-18s %-15s %-15s", eID, totalCap, bookedCap, availableCap);
			returnMessage.add(lineFormated);	
		}
		return returnMessage;
	}
	
	public ArrayList<String> bookEvent(String customerID, String eventID, String eventType){
		// push a add event message to the processing queue.
		// wait that the message is processed
		ArrayList<String> returnMessage = new ArrayList<String>();
		
		String eventTypeAndID = eventType.substring(0,1) + "" + eventID;		
		
		if (customerID.substring(0,3).toUpperCase().equals(eventID.substring(0,3).toUpperCase())) {// if this customer is booking for his/her own city
			if (!mainHashMap.get(eventType).containsKey(eventID)) { // if the event doesn't exist
				returnMessage.add("NoExist");
				returnMessage.add("The event you attampt to book doesn't exist.");
				return returnMessage;
			} else { // if the event exists
				if (! (mainHashMap.get(eventType).get(eventID).get(0) 
						> mainHashMap.get(eventType).get(eventID).get(1))) { // if the capacity left is not enough
					returnMessage.add("Full");
					returnMessage.add("This event is fully booked.");
					return returnMessage;
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
						return returnMessage;
					} else { //if this customer booked before and exists
						if (cBookingRecord.get(customerID).contains(eventTypeAndID)) { // if the event type and ID is not unique
							returnMessage.add("NotUnique");
							returnMessage.add("A customer can not book more than one event with the same event id and same event type.");
							return returnMessage;
						} else { // if the input event type and ID doesn't exist for this customer
							// update total booking record (by adding this event)
							cBookingRecord.get(customerID).add(eventTypeAndID);		
							
							//update space available of this event
							int usedSpace = mainHashMap.get(eventType).get(eventID).get(1);
							mainHashMap.get(eventType).get(eventID).set(1, usedSpace+1);
							
							returnMessage.add("Success");
							returnMessage.add("You have successfully booked a space in:  \n"
									+ "Event type: " + eventType + "; Event ID: " + eventID + ".");					
							return returnMessage;
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
						return returnMessage;
					} else { // if booking time less than 3, go UDP communicate with the target city
						result = UDPCommunicationBookEvent(customerID, eventID, eventType);
													
						// add 1 to the number of booking of this customer of this month
						if (result.equals("Success")) {
							int currentBookingOtherCities = cBookingOtherCity.get(customerID).get(monthYear);
							cBookingOtherCity.get(customerID).put(monthYear, currentBookingOtherCities+1);
						}
					}
				} else { //if customer exists in cBookingOtherCity but never booked this month, also can book
					result = UDPCommunicationBookEvent(customerID, eventID, eventType);
				
					// create this month, put 1
					if (result.equals("Success")) {
						HashMap<String, Integer> monthRecord = cBookingOtherCity.get(customerID);
						monthRecord.put(monthYear, 1);
						cBookingOtherCity.put(customerID, monthRecord);
					}
				}
			} else { //if this customer has never booked in other cities, can book
				result = UDPCommunicationBookEvent(customerID, eventID, eventType);
				
				// if booking successes, need to record in the cBookingOtherCity. As this customer doesn't exist, create
				if (result.equals("Success")) {
					HashMap<String, Integer> tempDateNumber =  new HashMap<String, Integer> ();
					tempDateNumber.put(monthYear, 1);
					cBookingOtherCity.put(customerID, tempDateNumber);						
				}
			}
			// ------ end communicate with target other city:------	
			returnMessage.add(result);			
			return returnMessage;
		}
	}

	public ArrayList<String> getBookingSchedule(String customerID){
		// push a add event message to the processing queue.
		// wait that the message is processed
		
		ArrayList<String> returnMessage = new ArrayList<String>(); // only return when combine info in all cities
		
		ArrayList<String> returnMessageOwnCity = new ArrayList<String>();
		ArrayList<String> returnMessageFirstOtherCity = new ArrayList<String>();
		ArrayList<String> returnMessageSecondOtherCity = new ArrayList<String>();
		
		if (cBookingRecord.containsKey(customerID)) {
			returnMessageOwnCity = cBookingRecord.get(customerID);
		}		
		
		// ------ begin communicate with the first other city:------
		// send message to target city 1, get reply, put reply to returnMessageFirstOtherCity
		DatagramSocket aSocket = null;  //a buffer
		String result1 =""; //initialize
		try{
			System.out.println("asking request");
			aSocket = new DatagramSocket(); //reference of the original socket

			String messageToSend = "getBookingSchedule " + customerID;//the message you want to send, e.g. "getBookingSchedule TORC1234"
			byte [] message = messageToSend.getBytes(); //message to be passed is stored in byte array
			InetAddress aHost = InetAddress.getByName("localhost");

			int serverPort = firstRemoteUDPPort;// defined for every server already in server classes
			DatagramPacket request = new DatagramPacket(message, messageToSend.length(), aHost, serverPort);//request packet ready
			aSocket.send(request);//request sent out
			System.out.println("Request message sent : "+ new String(request.getData()));
			
			//from here to below: after sending request, receive feedback from target city
			byte [] buffer = new byte[1000];//to store the received data, it will be populated by what receive method returns
			DatagramPacket reply = new DatagramPacket(buffer, buffer.length);//reply packet ready but not populated.

			//Client waits until the reply is received-----------------------------------------------------------------------
			aSocket.receive(reply);//reply received and will populate reply packet now.
			result1 = new String(reply.getData());
			System.out.println("Reply received from the server is: "+ result1);//print reply message after converting it to a string from bytes	
			if (!result1.equals("")) {						
				String[] replyArray = result1.split("\\s+"); //split the received info (e.g. "CTORA100519 CTORE100519 ..." (first letter is event type)			
				for (String s : replyArray) {
					returnMessageFirstOtherCity.add(s); // each element in this ArrayList<String> is "CTORA100519" etc.
				}			
			}			
		}
		catch(SocketException e){
			System.out.println("Socket: "+e.getMessage());
		}
		catch(IOException e){
			e.printStackTrace();
			System.out.println("IO: "+e.getMessage());
		}
		finally{
			if(aSocket != null) aSocket.close();//now all resources used by the socket are returned to the OS, so that there is no
			//resource leakage, therefore, close the socket after it's use is completed to release resources.
		}
		// ------ end communicate with the first other city:------
		
		// ------ begin communicate with the second other city:------
		// send message to target city 1, get reply, put reply to returnMessageFirstOtherCity
		DatagramSocket bSocket = null;  //a buffer
		String result2 =""; //initialize
		try{
			System.out.println("asking request");
			bSocket = new DatagramSocket(); //reference of the original socket

			String messageToSend = "getBookingSchedule " + customerID;//the message you want to send, e.g. "getBookingSchedule TORC1234"
			byte [] message = messageToSend.getBytes(); //message to be passed is stored in byte array
			InetAddress bHost = InetAddress.getByName("localhost");

			int serverPort = secondRemoteUDPPort;// defined for every server already in server classes
			DatagramPacket request = new DatagramPacket(message, messageToSend.length(), bHost, serverPort);//request packet ready
			bSocket.send(request);//request sent out
			System.out.println("Request message sent : "+ new String(request.getData()));
			
			//from here to below: after sending request, receive feedback from target city
			byte [] buffer = new byte[1000];//to store the received data, it will be populated by what receive method returns
			DatagramPacket reply = new DatagramPacket(buffer, buffer.length);//reply packet ready but not populated.

			//Client waits until the reply is received-----------------------------------------------------------------------
			bSocket.receive(reply);//reply received and will populate reply packet now.
			result2 = new String(reply.getData());
			System.out.println("Reply received from the server is: "+ new String(reply.getData()));//print reply message after converting it to a string from bytes
			
			if (!result2.equals("")) {						
				String[] replyArray = result2.split("\\s+"); //split the received info (e.g. "CTORA100519 CTORE100519 ..." (first letter is event type)			
				for (String s : replyArray) {
					returnMessageSecondOtherCity.add(s); // each element in this ArrayList<String> is "CTORA100519" etc.
				}			
			}			
		}
		catch(SocketException e){
			System.out.println("Socket: "+e.getMessage());
		}
		catch(IOException e){
			e.printStackTrace();
			System.out.println("IO: "+e.getMessage());
		}
		finally{
			if(aSocket != null) aSocket.close();//now all resources used by the socket are returned to the OS, so that there is no
			//resource leakage, therefore, close the socket after it's use is completed to release resources.
		}
		// ------ end communicate with the second other city:------
			
		// combine info in all 3 cities, and reply to client (return a ArrayList<String>, safe
		if (returnMessageOwnCity.size()!=0) {
			for (int i = 0; i<returnMessageOwnCity.size(); i++) {
				returnMessage.add(returnMessageOwnCity.get(i));
			}	
		}
		
		if (returnMessageFirstOtherCity.size()!=0) {
			for (int i = 0; i<returnMessageFirstOtherCity.size(); i++) {
				returnMessage.add(returnMessageFirstOtherCity.get(i));
			}
		}
			
		if (returnMessageSecondOtherCity.size()!=0) {
			for (int i = 0; i<returnMessageSecondOtherCity.size(); i++) {
				returnMessage.add(returnMessageSecondOtherCity.get(i));
			}
		}
		
		// TODO check using addAll instead of for loops
		//returnMessage.addAll(returnMessageSecondOtherCity);
				
		return  returnMessage;//return a ArrayList<String> to client, safe
	}
	//--------end of method "public ArrayList<String> getBookingSchedule(String customerID)"------------------
	
	public boolean cancelEvent(String customerID,String eventID){
		// push a add event message to the processing queue.
		// wait that the message is processed

		return true;//return true if action success
	}

	@Override
	public String listEventAvailabilityForUDP() throws Exception {
		//TODO:get what is needed
		return null;
	}

	@Override
	//book event in my city upon request of other cities, no record needed in cBookingOtherCites in target city, it is managed by its own city
	public ArrayList<String> bookEventForUDP(String customerID, String eventID, String eventType) throws Exception {
		ArrayList<String> returnMessage = new ArrayList<String>();
		String eventTypeAndID = eventType.substring(0,1) + "" + eventID;
		
		//validate, if book for own city, should not use this method
		if (customerID.substring(0,3).toUpperCase().equals(eventID.substring(0,3).toUpperCase())) { 
			returnMessage.add("Fail");
			returnMessage.add("City confusion");
			return returnMessage;
		}
		eventType = eventType.trim();

		 // this is to write if this customer wants to book in other cities, need UDP
		if (!(mainHashMap.get(eventType).containsKey(eventID))) { // if the event doesn't exist
			returnMessage.add("NoExist");
			returnMessage.add("The event you attampt to book doesn't exist.");
			return returnMessage;
		} else { // if the event exists
			if (! (mainHashMap.get(eventType).get(eventID).get(0) > mainHashMap.get(eventType).get(eventID).get(1))) { // if the capacity left is not enough
				returnMessage.add("Full");
				returnMessage.add("This event is fully booked.");
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
					return returnMessage;
				} else { // if this customer booked before and exists
					if (cBookingRecord.get(customerID).contains(eventTypeAndID)) { // if the event type and ID is not unique
						returnMessage.add("NotUnique");
						returnMessage.add("A customer can not book more than one event with the same event id and same event type.");
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
						return returnMessage;		
					}
				}
			}
		}			
	}

	@Override
	// done this method
	public ArrayList<String> getBookingScheduleForUDP(String customerID) throws Exception {
		//get a ArrayList<String>, elements are: CTORA100519, CTORE100519, ... (first letter is event type)
		
		if (cBookingRecord.containsKey(customerID)) {
			ArrayList<String> returnMessageThisCity = cBookingRecord.get(customerID); 
			return returnMessageThisCity;
		} else { //if this client of other city never booked in this city (doesn't exist in this city's database)
			return null;
		}
	}

	@Override
	public String cancelEventForUDP(String customerID, String eventID) throws Exception {
		//TODO:get what is needed
		return null;
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
				
		System.out.println("asking request");
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
			System.out.println("Reply received from the server is: "+ result);//print reply message after converting it to a string from bytes		
		}
		catch(SocketException e){
			System.out.println("Socket: "+e.getMessage());
		}
		catch(IOException e){
			e.printStackTrace();
			System.out.println("IO: "+e.getMessage());
		} 
		finally{
			//if(aSocket != null) aSocket.close();//now all resources used by the socket are returned to the OS, so that there is no
			//resource leakage, therefore, close the socket after it's use is completed to release resources.
		}
		return result;
	}



	//TODO:ask request template
		/*DatagramSocket aSocket = null;  //a buffer
		String result =""; //initialize
		try{
			System.out.println("asking request");
			aSocket = new DatagramSocket(); //reference of the original socket

			String messageToSend  =  "";//the message you want to send, e.g. "TORC1234, getBookingSchedule, ..."

			byte [] message = messageToSend.getBytes(); //message to be passed is stored in byte array

			InetAddress aHost = InetAddress.getByName("localhost");

			int serverPort = firstRemoteUDPPort;// don't forget the second server in 2 methods: listEventAvailability, getBookingSchedule
			DatagramPacket request = new DatagramPacket(message, messageToSend.length(), aHost, serverPort);//request packet ready
			aSocket.send(request);//request sent out
			System.out.println("Request message sent : "+ new String(request.getData()));
			
			//from here to below: after sending request, receive feedback from target city
			byte [] buffer = new byte[1000];//to store the received data, it will be populated by what receive method returns
			DatagramPacket reply = new DatagramPacket(buffer, buffer.length);//reply packet ready but not populated.

			//Client waits until the reply is received-----------------------------------------------------------------------
			aSocket.receive(reply);//reply received and will populate reply packet now.
			result = new String(reply.getData());
			System.out.println("Reply received from the server is: "+ new String(reply.getData()));//print reply message after converting it to a string from bytes
		}
		catch(SocketException e){
		System.out.println("Socket: "+e.getMessage());
		}
		catch(IOException e){
		e.printStackTrace();
		System.out.println("IO: "+e.getMessage());
		}
		finally{
		if(aSocket != null) aSocket.close();//now all resources used by the socket are returned to the OS, so that there is no
		//resource leakage, therefore, close the socket after it's use is completed to release resources.
		}*/
	
	

}
