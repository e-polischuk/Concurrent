package ua.itea.concurrency;

import java.util.concurrent.*;

public class Hairdresser extends Thread {
    private CountDownLatch latch = new CountDownLatch(100);
    private BlockingQueue<Customer> queue = new ArrayBlockingQueue<>(10);
    private volatile boolean isWaiting;

    public static void main(String[] args) {
	Hairdresser hairdresser = new Hairdresser();
	hairdresser.start();
	try {
	    hairdresser.latch.await();
	} catch (InterruptedException e) {
	    e.printStackTrace();
	}
    }

    public void run() {
	begin();
	while (latch.getCount() > 0) {
	    if (queue.isEmpty())
		pauseDresser(true, null);
	    try {
		dress(queue.take());
	    } catch (InterruptedException e) {
		e.printStackTrace();
	    }
	}
    }
    
    public void begin() {
	Thread input = new Thread(() -> {
	    int id = 0;
	    while (latch.getCount() > 0) {
		if (queue.size() == 10) {
		    System.out.println("Customer_" + ++id + " went to other hairdresser.");
		} else {
		    Customer c = new Customer(++id);
		    try {
			queue.put(c);
		    } catch (Exception e) {
			e.printStackTrace();
		    }
		    if (isWaiting)
			pauseDresser(false, c);
		}
		try {
		    Thread.sleep((long) (Math.random() * 10 + 8));
		} catch (InterruptedException e) {
		    e.printStackTrace();
		}
	    }
	});
	input.start();
    }

    public synchronized void pauseDresser(boolean mark, Customer c) {
	isWaiting = mark;
	if (mark) {
	    try {
		System.out.println("Hairdresser is sleeping");
		wait();
	    } catch (InterruptedException e) {
		e.printStackTrace();
	    }
	} else {
	    notify();
	    System.out.println("Hairdresser was woken up by Customer_" + c.id);
	}
    }

    public void dress(Customer c) {
	try {
	    Thread.sleep((long) (Math.random() * 10 + 10));
	} catch (InterruptedException e) {
	    e.printStackTrace();
	}
	System.out.println("Customer_" + c.id + " was dressed.");
	latch.countDown();
    }

    private class Customer {
	private int id;

	public Customer(int customerID) {
	    id = customerID;
	}
    }
}
