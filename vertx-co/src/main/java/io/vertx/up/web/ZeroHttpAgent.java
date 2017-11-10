package io.vertx.up.web;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.up.annotations.Agent;
import io.vertx.up.rs.Hub;
import io.vertx.up.rs.router.EventHub;
import io.vertx.up.rs.router.RouterHub;
import org.vie.cv.Values;
import org.vie.util.Instance;
import org.vie.util.log.Annal;

import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Default Http Server agent for router handlers.
 * Once you have defined another Agent, the default agent will be replaced.
 * Recommend: Do not modify any agent that vertx zero provided.
 */
@Agent
public class ZeroHttpAgent extends AbstractVerticle {

    private static final Annal LOGGER = Annal.get(ZeroHttpAgent.class);
    /**
     * Extract all http server options.
     */
    private static final ConcurrentMap<Integer, HttpServerOptions>
            SERVERS = ZeroGrid.getServerOptions();
    private static final ConcurrentMap<Integer, AtomicInteger>
            LOGS = new ConcurrentHashMap<Integer, AtomicInteger>() {
        {
            SERVERS.forEach((port, option) -> {
                put(port, new AtomicInteger(0));
            });
        }
    };

    private transient final String NAME = getClass().getSimpleName();

    @Override
    public void start() {
        /** 1.Get the default HttpServer Options **/
        SERVERS.forEach((port, option) -> {
            /** 2.Single server processing **/
            final HttpServer server = this.vertx.createHttpServer(option);
            /** 3.Build router with current option **/
            final Router router = Router.router(this.vertx);

            /** 4.Call router hub to mount commont **/
            Hub huber = Instance.singleton(RouterHub.class);
            huber.mount(router);
            /** 5.Call route hub to mount defined **/
            huber = Instance.singleton(EventHub.class);
            huber.mount(router);

            /** 6.Listen for router on the server **/
            server.requestHandler(router::accept).listen();
            {
                // 7. Log output
                recordServer(option, router);
            }
        });
    }

    private void recordServer(final HttpServerOptions options,
                              final Router router) {
        final Integer port = options.getPort();
        final AtomicInteger out = LOGS.get(port);
        if (Values.ZERO == out.getAndIncrement()) {
            // 1. Build logs for current server;
            final String portLiteral = String.valueOf(port);
            LOGGER.info(Message.HTTP_SERVERS, this.NAME, deploymentID(),
                    portLiteral);
            final List<Route> routes = router.getRoutes();
            for (final Route route : routes) {
                // 2.Route
                LOGGER.info(Message.MAPPED_ROUTE, this.NAME, route.getPath(),
                        route.toString());
            }
            // 3. Endpoint Publish
            final String address =
                    MessageFormat.format("http://{0}:{1}/",
                            options.getHost(), portLiteral);
            LOGGER.info(Message.HTTP_LISTEN, this.NAME, address);
        }
    }
}