package org.transtruct.cmthunes.util;

public class Flag {
    private boolean flag;

    public Flag() {
        this(false);
    }
    
    public Flag(boolean value) {
        this.flag = value;
    }

    public synchronized void set() {
        this.flag = true;
        this.notifyAll();
    }

    public synchronized void clear() {
        this.flag = false;
        this.notifyAll();
    }


    public synchronized void waitFor(boolean value) throws InterruptedException {
        while(this.flag != value) {
            this.wait();
        }
    }
    
    public void waitUninterruptiblyFor(boolean value) {
        while(true) {
            try {
                this.waitFor(value);
                break;
            } catch(InterruptedException e) {
                // Ignore and try again
            }
        }
    }

    public boolean isSet() {
        return this.flag;
    }
}
