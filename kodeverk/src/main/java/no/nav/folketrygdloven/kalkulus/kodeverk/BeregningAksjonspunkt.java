package no.nav.folketrygdloven.kalkulus.kodeverk;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * Definerer aksjonspunkter i beregning.
 */
@JsonFormat(shape = Shape.OBJECT)
@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public
enum BeregningAksjonspunkt implements Kodeverdi {

    // 5000 vanlig saksbehandlig
    FASTSETT_BEREGNINGSGRUNNLAG_ARBEIDSTAKER_FRILANS("5038", "Fastsette beregningsgrunnlag for arbeidstaker/frilanser skjønnsmessig"),
    VURDER_VARIG_ENDRET_ELLER_NYOPPSTARTET_NÆRING_SELVSTENDIG_NÆRINGSDRIVENDE("5039", "Vurder varig endret/nyoppstartet næring selvstendig næringsdrivende"),
    FASTSETT_BEREGNINGSGRUNNLAG_SELVSTENDIG_NÆRINGSDRIVENDE("5042" ,"Fastsett beregningsgrunnlag for selvstendig næringsdrivende"),
    FORDEL_BEREGNINGSGRUNNLAG("5046", "Fordel beregningsgrunnlag"),
    FASTSETT_BEREGNINGSGRUNNLAG_TIDSBEGRENSET_ARBEIDSFORHOLD("5047", "Fastsett beregningsgrunnlag for tidsbegrenset arbeidsforhold"),
    FASTSETT_BEREGNINGSGRUNNLAG_FOR_SN_NY_I_ARBEIDSLIVET("5049", "Fastsett beregningsgrunnlag for SN som er ny i arbeidslivet"),
    VURDER_GRADERING_UTEN_BEREGNINGSGRUNNLAG("5050","Vurder gradering på andel uten beregningsgrunnlag"),
    AVKLAR_AKTIVITETER("5052","Avklar aktivitet for beregning"),
    VURDER_FAKTA_FOR_ATFL_SN("5058","Vurder fakta for arbeidstaker, frilans og selvstendig næringsdrivende"),
    VURDER_REFUSJONSKRAV("5059", "Vurder refusjonskrav for beregningen"),

    // 6000 overstyring
    OVERSTYRING_AV_BEREGNINGSGRUNNLAG("6015","Overstyring av beregningsgrunnlag"),

    // 7000 automatisk satt på vent
    AUTO_VENT_PÅ_INNTEKT_RAPPORTERINGSFRIST("7014","Vent på rapporteringsfrist for inntekt"),
    AUTO_VENT_PÅ_SISTE_AAP_ELLER_DP_MELDEKORT("7020","Vent på siste meldekort for AAP eller DP-mottaker"),

    // 8000 frisinn
    AUTO_VENT_FRISINN("8000", "Sak settes på vent på grunn av manglende funksjonalitet"),
    INGEN_AKTIVITETER("8001", "Gir avslag"),

    UNDEFINED,

    // Punkter som ikke lenger utledes, tas vare på så vi vet hvilke koder kalkulus har brukt før
    @Deprecated
    AUTO_VENT_ULIKE_STARTDATOER_SVP("7026", "Autopunkt ulike startdatoer svangerskapspenger"),
    @Deprecated
    AUTO_VENT_DELVIS_TILRETTELEGGING_OG_REFUSJON_SVP("7027","Autopunkt delvis SVP og refusjon"),
    @Deprecated
    AUTO_VENT_PÅ_MANGLENDE_ARBEIDSFORHOLD_KOMMUNEREFORM("7036", "Sak settes på vent pga kommune- og fylkesammenslåing."),
    ;

    static final String KODEVERK = "BEREGNING_AKSJONSPUNKT_DEF";

    private static final Map<String, BeregningAksjonspunkt> KODER = new LinkedHashMap<>();

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

    private BeregningAksjonspunkt() {
        // for hibernate
    }

    private BeregningAksjonspunkt(String kode, String navn) {
        this.kode = Objects.requireNonNull(kode);
        this.navn = navn;
    }

    @JsonProperty
    @Override
    public String getKode() {
        return kode;
    }

    @JsonProperty
    @Override
    public String getKodeverk() {
        return KODEVERK;
    }

    @Override
    public String toString() {
        return super.toString() + "('" + getKode() + "')";
    }

    @JsonCreator(mode = Mode.DELEGATING)
    public static BeregningAksjonspunkt fraKode(Object node) {
        if (node == null) {
            return null;
        }
        String kode = TempAvledeKode.getVerdi(BeregningAksjonspunkt.class, node, "kode");
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent BeregningAksjonspunktDefinisjon: " + kode);
        }
        return ad;
    }

    public static Map<String, BeregningAksjonspunkt> kodeMap() {
        return Collections.unmodifiableMap(KODER);
    }
}
