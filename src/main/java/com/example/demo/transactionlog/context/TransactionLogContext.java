package com.example.demo.transactionlog.context;

import com.example.demo.transactionlog.entity.TransactionLog;

public class TransactionLogContext {

    private static final ThreadLocal<TransactionLog> holder = new ThreadLocal<>();

    public static void init(TransactionLog log) {
        holder.set(log);
    }

    public static TransactionLog get() {
        return holder.get();
    }

    public static void setMessage(String code, String name) {
        TransactionLog log = holder.get();
        if (log != null) {
            log.setMessageCode(code);
            log.setMessageName(name);
        }
    }

    public static void setError(String error) {
        TransactionLog log = holder.get();
        if (log != null) {
            log.setErrorMessage(error);
        }
    }

    public static void clear() {
        holder.remove();
    }
}