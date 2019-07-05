package client;

import java.io.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Scanner;

import server.DEMSInterface;



import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import DEMS_CORBA.DEMSInterfaceCorba;
import DEMS_CORBA.DEMSInterfaceCorbaHelper;

public class SynchronizedMethodThread implements Runnable{

    private String customerID;
    private String oldEventID;
    private String oldEventType;
    private String newEventID;
    private String newEventType;
    private String[] args;
    private DEMSInterfaceCorba obj;
    // private DEMSInterface obj;
    //private int MTLportNumber = 1028;
    private String ServerName = "MTLServer";

    public SynchronizedMethodThread(String customerID, String oldEventID, String oldEventType,String newEventID,String newEventType,String[] args){
        this.customerID = customerID;
        this.oldEventID = oldEventID;
        this.oldEventType = oldEventType;
        this.newEventID = newEventID;
        this.newEventType = newEventType;
        this.args = args;
    }


    @Override
    public void run() {
        try{
            Any any;
            ArrayList<String> returnMessage = new ArrayList<String>();
            //Registry registry = LocateRegistry.getRegistry(MTLportNumber);
            //obj = (DEMSInterface) registry.lookup(ServerName);
            ORB orb = ORB.init(args, null);
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
            obj = (DEMSInterfaceCorba) DEMSInterfaceCorbaHelper.narrow(ncRef.resolve_str(ServerName));

            any = obj.swapEvent(customerID, newEventID, newEventType ,oldEventID,oldEventType);
            returnMessage = (ArrayList<String>)any.extract_Value();
            String messageBookResult = returnMessage.get(0).trim();
            String messageCancelResult = returnMessage.get(1).trim();
            if (messageBookResult.equals("IdenticalEvents")) {
                System.out.println("You are trying to swap two identical events, no action is taken.");
            } else {
                if(messageBookResult.equals("NoExist")) {
                    System.out.println("Fail. The new event you attempt to book doesn't exist.");

                }else if (messageBookResult.equals("Full")){
                    System.out.println("Fail. This new event is fully booked.");

                } else if (messageBookResult.equals("NotUnique")) {
                    System.out.println("Fail. You have booked the new event in the past. A customer can not book more than one event with the same event id and same event type.");

                } else if (messageBookResult.equals("Exceed3LimitInOtherCity")) {
                    System.out.println("Fail. A customer can only book at most 3 events from other cities overall in a month.");

                } else if (messageBookResult.trim().equals("Success")) { // if book succeed
                    if (messageCancelResult.equals("Success")) {
                        System.out.println("Successfully swapped two events for customer " + customerID
                                + ", old event ID: " + oldEventID + ", old event type: " + oldEventType
                                + ", new event ID: " + newEventID + ", new event type: " + newEventType);

                    } else if (messageCancelResult.equals("EventNotExist")) {
                        System.out.println("The old event you want to cancel doesn't exist.");

                    } else if (messageCancelResult.equals("CustomerNeverBooked")) {
                        System.out.println("This customer never booked any event, can't cancel.");

                    } else if (messageCancelResult.equals("ThisCustomerHasNotBookedThis")) {
                        System.out.println("This customer has never booked the old event.");

                    } else if (messageCancelResult.equals("Capacity Error")) {
                        System.out.println("There is something wrong in the capacity record of the old event.");

                    } else if (messageCancelResult.equals("SuccessButNoSuchCustomerIncBookingOtherCity")) {
                        System.out.println("Successfully swapped. But for cancelling, NoSuchCustomerIncBookingOtherCity.");

                    } else if (messageCancelResult.equals("SuccessButNoSuchMonthIncBookingOtherCity")) {
                        System.out.println("Successfully swapped. But for cancelling, NoSuchMonthIncBookingOtherCity.");

                    } else if (messageCancelResult.equals("SuccessButWrongNumberOfBookingIncBookingOtherCity")) {
                        System.out.println("Successfully swapped. But for cancelling, WrongNumberOfBookingIncBookingOtherCity.");

                    } else {
                        System.out.println("wrong message received.");

                    }
                }
            }

        }catch (Exception e){
            System.out.println(e);
        }finally {

        }
    }
}
