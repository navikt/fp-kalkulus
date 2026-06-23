package no.nav.folketrygdloven.kalkulus.web.jetty;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.callback.BaseCallback;
import org.flywaydb.core.api.callback.Context;
import org.flywaydb.core.api.callback.Event;

import no.nav.folketrygdloven.kalkulus.web.app.konfig.ApiConfig;
import no.nav.folketrygdloven.kalkulus.web.app.konfig.InternalApiConfig;
import no.nav.foreldrepenger.konfig.Environment;
import no.nav.vedtak.felles.jpa.NamingStandard;
import no.nav.vedtak.felles.jpa.flyway.FlywayUtil;
import no.nav.vedtak.felles.jpa.jdbc.DataSourceHolder;
import no.nav.vedtak.log.metrics.MetricsUtil;
import no.nav.vedtak.server.jetty.DataSourceShutdownListener;
import no.nav.vedtak.server.jetty.JettyServerBuilder;

public class JettyServer {

    private static final Environment ENV = Environment.current();

    private static final String CONTEXT_PATH = ENV.getProperty("context.path", "/fpkalkulus");

    private final Integer serverPort;

    JettyServer(int serverPort) {
        this.serverPort = serverPort;
    }

    static void main(String[] args) throws Exception {
        jettyServer(args).bootStrap();
    }

    private static JettyServer jettyServer(String[] args) {
        if (args.length > 0) {
            return new JettyServer(Integer.parseUnsignedInt(args[0]));
        }
        return new JettyServer(ENV.getProperty("server.port", Integer.class, 8080));
    }

    void bootStrap() throws Exception {
        MetricsUtil.init(); // Sett opp registry før andre kobler seg på
        migrerDatabaser();
        konfigurerDataSource();
        start();
    }

    protected void konfigurerDataSource() {
        // Balanser så CP-size = TaskThreads+1 + Antall Connections man ønsker
        System.setProperty("task.manager.runner.threads", "6");
        var dataSource = LocalDatasourceUtil.createDatasource(12);
        DataSourceHolder.initialize(dataSource);
    }

    void migrerDatabaser() {
        try (var dataSource = LocalDatasourceUtil.createMigrationDatasource()) {
            var flyway = FlywayUtil.flywayConfig(dataSource, NamingStandard.DEFAULT_DS_MIGRATION_CLASSPATH);
            if (ENV.isProd() || ENV.isDev()) {
                flyway.callbacks(new BaseCallback() {
                    @Override
                    public boolean supports(Event event, Context context) {
                        return event == Event.AFTER_CONNECT;
                    }

                    @Override
                    public void handle(Event event, Context context) {
                        try (var stmt = context.getConnection().createStatement()) {
                            stmt.execute(String.format("SET ROLE \"%s\"", LocalDatasourceUtil.getRole(DatasourceRole.ADMIN))); // NOSONAR
                        } catch (Exception e) {
                            throw new FlywayException("Kunne ikke sette rolle etter connect", e);
                        }
                    }
                });
            }
            flyway.load().migrate();
        }
    }

    private void start() throws Exception {
        var server = JettyServerBuilder.builder()
            .port(serverPort)
            .contextPath(CONTEXT_PATH)
            .withForwardedRequestCustomizer()
            .addEventListener(new DataSourceShutdownListener(DataSourceHolder::close))
            .registerRestApp(InternalApiConfig.API_URI, InternalApiConfig.class)
            .registerRestApp(ApiConfig.API_URI, ApiConfig.class)
            .build();
        server.start();
        server.join();
    }
}
