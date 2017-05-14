package de.ahus1.vertxprometheus;

import com.codahale.metrics.SharedMetricRegistries;
import io.prometheus.client.dropwizard.DropwizardExports;
import io.prometheus.client.hotspot.DefaultExports;
import io.prometheus.client.vertx.MetricsHandler;
import io.vertx.core.Future;
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

        // add default and dropwizard metrics to prometheus simple clinet
        DefaultExports.initialize();
        // name of the registry needs to match the name at Vert.x startup
        new DropwizardExports(SharedMetricRegistries.getOrCreate("vertx")).register();

        // setup standard router
        final Router router = Router.router(vertx);
        router.route("/").handler(StaticHandler.create().setCachingEnabled(false));

        // map /manage/metrics to prometheus simple client
        final Router apiRouter = Router.router(vertx);
        router.mountSubRouter("/manage", apiRouter);
        apiRouter.route("/metrics").getDelegate().handler(new MetricsHandler());

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
