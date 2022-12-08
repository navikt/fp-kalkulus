package no.nav.folketrygdloven.kalkulus.jetty;

import static no.nav.k9.felles.konfigurasjon.env.Cluster.LOCAL;
import static no.nav.k9.felles.konfigurasjon.env.Cluster.NAIS_CLUSTER_NAME;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jetty.jaas.JAASLoginService;
import org.eclipse.jetty.plus.jndi.EnvEntry;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.DefaultIdentityService;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.security.jaspi.DefaultAuthConfigFactory;
import org.eclipse.jetty.security.jaspi.JaspiAuthenticatorFactory;
import org.eclipse.jetty.security.jaspi.provider.JaspiAuthConfigProvider;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.ResourceCollection;
import org.eclipse.jetty.webapp.MetaData;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import jakarta.security.auth.message.config.AuthConfigFactory;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import no.nav.folketrygdloven.kalkulus.app.konfig.ApplicationConfig;
import no.nav.folketrygdloven.kalkulus.jetty.db.DatabaseScript;
import no.nav.folketrygdloven.kalkulus.jetty.db.DatasourceRole;
import no.nav.folketrygdloven.kalkulus.jetty.db.DatasourceUtil;
import no.nav.k9.felles.konfigurasjon.env.Environment;
import no.nav.k9.felles.oidc.OidcApplication;
import no.nav.k9.felles.sikkerhet.jaspic.OidcAuthModule;

public class JettyServer {

    private static final Environment ENV = Environment.current();
    private static final Logger log = LoggerFactory.getLogger(JettyServer.class);
    private AppKonfigurasjon appKonfigurasjon;

    public JettyServer() {
        this(new JettyWebKonfigurasjon());
    }

    public JettyServer(int serverPort) {
        this(new JettyWebKonfigurasjon(serverPort));
    }

    JettyServer(AppKonfigurasjon appKonfigurasjon) {
        this.appKonfigurasjon = appKonfigurasjon;
    }

    public static void main(String[] args) throws Exception {
        // for logback import to work
        System.setProperty(NAIS_CLUSTER_NAME, ENV.clusterName());
        jettyServer(args).bootStrap();
    }

    private static JettyServer jettyServer(String[] args) {
        if (args.length > 0) {
            return new JettyServer(Integer.parseUnsignedInt(args[0]));
        }
        return new JettyServer();
    }

    protected void start(AppKonfigurasjon appKonfigurasjon) throws Exception {
        Server server = new Server(appKonfigurasjon.getServerPort());
        server.setConnectors(createConnectors(appKonfigurasjon, server).toArray(new Connector[]{}));

        var handlers = new HandlerList(new ResetLogContextHandler(), createContext(appKonfigurasjon));
        server.setHandler(handlers);
        server.start();
        server.join();
    }

    protected void bootStrap() throws Exception {
        konfigurer();
        migrerDatabaser();
        start(appKonfigurasjon);
    }

    protected void konfigurer() throws Exception {
        konfigurerMiljø();
        konfigurerSikkerhet();
        konfigurerJndi();
    }

    protected void konfigurerMiljø() throws Exception {
        // template method
    }

    protected void konfigurerJndi() throws Exception {
        new EnvEntry("jdbc/defaultDS",
                DatasourceUtil.createDatasource("defaultDS", DatasourceRole.USER, ENV.getCluster(), 4));
    }

    protected void konfigurerSikkerhet() {
        var factory = new DefaultAuthConfigFactory();

        factory.registerConfigProvider(new JaspiAuthConfigProvider(serverAuthModule),
                "HttpServlet",
                "server /ftkalkulus",
                "OIDC Authentication");

        AuthConfigFactory.setFactory(factory);
    }

