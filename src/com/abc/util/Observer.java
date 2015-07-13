package com.abc.util;

public interface Observer<T> {
    void update(Observable<T> o, T arg);
}
