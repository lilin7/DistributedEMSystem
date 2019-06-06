package client;

public class ThreadTest {

    public static void main(String[] args) {
        Thread firstCustomer = new Thread(new SynchronizedMethodThread("MTLC1111","MTLE100519","Seminars"));
        firstCustomer.start();
        Thread secondCustomer = new Thread(new SynchronizedMethodThread("MTLC2222","MTLE100519","Seminars"));
        secondCustomer.start();
        Thread thirdCustomer = new Thread(new SynchronizedMethodThread("MTLC3333","MTLE100519","Seminars"));
        thirdCustomer.start();
    }
}
