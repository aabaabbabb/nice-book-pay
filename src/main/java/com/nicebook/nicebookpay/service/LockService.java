package com.nicebook.nicebookpay.service;

public interface LockService {
    boolean tryLock(String key, String value, int seconds);

    boolean unlock(String key, String value);
}
