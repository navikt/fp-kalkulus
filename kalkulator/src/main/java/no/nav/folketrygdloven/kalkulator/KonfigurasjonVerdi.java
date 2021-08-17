package no.nav.folketrygdloven.kalkulator;

public final class KonfigurasjonVerdi {

    private KonfigurasjonVerdi() {
    }

    public static boolean get(String key, boolean defaultValue) {
        var property = System.getProperty(key);
        if (property != null) {
            return Boolean.parseBoolean(property);
        }
        var getenv = System.getenv(key);
        if (getenv != null) {
            return Boolean.parseBoolean(getenv);
        }

        return defaultValue;
    }
}
