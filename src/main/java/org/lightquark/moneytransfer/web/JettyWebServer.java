package org.lightquark.moneytransfer.web;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.lightquark.moneytransfer.config.Config;

@Slf4j
public class JettyWebServer {

    private static final String PORT_PROPERTY = "server_port";
    private static final int DEFAULT_PORT = 8080;
    private static final String CONTROLLER_PATH = "org.lightquark.moneytransfer.controller";
    private static final String CONTEXT_PATH = "/*";

    private Server server;

    public void start() {
        server = createServer();
        try {
            server.start();
            server.join();
        } catch (Exception e) {
            log.error("Web server failed with exception {}", e.getMessage(), e);
            Runtime.getRuntime().exit(1);
        } finally {
            server.destroy();
        }
    }

    public void startForTests() {
        server = createServer();
        try {
            server.start();
        } catch (Exception e) {
            log.error("Web server failed with exception {}", e.getMessage(), e);
            Runtime.getRuntime().exit(1);
        }
    }

    public void stop() {
        try {
            server.stop();
        } catch (Exception e) {
            log.error("Web server stopped with exception {}", e.getMessage(), e);
            Runtime.getRuntime().exit(1);
        } finally {
            server.destroy();
        }
    }

    private Server createServer() {
        Server server = new Server(Config.getInteger(PORT_PROPERTY, DEFAULT_PORT));
        ResourceConfig config = new ResourceConfig();
        config.packages(CONTROLLER_PATH);
        config.register(JacksonFeature.class);
        ServletHolder servlet = new ServletHolder(new ServletContainer(config));
        ServletContextHandler context = new ServletContextHandler(server, CONTEXT_PATH);
        context.addServlet(servlet, CONTEXT_PATH);
        return server;
    }
}
