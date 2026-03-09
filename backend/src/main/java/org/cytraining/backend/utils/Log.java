package org.cytraining.backend.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MarkerFactory;

import io.javalin.http.Context;

/**
 * Singleton used to log globally.
 */
public class Log {

    private Log() {
    }

    /**
     * Create a logger for the given class.
     *
     * @param class1 The class that needs a logger.
     * @return A configured logger for the class.
     */
    public static Logger createLogger(Class<?> class1) {
        return LoggerFactory.getLogger(class1);
    }

    /**
     * The normal info, looging which ip tried which url, with the request response
     * status.
     *
     * @param ctx
     */
    public static void infoIp(Logger log, Context ctx) {
        log.info("[ " + ctx.ip() + "\t] " + ctx.url() + ": " + ctx.res().getStatus());
    }

    /**
     * Shorthand for the log.error with the fatal marker
     *
     * @param format the format string
     */
    public static void fatal(Logger log, String format) {
        fatal(log, format, null);
    }

    /**
     * Shorthand for the log.error with the fatal marker
     *
     * @param format the format string
     * @param arg    the argument
     */
    public static void fatal(Logger log, String format, Object arg) {
        log.error(MarkerFactory.getMarker("FATAL"), format, arg);
    }

    /**
     * Shorthand for the log.error with the fatal marker
     *
     * @param format the format string
     * @param arg    the exception
     */
    public static void fatal(Logger log, String format, Throwable e) {
        log.error(MarkerFactory.getMarker("FATAL"), format, e.getMessage());
        e.printStackTrace();
    }
}
