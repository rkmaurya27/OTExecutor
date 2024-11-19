package com.org.OTExecutor.OT;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class ValueReturningTaskSecond implements Callable {
    private int a;
    private int b;
    final private long sleepTime;
    private static int count = 0;
    final private String taskId;
    private int sum;

    public ValueReturningTaskSecond(int a, int b, long sleepTime){
        this.a = a;
        this.b = b;
        this.sleepTime = sleepTime;
        int instanceNumber = ++count;
        this.taskId = "ValueReturningTaskSecond- "+ instanceNumber;
    }
    @Override
    public Integer call() throws Exception{
        String currentThreadName = Thread.currentThread().getName();
        System.out.println("##"+currentThreadName+ "<<>>"+taskId+ "<< starting >>");
        TimeUnit.MILLISECONDS.sleep(sleepTime);
        System.out.println("##"+currentThreadName+ "<<>>"+taskId+ "<< complated >>");
        return a + b;
    }
}
