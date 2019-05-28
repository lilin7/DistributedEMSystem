package client;

import java.io.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

import server.DEMSInterface;

/**
 * This class represents the object client for a distributed
 * object of class DEMS, which implements the remote interface
 * server.DEMSInterface.
 */
public class DEMSClient {

	private String userID;

	private String location;
	private String lookUpServerName;
	private String roll;
	private String number;

	private DEMSInterface obj;

	private int portNumber;

	public static void main(String[] args){
		try {
			String userID;
			InputStreamReader is = new InputStreamReader(System.in);
			BufferedReader br = new BufferedReader(is);
			System.out.println("Please enter your ID:");
			userID = br.readLine().trim().toUpperCase();

			DEMSClient newclient = new DEMSClient();

			newclient.setID(userID);
			newclient.start();
		} 
		catch (Exception e) {
			System.out.println("Exception in client.DEMSClient: " + e);
		} 
	}

	public void setID(String id){
		this.userID = id;
	}

	public void start()throws Exception{
		this.location = userID.substring(0,3);
		this.roll = userID.substring(3,4);
		this.number = userID.substring(4);

		if(location.equals("MTL")){
			this.portNumber = 1000;
			this.lookUpServerName = "MTLServer";
		}else if(location.equals("OTW")){
			this.portNumber = 1001;
			this.lookUpServerName = "OTWServer";
		} else if(location.equals("TOR")){
			this.portNumber = 1002;
			this.lookUpServerName = "TORServer";
		} else {
			System.out.println("wrong id");
		}

		Registry registry = LocateRegistry.getRegistry(portNumber);
		obj = (DEMSInterface) registry.lookup(lookUpServerName);


		if(roll.equals("M")){
			managerOperate();
		}else if(roll.equals("C")){
			customerOperate();
		}else{
			System.out.println("wrong id");
		}


	}

	private void managerOperate(){
		while(true){
			Scanner sc = new Scanner(System.in);
			int op = sc.nextInt();
			System.out.println("op1");
			System.out.println("op2");

			switch (op){
				case 1:
					break;
				case 2:
					break;
			}





		}
	}

	private  void customerOperate(){
		while (true){
			Scanner sc = new Scanner(System.in);
			int op = sc.nextInt();
			System.out.println("op1");
			System.out.println("op2");

			switch (op){
				case 1:
					break;
				case 2:
					break;
			}



		}
	}



	public void log(){

	}



}
