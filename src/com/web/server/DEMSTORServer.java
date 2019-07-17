package com.web.server;

import com.web.service.impl.DEMSImplWeb;

import javax.xml.ws.Endpoint;

public class DEMSTORServer{

	public static DEMSImplWeb DEMSobj;

	public static void main(String[] args) throws Exception {
		int localRMIPortNumber = DEMSInterface.PORT_TOR;

		int localUDPPortNumber = DEMSInterface.UDP_PORT_TOR;

		int firstRemoteUDPPortNumber = DEMSInterface.UDP_PORT_MTL;
		int secondRemoteUDPPortNumber = DEMSInterface.UDP_PORT_OTW;
		
		int MTLRemoteUDPPortNumber = DEMSInterface.UDP_PORT_MTL;
		int TORRemoteUDPPortNumber = DEMSInterface.UDP_PORT_TOR;
		int OTWRemoteUDPPortNumber = DEMSInterface.UDP_PORT_OTW;

		DEMSobj = new DEMSImplWeb(firstRemoteUDPPortNumber,secondRemoteUDPPortNumber,"TOR");
		Endpoint endpoint = Endpoint.publish("http://localhost:7070/DEMS",DEMSobj);

		System.out.println("Toronto com.web.server online");


		DEMSThread demsThread = new DEMSThread(DEMSobj,localUDPPortNumber);
		demsThread.start();
	}

}
