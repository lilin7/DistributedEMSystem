package com.web.client;

public class ThreadTest {

    public static void main(String[] args) {
        Thread firstCustomer = new Thread(new SynchronizedMethodThread("MTLC1111","MTLE100519","Seminars","MTLE100520","Seminars",args));
        firstCustomer.start();
        Thread secondCustomer = new Thread(new SynchronizedMethodThread("MTLC2222","MTLE100519","Seminars","MTLE100520","Seminars",args));
        secondCustomer.start();

    }
}
