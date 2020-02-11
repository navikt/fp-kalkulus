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
public class YtelseTyperKalkulusStøtterKontrakt extends Kodeverk {
    static final String KODEVERK = "YTELSE_TYPE";

    /** Folketrygdloven K4 ytelser. */
    public static final YtelseTyperKalkulusStøtterKontrakt DAGPENGER = new YtelseTyperKalkulusStøtterKontrakt("DAG");//$NON-NLS-1$

    /** Folketrygdloven K8 ytelser. */
    public static final YtelseTyperKalkulusStøtterKontrakt SYKEPENGER = new YtelseTyperKalkulusStøtterKontrakt("SP");//$NON-NLS-1$

    /** Folketrygdloven K9 ytelser. */
    public static final YtelseTyperKalkulusStøtterKontrakt PLEIEPENGER_SYKT_BARN = new YtelseTyperKalkulusStøtterKontrakt("PSB");
    public static final YtelseTyperKalkulusStøtterKontrakt PLEIEPENGER_NÆRSTÅENDE = new YtelseTyperKalkulusStøtterKontrakt("PPN");
    public static final YtelseTyperKalkulusStøtterKontrakt OMSORGSPENGER = new YtelseTyperKalkulusStøtterKontrakt("OMP");
    public static final YtelseTyperKalkulusStøtterKontrakt OPPLÆRINGSPENGER = new YtelseTyperKalkulusStøtterKontrakt("OLP");

    /** Folketrygdloven K11 ytelser. */
    public static final YtelseTyperKalkulusStøtterKontrakt ARBEIDSAVKLARINGSPENGER = new YtelseTyperKalkulusStøtterKontrakt("AAP");//$NON-NLS-1$

    /** Folketrygdloven K14 ytelser. */
    public static final YtelseTyperKalkulusStøtterKontrakt ENGANGSSTØNAD = new YtelseTyperKalkulusStøtterKontrakt("ES"); //$NON-NLS-1$
    public static final YtelseTyperKalkulusStøtterKontrakt FORELDREPENGER = new YtelseTyperKalkulusStøtterKontrakt("FP"); //$NON-NLS-1$
    public static final YtelseTyperKalkulusStøtterKontrakt SVANGERSKAPSPENGER = new YtelseTyperKalkulusStøtterKontrakt("SVP"); //$NON-NLS-1$

    /** Folketrygdloven K15 ytelser. */
    public static final YtelseTyperKalkulusStøtterKontrakt ENSLIG_FORSØRGER = new YtelseTyperKalkulusStøtterKontrakt("EF");//$NON-NLS-1$


    @JsonProperty(value = "kode", required = true, index = 1)
    @Pattern(regexp = "^[\\p{L}\\p{N}_\\.\\-]{2,5}$", message = "Kode '${validatedValue}' matcher ikke tillatt pattern '{regexp}'")
    @Size(min = 2, max = 5)
    @NotNull
    private String kode;

    @JsonCreator
    public YtelseTyperKalkulusStøtterKontrakt(@JsonProperty(value = "kode", required = true) String kode) {
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
