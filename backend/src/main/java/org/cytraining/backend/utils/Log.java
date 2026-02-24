package org.cytraining.backend.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MarkerFactory;

import io.javalin.http.Context;

/**
 * Singleton used to log globally.
 */
public class Log {
    private final static Log that = new Log();
    private Logger log;

    private Log() {
        // Setup the logger
        this.log = LoggerFactory.getLogger(Log.class);
    }

    /**
     * The normal info, looging which ip tried which url, with the request response
     * status.
     *
     * @param ctx
     */
    public static void infoIp(Context ctx) {
        that.log.info("[ " + ctx.ip() + "\t] " + ctx.url() + ": " + ctx.res().getStatus());
    }

    /**
     * Shorthand for the log.error with the fatal marker
     *
     * @param format the format string
     */
    public static void fatal(String format) {
        fatal(format, null);
    }

    /**
     * Shorthand for the log.error with the fatal marker
     *
     * @param format the format string
     * @param arg    the argument
     */
    public static void fatal(String format, Object arg) {
        that.log.error(MarkerFactory.getMarker("FATAL"), format, arg);
    }

    /**
     * Shorthand for the log.error with the fatal marker
     *
     * @param format the format string
     * @param arg    the exception
     */
    public static void fatal(String format, Throwable e) {
        that.log.error(MarkerFactory.getMarker("FATAL"), format, e.getMessage());
        e.printStackTrace();
    }

    /**
     * @return the logger
     */
    public static Logger getLog() {
        return that.log;
    }
}
