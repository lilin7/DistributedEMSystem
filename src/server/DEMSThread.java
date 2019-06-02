package server;

import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.rmi.registry.LocateRegistry;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class DEMSThread extends Thread{
    private DEMSImpl stub;
    private int localUDPport;

    public DEMSThread(DEMSImpl impl,int localudpport){
        this.stub = impl;
        this.localUDPport = localudpport;
    }

    @Override
    public void run(){
        DatagramSocket aSocket = null;
        try {
            aSocket = new DatagramSocket(localUDPport);
            byte[] buffer = new byte[1000];
            System.out.println("Server UDP Listen Started");
            while (true) {
            	// receive request (can only be String) from other city to act in this city
                DatagramPacket request = new DatagramPacket(buffer, buffer.length); 
                aSocket.receive(request); //e.g. TORC1234 getBookingSchedule
                System.out.println("Request received from client: " + new String(request.getData()));
                String re = "";
                String requestToString = new String(request.getData()); //receive message from source city
                String[] requestSplit = requestToString.split("\\s+");
                String action = requestSplit[0]; // e.g. method name, "getEventBookingSchedule"
                
                //TODO:need change, can split request to get more parameters and finish the task blow
                if (requestToString.equals("listEventAvailability")){
                    //re = stub.listEventAvailabilityForUDP(param1, param2...);
                } else if (requestToString.equals("bookEvent")){
                	String customerID = requestSplit[0];
                    //action = stub.bookEventForUDP(customerID);
                	// turn ArrayList<String> action to a String, assign to re
                	re = "";
                	
                	
                } else if (action.equals("getBookingSchedule")){  //done inside this else if condition
                	// String[] requestSplit length =2, elements are: "getBookingSchedule", customerID 
                	String customerID = requestSplit[1];
                	//get an ArrayList<String>, elements are: CTORA100519, CTORE100519, ... (first letter is event type)
                	if (stub.getBookingScheduleForUDP(customerID) != null) { //if this customer has record in that city
                		ArrayList<String> eventTypeAndIDAL = stub.getBookingScheduleForUDP(customerID);
                    	
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
                }else if(requestToString.equals("cancelEvent")){
                    //re = stub.cancelEventForUDP(param1, param2...);
                }else{
                    //errormassage
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
            if (aSocket != null)
                aSocket.close();
        }
    }
}
