package org.progfun;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Logs messages, including info about current thread
 */
public class Logger {

    private static final SimpleDateFormat formatter 
            = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static void log(String message) {
        System.out.println(message + " ["
                + Thread.currentThread().getName() + "] "
                + formatter.format(new Date()));
    }
}
