package DEMS_CORBA;


/**
* DEMS_CORBA/DEMSInterfaceCorbaHelper.java .
* 由IDL-to-Java 编译器 (可移植), 版本 "3.2"生成
* 从F:/xuexi/corba/src/server/DEMSidl.idl
* 2019年6月26日 星期三 下午05时43分05秒 EDT
*/

abstract public class DEMSInterfaceCorbaHelper
{
  private static String  _id = "IDL:DEMS_CORBA/DEMSInterfaceCorba:1.0";

  public static void insert (org.omg.CORBA.Any a, DEMS_CORBA.DEMSInterfaceCorba that)
  {
    org.omg.CORBA.portable.OutputStream out = a.create_output_stream ();
    a.type (type ());
    write (out, that);
    a.read_value (out.create_input_stream (), type ());
  }

  public static DEMS_CORBA.DEMSInterfaceCorba extract (org.omg.CORBA.Any a)
  {
    return read (a.create_input_stream ());
  }

  private static org.omg.CORBA.TypeCode __typeCode = null;
  synchronized public static org.omg.CORBA.TypeCode type ()
  {
    if (__typeCode == null)
    {
      __typeCode = org.omg.CORBA.ORB.init ().create_interface_tc (DEMS_CORBA.DEMSInterfaceCorbaHelper.id (), "DEMSInterfaceCorba");
    }
    return __typeCode;
  }

  public static String id ()
  {
    return _id;
  }

  public static DEMS_CORBA.DEMSInterfaceCorba read (org.omg.CORBA.portable.InputStream istream)
  {
    return narrow (istream.read_Object (_DEMSInterfaceCorbaStub.class));
  }

  public static void write (org.omg.CORBA.portable.OutputStream ostream, DEMS_CORBA.DEMSInterfaceCorba value)
  {
    ostream.write_Object ((org.omg.CORBA.Object) value);
  }

  public static DEMS_CORBA.DEMSInterfaceCorba narrow (org.omg.CORBA.Object obj)
  {
    if (obj == null)
      return null;
    else if (obj instanceof DEMS_CORBA.DEMSInterfaceCorba)
      return (DEMS_CORBA.DEMSInterfaceCorba)obj;
    else if (!obj._is_a (id ()))
      throw new org.omg.CORBA.BAD_PARAM ();
    else
    {
      org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate ();
      DEMS_CORBA._DEMSInterfaceCorbaStub stub = new DEMS_CORBA._DEMSInterfaceCorbaStub ();
      stub._set_delegate(delegate);
      return stub;
    }
  }

  public static DEMS_CORBA.DEMSInterfaceCorba unchecked_narrow (org.omg.CORBA.Object obj)
  {
    if (obj == null)
      return null;
    else if (obj instanceof DEMS_CORBA.DEMSInterfaceCorba)
      return (DEMS_CORBA.DEMSInterfaceCorba)obj;
    else
    {
      org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate ();
      DEMS_CORBA._DEMSInterfaceCorbaStub stub = new DEMS_CORBA._DEMSInterfaceCorbaStub ();
      stub._set_delegate(delegate);
      return stub;
    }
  }

}
