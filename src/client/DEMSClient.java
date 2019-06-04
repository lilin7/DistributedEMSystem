package client;
import java.io.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Scanner;

import server.DEMSInterface;

/**
 * This class represents the object client for a distributed
 * object of class DEMS, which implements the remote interface
 * server.DEMSInterface.
 */
public class DEMSClient {

	private String userID;
	private String location;
	private String lookUpServerName;
	private String role;
	private String number;
	private DEMSInterface obj;
	private int portNumber;

	public static void main(String[] args){
		try {
			// TODO change the name you'll get confused
			String userID = "";
			boolean IDFlag = false;
			InputStreamReader is = new InputStreamReader(System.in);
			BufferedReader br = new BufferedReader(is);

			while(!IDFlag){
				IDFlag = true;
				System.out.println("Please enter your ID:");
				userID = br.readLine().trim().toUpperCase();

				String lo = userID.substring(0,3).toUpperCase();
				if (!(lo.equals("MTL")||lo.equals("OTW")||lo.equals("TOR"))) {
					IDFlag = false;
				}
				String ro = userID.substring(3,4).toUpperCase();
				if (!(ro.equals("M")||ro.equals("C"))){
					IDFlag = false;
				}
				String nu = userID.substring(4).toUpperCase();
				if (nu.length() != 4) {
					IDFlag = false;
				}
				for(int i = 0; i<nu.length();i++){
					if(!Character.isDigit(nu.charAt(i))){
						IDFlag = false;
					}
				}
			}

			DEMSClient newclient = new DEMSClient();

			newclient.setID(userID);
			newclient.start();
		} 
		catch (Exception e) {
			System.out.println("Exception in client.DEMSClient: " + e);
		} 
	}

	public void setID(String id){
		this.userID = id;
	}

	public void start()throws Exception{
		this.location = userID.substring(0,3).toUpperCase();
		this.role = userID.substring(3,4).toUpperCase();
		this.number = userID.substring(4).toUpperCase();

		if(location.equals("MTL")){
			this.portNumber = DEMSInterface.PORT_MTL;
			this.lookUpServerName = "MTLServer";
		}else if(location.equals("OTW")){
			this.portNumber = DEMSInterface.PORT_OTW;
			this.lookUpServerName = "OTWServer";
		} else if(location.equals("TOR")){
			this.portNumber = DEMSInterface.PORT_TOR;
			this.lookUpServerName = "TORServer";
		} else {
			System.out.println("wrong id");
		}

		Registry registry = LocateRegistry.getRegistry(portNumber);
		obj = (DEMSInterface) registry.lookup(lookUpServerName);

		if(role.equals("M")){
			managerOperate();
		}else if(role.equals("C")){
			customerOperate();
		}else{
			System.out.println("wrong id");
		}
	}

