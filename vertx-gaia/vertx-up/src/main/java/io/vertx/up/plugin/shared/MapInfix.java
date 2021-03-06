package io.vertx.up.plugin.shared;

import io.vertx.core.Vertx;
import io.vertx.up.annotations.Plugin;
import io.vertx.up.func.Fn;
import io.vertx.up.plugin.Infix;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Plugin
@SuppressWarnings("all")
public class MapInfix implements Infix {

    private static final String NAME = "ZERO_MAP_POOL";

    private static final ConcurrentMap<String, SharedClient<String, Object>> CLIENTS
            = new ConcurrentHashMap<>();

    private static void initInternal(final Vertx vertx,
                                     final String name) {
        Fn.pool(CLIENTS, name,
                () -> Infix.initTp("shared",
                        (config) -> SharedClient.createShared(vertx, config, name),
                        MapInfix.class));
    }

    public static void init(final Vertx vertx) {
        initInternal(vertx, NAME);
    }

    @Override
    public SharedClient<String, Object> get() {
        return getClient();
    }

    public static SharedClient<String, Object> getClient() {
        return CLIENTS.get(NAME);
    }

    public static String getDefaultName() {
        return NAME;
    }
}
