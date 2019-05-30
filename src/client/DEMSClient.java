package client;
import java.io.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
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
					"Current User: Manager " + userID + "\n"
					+"Please input a number to select action:"
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
					
					//handle RMI exception PER action, and try to bounce back for a better error handling.
					try {
						result = obj.addEvent(userID, eventID, eventType, bookingCapacity);
					} catch (java.rmi.RemoteException e) {
						result = false;
					}
					
					if(result) {
						System.out.println("Successfully add an event. \n Event ID: " + eventID +"; "
								+ "Event type: " + eventType + "; Booking capacity: " + bookingCapacity);
						//TODO: log
					}else {
						System.out.println("Failed in adding an event");
						//TODO:log
					}
					break;
				}
				case 2:{
					System.out.println("Now performing: Remove an event.");
					System.out.println("Please enter event ID: (format example: MTLE100519)");
					String eventID = sc.nextLine().trim().toUpperCase();
					System.out.println("Please enter event Type: C for Conference, T for Trade shows, S for Seminars");
					String eventType = sc.nextLine().trim().toUpperCase();
					
					try {
						result = obj.removeEvent(userID, eventID, eventType);
					} catch (java.rmi.RemoteException e) {
						result = false;
					}
					
					if(result) {
						System.out.println("Successfully remove an event. \n "
								+ "Event ID: " + eventID + "Event type: " + eventType);
						//TODO: add log for this user
					}else {
						System.out.println("Failed in removing an event");
						//TODO: add log for this user
					}
					break;
				}
				case 3:{
					System.out.println("Now performing: List event availability.");
					System.out.println("Please enter event Type: C for Conference, T for Trade shows, S for Seminars");
					String eventType = sc.nextLine().trim().toUpperCase();	
					
					try {
						result = obj.listEventAvailability(userID, eventType);
					} catch (java.rmi.RemoteException e) {
						// look at the connection, if connection is dead ; exit
						// if connection is okay, notify the client of the error but continue executing
						result = false;
					}
					
					if(result) {
						System.out.println("Successfully list event availability.");
						//TODO: add log for this user
					}else {
						System.out.println("Failed in listing event availability");
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
					System.out.println("Please enter event Type: C for Conference, T for Trade shows, S for Seminars");
					String eventType = sc.nextLine().trim().toUpperCase();
									
					result = obj.bookEvent(customerID, eventID, eventType);
					
					if(result) {
						System.out.println("Success");
						//TODO: add log for this user
					}else {
						System.out.println("Fail");
						//TODO: add log for this user
					}
					break;
				}
				case 5:{
					System.out.println("Now performing: Get booking schedule of a customer.");
					System.out.println("Please enter customer ID: (format example: TORC2345)");
					String customerID = sc.nextLine().trim().toUpperCase();
					
					result = obj.getBookingSchedule(customerID);
					
					if(result) {
						System.out.println("Success");
						//TODO: add log for this user
					}else {
						System.out.println("Fail");
						//TODO: add log for this user
					}
					break;
				}
				case 6:{
					System.out.println("Now performing: Cancel event of a customer.");
					System.out.println("Please enter customer ID: (format example: TORC2345)");
					String customerID = sc.nextLine().trim().toUpperCase();
					System.out.println("Please enter event ID: (format example: MTLE100519)");
					String eventID = sc.nextLine().trim().toUpperCase();
					
					try {
						result = obj.cancelEvent(customerID, eventID);
					} catch (java.rmi.RemoteException e) {
						result = false;
					}
					
					if(result) {
						System.out.println("Success");
						//TODO: add log for this user
					}else {
						System.out.println("Fail");
						//TODO: add log for this user
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
					"Current User: Customer " + userID + "\n"
					+"Please input a number to select action:"
					+"1. Book an event \n"
					+"2. Get your booking schedule in all cities  \n"
					+"3. Cancel an event of yours \n"
					+"0. Quit"
					);

			user_input = Integer.parseInt(sc.nextLine().trim());
			boolean result;
			
			switch (user_input) {
				case 1:{					
					System.out.println("Now performing: Book an event.");
					System.out.println("Please enter event ID: (format example: MTLE100519)");
					String eventID = sc.nextLine().trim().toUpperCase();
					System.out.println("Please enter event Type: C for Conference, T for Trade shows, S for Seminars");
					String eventType = sc.nextLine().trim().toUpperCase();
							
					try {
						result = obj.bookEvent(userID, eventID, eventType);
					} catch (java.rmi.RemoteException e) {
						result = false;
					}
					
					if(result) {
						System.out.println("Success");
						//TODO:log
					}
						
					else {
						System.out.println("Fail");
						//TODO:log
					}
					break;
				}
				case 2:{
					System.out.println("Now performing: Get your booking schedule in all cities.");
					
					try {
						result = obj.getBookingSchedule(userID);
					} catch (java.rmi.RemoteException e) {
						result = false;
					}
					
					if(result) {
						System.out.println("Success");
						//TODO: add log for this user
					}else {
						System.out.println("Fail");
						//TODO: add log for this user
					}
					break;
				}
				case 3:{
					System.out.println("Now performing: Cancel an event of yours.");
					System.out.println("Please enter event ID: (format example: MTLE100519)");
					String eventID = sc.nextLine().trim().toUpperCase();
					
					try {
						result = obj.cancelEvent(userID, eventID);
					} catch (java.rmi.RemoteException e) {
						result = false;
					}
					
					if(result) {
						System.out.println("Success");
						//TODO: add log for this user
					}else {
						System.out.println("Fail");
						//TODO: add log for this user
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
