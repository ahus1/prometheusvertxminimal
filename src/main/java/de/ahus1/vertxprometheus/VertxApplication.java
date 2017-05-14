package de.ahus1.vertxprometheus;

import io.vertx.core.VertxOptions;
import io.vertx.ext.dropwizard.DropwizardMetricsOptions;
import io.vertx.ext.dropwizard.Match;
import io.vertx.ext.dropwizard.MatchType;
import io.vertx.rxjava.core.Vertx;
import lombok.extern.slf4j.Slf4j;

/**
 * Main Class for Verticle
 */
@Slf4j
public class VertxApplication {

    private Vertx vertx;

    public static void main(String[] args) {
        new VertxApplication().run(args);
    }

    private void run(String[] args) {

        vertx = Vertx.vertx(new VertxOptions().setMetricsOptions(
            new DropwizardMetricsOptions()
                // ensure that you give the registry a name (will need it later for prometheus simple client)
                .setRegistryName("vertx")
                .addMonitoredHttpClientEndpoint(
                    new Match().setValue(".*").setType(MatchType.REGEX))
                .setEnabled(true)
        ));

        VerticleWeb verticleWeb = new VerticleWeb();
        vertx.getDelegate().deployVerticle(verticleWeb);
    }

}
