package org.cytraining.backend;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.cytraining.backend.utils.Dotenv;
import org.cytraining.backend.utils.Log;
import org.jooq.codegen.GenerationTool;
import org.jooq.meta.jaxb.Configuration;
import org.jooq.meta.jaxb.Database;
import org.jooq.meta.jaxb.Generate;
import org.jooq.meta.jaxb.Generator;
import org.jooq.meta.jaxb.Jdbc;
import org.jooq.meta.jaxb.Target;

public class jOOQSetup {
    public static void main(String[] args) throws Exception {

        // if args[0] is "manual", then ignore properties file
        if (args.length >= 1 && args[0].equals("manual")) {
            setupWithException();
            return;
        }

        // read jooq-setup.properties file to know if we run the setup automatically
        boolean toSetup = false;
        Properties properties = new Properties();
        try (InputStream input = jOOQSetup.class.getClassLoader().getResourceAsStream("jooq-setup.properties")) {
            if (input == null) {
                Log.getLog().warn(
                        "Unable to read \"jooq-setup.properties\". Using default behavior, which is to generate.");
                toSetup = true;
            } else {
                properties.load(input);
                toSetup = Boolean.parseBoolean(properties.getProperty("auto_generate", "true"));
            }
        } catch (IOException ex) {
            Log.getLog().warn(
                    "Unable to read \"jooq-setup.properties\". Using default behavior, which is to generate. See error below:");
            ex.printStackTrace();
            toSetup = true;
        }

        if (toSetup) {
            setupWithException();
        } else {
            Log.getLog().info("Skipping generation");
        }
    }

    /**
     * Same as setup, but instead, do no catch the exception, and throw it.
     * Used when the class is the main class.
     *
     * @throws Exception when jOOQ `GeneratorTool.generate()` fails.
     * @see GenerationTool
     */
    private static void setupWithException() throws Exception {
        Exception e = setup();
        if (e != null) {
            throw e;
        }
    }

    /**
     * Use jOOQ to read the PostgreSQL database and generated type safe code for
     * querying.
     *
     * @return Exception if one is throw,n, null otherwise.
     */
    public static Exception setup() {
        Configuration configuration = new Configuration()
                .withJdbc(new Jdbc()
                        .withDriver("org.postgresql.Driver")
                        .withUrl(Dotenv.getDBUrl())
                        .withUser(Dotenv.getDBUser())
                        .withPassword(Dotenv.getDBPass()))
                .withGenerator(new Generator()
                        .withName("org.jooq.codegen.DefaultGenerator")
                        .withDatabase(new Database()
                                .withName("org.jooq.meta.postgres.PostgresDatabase")
                                .withInputSchema("public")
                                .withIncludes(".*")
                                .withExcludes("flyway_schema_history"))
                        .withGenerate(new Generate()
                                .withPojos(true)
                                .withDaos(true)
                                .withRecords(true))
                        .withTarget(new Target()
                                .withPackageName("org.cytraining.backend.model")
                                .withDirectory("./src/main/java/")
                                .withEncoding("UTF-8")
                                .withLocale("fr")
                                .withClean(true)));

        try {
            Log.getLog().info("Generating jOOQ database informations ...");
            GenerationTool.generate(configuration);
            return null;
        } catch (Exception e) {
            return e;
        }
    }
}
