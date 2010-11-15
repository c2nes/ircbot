package org.transtruct.cmthunes.util;

import java.util.*;

public class FixedBlockingBuffer<T> {
    private LinkedList<T> elements;
    private int capacity;
    private Flag full;
    private Flag empty;
    
    public FixedBlockingBuffer(int capacity) {
        this.elements = new LinkedList<T>();
        this.capacity = 0;
        
        this.full = new Flag(false);
        this.empty = new Flag(true);
    }
    
    public void add(T e) {
        synchronized(this.full) {
            while(this.full.isSet()) {
                this.full.waitUninterruptiblyFor(false);
            }
            
            this.elements.add(e);
            if(this.empty.isSet()) {
                this.empty.clear();
            }
            
            if(this.elements.size() == this.capacity) {
                this.full.set();
            }
        }
    }
    
    public T get() {
        T element;
        synchronized(this.empty) {
            while(this.empty.isSet()) {
                this.empty.waitUninterruptiblyFor(false);
            }
            
            element = this.elements.remove();
            if(this.full.isSet()) {
                this.full.clear();
            }
            
            if(this.elements.size() == 0) {
                this.empty.set();
            }
        }
        
        return element;
    }
    
    public int getCapacity() {
        return this.capacity;
    }
    
    public int getSize() {
        return this.elements.size();
    }
}
