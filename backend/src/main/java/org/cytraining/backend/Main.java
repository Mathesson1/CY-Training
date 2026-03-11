package org.cytraining.backend;

import static io.javalin.apibuilder.ApiBuilder.path;
import static org.cytraining.backend.model.Tables.ACCOUNT;

import org.cytraining.backend.routers.SetupRouter;
import org.cytraining.backend.utils.Dotenv;
import org.cytraining.backend.utils.Hasher;
import org.cytraining.backend.utils.Hikari;
import org.cytraining.backend.utils.Log;
import org.cytraining.backend.utils.jOOQ;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.slf4j.Logger;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;

public class Main {
    // create a logger
    private static final Logger log = Log.createLogger(Main.class);

    public static void main(String[] args) {
        setupAdmin();

        // Javalin app
        Javalin app = Javalin.create(config -> {
            // allow specific host on the api (CORS Access-Control-Allow-Origin), but ONLY
            // for dev mode
            // because in production, the backend and frontend are both served using the
            // same javalin server, so there are no cross origin requests
            if (Dotenv.isDevMode()) {
                config.bundledPlugins.enableCors(cors -> {
                    cors.addRule(it -> {
                        log.warn(
                                "Development mode enabled, allowing any host. This should not be used is production mode.");
                        it.anyHost();
                    });
                });
            }

            if (Dotenv.isDevMode()) {
                // for in depth automatic logging on each request
                // config.bundledPlugins.enableDevLogging();

                config.requestLogger.http((ctx, ms) -> {
                    // development logging here
                    Log.infoIp(log, ctx);
                });
            } else {
                config.showJavalinBanner = false;
                config.requestLogger.http((ctx, ms) -> {
                    // production logging
                    Log.infoIp(log, ctx);
                });
            }

            // will serve the built static frontend files
            // this is for all assets
            config.staticFiles.add(staticFiles -> {
                staticFiles.directory = "../frontend/dist";
                staticFiles.hostedPath = "/";
                // to say it's not packaged inside the .jar
                staticFiles.location = Location.EXTERNAL;
            });
            // this is for our SPA specifically
            config.spaRoot.addFile("/", "../frontend/dist/index.html", Location.EXTERNAL);

            config.jetty.modifyServer(server -> server.setStopTimeout(5_000)); // wait 5 seconds for existing requests
                                                                               // to finish

            config.http.maxRequestSize = 20 * 1000; // 20kb

            // create the routes here
            config.router.apiBuilder(() -> {
                path("/api", () -> {
                    new SetupRouter().register();
                });
            });
        }).start(Dotenv.getServerPort());

        // for clean shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            app.stop();
            Hikari.getHds().close();
        }));
    }

    private static void setupAdmin() {
        // setup admin account
        DSLContext jctx = jOOQ.getDsl();

        String admin_email = Dotenv.getAdminMail();

        Result<Record> query = jctx.select().from(ACCOUNT)
                .where(ACCOUNT.EMAIL.eq(admin_email)).fetch();
        if (query.size() == 0) {
            log.info("Admin account not found, creating one ...");
            // create an admin account
            jctx.insertInto(ACCOUNT, ACCOUNT.EMAIL,
                    // ACCOUNT.EMAIL_VERIFIED,
                    ACCOUNT.FIRST_NAME,
                    ACCOUNT.LAST_NAME, ACCOUNT.PASSWORD)
                    .values(admin_email,
                            // true,
                            "admin", "admin",
                            Hasher.hash(Dotenv.getAdminPass()))
                    .execute();
            // jctx.insertInto(ACCOUNT_ROLE, ACCOUNT_ROLE.ACCOUNT_ID, ACCOUNT_ROLE.ROLE)
            // .values(jctx.lastID().longValue(), RoleEnum.admin).execute();
        }
    }
}
