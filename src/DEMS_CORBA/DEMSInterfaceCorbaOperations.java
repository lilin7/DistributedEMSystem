package DEMS_CORBA;


/**
* DEMS_CORBA/DEMSInterfaceCorbaOperations.java .
* 由IDL-to-Java 编译器 (可移植), 版本 "3.2"生成
* 从F:/xuexi/corba/src/server/DEMSidl.idl
* 2019年6月26日 星期三 下午05时43分05秒 EDT
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
