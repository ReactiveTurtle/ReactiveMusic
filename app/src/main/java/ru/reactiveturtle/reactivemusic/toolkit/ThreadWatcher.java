package ru.reactiveturtle.reactivemusic.toolkit;

/**
 * To start observing the thread, you need to call the observe() method
 */
public class ThreadWatcher {
    private Thread observedThread;
    private Thread thread;

    public ThreadWatcher(Thread observedThread) {
        this.observedThread = observedThread;
    }

    /**
     * Starts monitoring the thread. It is desirable to hang up a ThreadListener
     */
    private boolean isObserving = false;
    private boolean isEnd = false;

    public synchronized void observe() {
        if (isObserving) {
            throw new IllegalStateException("ThreadWatcher already observing the thread");
        }
        isObserving = true;
        thread = new Thread(() -> {
            try {
                observedThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            isObserving = false;
            isEnd = true;
            if (threadListener != null) {
                threadListener.onDead();
            }
        });
        thread.start();
    }

    public void stopObserving() {
        isObserving = false;
        threadListener = null;
        thread.interrupt();
    }

    public boolean isEnd() {
        return isEnd;
    }

    private ThreadListener threadListener;

    public void setThreadListener(ThreadListener threadListener) {
        this.threadListener = threadListener;
    }

    public interface ThreadListener {
        void onDead();
    }
}