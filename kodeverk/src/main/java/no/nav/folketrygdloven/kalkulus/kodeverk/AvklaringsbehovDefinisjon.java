package no.nav.folketrygdloven.kalkulus.kodeverk;

import static no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovType.AUTOPUNKT;
import static no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovType.MANUELL;
import static no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovType.OVERSTYRING;

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
 * Definerer avklaringsbehov som kan utledes i beregning.
 */
@JsonFormat(shape = Shape.OBJECT)
@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public
enum AvklaringsbehovDefinisjon implements Kodeverdi {

    // 5000 vanlig saksbehandlig
    FASTSETT_BG_AT_FL("5038", "FASTSETT_BG_AT_FL", MANUELL, BeregningSteg.FORS_BERGRUNN, "Fastsette beregningsgrunnlag for arbeidstaker/frilanser skjønnsmessig"),
    VURDER_VARIG_ENDRT_NYOPPSTR_NAERNG_SN("5039", "VURDER_VARIG_ENDRT_NYOPPSTR_NAERNG_SN", MANUELL, BeregningSteg.FORTS_FORS_BERGRUNN, "Vurder varig endret/nyoppstartet næring selvstendig næringsdrivende"),
    VURDER_VARIG_ENDRT_ARB_SITSJN_MDL_INAKTV("5054", "VURDER_VARIG_ENDRT_ARB_SITSJN_MDL_INAKTV", MANUELL, BeregningSteg.FORTS_FORS_BERGRUNN, "Vurder varig endret arbeidssituasjon for bruker som er midlertidig inaktiv"),
    FASTSETT_BG_SN("5042", "FASTSETT_BG_SN", MANUELL, BeregningSteg.FORTS_FORS_BERGRUNN, "Fastsett beregningsgrunnlag for selvstendig næringsdrivende"),
    FORDEL_BG("5046", "FORDEL_BG", MANUELL, BeregningSteg.FORDEL_BERGRUNN, "Fordel beregningsgrunnlag"),
    FASTSETT_BG_TB_ARB("5047", "FASTSETT_BG_TB_ARB", MANUELL, BeregningSteg.FORS_BERGRUNN, "Fastsett beregningsgrunnlag for tidsbegrenset arbeidsforhold"),
    VURDER_NYTT_INNTKTSFRHLD("5067", "VURDER_NYTT_INNTKTSFRHLD", MANUELL, BeregningSteg.FORDEL_BERGRUNN, "Vurder nytt inntektsforhold"),
    VURDER_REPRESENTERER_STORTINGET("5087", "VURDER_REPRSNTR_STORTNGT", MANUELL, BeregningSteg.FORDEL_BERGRUNN, "Vurder om bruker representerer stortinget i perioden"),

    FASTSETT_BG_SN_NY_I_ARB_LIVT("5049", "FASTSETT_BG_SN_NY_I_ARB_LIVT", MANUELL, BeregningSteg.FORTS_FORS_BERGRUNN, "Fastsett beregningsgrunnlag for SN som er ny i arbeidslivet"),
    VURDER_GRADERING_UTEN_BG("5050", "VURDER_GRADERING_UTEN_BG", MANUELL, BeregningSteg.FAST_BERGRUNN, "Vurder gradering på andel uten beregningsgrunnlag"),
    AVKLAR_AKTIVITETER("5052", "AVKLAR_AKTIVITETER", MANUELL, BeregningSteg.FASTSETT_STP_BER, "Avklar aktivitet for beregning"),
    VURDER_FAKTA_ATFL_SN("5058", "VURDER_FAKTA_ATFL_SN", MANUELL, BeregningSteg.KOFAKBER, "Vurder fakta for arbeidstaker, frilans og selvstendig næringsdrivende"),
    VURDER_REFUSJONSKRAV("5059", "VURDER_REFUSJONSKRAV", MANUELL, BeregningSteg.VURDER_REF_BERGRUNN, "Vurder refusjonskrav for beregningen"),

    // 6000 overstyring
    OVST_BEREGNINGSAKTIVITETER("6014", "OVST_BEREGNINGSAKTIVITETER", OVERSTYRING, BeregningSteg.FASTSETT_STP_BER, "Overstyring av beregningsaktiviteter"),
    OVST_INNTEKT("6015", "OVST_INNTEKT", OVERSTYRING, BeregningSteg.KOFAKBER, "Overstyring av beregningsgrunnlag"),

