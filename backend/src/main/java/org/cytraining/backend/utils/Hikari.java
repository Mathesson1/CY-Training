package org.cytraining.backend.utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Singleton to get Hikari connection pool globally
 */
public class Hikari {
    private static final Hikari that = new Hikari();

    private HikariDataSource hds;

    private Hikari() {
        // setup HikariCP for ProgreSQL connection pool
        HikariConfig hc = new HikariConfig();

        hc.setJdbcUrl(Dotenv.getDBUrl());
        hc.setUsername(Dotenv.getDBUser());
        hc.setPassword(Dotenv.getDBPass());
        hc.addDataSourceProperty("cachePrepStmts", "true");
        hc.addDataSourceProperty("prepStmtCacheSize", "250");
        hc.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hc.addDataSourceProperty("tcpKeepAlive", "true");
        hc.setConnectionTimeout(30000);
        hc.setLeakDetectionThreshold(60000);
        hc.setInitializationFailTimeout(0);
        this.hds = new HikariDataSource(hc);
    }

    /**
     * @return the hikari data source
     */
    public static HikariDataSource getHds() {
        return that.hds;
    }
}
