package client;

import java.io.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Scanner;

import server.DEMSInterface;

public class SynchronizedMethodThread implements Runnable{

    private String customerID;
    private String eventID;
    private String eventType;
    private DEMSInterface obj;
    private int MTLportNumber = 1028;
    private String ServerName = "MTLServer";

    public SynchronizedMethodThread(String customerID, String eventID, String eventType){
        this.customerID = customerID;
        this.eventID = eventID;
        this.eventType = eventType;
    }


    @Override
    public void run() {
        try{
            ArrayList<String> returnMessage = new ArrayList<String>();
            Registry registry = LocateRegistry.getRegistry(MTLportNumber);
            obj = (DEMSInterface) registry.lookup(ServerName);
            returnMessage = obj.bookEvent(customerID, eventID, eventType);

            if(returnMessage.get(0).equals("NoExist")) {
                System.out.println("Fail. The event you attempt to book doesn't exist.");

            }else if (returnMessage.get(0).equals("Full")){
                System.out.println("customer: "+customerID+" Fail. This event is fully booked.");

            } else if (returnMessage.get(0).trim().equals("Success")) {
                System.out.println(customerID+" have successfully booked a space in:  \n"
                        + "Event type: " + eventType + "; Event ID: " + eventID + ".");
            } else if (returnMessage.get(0).equals("NotUnique")) {
                System.out.println("Fail. A customer can not book more than one event with the same event id and same event type.");
            } else if (returnMessage.get(0).equals("Exceed3LimitInOtherCity")) {
                System.out.println("Fail. A customer can only book at most 3 events from other cities overall in a month.");
            }

        }catch (Exception e){
            System.out.println(e);
        }finally {

        }
    }
}
