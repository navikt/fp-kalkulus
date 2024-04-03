package no.nav.folketrygdloven.kalkulus.app.tjenester;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import io.prometheus.client.hotspot.DefaultExports;

/**
 * Starter interne applikasjontjenester
 */
@WebListener
public class ServiceStarterListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        DefaultExports.initialize();

    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }


}
