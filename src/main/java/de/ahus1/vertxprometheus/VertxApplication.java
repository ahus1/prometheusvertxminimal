package de.ahus1.vertxprometheus;

import io.vertx.core.VertxOptions;
import io.vertx.micrometer.MicrometerMetricsOptions;
import io.vertx.micrometer.VertxPrometheusOptions;
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
                new MicrometerMetricsOptions()
                        .setPrometheusOptions(new VertxPrometheusOptions().setEnabled(true))
                        .setEnabled(true)
        ));

        VerticleWeb verticleWeb = new VerticleWeb();
        vertx.getDelegate().deployVerticle(verticleWeb);
    }

}
