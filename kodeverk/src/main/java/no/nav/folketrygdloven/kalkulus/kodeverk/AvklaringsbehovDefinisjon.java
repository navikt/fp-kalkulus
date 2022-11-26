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
    FASTSETT_BEREGNINGSGRUNNLAG_ARBEIDSTAKER_FRILANS("5038", MANUELL, BeregningSteg.FORS_BERGRUNN, "Fastsette beregningsgrunnlag for arbeidstaker/frilanser skjønnsmessig"),
    VURDER_VARIG_ENDRET_ELLER_NYOPPSTARTET_NÆRING_SELVSTENDIG_NÆRINGSDRIVENDE("5039", MANUELL, BeregningSteg.FORTS_FORS_BERGRUNN, "Vurder varig endret/nyoppstartet næring selvstendig næringsdrivende"),
    VURDER_VARIG_ENDRET_ARBEIDSSITUASJON_MIDLERTIDIG_INAKTIV("5054", MANUELL, BeregningSteg.FORTS_FORS_BERGRUNN, "Vurder varig endret arbeidssituasjon for bruker som er midlertidig inaktiv"),
    FASTSETT_BEREGNINGSGRUNNLAG_SELVSTENDIG_NÆRINGSDRIVENDE("5042", MANUELL, BeregningSteg.FORTS_FORS_BERGRUNN, "Fastsett beregningsgrunnlag for selvstendig næringsdrivende"),
    FORDEL_BEREGNINGSGRUNNLAG("5046", MANUELL, BeregningSteg.FORDEL_BERGRUNN, "Fordel beregningsgrunnlag"),
    FASTSETT_BEREGNINGSGRUNNLAG_TIDSBEGRENSET_ARBEIDSFORHOLD("5047", MANUELL, BeregningSteg.FORS_BERGRUNN, "Fastsett beregningsgrunnlag for tidsbegrenset arbeidsforhold"),
    VURDER_NYTT_INNTEKTSFORHOLD("5067", MANUELL, BeregningSteg.FORDEL_BERGRUNN, "Vurder nytt inntektsforhold"),

    FASTSETT_BEREGNINGSGRUNNLAG_FOR_SN_NY_I_ARBEIDSLIVET("5049", MANUELL, BeregningSteg.FORTS_FORS_BERGRUNN, "Fastsett beregningsgrunnlag for SN som er ny i arbeidslivet"),
    VURDER_GRADERING_UTEN_BEREGNINGSGRUNNLAG("5050", MANUELL, BeregningSteg.FAST_BERGRUNN, "Vurder gradering på andel uten beregningsgrunnlag"),
    AVKLAR_AKTIVITETER("5052", MANUELL, BeregningSteg.FASTSETT_STP_BER, "Avklar aktivitet for beregning"),
    VURDER_FAKTA_FOR_ATFL_SN("5058", MANUELL, BeregningSteg.KOFAKBER, "Vurder fakta for arbeidstaker, frilans og selvstendig næringsdrivende"),
    VURDER_REFUSJONSKRAV("5059", MANUELL, BeregningSteg.VURDER_REF_BERGRUNN, "Vurder refusjonskrav for beregningen"),

    // 6000 overstyring
    OVERSTYRING_AV_BEREGNINGSAKTIVITETER("6014", OVERSTYRING, BeregningSteg.FASTSETT_STP_BER, "Overstyring av beregningsaktiviteter"),
    OVERSTYRING_AV_BEREGNINGSGRUNNLAG("6015", OVERSTYRING, BeregningSteg.KOFAKBER, "Overstyring av beregningsgrunnlag"),

    // 7000 automatisk satt på vent
    AUTO_VENT_PÅ_INNTEKT_RAPPORTERINGSFRIST("7014", AUTOPUNKT, BeregningSteg.FASTSETT_STP_BER, "Vent på rapporteringsfrist for inntekt"),
    AUTO_VENT_PÅ_SISTE_AAP_ELLER_DP_MELDEKORT("7020", AUTOPUNKT, BeregningSteg.FASTSETT_STP_BER, "Vent på siste meldekort for AAP eller DP-mottaker"),

    // 8000 frisinn
    AUTO_VENT_FRISINN("8000", AUTOPUNKT, BeregningSteg.FASTSETT_STP_BER, "Sak settes på vent på grunn av manglende funksjonalitet"),
    INGEN_AKTIVITETER("8001", AUTOPUNKT, BeregningSteg.FASTSETT_STP_BER, "Gir avslag"),

    UNDEFINED,

    // Punkter som ikke lenger utledes, tas vare på så vi vet hvilke koder kalkulus har brukt før
    @Deprecated
    AUTO_VENT_ULIKE_STARTDATOER_SVP("7026", AUTOPUNKT, null, "Autopunkt ulike startdatoer svangerskapspenger"),
    @Deprecated
    AUTO_VENT_DELVIS_TILRETTELEGGING_OG_REFUSJON_SVP("7027", AUTOPUNKT, null, "Autopunkt delvis SVP og refusjon"),
    @Deprecated
    AUTO_VENT_PÅ_MANGLENDE_ARBEIDSFORHOLD_KOMMUNEREFORM("7036", AUTOPUNKT, null ,"Sak settes på vent pga kommune- og fylkesammenslåing."),
    ;

    static final String KODEVERK = "AVKLARINGSBEHOV_DEF";

    private static final Map<String, AvklaringsbehovDefinisjon> KODER = new LinkedHashMap<>();

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

    private AvklaringsbehovType avklaringsbehovType;

    private BeregningSteg stegFunnet;

    private AvklaringsbehovDefinisjon() {
        // for hibernate
    }

    private AvklaringsbehovDefinisjon(String kode, AvklaringsbehovType avklaringsbehovType, BeregningSteg stegFunnet, String navn) {
        this.kode = Objects.requireNonNull(kode);
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
    public static AvklaringsbehovDefinisjon fraKode(Object node) {
        if (node == null) {
            return null;
        }
        String kode = TempAvledeKode.getVerdi(AvklaringsbehovDefinisjon.class, node, "kode");
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent BeregningAvklaringsbehovDefinisjon: " + kode);
        }
        return ad;
    }

    public static AvklaringsbehovDefinisjon fraHåndtering(HåndteringKode håndteringKode) {
        return fraKode(håndteringKode.getKode());
    }

    public static Map<String, AvklaringsbehovDefinisjon> kodeMap() {
        return Collections.unmodifiableMap(KODER);
    }

    public boolean erVentepunkt() {
        return AUTOPUNKT.equals(this.avklaringsbehovType);
    }

    public boolean erOverstyring() {
        return OVERSTYRING.equals(this.avklaringsbehovType);
    }


}
