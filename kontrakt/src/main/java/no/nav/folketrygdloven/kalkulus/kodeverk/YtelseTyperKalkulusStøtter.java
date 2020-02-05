package no.nav.folketrygdloven.kalkulus.kodeverk;

import java.util.Objects;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Definerer ytelse typer støttet i Kalkulus
 *
 * <h3>Bruk av konstanter</h3>
 * konstanter representerer definerte eksempler på kjente konstanter. Nye kan - men må ikke - legges her - såfremt
 * avsender/mottager er kjent med de og kan håndtere de.
 */
public class YtelseTyperKalkulusStøtter extends Kodeverk {
    static final String KODEVERK = "YTELSE_TYPE";

    /** Folketrygdloven K4 ytelser. */
    public static final YtelseTyperKalkulusStøtter DAGPENGER = new YtelseTyperKalkulusStøtter("DAG");//$NON-NLS-1$

    /** Folketrygdloven K8 ytelser. */
    public static final YtelseTyperKalkulusStøtter SYKEPENGER = new YtelseTyperKalkulusStøtter("SP");//$NON-NLS-1$

    /** Folketrygdloven K9 ytelser. */
    public static final YtelseTyperKalkulusStøtter PLEIEPENGER_SYKT_BARN = new YtelseTyperKalkulusStøtter("PSB");
    public static final YtelseTyperKalkulusStøtter PLEIEPENGER_NÆRSTÅENDE = new YtelseTyperKalkulusStøtter("PPN");
    public static final YtelseTyperKalkulusStøtter OMSORGSPENGER = new YtelseTyperKalkulusStøtter("OMP");
    public static final YtelseTyperKalkulusStøtter OPPLÆRINGSPENGER = new YtelseTyperKalkulusStøtter("OLP");

    /** Folketrygdloven K11 ytelser. */
    public static final YtelseTyperKalkulusStøtter ARBEIDSAVKLARINGSPENGER = new YtelseTyperKalkulusStøtter("AAP");//$NON-NLS-1$

    /** Folketrygdloven K14 ytelser. */
    public static final YtelseTyperKalkulusStøtter ENGANGSSTØNAD = new YtelseTyperKalkulusStøtter("ES"); //$NON-NLS-1$
    public static final YtelseTyperKalkulusStøtter FORELDREPENGER = new YtelseTyperKalkulusStøtter("FP"); //$NON-NLS-1$
    public static final YtelseTyperKalkulusStøtter SVANGERSKAPSPENGER = new YtelseTyperKalkulusStøtter("SVP"); //$NON-NLS-1$

    /** Folketrygdloven K15 ytelser. */
    public static final YtelseTyperKalkulusStøtter ENSLIG_FORSØRGER = new YtelseTyperKalkulusStøtter("EF");//$NON-NLS-1$


    @JsonProperty(value = "kode", required = true, index = 1)
    @Pattern(regexp = "^[\\p{L}\\p{N}_\\.\\-]{2,5}$", message = "Kode '${validatedValue}' matcher ikke tillatt pattern '{regexp}'")
    @Size(min = 2, max = 5)
    @NotNull
    private String kode;

    @JsonCreator
    public YtelseTyperKalkulusStøtter(@JsonProperty(value = "kode", required = true) String kode) {
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
