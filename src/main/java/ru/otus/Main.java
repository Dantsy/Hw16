package ru.otus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);
    private static final String Message = ">>>>>>>>>>>>>>>>>>>>>>>> {} : [{}]";
    private static final int min = 1;
    private static final int max = 10;
    private static final String First_thread = "Thread-1";
    private static final String Second_thread = "Thread-2";
    private static final Lock R_lock = new ReentrantLock();
    private static final Condition Try_again = R_lock.newCondition();

    private static boolean next;
    private final String threadName;
    private int counter = 0;

    public Main() {
        this.threadName = Thread.currentThread().getName();
    }

    public static void main(String... args) {
        new Thread(() -> new Main().task(), First_thread).start();
        new Thread(() -> new Main().task(), Second_thread).start();
    }

    private void task() {
        while (counter < max) {
            lock();
            increment();
            unlock();
        }
        while (counter > min) {
            lock();
            decrement();
            unlock();
        }
    }

    private void lock() {
        R_lock.lock();
        log.debug("{}: Locked", threadName);
        orderThreads();
    }

    private synchronized void orderThreads() {
        if (isFirst()) {
            next = true;
        } else if (isNext()) {
            next = false;
        } else {
            await();
            orderThreads();
        }
    }

    private boolean isFirst() {
        return threadName.equals(First_thread) && !next;
    }

    private boolean isNext() {
        return !threadName.equals(First_thread) && next;
    }

    private void await() {
        try {
            Try_again.await();
            log.debug("{}: Waiting...", threadName);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void unlock() {
        try {
            Try_again.signalAll();
            log.debug("{}: Signal All", threadName);
        } finally {
            R_lock.unlock();
            log.debug("{}: Unlocked", threadName);
        }
    }

    private void increment() {
        ++counter;
        log();
    }

    private void decrement() {
        --counter;
        log();
    }

    private void log() {
        log.info(Message, Thread.currentThread().getName(), counter);
    }
}