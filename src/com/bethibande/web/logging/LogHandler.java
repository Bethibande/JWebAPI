package com.bethibande.web.logging;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class LogHandler extends Handler {

    private final ApiLogger owner;
    private final ThreadPoolExecutor executor;

    private volatile Runnable printTask = null;
    private final Queue<LogMessage> logQueue = new ConcurrentLinkedQueue<>();

    public LogHandler(final ApiLogger owner) {
        this.owner = owner;
        this.executor = owner.getOwner().getExecutor();
    }

    private void print() {
        while(!logQueue.isEmpty()) {
            final LogMessage message = logQueue.poll();
            final String formatted = owner.getStyle().format(message);
            System.out.println(formatted);
        }

        printTask = null;
    }

    @Override
    public void publish(final LogRecord record) {
        logQueue.offer(new LogMessage(Thread.currentThread(), record, owner));

        if(printTask == null) {
            final Runnable task = this::print;
            printTask = task;
            executor.execute(task);
        }
    }

    @Override
    public void flush() {
        print();
        System.out.flush();
    }

    @Override
    public void close() throws SecurityException {

    }
}
