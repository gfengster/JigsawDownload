package com.abc.util;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Vector;

public class Observable<T> {

    private boolean changed = false;
    private final Vector<Observer<T>> obs;

    public Observable() {
        obs = new Vector<Observer<T>>();
    }

    public synchronized void addObserver(Observer<T> o) {
        if (o == null)
            throw new NullPointerException();
        if (!obs.contains(o)) {
            obs.addElement(o);
        }
    }

    public synchronized void deleteObserver(Observer<T> o) {
        obs.removeElement(o);
    }

    public void notifyObservers() {
        notifyObservers(null);
    }

    public void notifyObservers(T arg) {

        final Deque<Observer<T>> arrLocal;

        synchronized (this) {

            if (!changed)
                return;
            
            arrLocal = new ArrayDeque<Observer<T>>(obs);
            
            clearChanged();
        }
        
        for (Iterator<Observer<T>> iter = arrLocal.descendingIterator(); iter.hasNext();) {
            iter.next().update(this, arg);
        }
    }

    public synchronized void deleteObservers() {
        obs.removeAllElements();
    }

    protected synchronized void setChanged() {
        changed = true;
    }

    protected synchronized void clearChanged() {
        changed = false;
    }

    public synchronized boolean hasChanged() {
        return changed;
    }

    public synchronized int countObservers() {
        return obs.size();
    }
}

