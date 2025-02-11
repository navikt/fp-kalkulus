package no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter;

import java.util.Objects;
import java.util.regex.Pattern;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class AktørId {
    private static final String VALID_REGEXP = "^\\d{13}$";
    private static final Pattern VALID = Pattern.compile(VALID_REGEXP, Pattern.CASE_INSENSITIVE);

    @Column(name = "aktoer_id", updatable = false)
    private String aktørId;

    protected AktørId() {
        // For hibernate
    }

    public AktørId(String aktørId) {
        Objects.requireNonNull(aktørId, "aktørId");
        this.aktørId = validateAktørId(aktørId);
    }

    public String getId() {
        return aktørId;
    }

    private String validateAktørId(String aktørId) {
        Objects.requireNonNull(aktørId, "aktørId");
        if (!VALID.matcher(aktørId).matches()) {
            // skal ikke skje, funksjonelle feilmeldinger håndteres ikke her.
            throw new IllegalArgumentException("Ugyldig aktørId '" + aktørId +"', tillatt pattern: "+ VALID_REGEXP);
        }
        return aktørId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || !(obj.getClass().equals(this.getClass()))) {
            return false;
        }
        AktørId other = (AktørId) obj;
        return Objects.equals(aktørId, other.aktørId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(aktørId);
    }

    @Override
    public String toString() {
        return getClass().getName() + "{" +
            "aktørId=" + aktørId +
            '}';
    }
}
