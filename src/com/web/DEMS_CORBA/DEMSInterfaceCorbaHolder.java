package com.web.DEMS_CORBA;

/**
* com.web.DEMS_CORBA/DEMSInterfaceCorbaHolder.java .
* ��IDL-to-Java ������ (����ֲ), �汾 "3.2"����
* ��F:/xuexi/corba/src/com.web.server/DEMSidl.idl
* 2019��6��26�� ������ ����05ʱ43��05�� EDT
*/

public final class DEMSInterfaceCorbaHolder implements org.omg.CORBA.portable.Streamable
{
  public com.web.DEMS_CORBA.DEMSInterfaceCorba value = null;

  public DEMSInterfaceCorbaHolder ()
  {
  }

  public DEMSInterfaceCorbaHolder (com.web.DEMS_CORBA.DEMSInterfaceCorba initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = com.web.DEMS_CORBA.DEMSInterfaceCorbaHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    com.web.DEMS_CORBA.DEMSInterfaceCorbaHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return com.web.DEMS_CORBA.DEMSInterfaceCorbaHelper.type ();
  }

}
