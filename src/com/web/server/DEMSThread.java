package com.web.server;

import com.web.service.adaptorArrayList;
import com.web.service.impl.DEMSImplWeb;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class DEMSThread extends Thread{
    private DEMSImplWeb stub;
    private int localUDPport;
    private adaptorArrayList adaptor = new adaptorArrayList();

    public DEMSThread(DEMSImplWeb impl, int localudpport){
        this.stub = impl;
        this.localUDPport = localudpport;
    }

    @Override
    public void run(){
        DatagramSocket aSocket = null;
        try {
            aSocket = new DatagramSocket(localUDPport);          
            System.out.println("Server UDP Listen Started");
            while (true) {
            	// receive request (can only be String) from other city to act in this city
            	byte[] buffer = new byte[1000];
                DatagramPacket request = new DatagramPacket(buffer, buffer.length); 
                aSocket.receive(request); //e.g. TORC1234 getBookingSchedule
                System.out.println("Request received from com.web.client: " + new String(request.getData()));
                String re = "";
                String requestToString = new String(request.getData()); //receive message from source city
                String[] requestSplit = requestToString.split("\\s+");
                String action = requestSplit[0].trim(); // e.g. method name, "getEventBookingSchedule"
                              
                if (action.equals("listEventAvailability")){                	
                	String eventType = requestSplit[1].trim(); 
                	
                	StringBuffer sb = new StringBuffer();
                	
                	ConcurrentHashMap<String, ArrayList<Integer>> eventSubHashMap = stub.listEventAvailabilityForUDP(eventType);
                	if (eventSubHashMap.size()!=0) {
                		Set<String> keySet = eventSubHashMap.keySet();              	
                    	for (String s : keySet) { // each event ID in this HashMap
                			String eID = s.trim();
                			int totalCap = eventSubHashMap.get(s).get(0);
                			int bookedCap = eventSubHashMap.get(s).get(1);
                			int availableCap = totalCap-bookedCap;
                			String eachEventInfo = eID + " " + totalCap + " " + bookedCap + " ";
                			sb.append(eachEventInfo);           			
                		}
                    
                    	re = sb.toString().trim();
                	} else {
                		re = "";
                	}                	

                } else if (action.equals("bookEvent")){
                	String customerID = requestSplit[1];
                	String eventID = requestSplit[2];
                	String eventType = requestSplit[3];
                	              	
                	ArrayList<String> bookEventResult = adaptor.unmarshal(stub.bookEventForUDP(customerID, eventID, eventType));
                	re = bookEventResult.get(0);  //e.g. "Success" "NotUnique" "Exceed3LimitInOtherCity"       
                	
                } else if (action.equals("getBookingSchedule")){  //done inside this else if condition
                	// String[] requestSplit length =2, elements are: "getBookingSchedule", customerID 
                	String customerID = requestSplit[1].trim();
                	//String customerID = requestSplit[1].trim().substring(0, 8);
                	//get an ArrayList<String>, elements are: CTORA100519, CTORE100519, ... (first letter is event type)
                	              	
                	if (stub.getcBookingRecord().containsKey(customerID)) { //if this customer has record in that city
                		ArrayList<String> eventTypeAndIDAL = adaptor.unmarshal(stub.getBookingScheduleForUDP(customerID));
                    	
                    	StringBuffer sb = new StringBuffer(); //use StringBuffer to avoid creating too much String
                    	for (String s : eventTypeAndIDAL) {
                    		sb.append(s);
                    		sb.append(" ");
                    	}
                    	//change the info which need to be passed to one String, 
                    	//the original elements in ArrayList<String> are separated by space, and no space at end
                    	re = sb.toString().trim();                  	
                	} else { // if this customer doesn't have any record in that city
                		re = "";
                	}              	              	
                }else if(action.equals("cancelEvent")){
                	String customerID = requestSplit[1];
                	String eventID = requestSplit[2];
                	String eventType = requestSplit[3];
                	
                	re = stub.cancelEventForUDP(customerID, eventID, eventType).trim(); 
                	//e.g. "Success", "EventNotExist", "CustomerNeverBooked", "ThisCustomerHasNotBookedThis", "Capacity Error"
                	    	
                }else{
                    //error massage
                }
                
                //done booking or cancel
                byte [] me = re.getBytes();
                DatagramPacket reply = new DatagramPacket(me, re.length(), request.getAddress(), request.getPort());// reply packet ready
                aSocket.send(reply);// reply sent
            }
        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        } catch (Exception e) {
			e.printStackTrace();
		} finally {
            if (aSocket != null)
                aSocket.close();
        }
    }
}
