package no.nav.folketrygdloven.kalkulus.kodeverk;

import java.util.Objects;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Fagsystem extends Kodeverk {
    public static final String KODEVERK = "FAGSYSTEM";

    public static final Fagsystem ARENA = new Fagsystem("ARENA");


    @JsonProperty(value = "kode", required = true, index = 1)
    @Pattern(regexp = "^[0-9_.\\-:]+$", message = "'${validatedValue}' matcher ikke tillatt pattern '{value}'")
    @Size(min = 2, max = 50)
    @NotNull
    private String kode;

    @JsonCreator
    public Fagsystem(@JsonProperty(value = "kode", required = true) String kode) {
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
