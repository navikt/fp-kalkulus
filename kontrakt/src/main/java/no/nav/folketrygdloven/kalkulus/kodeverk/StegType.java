package no.nav.folketrygdloven.kalkulus.kodeverk;

import java.util.Objects;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class StegType extends Kodeverk{
    static final String KODEVERK = "STEG_TYPE";

    public static final StegType KOFAKBER = new StegType("KOFAKBER");
    public static final StegType FORS_BERGRUNN = new StegType("FORS_BERGRUNN");
    public static final StegType FAST_BERGRUNN = new StegType("FAST_BERGRUNN");
    public static final StegType FORDEL_BERGRUNN = new StegType("FORDEL_BERGRUNN");

    @JsonProperty(value = "kode", required = true, index = 1)
    @Pattern(regexp = "^[\\p{L}\\p{N}_\\.\\-]+$", message="Kode '${validatedValue}' matcher ikke tillatt pattern '{regexp}'")
    @Size(min = 3, max = 50)
    @NotNull
    private String kode;

    @JsonCreator
    public StegType(@JsonProperty(value = "kode", required = true) String kode) {
        Objects.requireNonNull(kode, "kode");
        this.kode = kode;
    }

    @Override
    public String getKode() {
        return kode;
    }

    @Override
    public String getKodeverk() {
        return KODEVERK;
    }
}
