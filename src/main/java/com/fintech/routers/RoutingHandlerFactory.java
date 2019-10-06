package com.fintech.routers;

import io.undertow.server.RoutingHandler;

public interface RoutingHandlerFactory {

  RoutingHandler handler();

}
