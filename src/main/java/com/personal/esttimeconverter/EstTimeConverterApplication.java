package com.personal.esttimeconverter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.URI;
import java.net.URISyntaxException;

@SpringBootApplication
public class EstTimeConverterApplication {

    public static void main(String[] args) {
        configureDatabaseFromEnv();
        SpringApplication.run(EstTimeConverterApplication.class, args);
    }

    /**
     * Koyeb (and similar platforms) hand over database connection details as
     * a single DATABASE_URL environment variable, shaped like
     * postgres://user:password@host:port/database. Spring Boot's datasource
     * config expects separate url/username/password properties with a
     * jdbc: prefix, so this translates one into the other whenever
     * DATABASE_URL is present. When it isn't (normal local development),
     * this does nothing and the local H2 database configured in
     * application.properties is used instead.
     */
    private static void configureDatabaseFromEnv() {
        String databaseUrl = System.getenv("DATABASE_URL");
        if (databaseUrl == null || databaseUrl.isBlank()) {
            return;
        }
        try {
            URI uri = new URI(databaseUrl);
            String[] userInfo = uri.getUserInfo().split(":", 2);
            String jdbcUrl = "jdbc:postgresql://" + uri.getHost() + ":" + uri.getPort() + uri.getPath();

            System.setProperty("spring.datasource.url", jdbcUrl);
            System.setProperty("spring.datasource.username", userInfo[0]);
            System.setProperty("spring.datasource.password", userInfo.length > 1 ? userInfo[1] : "");
            System.setProperty("spring.datasource.driver-class-name", "org.postgresql.Driver");
        } catch (URISyntaxException e) {
            throw new IllegalStateException(
                    "DATABASE_URL is set but isn't a valid connection string: " + databaseUrl, e);
        }
    }
}

