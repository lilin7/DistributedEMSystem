import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.net.*;
import java.io.*;

/**
 * This class represents the object server for a distributed
 * object of class DEMS, which implements the remote interface
 * DEMSInterface.
 */
public class DEMSTemplateServer {
	public static void main(String[] args) {
		InputStreamReader is = new InputStreamReader(System.in);
		BufferedReader br = new BufferedReader(is);
		String portNum, registryURL;
		
		try{
			
	         
		}
		catch (Exception re) {
			System.out.println("Exception in DEMSServer.main: " + re);
		}

	}

}
