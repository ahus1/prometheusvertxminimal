package de.ahus1.vertxprometheus;

import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.hotspot.*;
import io.prometheus.client.vertx.MetricsHandler;
import io.vertx.core.Future;
import io.vertx.micrometer.backends.BackendRegistries;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.http.HttpServer;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.handler.StaticHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * Main Verticles class defines the http socket configurations.
 *
 * @author Alexander Schwartz 2016
 */
@Slf4j
public class VerticleWeb extends AbstractVerticle {

    private int port = 8081;

    private HttpServer httpServer;

    @Override
    public void start(Future<Void> fut) {

        // add default metrics to prometheus client
        CollectorRegistry registry =
                ((PrometheusMeterRegistry) BackendRegistries.getDefaultNow()).getPrometheusRegistry();
        (new StandardExports()).register(registry);
        (new MemoryPoolsExports()).register(registry);
        (new BufferPoolsExports()).register(registry);
        (new GarbageCollectorExports()).register(registry);
        (new ThreadExports()).register(registry);
        (new ClassLoadingExports()).register(registry);
        (new VersionInfoExports()).register(registry);

        // setup standard router
        final Router router = Router.router(vertx);
        router.route("/").handler(StaticHandler.create().setCachingEnabled(false));

        // map /metrics to prometheus
        router.route("/metrics").getDelegate().handler(new MetricsHandler(registry));

        // create web server.
        vertx.createHttpServer().requestHandler(router::accept).rxListen(port).subscribe((httpServer) -> {
            this.httpServer = httpServer;
            log.info("Endpoint started on port " + port);
            fut.complete();
        }, fut::fail);
    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
        httpServer.rxClose().subscribe(stopFuture::complete, stopFuture::fail);
    }

}
