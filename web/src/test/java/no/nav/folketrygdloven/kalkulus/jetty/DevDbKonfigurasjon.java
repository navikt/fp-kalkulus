package no.nav.folketrygdloven.kalkulus.jetty;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.ClassicConfiguration;

/** Dummy konfig for lokal testing. */
public class DevDbKonfigurasjon {

    private static final String location = "classpath:/db/migration/";

    private String datasource = "defaultDS";
    private String url = "jdbc:postgresql://127.0.0.1:5433/ftkalkulus?reWriteBatchedInserts=true";
    private String user = "ftkalkulus";
    private String password = user;

    static void clean(DataSource dataSource) {
        ClassicConfiguration conf = new ClassicConfiguration();
        conf.setDataSource(dataSource);
        conf.setLocationsAsStrings(location);
        conf.setBaselineOnMigrate(true);
        Flyway flyway = new Flyway(conf);
        try {
            flyway.clean();
        } catch (FlywayException fwe) {
            throw new IllegalStateException("Migrering feiler", fwe);
        }
    }

    DevDbKonfigurasjon() {
    }

    public String getDatasource() {
        return datasource;
    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

}

