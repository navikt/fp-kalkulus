package no.nav.folketrygdloven.kalkulus.app.tjenester;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/**
 * Starter interne applikasjontjenester
 */
@WebListener
public class ServiceStarterListener implements ServletContextListener {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ServiceStarterListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        LOG.info("fp-kalkulus har startet.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LOG.info("fp-kalkulus stoppes.");
    }


}
