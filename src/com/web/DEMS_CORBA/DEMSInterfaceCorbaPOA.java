package com.web.DEMS_CORBA;


/**
* com.web.DEMS_CORBA/DEMSInterfaceCorbaPOA.java .
* 由IDL-to-Java 编译器 (可移植), 版本 "3.2"生成
* 从F:/xuexi/corba/src/com.web.server/DEMSidl.idl
* 2019年6月26日 星期三 下午05时43分05秒 EDT
*/

public abstract class DEMSInterfaceCorbaPOA extends org.omg.PortableServer.Servant
 implements com.web.DEMS_CORBA.DEMSInterfaceCorbaOperations, org.omg.CORBA.portable.InvokeHandler
{

  // Constructors

  private static java.util.Hashtable _methods = new java.util.Hashtable ();
  static
  {
    _methods.put ("addEvent", new java.lang.Integer (0));
    _methods.put ("removeEvent", new java.lang.Integer (1));
    _methods.put ("listEventAvailability", new java.lang.Integer (2));
    _methods.put ("bookEvent", new java.lang.Integer (3));
    _methods.put ("getBookingSchedule", new java.lang.Integer (4));
    _methods.put ("cancelEvent", new java.lang.Integer (5));
    _methods.put ("swapEvent", new java.lang.Integer (6));
  }

  public org.omg.CORBA.portable.OutputStream _invoke (String $method,
                                org.omg.CORBA.portable.InputStream in,
                                org.omg.CORBA.portable.ResponseHandler $rh)
  {
    org.omg.CORBA.portable.OutputStream out = null;
    java.lang.Integer __method = (java.lang.Integer)_methods.get ($method);
    if (__method == null)
      throw new org.omg.CORBA.BAD_OPERATION (0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);

    switch (__method.intValue ())
    {
       case 0:  // com.web.DEMS_CORBA/DEMSInterfaceCorba/addEvent
       {
         String MID = in.read_string ();
         String eventID = in.read_string ();
         String eventType = in.read_string ();
         int bookingCapacity = in.read_long ();
         org.omg.CORBA.Any $result = null;
         $result = this.addEvent (MID, eventID, eventType, bookingCapacity);
         out = $rh.createReply();
         out.write_any ($result);
         break;
       }

       case 1:  // com.web.DEMS_CORBA/DEMSInterfaceCorba/removeEvent
       {
         String MID = in.read_string ();
         String eventID = in.read_string ();
         String eventType = in.read_string ();
         org.omg.CORBA.Any $result = null;
         $result = this.removeEvent (MID, eventID, eventType);
         out = $rh.createReply();
         out.write_any ($result);
         break;
       }

       case 2:  // com.web.DEMS_CORBA/DEMSInterfaceCorba/listEventAvailability
       {
         String MID = in.read_string ();
         String eventType = in.read_string ();
         org.omg.CORBA.Any $result = null;
         $result = this.listEventAvailability (MID, eventType);
         out = $rh.createReply();
         out.write_any ($result);
         break;
       }

       case 3:  // com.web.DEMS_CORBA/DEMSInterfaceCorba/bookEvent
       {
         String customerID = in.read_string ();
         String eventID = in.read_string ();
         String eventType = in.read_string ();
         org.omg.CORBA.Any $result = null;
         $result = this.bookEvent (customerID, eventID, eventType);
         out = $rh.createReply();
         out.write_any ($result);
         break;
       }

       case 4:  // com.web.DEMS_CORBA/DEMSInterfaceCorba/getBookingSchedule
       {
         String customerID = in.read_string ();
         org.omg.CORBA.Any $result = null;
         $result = this.getBookingSchedule (customerID);
         out = $rh.createReply();
         out.write_any ($result);
         break;
       }

       case 5:  // com.web.DEMS_CORBA/DEMSInterfaceCorba/cancelEvent
       {
         String customerID = in.read_string ();
         String eventID = in.read_string ();
         String eventType = in.read_string ();
         String $result = null;
         $result = this.cancelEvent (customerID, eventID, eventType);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 6:  // com.web.DEMS_CORBA/DEMSInterfaceCorba/swapEvent
       {
         String customerID = in.read_string ();
         String newEventID = in.read_string ();
         String newEventType = in.read_string ();
         String oldEventID = in.read_string ();
         String oldEventType = in.read_string ();
         org.omg.CORBA.Any $result = null;
         $result = this.swapEvent (customerID, newEventID, newEventType, oldEventID, oldEventType);
         out = $rh.createReply();
         out.write_any ($result);
         break;
       }

       default:
         throw new org.omg.CORBA.BAD_OPERATION (0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
    }

    return out;
  } // _invoke

  // Type-specific CORBA::Object operations
  private static String[] __ids = {
    "IDL:com.web.DEMS_CORBA/DEMSInterfaceCorba:1.0"};

  public String[] _all_interfaces (org.omg.PortableServer.POA poa, byte[] objectId)
  {
    return (String[])__ids.clone ();
  }

  public DEMSInterfaceCorba _this() 
  {
    return DEMSInterfaceCorbaHelper.narrow(
    super._this_object());
  }

  public DEMSInterfaceCorba _this(org.omg.CORBA.ORB orb) 
  {
    return DEMSInterfaceCorbaHelper.narrow(
    super._this_object(orb));
  }


} // class DEMSInterfaceCorbaPOA
