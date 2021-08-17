package no.nav.folketrygdloven.kalkulus.jetty;

import no.nav.k9.felles.sikkerhet.ContextPathHolder;

public class JettyWebKonfigurasjon implements AppKonfigurasjon {
    private static final String CONTEXT_PATH = "/ftkalkulus";
    private static final String SWAGGER_HASH = "sha256-/k28/Xs33wXyOXYCOpKXui8gJ/Y6nxd5AnKA9iP63/s=";

    private Integer serverPort;

    public JettyWebKonfigurasjon() {
        ContextPathHolder.instance(CONTEXT_PATH);
    }

    public JettyWebKonfigurasjon(int serverPort) {
        this();
        this.serverPort = serverPort;
    }

    @Override
    public int getServerPort() {
        if (serverPort == null) {
            return DEFAULT_SERVER_PORT;
        }
        return serverPort;
    }

    @Override
    public String getContextPath() {
        return CONTEXT_PATH;
    }

    @Override
    public int getSslPort() {
        throw new IllegalStateException("SSL port should only be used locally");
    }

    @Override
    public String getSwaggerHash() {
        return SWAGGER_HASH;
    }
}
