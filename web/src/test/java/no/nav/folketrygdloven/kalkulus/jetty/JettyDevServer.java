package no.nav.folketrygdloven.kalkulus.jetty;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.folketrygdloven.kalkulus.jetty.db.DatasourceRole;
import no.nav.folketrygdloven.kalkulus.jetty.db.DatasourceUtil;
import no.nav.vedtak.util.env.Environment;

public class JettyDevServer extends JettyServer {
    private static final Environment ENV = Environment.current();

    private static final Logger log = LoggerFactory.getLogger(JettyDevServer.class);

    public JettyDevServer() {
        super(new JettyDevKonfigurasjon());
    }

    public static void main(String[] args) throws Exception {
        JettyDevServer devServer = new JettyDevServer();
        devServer.bootStrap();
    }

    private static String initCryptoStoreConfig(String storeName, String storeProperty, String storePasswordProperty,
            String defaultPassword) {
        String defaultLocation = getProperty("user.home", ".") + "/.modig/" + storeName + ".jks";

        String storePath = getProperty(storeProperty, defaultLocation);
        File storeFile = new File(storePath);
        if (!storeFile.exists()) {
            throw new IllegalStateException("Finner ikke " + storeName + " i " + storePath
                    + "\n\tKonfigurer enten som System property '" + storeProperty + "' eller environment variabel '"
                    + storeProperty.toUpperCase().replace('.', '_') + "'");
        }
        String password = getProperty(storePasswordProperty, defaultPassword);
        if (password == null) {
            throw new IllegalStateException(
                    "Passord for å aksessere store " + storeName + " i " + storePath + " er null");
        }

        System.setProperty(storeProperty, storeFile.getAbsolutePath());
        System.setProperty(storePasswordProperty, password);
        return storePath;
    }

    private static String getProperty(String key, String defaultValue) {
        String val = System.getProperty(key, defaultValue);
        if (val == null) {
            val = System.getenv(key.toUpperCase().replace('.', '_'));
            val = val == null ? defaultValue : val;
        }
        return val;
    }

    @Override
    protected void migrerDatabaser() {
        DataSource migreringDs = DatasourceUtil.createDatasource("defaultDS", DatasourceRole.ADMIN, ENV.getCluster(),
                1);
        try {
            JettyDevDbKonfigurasjon.kjørMigreringForDev(migreringDs);
            migreringDs.getConnection().close();
        } catch (SQLException e) {
            log.warn("Klarte ikke stenge connection etter migrering", e);
        }
    }

    @Override
    protected void konfigurerMiljø() throws Exception {
        System.setProperty("develop-local", "true");
        PropertiesUtils.lagPropertiesFilFraTemplate();
        PropertiesUtils.initProperties();

        List<JettyDevDbKonfigurasjon> konfigs = PropertiesUtils.getDBConnectionProperties()
                .stream()
                .filter(jettyDevDbKonfigurasjon -> jettyDevDbKonfigurasjon.getDatasource().equals("defaultDS"))
                .collect(Collectors.toList());
        if (konfigs.size() == 1) {
            final JettyDevDbKonfigurasjon konfig = konfigs.get(0);
            System.setProperty("defaultDS.url", konfig.getUrl());
            System.setProperty("defaultDS.username", konfig.getUser()); // benyttes kun hvis vault.enable=false
            System.setProperty("defaultDS.password", konfig.getPassword()); // benyttes kun hvis vault.enable=false
        } else {
            throw new RuntimeException("forventet én datasourc-konfiger med defaultDS, men fant " + konfigs.size());
        }
    }

    @Override
    protected void konfigurerSikkerhet() {
        System.setProperty("conf", "src/main/resources/jetty/");
        super.konfigurerSikkerhet();

        // truststore avgjør hva vi stoler på av sertifikater når vi gjør utadgående TLS kall
        initCryptoStoreConfig("truststore", "javax.net.ssl.trustStore", "javax.net.ssl.trustStorePassword", "changeit");
        initCryptoStoreConfig("keystore", "javax.net.ssl.keyStore", "javax.net.ssl.keyStorePassword", "changeit");
    }

    @SuppressWarnings("resource")
    @Override
    protected List<Connector> createConnectors(AppKonfigurasjon appKonfigurasjon, Server server) {
        List<Connector> connectors = super.createConnectors(appKonfigurasjon, server);

        SslContextFactory sslContextFactory = new SslContextFactory.Server();
        sslContextFactory.setKeyStorePath(System.getProperty("javax.net.ssl.keyStore"));
        sslContextFactory.setKeyStorePassword(System.getProperty("javax.net.ssl.keyStorePassword"));
        sslContextFactory.setKeyManagerPassword(System.getProperty("javax.net.ssl.keyStorePassword"));

        HttpConfiguration https = createHttpConfiguration();
        https.addCustomizer(new SecureRequestCustomizer());

        ServerConnector sslConnector = new ServerConnector(server,
            new SslConnectionFactory(sslContextFactory, "http/1.1"),
            new HttpConnectionFactory(https));
        sslConnector.setPort(appKonfigurasjon.getSslPort());
        connectors.add(sslConnector);

        return connectors;
    }

    @Override
    protected WebAppContext createContext(AppKonfigurasjon appKonfigurasjon) throws IOException {
        WebAppContext webAppContext = super.createContext(appKonfigurasjon);
        // https://www.eclipse.org/jetty/documentation/9.4.x/troubleshooting-locked-files-on-windows.html
        webAppContext.setInitParameter("org.eclipse.jetty.servlet.Default.useFileMappedBuffer", "false");
        return webAppContext;
    }
}