	private void managerOperate() throws RemoteException{
		int user_input;
		Scanner sc = new Scanner(System.in);
		
		do {
			System.out.print(
					"\nCurrent User: Manager " + userID + "\n"
					+"Please input a number to select action: \n"
					+"1. Add an event  \n"
					+"2. Remove and event \n"
					+"3. List event availability \n"
					+"4. Book event for a customer  \n"
					+"5. Get booking schedule of a customer  \n"
					+"6. Cancel event of a customer \n"
					+"0. Quit \n"
					);

			user_input = Integer.parseInt(sc.nextLine().trim());
			boolean result = false;
			
			switch (user_input) {
				case 1:{
					System.out.println("Now performing: Add an event.");
					System.out.println("Please enter event ID: (format example: MTLE100519)");
					String eventID = sc.nextLine().trim().toUpperCase();
					System.out.println("Please enter event type: Conferences, Seminars, TradeShows");
					String eventType = sc.nextLine().trim();
					System.out.println("Please enter a number for Booking Capacity:");
					int bookingCapacity = Integer.parseInt(sc.nextLine().trim());
					
					ArrayList<String> returnMessage = new ArrayList<String>();
					//handle RMI exception PER action, and try to bounce back for a better error handling.
					try {
						returnMessage = obj.addEvent(userID, eventID, eventType, bookingCapacity);
					} catch (java.rmi.RemoteException e) {
						System.out.println("java.rmi.RemoteException: "+e.getMessage());
					}
					
					if(returnMessage.get(0).equals("Added")) {						
						System.out.println("Successfully add an event. \n Event ID: " + eventID +"; "
								+ "Event type: " + eventType + "; Booking capacity: " + bookingCapacity);
						//TODO: log
					}else if (returnMessage.get(0).equals("Fail")) {
						System.out.println("Failed in adding an event");
						System.out.println("Event already exist and the new booking capacity you entered is less than space already booked.");						
						//TODO:log
					} else if (returnMessage.get(0).equals("Updated")) {
						System.out.println("Event exists, no new event added. Event capacity updated.");
					}
					break;
				}
				case 2:{
					System.out.println("Now performing: Remove an event.");
					System.out.println("Please enter event ID: (format example: MTLE100519)");
					String eventID = sc.nextLine().trim().toUpperCase();
					System.out.println("Please enter event type: Conferences, Seminars, TradeShows");
					String eventType = sc.nextLine().trim();
					
					ArrayList<String> returnMessage = new ArrayList<String>();
					
					try {
						returnMessage = obj.removeEvent(userID, eventID, eventType);
					} catch (java.rmi.RemoteException e) {
						System.out.println("java.rmi.RemoteException: "+e.getMessage());
					}
					
					if(returnMessage.get(0).equals("NoExist")) {
						System.out.println("Failed in removing an event, no such event exist.");
						//TODO: add log for this user
					}else if (returnMessage.get(0).equals("Success")){											
						System.out.println("Successfully removed an event. \n "
								+ "Event ID: " + eventID + " Event type: " + eventType);
						//TODO: add log for this user
					}
					break;
				}
				case 3:{
					System.out.println("Now performing: List event availability.");
					System.out.println("Please enter event type: Conferences, Seminars, TradeShows");
					String eventType = sc.nextLine().trim();	
					
					ArrayList<String> returnMessage = new ArrayList<String>();
					
					try {
						returnMessage = obj.listEventAvailability(userID, eventType);
					} catch (java.rmi.RemoteException e) {
						// look at the connection, if connection is dead ; exit
						// if connection is okay, notify the client of the error but continue executing
					}
					
					if(returnMessage.size()!=0) {
						for (String s : returnMessage) {
							System.out.println(s);
						}
						System.out.println();
						//TODO: add log for this user
					}else {
						System.out.println("No record for such event type.");
						//TODO: add log for this user
					}
					break;
				}
				case 4:{					
					System.out.println("Now performing: Book event for a customer.");
					System.out.println("Please enter customer ID: (format example: TORC2345)");
					String customerID = sc.nextLine().trim().toUpperCase();
					System.out.println("Please enter event ID: (format example: MTLE100519)");
					String eventID = sc.nextLine().trim().toUpperCase();
					System.out.println("Please enter event type: Conferences, Seminars, TradeShows");
					String eventType = sc.nextLine().trim();
					
					ArrayList<String> returnMessage = new ArrayList<String>();
					try {			
						returnMessage = obj.bookEvent(customerID, eventID, eventType);
					} catch (java.rmi.RemoteException e) {
						
					}
					
					if(returnMessage.get(0).equals("NoExist")) {
						System.out.println("Fail. The event you attempt to book doesn't exist.");
						//TODO: add log for this user
					}else if (returnMessage.get(0).equals("Full")){
						System.out.println("Fail. This event is fully booked.");
						//TODO: add log for this user
					} else if (returnMessage.get(0).trim().equals("Success")) {
						System.out.println("You have successfully booked a space in:  \n"
								+ "Event type: " + eventType + "; Event ID: " + eventID + ".");
					} else if (returnMessage.get(0).equals("NotUnique")) {
						System.out.println("Fail. A customer can not book more than one event with the same event id and same event type.");
					} else if (returnMessage.get(0).equals("Exceed3LimitInOtherCity")) {
						System.out.println("Fail. A customer can only book at most 3 events from other cities overall in a month.");
					} 
					break;
				}
				case 5:{ //done this case of manager
					System.out.println("Now performing: Get booking schedule of a customer.");
					System.out.println("Please enter customer ID: (format example: TORC2345)");
					String customerID = sc.nextLine().trim().toUpperCase();
					
					ArrayList<String> returnMessage = new ArrayList<String>();
					
					//receive ArrayList<String> of info in all 3 cities. Elements like CTORE100519, need to decode, C means Conferences
					returnMessage = obj.getBookingSchedule(customerID);	
					if (returnMessage.size()==0) {
						System.out.println("There is no booking record for customer " + customerID + ".");
					} else {
						System.out.println("Now printing booking schedule for customer " + customerID + ":");
						System.out.printf("%-15s %-18s %-15s", "City", "Event Type", "Event ID");
						System.out.println();
						
						for (String s : returnMessage) {	
							String subStringCity = s.trim().substring(1, 4);
							String subStringEventType = s.trim().substring(0, 1);
							String eventID = s.trim().substring(1);
							
							if (subStringCity.equals("MTL")) {
								if (subStringEventType.contentEquals("C")) {		
									System.out.printf("%-15s %-18s %-15s", "Montreal", "Conference", eventID);
									System.out.println();
								} else if (subStringEventType.contentEquals("S")) {
									System.out.printf("%-15s %-18s %-15s", "Montreal", "Seminar", eventID);
									System.out.println();
								} else if (subStringEventType.contentEquals("T")) {
									System.out.printf("%-15s %-18s %-15s", "Montreal", "Trade Show", eventID);
									System.out.println();
								}
							} else if (subStringCity.equals("TOR")) {
								if (subStringEventType.contentEquals("C")) {		
									System.out.printf("%-15s %-18s %-15s", "Toronto", "Conference", eventID);
									System.out.println();
								} else if (subStringEventType.contentEquals("S")) {
									System.out.printf("%-15s %-18s %-15s", "Toronto", "Seminar", eventID);
									System.out.println();
								} else if (subStringEventType.contentEquals("T")) {
									System.out.printf("%-15s %-18s %-15s", "Toronto", "Trade Show", eventID);
									System.out.println();
								}
							} else if (subStringCity.equals("OTW")) {
								if (subStringEventType.contentEquals("C")) {		
									System.out.printf("%-15s %-18s %-15s", "Ottawa", "Conference", eventID);
									System.out.println();
								} else if (subStringEventType.contentEquals("S")) {
									System.out.printf("%-15s %-18s %-15s", "Ottawa", "Seminar", eventID);
									System.out.println();
								} else if (subStringEventType.contentEquals("T")) {
									System.out.printf("%-15s %-18s %-15s", "Ottawa", "Trade Show", eventID);
									System.out.println();
								}
							}	
						}	
					}
					
					break;
				}
				case 6:{
					System.out.println("Now performing: Cancel event of a customer.");
					System.out.println("Please enter customer ID: (format example: TORC2345)");
					String customerID = sc.nextLine().trim().toUpperCase();
					System.out.println("Please enter event ID: (format example: MTLE100519)");
					String eventID = sc.nextLine().trim().toUpperCase();
					System.out.println("Please enter event type: Conferences, Seminars, TradeShows");
					String eventType = sc.nextLine().trim();
					
					String returnMessage = "";
					try {
						returnMessage = obj.cancelEvent(customerID, eventID, eventType).trim();
					} catch (java.rmi.RemoteException e) {

					}
					returnMessage=returnMessage.trim();
					if (returnMessage.equals("Success")) {
						System.out.println("Successfully cancelled a space of customer " + customerID + " in event type: " + eventType + " with event ID " + eventID);
					} else if (returnMessage.equals("EventNotExist")) {
						System.out.println("This event of this type doesn't exist.");
					} else if (returnMessage.equals("CustomerNeverBooked")) {
						System.out.println("This customer doesn't exist in the database.");
					} else if (returnMessage.equals("ThisCustomerHasNotBookedThis")) {
						System.out.println("This customer has never booked this event.");
					} else if (returnMessage.equals("Capacity Error")) {
						System.out.println("There is something wrong in the capacity record.");
					} else if (returnMessage.equals("SuccessButNoSuchCustomerIncBookingOtherCity")) {
						System.out.println("Successfully cancelled in the target city, but NoSuchCustomerIncBookingOtherCity.");
					} else if (returnMessage.equals("SuccessButNoSuchMonthIncBookingOtherCity")) {
						System.out.println("Successfully cancelled in the target city, but NoSuchMonthIncBookingOtherCity.");
					} else if (returnMessage.equals("SuccessButWrongNumberOfBookingIncBookingOtherCity")) {
						System.out.println("Successfully cancelled in the target city, but WrongNumberOfBookingIncBookingOtherCity.");
					} else if (returnMessage.equals("SuccessUpdatedAllRecords")) {
						System.out.println("Successfully cancelled a space of customer " + customerID + " in event type: " + eventType + " with event ID " + eventID + "in target city");
					} else {
						System.out.println("wrong message received.");
					}
					break;
				}
				case 0:
					break;
				default:
					System.out.println("Input is wrong, please try again!");
			}	
		}
		while (user_input != 0);	
	}

