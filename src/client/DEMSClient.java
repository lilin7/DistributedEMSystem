package client;
import java.io.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.*;

import server.DEMSInterface;

import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import DEMS_CORBA.DEMSInterfaceCorba;
import DEMS_CORBA.DEMSInterfaceCorbaHelper;
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
	//private DEMSInterface obj;
	private DEMSInterfaceCorba obj;
	private int portNumber;
	private static FileHandler fh = null;
	private static Logger clientLogger;

	public static void main(String[] args){
		try {
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
			clientLogger = Logger.getLogger(userID);

			clientLogger.setUseParentHandlers(true);
			fh = new FileHandler("src/client/client_log/"+userID+".log",true);
			fh.setFormatter(new SimpleFormatter());
			clientLogger.addHandler(fh);

			clientLogger.info("log start\n");
			DEMSClient newclient = new DEMSClient();

			newclient.setID(userID);
			newclient.start(args);
			fh.close();
		} 
		catch (Exception e) {
			System.out.println("Exception in client.DEMSClient: " + e);
			clientLogger.warning(e.getLocalizedMessage()+"\n");
		}
	}

	public void setID(String id){
		this.userID = id;
	}

	public void start(String[] args)throws Exception{
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

		ORB orb = ORB.init(args, null);
		//-ORBInitialPort 1050 -ORBInitialHost localhost
		org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
		NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
		obj = (DEMSInterfaceCorba) DEMSInterfaceCorbaHelper.narrow(ncRef.resolve_str(lookUpServerName));
		//Registry registry = LocateRegistry.getRegistry(portNumber);
		//obj = (DEMSInterface) registry.lookup(lookUpServerName);

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
		Any any;
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
					+"7. Swap two events for a customer \n"
					+"0. Quit \n"
					);

			user_input = Integer.parseInt(sc.nextLine().trim());
			boolean result = false;
			
			switch (user_input) {
				case 1:{
					System.out.println("Now performing: Add an event.");
					System.out.println("Please enter event ID: (format example: MTLE100519)");
					String eventID = sc.nextLine().trim().toUpperCase();
					
					String managerCity = userID.substring(0,3);
					String eventCity = eventID.substring(0,3);
					if (!managerCity.equals(eventCity)) {
						System.out.println("A manager can only add event in your own city.");
						break;
					}
					
					System.out.println("Please enter event type: Conferences, Seminars, TradeShows");
					String eventType = sc.nextLine().trim();
					
					if ((!eventType.equals("Conferences")) && (!eventType.equals("Seminars") ) && (!eventType.equals("TradeShows"))) {
						System.out.println("Event type should only be one of the following 3: Conferences, Seminars, or TradeShows.");
						break;
					}
					
					System.out.println("Please enter a number for Booking Capacity:");
					int bookingCapacity = Integer.parseInt(sc.nextLine().trim());

					clientLogger.info("add event : user: "+userID+" event id: "+eventID+" event type: "+eventType+" event capacity: "+bookingCapacity+"\n");

					ArrayList<String> returnMessage = new ArrayList<String>();
					//handle RMI exception PER action, and try to bounce back for a better error handling.
					try {
						any = obj.addEvent(userID, eventID, eventType, bookingCapacity);
						returnMessage = (ArrayList<String>)any.extract_Value();
					} catch (Exception e) {
						System.out.println("Exception: "+e.getMessage());
						clientLogger.warning(e.getMessage()+"\n");
					}
					
					if(returnMessage.get(0).equals("Added")) {						
						System.out.println("Successfully add an event. \n Event ID: " + eventID +"; "
								+ "Event type: " + eventType + "; Booking capacity: " + bookingCapacity);
						clientLogger.info("Successfully add an event. \n Event ID: " + eventID +"; "
								+ "Event type: " + eventType + "; Booking capacity: " + bookingCapacity+"\n");
					}else if (returnMessage.get(0).equals("Fail")) {
						System.out.println("Failed in adding an event");
						System.out.println("Event already exist and the new booking capacity you entered is less than space already booked.");						
						clientLogger.info("Failed in adding an event"+"\n");
						clientLogger.info("Event already exist and the new booking capacity you entered is less than space already booked."+"\n");

					} else if (returnMessage.get(0).equals("Updated")) {
						System.out.println("Event exists, no new event added. Event capacity updated.");
						clientLogger.info("Event exists, no new event added. Event capacity updated."+"\n");
					}
					break;
				}
				case 2:{
					System.out.println("Now performing: Remove an event.");
					System.out.println("Please enter event ID: (format example: MTLE100519)");
					String eventID = sc.nextLine().trim().toUpperCase();
					
					String managerCity = userID.substring(0,3);
					String eventCity = eventID.substring(0,3);
					if (!managerCity.equals(eventCity)) {
						System.out.println("A manager can only add event in your own city.");
						break;
					}
					
					System.out.println("Please enter event type: Conferences, Seminars, TradeShows");
					String eventType = sc.nextLine().trim();
					
					if ((!eventType.equals("Conferences")) && (!eventType.equals("Seminars") ) && (!eventType.equals("TradeShows"))) {
						System.out.println("Event type should only be one of the following 3: Conferences, Seminars, or TradeShows.");
						break;
					}

					clientLogger.info("remove an event: user id: "+userID+" event id: "+eventID+" event type: "+eventType+"\n");
					ArrayList<String> returnMessage = new ArrayList<String>();
					
					try {
						any = obj.removeEvent(userID, eventID, eventType);
						returnMessage = (ArrayList<String>)any.extract_Value();
					} catch (Exception e) {
						System.out.println("Exception: "+e.getMessage());
						clientLogger.warning(e.getMessage()+"\n");
					}
					
					if(returnMessage.get(0).equals("NoExist")) {
						System.out.println("Failed in removing an event, no such event exist.");
						clientLogger.info("Failed in removing an event, no such event exist."+"\n");
					}else if (returnMessage.get(0).equals("Success")){											
						System.out.println("Successfully removed an event. \n "
								+ "Event ID: " + eventID + " Event type: " + eventType);
						clientLogger.info("Successfully removed an event. \n "
								+ "Event ID: " + eventID + " Event type: " + eventType+"\n");
					}
					break;
				}
				case 3:{
					System.out.println("Now performing: List event availability.");
					System.out.println("Please enter event type: Conferences, Seminars, TradeShows");
					String eventType = sc.nextLine().trim();	
					
					if ((!eventType.equals("Conferences")) && (!eventType.equals("Seminars") ) && (!eventType.equals("TradeShows"))) {
						System.out.println("Event type should only be one of the following 3: Conferences, Seminars, or TradeShows.");
						break;
					}
					
					ArrayList<String> returnMessage = new ArrayList<String>();
					clientLogger.info("list event availability : user id: "+userID+" event type: "+eventType+"\n");
					try {
						any = obj.listEventAvailability(userID, eventType);
						returnMessage = (ArrayList<String>)any.extract_Value();
					} catch (Exception e) {
						// look at the connection, if connection is dead ; exit
						// if connection is okay, notify the client of the error but continue executing
					}
					
					if(returnMessage.size()!=0) {
						for (String s : returnMessage) {
							System.out.println(s);
						}
						System.out.println();
						clientLogger.info("Message showed up."+"\n");
					}else {
						System.out.println("No record for such event type.");
						clientLogger.info("No record for such event type."+"\n");
					}
					break;
				}
				case 4:{					
					System.out.println("Now performing: Book event for a customer.");
					System.out.println("Please enter customer ID: (format example: TORC2345)");
					String customerID = sc.nextLine().trim().toUpperCase();
					
					String managerCity = userID.substring(0,3);
					String customerCity = customerID.substring(0,3);
					if (!managerCity.equals(customerCity)) {
						System.out.println("A manager can only operate for a customer in your own city.");
						break;
					}	
					
					System.out.println("Please enter event ID: (format example: MTLE100519)");
					String eventID = sc.nextLine().trim().toUpperCase();
					System.out.println("Please enter event type: Conferences, Seminars, TradeShows");
					String eventType = sc.nextLine().trim();		
					
					if ((!eventType.equals("Conferences")) && (!eventType.equals("Seminars") ) && (!eventType.equals("TradeShows"))) {
						System.out.println("Event type should only be one of the following 3: Conferences, Seminars, or TradeShows.");
						break;
					}
					
					ArrayList<String> returnMessage = new ArrayList<String>();

					clientLogger.info("book event for a customer : manager id: "+userID+" customer id: "+customerID+" event id: "+eventID+" event type: "+eventType+"\n");

					try {			
						any = obj.bookEvent(customerID, eventID, eventType);
						returnMessage = (ArrayList<String>)any.extract_Value();
					} catch (Exception e) {
						clientLogger.warning(e.getMessage()+"\n");
					}
					
					if(returnMessage.get(0).equals("NoExist")) {
						System.out.println("Fail. The event you attempt to book doesn't exist.");
						clientLogger.info("Fail. The event you attempt to book doesn't exist."+"\n");
					}else if (returnMessage.get(0).equals("Full")){
						System.out.println("Fail. This event is fully booked.");
						clientLogger.info("Fail. This event is fully booked."+"\n");
					} else if (returnMessage.get(0).trim().equals("Success")) {
						System.out.println("You have successfully booked a space in:  \n"
								+ "Event type: " + eventType + "; Event ID: " + eventID + ".");
						clientLogger.info("You have successfully booked a space in:  \n"
								+ "Event type: " + eventType + "; Event ID: " + eventID + "."+"\n");
					} else if (returnMessage.get(0).equals("NotUnique")) {
						System.out.println("Fail. A customer can not book more than one event with the same event id and same event type.");
						clientLogger.info("Fail. A customer can not book more than one event with the same event id and same event type."+"\n");
					} else if (returnMessage.get(0).equals("Exceed3LimitInOtherCity")) {
						System.out.println("Fail. A customer can only book at most 3 events from other cities overall in a month.");
						clientLogger.info("Fail. A customer can only book at most 3 events from other cities overall in a month."+"\n");
					} 
					break;
				}
				case 5:{ //done this case of manager
					System.out.println("Now performing: Get booking schedule of a customer.");
					System.out.println("Please enter customer ID: (format example: TORC2345)");
					String customerID = sc.nextLine().trim().toUpperCase();
					
					String managerCity = userID.substring(0,3);
					String customerCity = customerID.substring(0,3);
					if (!managerCity.equals(customerCity)) {
						System.out.println("A manager can only operate for a customer in your own city.");
						break;
					}	
					
					ArrayList<String> returnMessage = new ArrayList<String>();

					clientLogger.info("show booking schedule for customer : manager id: "+userID+" customer id: "+customerID+"\n");

					//receive ArrayList<String> of info in all 3 cities. Elements like CTORE100519, need to decode, C means Conferences
					any = obj.getBookingSchedule(customerID);
					returnMessage = (ArrayList<String>)any.extract_Value();
					if (returnMessage.size()==0) {
						System.out.println("There is no booking record for customer " + customerID + ".");
						clientLogger.info("There is no booking record for customer " + customerID + "."+"\n");
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
						clientLogger.info("information showed"+"\n");
					}
					
					break;
				}
				case 6:{
					System.out.println("Now performing: Cancel event of a customer.");
					System.out.println("Please enter customer ID: (format example: TORC2345)");
					String customerID = sc.nextLine().trim().toUpperCase();
					
					String managerCity = userID.substring(0,3);
					String customerCity = customerID.substring(0,3);
					if (!managerCity.equals(customerCity)) {
						System.out.println("A manager can only operate for a customer in your own city.");
						break;
					}	
					
					System.out.println("Please enter event ID: (format example: MTLE100519)");
					String eventID = sc.nextLine().trim().toUpperCase();
					System.out.println("Please enter event type: Conferences, Seminars, TradeShows");
					String eventType = sc.nextLine().trim();

					if ((!eventType.equals("Conferences")) && (!eventType.equals("Seminars") ) && (!eventType.equals("TradeShows"))) {
						System.out.println("Event type should only be one of the following 3: Conferences, Seminars, or TradeShows.");
						break;
					}
					
					clientLogger.info("manager cancel event for customer: manager id: "+userID+" customer id: "+customerID+"\n");

					String returnMessage = "";
					try {
						returnMessage = obj.cancelEvent(customerID, eventID, eventType).trim();
					} catch (Exception e) {
						clientLogger.warning(e.getMessage()+"\n");
					}
					returnMessage=returnMessage.trim();
					if (returnMessage.equals("Success")) {
						System.out.println("Successfully cancelled a space of customer " + customerID + " in event type: " + eventType + " with event ID " + eventID);
						clientLogger.info("Successfully cancelled a space of customer " + customerID + " in event type: " + eventType + " with event ID " + eventID+"\n");
					} else if (returnMessage.equals("EventNotExist")) {
						System.out.println("This event of this type doesn't exist.");
						clientLogger.info("This event of this type doesn't exist."+"\n");
					} else if (returnMessage.equals("CustomerNeverBooked")) {
						System.out.println("This customer doesn't exist in the database.");
						clientLogger.info("This customer doesn't exist in the database."+"\n");
					} else if (returnMessage.equals("ThisCustomerHasNotBookedThis")) {
						System.out.println("This customer has never booked this event.");
						clientLogger.info("This customer has never booked this event."+"\n");
					} else if (returnMessage.equals("Capacity Error")) {
						System.out.println("There is something wrong in the capacity record.");
						clientLogger.info("There is something wrong in the capacity record."+"\n");
					} else if (returnMessage.equals("SuccessButNoSuchCustomerIncBookingOtherCity")) {
						System.out.println("Successfully cancelled in the target city, but NoSuchCustomerIncBookingOtherCity.");
						clientLogger.info("Successfully cancelled in the target city, but NoSuchCustomerIncBookingOtherCity."+"\n");
					} else if (returnMessage.equals("SuccessButNoSuchMonthIncBookingOtherCity")) {
						System.out.println("Successfully cancelled in the target city, but NoSuchMonthIncBookingOtherCity.");
						clientLogger.info("Successfully cancelled in the target city, but NoSuchMonthIncBookingOtherCity."+"\n");
					} else if (returnMessage.equals("SuccessButWrongNumberOfBookingIncBookingOtherCity")) {
						System.out.println("Successfully cancelled in the target city, but WrongNumberOfBookingIncBookingOtherCity.");
						clientLogger.info("Successfully cancelled in the target city, but WrongNumberOfBookingIncBookingOtherCity."+"\n");
					} else if (returnMessage.equals("SuccessUpdatedAllRecords")) {
						System.out.println("Successfully cancelled a space of customer " + customerID + " in event type: " + eventType + " with event ID " + eventID + "in target city");
						clientLogger.info("Successfully cancelled a space of customer " + customerID + " in event type: " + eventType + " with event ID " + eventID + "in target city"+"\n");
					} else {
						System.out.println("wrong message received.");
						clientLogger.info("wrong message received."+"\n");
					}
					break;
				}
				case 7:{ //to be modified the whole case
					System.out.println("Now performing: Swap two events for a customer.");
					System.out.println("Please enter customer ID: (format example: TORC2345)");
					String customerID = sc.nextLine().trim().toUpperCase();
					
					String managerCity = userID.substring(0,3);
					String customerCity = customerID.substring(0,3);
					if (!managerCity.equals(customerCity)) {
						System.out.println("A manager can only operate for a customer in your own city.");
						break;
					}	

					System.out.println("Please enter old event ID: (format example: MTLE100519)");
					String oldEventID = sc.nextLine().trim().toUpperCase();
					System.out.println("Please enter old event type: Conferences, Seminars, TradeShows");
					String oldEventType = sc.nextLine().trim();
					if ((!oldEventType.equals("Conferences")) && (!oldEventType.equals("Seminars") ) && (!oldEventType.equals("TradeShows"))) {
						System.out.println("Event type should only be one of the following 3: Conferences, Seminars, or TradeShows.");
						break;
					}
					
					System.out.println("Please enter new event ID: (format example: MTLE100519)");
					String newEventID = sc.nextLine().trim().toUpperCase();
					System.out.println("Please enter new event type: Conferences, Seminars, TradeShows");
					String newEventType = sc.nextLine().trim();
					if ((!newEventType.equals("Conferences")) && (!newEventType.equals("Seminars") ) && (!newEventType.equals("TradeShows"))) {
						System.out.println("Event type should only be one of the following 3: Conferences, Seminars, or TradeShows.");
						break;
					}
					
					clientLogger.info("manager swap two events for customer: manager id: "+userID+" customer id: "+customerID+"\n");
					
					ArrayList<String> returnMessage = new ArrayList<String>();
					try {
						any = obj.swapEvent(customerID, newEventID, newEventType, oldEventID, oldEventType);
						returnMessage = (ArrayList<String>)any.extract_Value();
					} catch (Exception e) {
						clientLogger.warning(e.getMessage()+"\n");
					}
					
					String messageBookResult = returnMessage.get(0).trim();
					String messageCancelResult = returnMessage.get(1).trim();
					
					if (messageBookResult.equals("IdenticalEvents")) {
						System.out.println("You are trying to swap two identical events, no action is taken.");	
					} else {
						if(messageBookResult.equals("NoExist")) {
							System.out.println("Fail. The new event you attempt to book doesn't exist.");
							clientLogger.info("Fail. The new event you attempt to book doesn't exist."+"\n");
						}else if (messageBookResult.equals("Full")){
							System.out.println("Fail. This new event is fully booked.");
							clientLogger.info("Fail. This new event is fully booked."+"\n");
						} else if (messageBookResult.equals("NotUnique")) {
							System.out.println("Fail. You have booked the new event in the past. A customer can not book more than one event with the same event id and same event type.");
							clientLogger.info("Fail. A customer can not book more than one event with the same event id and same event type."+"\n");
						} else if (messageBookResult.equals("Exceed3LimitInOtherCity")) {
							System.out.println("Fail. A customer can only book at most 3 events from other cities overall in a month.");
							clientLogger.info("Fail. A customer can only book at most 3 events from other cities overall in a month."+"\n");
						} else if (messageBookResult.trim().equals("Success")) { // if book succeed							
							if (messageCancelResult.equals("Success")) {
								System.out.println("Successfully swapped two events for customer " + customerID 
										+ ", old event ID: " + oldEventID + ", old event type: " + oldEventType 
										+ ", new event ID: " + newEventID + ", new event type: " + newEventType);
								clientLogger.info("Successfully swapped two events for customer " + customerID 
										+ ", old event ID: " + oldEventID + ", old event type: " + oldEventType 
										+ ", new event ID: " + newEventID + ", new event type: " + newEventType + "\n");
							} else if (messageCancelResult.equals("EventNotExist")) {
								System.out.println("The old event you want to cancel doesn't exist.");
								clientLogger.info("The old event you want to cancel doesn't exist."+"\n");
							} else if (messageCancelResult.equals("CustomerNeverBooked")) {
								System.out.println("This customer never booked any event, can't cancel.");
								clientLogger.info("This customer never booked any event, can't cancel."+"\n");
							} else if (messageCancelResult.equals("ThisCustomerHasNotBookedThis")) {
								System.out.println("This customer has never booked the old event.");
								clientLogger.info("This customer has never booked the old event."+"\n");
							} else if (messageCancelResult.equals("Capacity Error")) {
								System.out.println("There is something wrong in the capacity record of the old event.");
								clientLogger.info("There is something wrong in the capacity record of the old event."+"\n");
							} else if (messageCancelResult.equals("SuccessButNoSuchCustomerIncBookingOtherCity")) {
								System.out.println("Successfully swapped. But for cancelling, NoSuchCustomerIncBookingOtherCity.");
								clientLogger.info("Successfully swapped. But for cancelling, NoSuchCustomerIncBookingOtherCity."+"\n");
							} else if (messageCancelResult.equals("SuccessButNoSuchMonthIncBookingOtherCity")) {
								System.out.println("Successfully swapped. But for cancelling, NoSuchMonthIncBookingOtherCity.");
								clientLogger.info("Successfully swapped. But for cancelling, NoSuchMonthIncBookingOtherCity."+"\n");
							} else if (messageCancelResult.equals("SuccessButWrongNumberOfBookingIncBookingOtherCity")) {
								System.out.println("Successfully swapped. But for cancelling, WrongNumberOfBookingIncBookingOtherCity.");
								clientLogger.info("Successfully swapped. But for cancelling, WrongNumberOfBookingIncBookingOtherCity."+"\n");
							} else {
								System.out.println("wrong message received.");
								clientLogger.info("wrong message received."+"\n");
							}
						} 
					}
					break;
				}
				case 0:
					break;
				default:
					System.out.println("Input is wrong, please try again!");
					clientLogger.info("Input is wrong, please try again!"+"\n");
			}	
		}
		while (user_input != 0);	
	}

	private  void customerOperate() throws RemoteException{
		int user_input;
		Scanner sc = new Scanner(System.in);
		Any any;
		do {
			System.out.println(
					"\nCurrent User: Customer " + userID + "\n"
					+"Please input a number to select action: \n"
					+"1. Book an event \n"
					+"2. Get your booking schedule in all cities  \n"
					+"3. Cancel an event of yours \n"
					+"4. Swap two events for you \n"
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
					
					if ((!eventType.equals("Conferences")) && (!eventType.equals("Seminars") ) && (!eventType.equals("TradeShows"))) {
						System.out.println("Event type should only be one of the following 3: Conferences, Seminars, or TradeShows.");
						break;
					}

					clientLogger.info("customer book event: customer id: "+userID+" event id: "+eventID+" event type: "+eventType+"\n");

					ArrayList<String> returnMessage = new ArrayList<String>();
					try {			
						any = obj.bookEvent(userID, eventID, eventType);
						returnMessage = (ArrayList<String>)any.extract_Value();
					} catch (Exception e) {
						clientLogger.warning(e.getMessage()+"\n");
					}
					
					if(returnMessage.get(0).equals("NoExist")) {
						System.out.println("Fail. The event you attampt to book doesn't exist.");
						clientLogger.info("Fail. The event you attampt to book doesn't exist."+"\n");
					}else if (returnMessage.get(0).equals("Full")){
						System.out.println("Fail. This event is fully booked.");
						clientLogger.info("Fail. This event is fully booked."+"\n");
					} else if (returnMessage.get(0).equals("Success")) {
						System.out.println("You have successfully booked a space in:  \n"
								+ "Event type: " + eventType + "; Event ID: " + eventID + ".");
						clientLogger.info("You have successfully booked a space in:  \n"
								+ "Event type: " + eventType + "; Event ID: " + eventID + "."+"\n");
					} else if (returnMessage.get(0).equals("NotUnique")) {
						System.out.println("Fail. A customer can not book more than one event with the same event id and same event type.");
						clientLogger.info("Fail. A customer can not book more than one event with the same event id and same event type."+"\n");
					} else if (returnMessage.get(0).equals("Exceed3LimitInOtherCity")) {
						System.out.println("Fail. A customer can only book at most 3 events from other cities overall in a month.");
						clientLogger.info("Fail. A customer can only book at most 3 events from other cities overall in a month."+"\n");
					} 
					break;
				}
				case 2:{ //done this case of customer
					System.out.println("Now performing: Get your booking schedule in all cities.");					
					ArrayList<String> returnMessage = new ArrayList<String>();

					clientLogger.info("get booking schedule: customer id: "+userID+"\n");

					//receive ArrayList<String> of info in all 3 cities. Elements like CTORE100519, need to decode, C means Conferences
					any = obj.getBookingSchedule(userID);
					returnMessage = (ArrayList<String>)any.extract_Value();
					System.out.println("Now printing booking schedule for customer " + userID + ":");
					System.out.printf("%-15s %-18s %-15s", "City", "Event Type", "Event ID");
					System.out.println();

					clientLogger.info("information showed"+"\n");
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
					
					if ((!eventType.equals("Conferences")) && (!eventType.equals("Seminars") ) && (!eventType.equals("TradeShows"))) {
						System.out.println("Event type should only be one of the following 3: Conferences, Seminars, or TradeShows.");
						break;
					}

					clientLogger.info("cancel an event: customer id:"+userID+" event id: "+eventID+" event type: "+eventType+"\n");

					String returnMessage = "";
					try {
						returnMessage = obj.cancelEvent(userID, eventID, eventType);
					} catch (Exception e) {
						clientLogger.warning(e.getMessage()+"\n");
					}
					
					if (returnMessage.equals("Success")) {
						System.out.println("Successfully cancelled a space for you in event type: " + eventType + " with event ID " + eventID);
						clientLogger.info("Successfully cancelled a space for you in event type: " + eventType + " with event ID " + eventID+"\n");
					} else if (returnMessage.equals("EventNotExist")) {
						System.out.println("This event of this type doesn't exist.");
						clientLogger.info("This event of this type doesn't exist."+"\n");
					} else if (returnMessage.equals("CustomerNeverBooked")) {
						System.out.println("You don't exist in the database.");
						clientLogger.info("You don't exist in the database."+"\n");
					} else if (returnMessage.equals("ThisCustomerHasNotBookedThis")) {
						System.out.println("You have never booked this event.");
						clientLogger.info("You have never booked this event."+"\n");
					} else if (returnMessage.equals("Capacity Error")) {
						System.out.println("There is something wrong in the capacity record.");
						clientLogger.info("There is something wrong in the capacity record."+"\n");
					} else if (returnMessage.equals("SuccessButNoSuchCustomerIncBookingOtherCity")) {
						System.out.println("Successfully cancelled in the target city, but NoSuchCustomerIncBookingOtherCity.");
						clientLogger.info("Successfully cancelled in the target city, but NoSuchCustomerIncBookingOtherCity."+"\n");
					} else if (returnMessage.equals("SuccessButNoSuchMonthIncBookingOtherCity")) {
						System.out.println("Successfully cancelled in the target city, but NoSuchMonthIncBookingOtherCity.");
						clientLogger.info("Successfully cancelled in the target city, but NoSuchMonthIncBookingOtherCity."+"\n");
					} else if (returnMessage.equals("SuccessButWrongNumberOfBookingIncBookingOtherCity")) {
						System.out.println("Successfully cancelled in the target city, but WrongNumberOfBookingIncBookingOtherCity.");
						clientLogger.info("Successfully cancelled in the target city, but WrongNumberOfBookingIncBookingOtherCity."+"\n");
					} else if (returnMessage.equals("SuccessUpdatedAllRecords")) {
						System.out.println("Successfully cancelled a space for you in event type: " + eventType + " with event ID " + eventID + "in target city");
						clientLogger.info("Successfully cancelled a space for you in event type: " + eventType + " with event ID " + eventID + "in target city"+"\n");
					} else {
						System.out.println("wrong message received.");
						clientLogger.info("wrong message received."+"\n");
					}
					break;
				}
				case 4:{
					System.out.println("Now performing: Swap two events for you.");				
					String customerID = userID;		

					System.out.println("Please enter old event ID: (format example: MTLE100519)");
					String oldEventID = sc.nextLine().trim().toUpperCase();
					System.out.println("Please enter old event type: Conferences, Seminars, TradeShows");
					String oldEventType = sc.nextLine().trim();
					if ((!oldEventType.equals("Conferences")) && (!oldEventType.equals("Seminars") ) && (!oldEventType.equals("TradeShows"))) {
						System.out.println("Event type should only be one of the following 3: Conferences, Seminars, or TradeShows.");
						break;
					}
					
					System.out.println("Please enter new event ID: (format example: MTLE100519)");
					String newEventID = sc.nextLine().trim().toUpperCase();
					System.out.println("Please enter new event type: Conferences, Seminars, TradeShows");
					String newEventType = sc.nextLine().trim();
					if ((!newEventType.equals("Conferences")) && (!newEventType.equals("Seminars") ) && (!newEventType.equals("TradeShows"))) {
						System.out.println("Event type should only be one of the following 3: Conferences, Seminars, or TradeShows.");
						break;
					}
					
					clientLogger.info("manager swap two events for customer: manager id: "+userID+" customer id: "+customerID+"\n");
					
					ArrayList<String> returnMessage = new ArrayList<String>();
					try {
						any = obj.swapEvent(customerID, newEventID, newEventType, oldEventID, oldEventType);
						returnMessage = (ArrayList<String>)any.extract_Value();
					} catch (Exception e) {
						clientLogger.warning(e.getMessage()+"\n");
					}
					
					String messageBookResult = returnMessage.get(0).trim();
					String messageCancelResult = returnMessage.get(1).trim();
					
					if (messageBookResult.equals("IdenticalEvents")) {
						System.out.println("You are trying to swap two identical events, no action is taken.");	
					} else {
						if(messageBookResult.equals("NoExist")) {
							System.out.println("Fail. The new event you attempt to book doesn't exist.");
							clientLogger.info("Fail. The new event you attempt to book doesn't exist."+"\n");
						}else if (messageBookResult.equals("Full")){
							System.out.println("Fail. This new event is fully booked.");
							clientLogger.info("Fail. This new event is fully booked."+"\n");
						} else if (messageBookResult.equals("NotUnique")) {
							System.out.println("Fail. You have booked the new event in the past. A customer can not book more than one event with the same event id and same event type.");
							clientLogger.info("Fail. A customer can not book more than one event with the same event id and same event type."+"\n");
						} else if (messageBookResult.equals("Exceed3LimitInOtherCity")) {
							System.out.println("Fail. A customer can only book at most 3 events from other cities overall in a month.");
							clientLogger.info("Fail. A customer can only book at most 3 events from other cities overall in a month."+"\n");
						} else if (messageBookResult.trim().equals("Success")) { // if book succeed							
							if (messageCancelResult.equals("Success")) {
								System.out.println("Successfully swapped two events for customer " + customerID 
										+ ", old event ID: " + oldEventID + ", old event type: " + oldEventType 
										+ ", new event ID: " + newEventID + ", new event type: " + newEventType);
								clientLogger.info("Successfully swapped two events for customer " + customerID 
										+ ", old event ID: " + oldEventID + ", old event type: " + oldEventType 
										+ ", new event ID: " + newEventID + ", new event type: " + newEventType + "\n");
							} else if (messageCancelResult.equals("EventNotExist")) {
								System.out.println("The old event you want to cancel doesn't exist.");
								clientLogger.info("The old event you want to cancel doesn't exist."+"\n");
							} else if (messageCancelResult.equals("CustomerNeverBooked")) {
								System.out.println("This customer never booked any event, can't cancel.");
								clientLogger.info("This customer never booked any event, can't cancel."+"\n");
							} else if (messageCancelResult.equals("ThisCustomerHasNotBookedThis")) {
								System.out.println("This customer has never booked the old event.");
								clientLogger.info("This customer has never booked the old event."+"\n");
							} else if (messageCancelResult.equals("Capacity Error")) {
								System.out.println("There is something wrong in the capacity record of the old event.");
								clientLogger.info("There is something wrong in the capacity record of the old event."+"\n");
							} else if (messageCancelResult.equals("SuccessButNoSuchCustomerIncBookingOtherCity")) {
								System.out.println("Successfully swapped. But for cancelling, NoSuchCustomerIncBookingOtherCity.");
								clientLogger.info("Successfully swapped. But for cancelling, NoSuchCustomerIncBookingOtherCity."+"\n");
							} else if (messageCancelResult.equals("SuccessButNoSuchMonthIncBookingOtherCity")) {
								System.out.println("Successfully swapped. But for cancelling, NoSuchMonthIncBookingOtherCity.");
								clientLogger.info("Successfully swapped. But for cancelling, NoSuchMonthIncBookingOtherCity."+"\n");
							} else if (messageCancelResult.equals("SuccessButWrongNumberOfBookingIncBookingOtherCity")) {
								System.out.println("Successfully swapped. But for cancelling, WrongNumberOfBookingIncBookingOtherCity.");
								clientLogger.info("Successfully swapped. But for cancelling, WrongNumberOfBookingIncBookingOtherCity."+"\n");
							} else {
								System.out.println("wrong message received.");
								clientLogger.info("wrong message received."+"\n");
							}
						} 
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
}
