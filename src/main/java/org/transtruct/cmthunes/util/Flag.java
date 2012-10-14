package org.transtruct.cmthunes.util;

/**
 * Synchronized flag. A flag can be set or cleared and it is possible to perform
 * a blocking call to wait for the flag to be a certain value. This provides a
 * simple means of synchronization between threads which depend on one another.
 * 
 * @author Christopher Thunes <cthunes@transtruct.org>
 */
public class Flag {
    /** Internal value of the flag */
    private boolean flag;

    /**
     * Create a new Flag, initially cleared
     */
    public Flag() {
        this(false);
    }

    /**
     * Create a new Flag, initially set to the given value
     * 
     * @param value
     *            Initial value of the flag. If {@code true} then the flag is
     *            set, if {@code false} then the flag is cleared.
     */
    public Flag(boolean value) {
        this.flag = value;
    }

    /**
     * Set the flag
     */
    public synchronized void set() {
        this.flag = true;
        this.notifyAll();
    }

    /**
     * Clear the flag
     */
    public synchronized void clear() {
        this.flag = false;
        this.notifyAll();
    }

    /**
     * Wait for the internal flag to match the give value
     * 
     * @param value
     *            Value to wait for the internal flag to equal
     * @throws InterruptedException
     *             if the current thread is interrupted while waiting
     */
    public synchronized void waitFor(boolean value) throws InterruptedException {
        while (this.flag != value) {
            this.wait();
        }
    }

    /**
     * Wait for the internal flag to match the given value. This call will not
     * return prematurely due to being interrupted.
     * 
     * @param value
     *            Value to wait for the internal flag to equal
     */
    public void waitUninterruptiblyFor(boolean value) {
        while (true) {
            try {
                this.waitFor(value);
                break;
            } catch (InterruptedException e) {
                // Ignore and try again
            }
        }
    }

    /**
     * Return the internal status of the flag
     * 
     * @return the status of the flag
     */
    public boolean isSet() {
        return this.flag;
    }
}
