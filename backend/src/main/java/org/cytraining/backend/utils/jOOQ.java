package org.cytraining.backend.utils;

import org.jooq.Configuration;
import org.jooq.ConnectionProvider;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.jooq.impl.DataSourceConnectionProvider;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultTransactionProvider;

/**
 * Singleton to setup jOOQ and get the connection globally.
 */
public class jOOQ {
    private static final jOOQ that = new jOOQ();

    private DSLContext dsl;

    private jOOQ() {
        // setting up jOOQ query object "DSL"
        Settings settings = new Settings()
                .withRenderFormatted(true)
                .withExecuteLogging(true);

        ConnectionProvider cp = new DataSourceConnectionProvider(Hikari.getHds());

        Configuration configuration = new DefaultConfiguration()
                .set(cp)
                .set(new DefaultTransactionProvider(cp))
                .set(SQLDialect.POSTGRES)
                .set(settings);

        this.dsl = DSL.using(configuration);
    }

    /**
     * @return the dslc
     */
    public static DSLContext getDsl() {
        return that.dsl;
    }
}
