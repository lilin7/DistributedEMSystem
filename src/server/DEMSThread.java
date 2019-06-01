package server;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class DEMSThread extends Thread{

    private DEMSImpl stub;
    private int localUDPport;

    public DEMSThread(DEMSImpl impl,int localudpport){
        this.stub = impl;
        this.localUDPport = localudpport;

    }

    @Override

    public void run(){
        DatagramSocket aSocket = null;
        try {
            aSocket = new DatagramSocket(localUDPport);
            byte[] buffer = new byte[1000];
            System.out.println("Server UDP Listen Started");
            while (true) {
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                aSocket.receive(request);
                System.out.println("Request received from client: " + new String(request.getData()));

                String requestToString = new String(request.getData()); //receive message from source city
                
                String re = "";
                //TODO:need change, can split request to get more parameters and finish the task blow
                if(requestToString.equals("listEventAvailability")){
                    //re = stub.listEventAvailabilityForUDP(param1, param2...);
                }else if(requestToString.equals("bookEvent")){
                    //re = stub.bookEventForUDP(param1, param2...);
                }else if(requestToString.equals("getBookingSchedule")){
                    //re = stub.getBookingScheduleForUDP(param1, param2...);
                }else if(requestToString.equals("cancelEvent")){
                    //re = stub.cancelEventForUDP(param1, param2...);
                }else{
                    //errormassage
                }
                
                //done booking or cancel
                byte [] me = re.getBytes();
                DatagramPacket reply = new DatagramPacket(me, re.length(), request.getAddress(), request.getPort());// reply packet ready
                aSocket.send(reply);// reply sent
            }
        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        } finally {
            if (aSocket != null)
                aSocket.close();
        }
    }
}
