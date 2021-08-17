package no.nav.folketrygdloven.kalkulus.felles.feil;

public class TekniskException extends RuntimeException {
    private final String id;
    private final String feilmelding;

    public TekniskException(String id, String feilmelding) {
        this.id = id;
        this.feilmelding = feilmelding;
    }

    public String getId() {
        return id;
    }

    public String getFeilmelding() {
        return feilmelding;
    }

    @Override
    public String toString() {
        return "TekniskException{" +
                "id='" + id + '\'' +
                ", feilmelding='" + feilmelding + '\'' +
                '}';
    }
}
