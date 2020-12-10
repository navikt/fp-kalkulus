package no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene;

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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.felles.kodeverk.Kodeverdi;
import no.nav.folketrygdloven.kalkulus.kodeverk.TempAvledeKode;


@JsonFormat(shape = Shape.OBJECT)
@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum ArbeidType implements Kodeverdi {

    ETTERLØNN_SLUTTPAKKE("ETTERLØNN_SLUTTPAKKE", "Etterlønn eller sluttpakke", null),
    FORENKLET_OPPGJØRSORDNING("FORENKLET_OPPGJØRSORDNING", "Forenklet oppgjørsordning ", "forenkletOppgjoersordning"),
    FRILANSER("FRILANSER", "Frilanser, samlet aktivitet", null),
    FRILANSER_OPPDRAGSTAKER_MED_MER("FRILANSER_OPPDRAGSTAKER", "Frilansere/oppdragstakere, med mer", "frilanserOppdragstakerHonorarPersonerMm"),
    LØNN_UNDER_UTDANNING("LØNN_UNDER_UTDANNING", "Lønn under utdanning", null),
    MARITIMT_ARBEIDSFORHOLD("MARITIMT_ARBEIDSFORHOLD", "Maritimt arbeidsforhold", "maritimtArbeidsforhold"),
    MILITÆR_ELLER_SIVILTJENESTE("MILITÆR_ELLER_SIVILTJENESTE", "Militær eller siviltjeneste", null),
    ORDINÆRT_ARBEIDSFORHOLD("ORDINÆRT_ARBEIDSFORHOLD", "Ordinært arbeidsforhold", "ordinaertArbeidsforhold"),
    PENSJON_OG_ANDRE_TYPER_YTELSER_UTEN_ANSETTELSESFORHOLD("PENSJON_OG_ANDRE_TYPER_YTELSER_UTEN_ANSETTELSESFORHOLD", "Pensjoner og andre typer ytelser",
            "pensjonOgAndreTyperYtelserUtenAnsettelsesforhold"),
    SELVSTENDIG_NÆRINGSDRIVENDE("NÆRING", "Selvstendig næringsdrivende", null),
    UTENLANDSK_ARBEIDSFORHOLD("UTENLANDSK_ARBEIDSFORHOLD", "Arbeid i utlandet", null),
    VENTELØNN_VARTPENGER("VENTELØNN_VARTPENGER", "Ventelønn eller vartpenger", null),
    VANLIG("VANLIG", "Vanlig", "VANLIG"),
    UDEFINERT("-", "Ikke definert", null),
    ;

    public static final Set<ArbeidType> AA_REGISTER_TYPER = Set.of(
        ArbeidType.ORDINÆRT_ARBEIDSFORHOLD,
        ArbeidType.MARITIMT_ARBEIDSFORHOLD,
        ArbeidType.FORENKLET_OPPGJØRSORDNING);

    public static final String KODEVERK = "ARBEID_TYPE";

    private static final Map<String, ArbeidType> KODER = new LinkedHashMap<>();

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    private String kode;

    @JsonIgnore
    private String navn;

    @JsonIgnore
    private String offisiellKode;

    ArbeidType(String kode, String navn, String offisiellKode) {
        this.kode = kode;
        this.navn = navn;
        this.offisiellKode = offisiellKode;
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
    
    public static Map<String, ArbeidType> kodeMap() {
        return Collections.unmodifiableMap(KODER);
    }

    @JsonProperty
    @Override
    public String getKode() {
        return kode;
    }

}
