package com.web.service;


import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

@WebService
@SOAPBinding(style = SOAPBinding.Style.RPC)

public interface DEMSInterfaceWeb {

    // port number need to start from 1025
    public final int PORT_MTL = 1028;
    public final int PORT_OTW = 1033;
    public final int PORT_TOR = 1029;

    public final int UDP_PORT_MTL = 5555;
    public final int UDP_PORT_OTW = 6666;
    public final int UDP_PORT_TOR = 7777;


    public String[] addEvent(String MID, String eventID, String eventType, int bookingCapacity);

    public String[] removeEvent(String MID, String eventID, String eventType);

    public String[] listEventAvailability(String MID, String eventType);

    public String[] bookEvent (String customerID,String eventID, String eventType);

    public String[] getBookingSchedule(String customerID);

    public String cancelEvent(String customerID, String eventID, String eventType);

    public String[] swapEvent(String customerID, String newEventID, String newEventType, String oldEventID, String oldEventType);

    public ConcurrentHashMap<String, ArrayList<Integer>> listEventAvailabilityForUDP(String eventType) throws Exception;

    public String[] bookEventForUDP(String customerID,String eventID,String eventType) throws Exception;

    public String[] getBookingScheduleForUDP(String customerID) throws Exception;

    public String cancelEventForUDP(String customerID, String eventID, String eventType) throws Exception;

}
