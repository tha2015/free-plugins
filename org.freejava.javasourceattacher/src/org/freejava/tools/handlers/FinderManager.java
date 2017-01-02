package org.freejava.tools.handlers;

import java.util.LinkedList;
import java.util.List;

public class FinderManager {

    private Worker[] workers;

    public FinderManager() {
        // Create a set of worker threads
        final int numWorkers = 10;
        workers = new Worker[numWorkers];
    }

    public boolean isRunning() {
        boolean result = false;
        for (int i = 0; i < workers.length; i++) {
            if (workers[i] != null && workers[i].isAlive()) {
                result = true;
                break;
            }
        }
        return result;
    }

    public void cancel() {
        for (int i = 0; i < workers.length; i++) {
            if (workers[i] != null && workers[i].isAlive()) {
                workers[i].cancel();
            }
        }
    }
    public void findSources(List<String> libs, List<SourceFileResult> results) {

        // Create the work queue
        WorkQueue queue = new WorkQueue();

        for (int i = 0; i < workers.length; i++) {
            workers[i] = new Worker(queue, results);
            workers[i].start();
        }

        // Add some work to the queue
        for (String lib : libs) {
            queue.addWork(lib);
        }

        // Add special end-of-stream markers to terminate the workers
        for (int i = 0; i < workers.length; i++) {
            queue.addWork(Worker.NO_MORE_WORK);
        }
    }

    private static class WorkQueue {
        LinkedList<String> queue = new LinkedList<String>();

        // Add work to the work queue
        public synchronized void addWork(String o) {
            queue.addLast(o);
            notify();
        }

        // Retrieve work from the work queue; block if the queue is empty
        public synchronized String getWork() throws InterruptedException {
            while (queue.isEmpty()) {
                wait();
            }
            return queue.removeFirst();
        }
    }

    private static class Worker extends Thread {
        public static final String NO_MORE_WORK = new String("NO_MORE_WORK");
        private WorkQueue q;
        private List<SourceFileResult> results;
        private boolean canceled;
        private SourceCodeFinder finder;

        public Worker(WorkQueue q, List<SourceFileResult> results) {
            this.q = q;
            this.results = results;
            this.finder = new SourceCodeFinderFacade();
        }

        public void cancel() {
            canceled = true;
            this.finder.cancel();
        }

        public void run() {
            try {
                while (true && !canceled) {
                    String binFile = q.getWork();
                    if (binFile == NO_MORE_WORK) {
                        break;
                    }
                    this.finder.find(binFile, results);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


}
