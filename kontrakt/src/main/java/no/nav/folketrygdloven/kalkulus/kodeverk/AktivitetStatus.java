package no.nav.folketrygdloven.kalkulus.kodeverk;

import java.util.Objects;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AktivitetStatus extends Kodeverk{
    static final String KODEVERK = "AKTIVITET_STATUS";

    public static final AktivitetStatus ARBEIDSTAKER = new AktivitetStatus("AT");
    public static final AktivitetStatus BRUKERS_ANDEL = new AktivitetStatus("BA");
    public static final AktivitetStatus KUN_YTELSE = new AktivitetStatus("KUN_YTELSE");
    public static final AktivitetStatus FRILANSER = new AktivitetStatus("FL");
    public static final AktivitetStatus SELVSTENDIG_NÆRINGSDRIVENDE = new AktivitetStatus("SN");


    @JsonProperty(value = "kode", required = true, index = 1)
    @Pattern(regexp = "^[\\p{L}\\p{N}_\\.\\-]+$", message="Kode '${validatedValue}' matcher ikke tillatt pattern '{regexp}'")
    @Size(min = 2, max = 50)
    @NotNull
    private String kode;

    @JsonCreator
    public AktivitetStatus(@JsonProperty(value = "kode", required = true) String kode) {
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