	private  void customerOperate() throws RemoteException{
		int user_input;
		Scanner sc = new Scanner(System.in);
		
		do {
			System.out.println(
					"\nCurrent User: Customer " + userID + "\n"
					+"Please input a number to select action: \n"
					+"1. Book an event \n"
					+"2. Get your booking schedule in all cities  \n"
					+"3. Cancel an event of yours \n"
					+"0. Quit"
					);

			user_input = Integer.parseInt(sc.nextLine().trim());
			boolean result;
			
			switch (user_input) {
				case 1:{					
					System.out.println("Now performing: Book event for you, your customer ID is: " + userID);
					System.out.println("Please enter event ID: (format example: MTLE100519)");
					String eventID = sc.nextLine().trim().toUpperCase();
					System.out.println("Please enter event type: Conferences, Seminars, TradeShows");
					String eventType = sc.nextLine().trim();
					
					ArrayList<String> returnMessage = new ArrayList<String>();
					try {			
						returnMessage = obj.bookEvent(userID, eventID, eventType);
					} catch (java.rmi.RemoteException e) {
						
					}
					
					if(returnMessage.get(0).equals("NoExist")) {
						System.out.println("Fail. The event you attampt to book doesn't exist.");
						//TODO: add log for this user
					}else if (returnMessage.get(0).equals("Full")){
						System.out.println("Fail. This event is fully booked.");
						//TODO: add log for this user
					} else if (returnMessage.get(0).equals("Success")) {
						System.out.println("You have successfully booked a space in:  \n"
								+ "Event type: " + eventType + "; Event ID: " + eventID + ".");
					} else if (returnMessage.get(0).equals("NotUnique")) {
						System.out.println("Fail. A customer can not book more than one event with the same event id and same event type.");
					} else if (returnMessage.get(0).equals("Exceed3LimitInOtherCity")) {
						System.out.println("Fail. A customer can only book at most 3 events from other cities overall in a month.");
					} 
					break;
				}
				case 2:{ //done this case of customer
					System.out.println("Now performing: Get your booking schedule in all cities.");					
					ArrayList<String> returnMessage = new ArrayList<String>();
					
					//receive ArrayList<String> of info in all 3 cities. Elements like CTORE100519, need to decode, C means Conferences
					returnMessage = obj.getBookingSchedule(userID);	
					System.out.println("Now printing booking schedule for customer " + userID + ":");
					System.out.printf("%-15s %-18s %-15s", "City", "Event Type", "Event ID");
					System.out.println();
					
					for (String s : returnMessage) {	
						String subStringCity = s.substring(1, 4);
						String subStringEventType = s.substring(0, 1);
						String eventID = s.substring(1);
						
						if (subStringCity.equals("MTL")) {
							if (subStringEventType.contentEquals("C")) {		
								System.out.printf("%-15s %-18s %-15s", "Montreal", "Conference", eventID);
								System.out.println();
							} else if (subStringEventType.contentEquals("S")) {
								System.out.printf("%-15s %-18s %-15s", "Montreal", "Seminar", eventID);
								System.out.println();
							} else if (subStringEventType.contentEquals("T")) {
								System.out.printf("%-15s %-18s %-15s", "Montreal", "Trade Show", eventID);
								System.out.println();
							}
						} else if (subStringCity.equals("TOR")) {
							if (subStringEventType.contentEquals("C")) {		
								System.out.printf("%-15s %-18s %-15s", "Montreal", "Conference", eventID);
								System.out.println();
							} else if (subStringEventType.contentEquals("S")) {
								System.out.printf("%-15s %-18s %-15s", "Montreal", "Seminar", eventID);
								System.out.println();
							} else if (subStringEventType.contentEquals("T")) {
								System.out.printf("%-15s %-18s %-15s", "Montreal", "Trade Show", eventID);
								System.out.println();
							}
						} else if (subStringCity.equals("OTW")) {
							if (subStringEventType.contentEquals("C")) {		
								System.out.printf("%-15s %-18s %-15s", "Montreal", "Conference", eventID);
								System.out.println();
							} else if (subStringEventType.contentEquals("S")) {
								System.out.printf("%-15s %-18s %-15s", "Montreal", "Seminar", eventID);
								System.out.println();
							} else if (subStringEventType.contentEquals("T")) {
								System.out.printf("%-15s %-18s %-15s", "Montreal", "Trade Show", eventID);
								System.out.println();
							}
						}	
					}
					break;
				}
				
				case 3:{
					System.out.println("Now performing: Cancel an event that you have booked.");
					System.out.println("Please enter event ID: (format example: MTLE100519)");
					String eventID = sc.nextLine().trim().toUpperCase();
					System.out.println("Please enter event type: Conferences, Seminars, TradeShows");
					String eventType = sc.nextLine().trim();
					
					String returnMessage = "";
					try {
						returnMessage = obj.cancelEvent(userID, eventID, eventType);
					} catch (java.rmi.RemoteException e) {

					}
					
					if (returnMessage.equals("Success")) {
						System.out.println("Successfully cancelled a space for you in event type: " + eventType + " with event ID " + eventID);
					} else if (returnMessage.equals("EventNotExist")) {
						System.out.println("This event of this type doesn't exist.");
					} else if (returnMessage.equals("CustomerNeverBooked")) {
						System.out.println("You don't exist in the database.");
					} else if (returnMessage.equals("ThisCustomerHasNotBookedThis")) {
						System.out.println("You have never booked this event.");
					} else if (returnMessage.equals("Capacity Error")) {
						System.out.println("There is something wrong in the capacity record.");
					} else if (returnMessage.equals("SuccessButNoSuchCustomerIncBookingOtherCity")) {
						System.out.println("Successfully cancelled in the target city, but NoSuchCustomerIncBookingOtherCity.");
					} else if (returnMessage.equals("SuccessButNoSuchMonthIncBookingOtherCity")) {
						System.out.println("Successfully cancelled in the target city, but NoSuchMonthIncBookingOtherCity.");
					} else if (returnMessage.equals("SuccessButWrongNumberOfBookingIncBookingOtherCity")) {
						System.out.println("Successfully cancelled in the target city, but WrongNumberOfBookingIncBookingOtherCity.");
					} else if (returnMessage.equals("SuccessUpdatedAllRecords")) {
						System.out.println("Successfully cancelled a space for you in event type: " + eventType + " with event ID " + eventID + "in target city");
					} else {
						System.out.println("wrong message received.");
					}
					break;
				}
				case 0:
					break;
				default:
					System.out.println("Input is wrong, please try again!");
			}	
		}
		while (user_input != 0);
	}

	public void log(){

	}

}
