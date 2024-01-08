package no.nav.folketrygdloven.kalkulus.kodeverk;

/**
 * Typer av arbeidsforhold.
 * <p>
 * <h3>Kilde: NAV kodeverk</h3>
 * https://modapp.adeo.no/kodeverksklient/viskodeverk/Arbeidsforholdstyper/2
 * <p>
 * <h3>Tjeneste(r) som returnerer dette:</h3>
 * <ul>
 * <li>https://confluence.adeo.no/display/SDFS/tjeneste_v3%3Avirksomhet%3AArbeidsforhold_v3</li>
 * </ul>
 * <h3>Tjeneste(r) som konsumerer dete:</h3>
 * <ul>
 * <li></li>
 * </ul>
 */

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;


@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum ArbeidType implements Kodeverdi, KontraktKode {

    ETTERLØNN_SLUTTPAKKE("ETTERLØNN_SLUTTPAKKE", "Etterlønn eller sluttpakke"),
    FORENKLET_OPPGJØRSORDNING("FORENKLET_OPPGJØRSORDNING", "Forenklet oppgjørsordning "),
    FRILANSER("FRILANSER", "Frilanser, samlet aktivitet"),
    FRILANSER_OPPDRAGSTAKER_MED_MER("FRILANSER_OPPDRAGSTAKER", "Frilansere/oppdragstakere, med mer"),
    LØNN_UNDER_UTDANNING("LØNN_UNDER_UTDANNING", "Lønn under utdanning"),
    MARITIMT_ARBEIDSFORHOLD("MARITIMT_ARBEIDSFORHOLD", "Maritimt arbeidsforhold"),
    MILITÆR_ELLER_SIVILTJENESTE("MILITÆR_ELLER_SIVILTJENESTE", "Militær eller siviltjeneste"),
    ORDINÆRT_ARBEIDSFORHOLD("ORDINÆRT_ARBEIDSFORHOLD", "Ordinært arbeidsforhold"),
    PENSJON_OG_ANDRE_TYPER_YTELSER_UTEN_ANSETTELSESFORHOLD("PENSJON_OG_ANDRE_TYPER_YTELSER_UTEN_ANSETTELSESFORHOLD", "Pensjoner og andre typer ytelser"
    ),
    SELVSTENDIG_NÆRINGSDRIVENDE("NÆRING", "Selvstendig næringsdrivende"),
    UTENLANDSK_ARBEIDSFORHOLD("UTENLANDSK_ARBEIDSFORHOLD", "Arbeid i utlandet"),
    VENTELØNN_VARTPENGER("VENTELØNN_VARTPENGER", "Ventelønn eller vartpenger"),
    VANLIG("VANLIG", "Vanlig"),
    UDEFINERT("-", "Ikke definert"),
    ;

    public static final Set<ArbeidType> AA_REGISTER_TYPER = Set.of(
            ArbeidType.ORDINÆRT_ARBEIDSFORHOLD,
            ArbeidType.MARITIMT_ARBEIDSFORHOLD,
            ArbeidType.FORENKLET_OPPGJØRSORDNING);

    private static final Map<String, ArbeidType> KODER = new LinkedHashMap<>();

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    @JsonValue
    private final String kode;

    @JsonIgnore
    private final String navn;

    ArbeidType(String kode, String navn) {
        this.kode = kode;
        this.navn = navn;
    }

    @JsonCreator(mode = Mode.DELEGATING)
    public static ArbeidType fraKode(Object node) {
        if (node == null) {
            return null;
        }
        String kode = TempAvledeKode.getVerdi(ArbeidType.class, node, "kode");
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent ArbeidType: " + kode);
        }
        return ad;
    }

    @Override
    public String getKode() {
        return kode;
    }

}
