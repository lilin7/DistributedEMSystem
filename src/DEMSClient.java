import java.io.*;

/**
 * This class represents the object client for a distributed
 * object of class DEMS, which implements the remote interface
 * DEMSInterface.
 */
public class DEMSClient {
	public static void main(String[] args) {
		try {
			String userID;
			InputStreamReader is = new InputStreamReader(System.in);
			BufferedReader br = new BufferedReader(is);
			System.out.println("Please enter your ID:");
			userID = br.readLine().trim().toUpperCase();
			
			if (userID.charAt(3)=='M') {
				//is manager role
			} else if (userID.charAt(3)=='C') {
				//is customer role
			}
				
		} 
		catch (Exception e) {
			System.out.println("Exception in DEMSClient: " + e);
		} 
	}

}
