package DEMS_CORBA;


/**
* DEMS_CORBA/DEMSInterfaceCorbaOperations.java .
* ��IDL-to-Java ������ (����ֲ), �汾 "3.2"����
* ��F:/xuexi/corba/src/server/DEMSidl.idl
* 2019��6��26�� ������ ����05ʱ43��05�� EDT
*/

public interface DEMSInterfaceCorbaOperations 
{
  org.omg.CORBA.Any addEvent (String MID, String eventID, String eventType, int bookingCapacity);
  org.omg.CORBA.Any removeEvent (String MID, String eventID, String eventType);
  org.omg.CORBA.Any listEventAvailability (String MID, String eventType);
  org.omg.CORBA.Any bookEvent (String customerID, String eventID, String eventType);
  org.omg.CORBA.Any getBookingSchedule (String customerID);
  String cancelEvent (String customerID, String eventID, String eventType);
  org.omg.CORBA.Any swapEvent (String customerID, String newEventID, String newEventType, String oldEventID, String oldEventType);
} // interface DEMSInterfaceCorbaOperations