    // 7000 automatisk satt på vent
    AUTO_VENT_PÅ_INNTKT_RAP_FRST("7014", "AUTO_VENT_PAA_INNTKT_RAP_FRST", AUTOPUNKT, BeregningSteg.FASTSETT_STP_BER, "Vent på rapporteringsfrist for inntekt"),
    AUTO_VENT_PÅ_SISTE_AAP_DP_MELDKRT("7020", "AUTO_VENT_PAA_SISTE_AAP_DP_MELDKRT", AUTOPUNKT, BeregningSteg.FASTSETT_STP_BER, "Vent på siste meldekort for AAP eller DP-mottaker"),

    // 8000 frisinn
    AUTO_VENT_FRISINN("8000", "AUTO_VENT_FRISINN", AUTOPUNKT, BeregningSteg.FASTSETT_STP_BER, "Sak settes på vent på grunn av manglende funksjonalitet"),
    INGEN_AKTIVITETER("8001", "INGEN_AKTIVITETER", AUTOPUNKT, BeregningSteg.FASTSETT_STP_BER, "Gir avslag"),

    UNDEFINED,

    // Punkter som ikke lenger utledes, tas vare på så vi vet hvilke koder kalkulus har brukt før
    @Deprecated
    AUTO_VENT_ULIKE_STARTDTR_SVP("7026", "AUTO_VENT_ULIKE_STARTDTR_SVP", AUTOPUNKT, null, "Autopunkt ulike startdatoer svangerskapspenger"),
    @Deprecated
    AUTO_VENT_DELVS_TILRTLGGNG_REFSJN_SVP("7027", "AUTO_VENT_DELVS_TILRTLGGNG_REFSJN_SVP", AUTOPUNKT, null, "Autopunkt delvis SVP og refusjon"),
    @Deprecated
    AUTO_VENT_PÅ_MANGLND_ARB_FHLD_KOMNRFRM("7036", "AUTO_VENT_PAA_MANGLND_ARB_FHLD_KOMNRFRM", AUTOPUNKT, null, "Sak settes på vent pga kommune- og fylkesammenslåing."),
    ;

    static final String KODEVERK = "AVKLARINGSBEHOV_DEF";

    public static final Map<String, AvklaringsbehovDefinisjon> KODER_GAMMEL = new LinkedHashMap<>();
    private static final Map<String, AvklaringsbehovDefinisjon> KODER = new LinkedHashMap<>();

    static {
        for (var v : values()) {
            if (KODER_GAMMEL.putIfAbsent(v.kodeGammel, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kodeGammel);
            }
        }
    }

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    private String kode;

    private String kodeNy;

    private String kodeGammel;

    @JsonIgnore
    private String navn;

    private AvklaringsbehovType avklaringsbehovType;

    private BeregningSteg stegFunnet;

    private AvklaringsbehovDefinisjon() {
        // for hibernate
    }


    private AvklaringsbehovDefinisjon(String kodeGammel, String kode, AvklaringsbehovType avklaringsbehovType, BeregningSteg stegFunnet, String navn) {
        this.kode = Objects.requireNonNull(kode);
        this.kodeNy = kode;
        this.kodeGammel = kodeGammel;
        this.stegFunnet = stegFunnet;
        this.navn = navn;
        this.avklaringsbehovType = avklaringsbehovType;
    }

    @JsonProperty
    @Override
    public String getKode() {
        return kode;
    }

    @JsonProperty
    public String getKodeNy() {
        return kodeNy;
    }

    @JsonProperty
    @Override
    public String getKodeverk() {
        return KODEVERK;
    }

    public AvklaringsbehovType getAvklaringsbehovType() {
        return avklaringsbehovType;
    }

    public BeregningSteg getStegFunnet() {
        return stegFunnet;
    }

    @Override
    public String toString() {
        return super.toString() + "('" + getKode() + "')";
    }

    @JsonCreator(mode = Mode.DELEGATING)
    @Deprecated
    public static AvklaringsbehovDefinisjon fraKode(Object node) {
        if (node == null) {
            return null;
        }
        String kode = TempAvledeKode.getVerdi(AvklaringsbehovDefinisjon.class, node, "kode");
        var ny = KODER.get(kode);
        var gammel = KODER_GAMMEL.get(kode);
        if (ny == null && gammel == null) {
            throw new IllegalArgumentException("Ukjent BeregningAvklaringsbehovDefinisjon: " + kode);
        }
        return ny != null ? ny : gammel;
    }

    public static Map<String, AvklaringsbehovDefinisjon> kodeMap() {
        return Collections.unmodifiableMap(KODER_GAMMEL);
    }

    public boolean erVentepunkt() {
        return AUTOPUNKT.equals(this.avklaringsbehovType);
    }

    public boolean erOverstyring() {
        return OVERSTYRING.equals(this.avklaringsbehovType);
    }


}
