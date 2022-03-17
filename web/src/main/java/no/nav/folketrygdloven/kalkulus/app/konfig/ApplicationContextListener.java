package no.nav.folketrygdloven.kalkulus.app.konfig;

import io.prometheus.client.hotspot.DefaultExports;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/**
 * Starter interne applikasjontjenester
 */
@WebListener
public class ApplicationContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        DefaultExports.initialize();

    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }


}