    protected void migrerDatabaser() throws IOException {
        String initSql = String.format("SET ROLE \"%s\"", DatasourceUtil.getDbRole("defaultDS", DatasourceRole.ADMIN));
        if (LOCAL.equals(ENV.getCluster())) {
            // TODO: Ønsker egentlig ikke dette, men har ikke satt opp skjema lokalt
            // til å ha en admin bruker som gjør migrering og en annen som gjør CRUD
            // operasjoner
            initSql = null;
        }
        try (var migreringDs = DatasourceUtil.createDatasource("defaultDS", DatasourceRole.ADMIN, ENV.getCluster(),
                2)) {
            DatabaseScript.migrate(migreringDs, initSql);
        }
    }

    @SuppressWarnings("resource")
    protected WebAppContext createContext(AppKonfigurasjon appKonfigurasjon) throws IOException {
        var webAppContext = new WebAppContext();
        webAppContext.setParentLoaderPriority(true);

        // må hoppe litt bukk for å hente web.xml fra classpath i stedet for fra
        // filsystem.
        String descriptor;
        try (var resource = Resource.newClassPathResource("/WEB-INF/web.xml")) {
            descriptor = resource.getURI().toURL().toExternalForm();
        }
        webAppContext.setDescriptor(descriptor);
        webAppContext.setBaseResource(createResourceCollection());
        webAppContext.setContextPath(appKonfigurasjon.getContextPath());
        webAppContext.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed", "false");

        webAppContext.setAttribute("org.eclipse.jetty.server.webapp.WebInfIncludeJarPattern",
                "^.*jersey-.*.jar$|^.*felles-.*.jar$");
        webAppContext.setSecurityHandler(createSecurityHandler());
        updateMetaData(webAppContext.getMetaData());
        return webAppContext;
    }

    protected HttpConfiguration createHttpConfiguration() {
        // Create HTTP Config
        HttpConfiguration httpConfig = new HttpConfiguration();

        // Add support for X-Forwarded headers
        httpConfig.addCustomizer(new org.eclipse.jetty.server.ForwardedRequestCustomizer());

        return httpConfig;
    }

    private SecurityHandler createSecurityHandler() {
        ConstraintSecurityHandler securityHandler = new ConstraintSecurityHandler();
        securityHandler.setAuthenticatorFactory(new JaspiAuthenticatorFactory());

        JAASLoginService loginService = new JAASLoginService();
        loginService.setName("jetty-login");
        loginService.setLoginModuleName("jetty-login");
        loginService.setIdentityService(new DefaultIdentityService());
        securityHandler.setLoginService(loginService);

        return securityHandler;
    }

    private void updateMetaData(MetaData metaData) {
        // Find path to class-files while starting jetty from development environment.
        List<Class<?>> appClasses = getWebInfClasses();

        List<Resource> resources = appClasses.stream()
                .map(c -> Resource.newResource(c.getProtectionDomain().getCodeSource().getLocation()))
                .distinct()
                .collect(Collectors.toList());

        metaData.setWebInfClassesResources(resources);
    }

    protected List<Class<?>> getWebInfClasses() {
        return Arrays.asList(ApplicationConfig.class, OidcApplication.class);
    }

    @SuppressWarnings("resource")
    protected List<Connector> createConnectors(AppKonfigurasjon appKonfigurasjon, Server server) {
        List<Connector> connectors = new ArrayList<>();
        ServerConnector httpConnector = new ServerConnector(server,
                new HttpConnectionFactory(createHttpConfiguration()));
        httpConnector.setPort(appKonfigurasjon.getServerPort());
        connectors.add(httpConnector);

        return connectors;
    }

    @SuppressWarnings("resource")
    protected ResourceCollection createResourceCollection() throws IOException {
        return new ResourceCollection(
            Resource.newClassPathResource("META-INF/resources/webjars/"),
            Resource.newClassPathResource("/web"));
    }

    /**
     * Legges først slik at alltid resetter context før prosesserer nye requests.
     * Kjøres først så ikke risikerer andre har satt Request#setHandled(true).
     */
    static final class ResetLogContextHandler extends AbstractHandler {
        @Override
        public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
                throws IOException, ServletException {
            MDC.clear();
        }
    }

}
