/**
 * Copyright (C) 2015 Guangjie Feng
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

