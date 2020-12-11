package no.nav.folketrygdloven.kalkulus.jetty;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class PropertiesUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesUtils.class);

    private static String JETTY_SCHEMAS_LOCAL = "jetty_web_server.json";
    private static String DEV_FILNAVN = "app.properties";
    private static String DEV_FILNAVN_LOCAL = "app-local.properties";
    private static String VTP_FILNAVN_LOCAL = "app-vtp.properties";

    private PropertiesUtils() {
    }

    static List<JettyDevDbKonfigurasjon> getDBConnectionProperties() throws IOException {
        ClassLoader classLoader = PropertiesUtils.class.getClassLoader();
        File file = new File(classLoader.getResource(JETTY_SCHEMAS_LOCAL).getFile());
        return JettyDevDbKonfigurasjon.fraFil(file);
    }

    static void lagPropertiesFilFraTemplate() throws IOException {
        // create local file for passwords etc
        File localProps = new File(DEV_FILNAVN_LOCAL);
        if (!localProps.exists()) {
            if (!localProps.createNewFile()) {
                LOGGER.error("Kunne ikke opprette properties-fil {}", localProps.getAbsolutePath());
            }
        }
    }

    static void initProperties() {
        File devFil = new File(DEV_FILNAVN);
        loadPropertyFile(devFil);
        loadPropertyFile(new File(DEV_FILNAVN_LOCAL));
        loadPropertyFile(new File(VTP_FILNAVN_LOCAL));
    }

    private static void loadPropertyFile(File devFil) {
        if (devFil.exists()) {
            Properties prop = new Properties();
            try (InputStream inputStream = new FileInputStream(devFil)) {
                prop.load(inputStream);
            } catch (IOException e) {
                LOGGER.error("Kunne ikke finne properties-fil", e);
            }
            System.getProperties().putAll(prop);
        }
    }
}
