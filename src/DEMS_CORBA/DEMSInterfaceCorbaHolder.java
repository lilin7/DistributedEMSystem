package DEMS_CORBA;

/**
* DEMS_CORBA/DEMSInterfaceCorbaHolder.java .
* ��IDL-to-Java ������ (����ֲ), �汾 "3.2"����
* ��F:/xuexi/corba/src/server/DEMSidl.idl
* 2019��6��26�� ������ ����05ʱ43��05�� EDT
*/

public final class DEMSInterfaceCorbaHolder implements org.omg.CORBA.portable.Streamable
{
  public DEMS_CORBA.DEMSInterfaceCorba value = null;

  public DEMSInterfaceCorbaHolder ()
  {
  }

  public DEMSInterfaceCorbaHolder (DEMS_CORBA.DEMSInterfaceCorba initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = DEMS_CORBA.DEMSInterfaceCorbaHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    DEMS_CORBA.DEMSInterfaceCorbaHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return DEMS_CORBA.DEMSInterfaceCorbaHelper.type ();
  }

}
