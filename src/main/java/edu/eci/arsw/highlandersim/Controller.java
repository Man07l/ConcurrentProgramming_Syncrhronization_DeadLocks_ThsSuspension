package edu.eci.arsw.highlandersim;
import java.util.concurrent.atomic.AtomicInteger;

public class Controller {
    private boolean paused = false;
    private final Object pauseLock = new Object();
    private final AtomicInteger pausedThreads = new AtomicInteger(0);
    private int totalThreads;

    public void setTotalThreads(int totalThreads) {
        this.totalThreads = totalThreads;
    }

    public void pause() {
        synchronized (pauseLock) {
            paused = true;
        }
    }

    public void resume() {
        synchronized (pauseLock) {
            paused = false;
            pausedThreads.set(0);
            pauseLock.notifyAll();
        }
    }

    public void verifyPaused() throws InterruptedException {
        synchronized (pauseLock) {
            if (paused) {
                pausedThreads.incrementAndGet();
                pauseLock.wait();

            }
        }
    }

    public boolean allPaused() {
        return pausedThreads.get() == totalThreads;
    }
}

