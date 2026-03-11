package org.cytraining.backend.routers;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;

import org.cytraining.backend.api.Response;
import org.cytraining.backend.utils.Log;
import org.slf4j.Logger;

/**
 * Router used to make an example for the setup.
 */
public class SetupRouter implements Router {
    // create a logger
    private static final Logger log = Log.createLogger(SetupRouter.class);

    public void register() {
        get(ctx -> ctx.json(Response.ok("Hello world!")));
        path("/test", () -> {
            get(ctx -> {
                ctx.json(Response.ok("Hello world, but in test!"));
                log.info("User saw: Hello World in test");
            });
        });
    }
}
