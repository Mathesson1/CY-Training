package org.cytraining.backend.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;

import io.github.cdimascio.dotenv.DotenvEntry;

/**
 * Singleton that enable to get all .env variables.
 */
public class Dotenv {
    // create a logger
    private static final Logger log = Log.createLogger(Dotenv.class);

    private static final Dotenv that = new Dotenv();

    private boolean valid = false;
    private boolean dev_mode = false;

    private String db_host;
    private String db_port;
    private String db_name;
    private String db_user;
    private String db_pass;

    private String db_url;

    private int server_port;

    private String admin_pass;
    private String admin_mail;

    private Dotenv() {
        // Load .env
        io.github.cdimascio.dotenv.Dotenv dotenv = io.github.cdimascio.dotenv.Dotenv.load();

        // check that all value are present in the .env
        Set<DotenvEntry> entries = dotenv.entries();
        ArrayList<String> expectedEntries = new ArrayList<String>(
                List.of("DATABASE_HOST", "DATABASE_PORT", "DATABASE_NAME", "DATABASE_USER",
                        "DATABASE_PASS", "BACKEND_PORT", "APP_MODE"));

        // remove expectedEntries we find
        entries.forEach(entry -> {
            if (expectedEntries.contains(entry.getKey())) {
                expectedEntries.remove(entry.getKey());
            }
        });

        // each expectedEntries left are missing
        if (expectedEntries.size() > 0) {
            expectedEntries.forEach(entry -> {
                Log.fatal(log, "The .env file does not contains the \"" + entry + "\" field!");
            });
            return;
        }

        this.db_host = dotenv.get("DATABASE_HOST");
        this.db_port = dotenv.get("DATABASE_PORT");
        this.db_name = dotenv.get("DATABASE_NAME");
        this.db_user = dotenv.get("DATABASE_USER");
        this.db_pass = dotenv.get("DATABASE_PASS");

        this.server_port = Integer.parseInt(dotenv.get("BACKEND_PORT"));

        this.admin_pass = dotenv.get("ADMIN_PASS");
        this.admin_mail = dotenv.get("ADMIN_MAIL");

        // a bit weird here:
        // APP_MODE is only for development, it only serve to emulate which mode is used
        // when launching the server
        // it is overriden by "System.getProperty("env")", when you build for prod.
        // Setting anything in the mode property will make the program believe it's in
        // production mode
        this.dev_mode = System.getProperty("mode") == null && dotenv.get("APP_MODE") != "prod";

        if (this.dev_mode) {
            log.warn("This instance is in development mode. Do not use it for production!");
        } else {
            log.info("This instance is in production mode.");
        }

        // Build the PostgreSQL url
        this.db_url = "jdbc:postgresql://" + db_host + ":" + db_port + "/" + db_name;

        this.valid = true;
    }

    /**
     * @return true if the dotenv has loaded properly
     */
    public static boolean isValid() {
        return that.valid;
    }

    /**
     * @return true if dev mode is enabled
     */
    public static boolean isDevMode() {
        return that.dev_mode;
    }

    /**
     * @return the db_host
     */
    public static String getDBHost() {
        return that.db_host;
    }

    /**
     * @return the db_name
     */
    public static String getDBName() {
        return that.db_name;
    }

    /**
     * @return the db_pass
     */
    public static String getDBPass() {
        return that.db_pass;
    }

    /**
     * @return the db_port
     */
    public static String getDBPort() {
        return that.db_port;
    }

    /**
     * @return the db_url
     */
    public static String getDBUrl() {
        return that.db_url;
    }

    /**
     * @return the db_user
     */
    public static String getDBUser() {
        return that.db_user;
    }

    /**
     * @return the server_port
     */
    public static int getServerPort() {
        return that.server_port;
    }

    /**
     * @return the admin_pass
     */
    public static String getAdminPass() {
        return that.admin_pass;
    }

    /**
     * @return the admin_mail
     */
    public static String getAdminMail() {
        return that.admin_mail + "@cytraining.fr";
    }
}
