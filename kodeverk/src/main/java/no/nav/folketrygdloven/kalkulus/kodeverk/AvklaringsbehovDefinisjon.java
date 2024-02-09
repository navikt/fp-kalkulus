package no.nav.folketrygdloven.kalkulus.kodeverk;

import static no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovType.AUTOPUNKT;
import static no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovType.MANUELL;
import static no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovType.OVERSTYRING;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;


/**
 * Definerer avklaringsbehov som kan utledes i beregning.
 */
@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public
enum AvklaringsbehovDefinisjon implements Kodeverdi, DatabaseKode, KontraktKode {

    // 5000 vanlig saksbehandlig
    FASTSETT_BG_AT_FL(KodeKonstanter.AB_FASTSETT_BG_AT_FL, MANUELL, BeregningSteg.FORS_BERGRUNN, "Fastsette beregningsgrunnlag for arbeidstaker/frilanser skjønnsmessig"),
    VURDER_VARIG_ENDRT_NYOPPSTR_NAERNG_SN(KodeKonstanter.AB_VURDER_VARIG_ENDRT_NYOPPSTR_NAERNG_SN, MANUELL, BeregningSteg.FORTS_FORS_BERGRUNN, "Vurder varig endret/nyoppstartet næring selvstendig næringsdrivende"),
    VURDER_VARIG_ENDRT_ARB_SITSJN_MDL_INAKTV(KodeKonstanter.AB_VURDER_VARIG_ENDRT_ARB_SITSJN_MDL_INAKTV, MANUELL, BeregningSteg.FORTS_FORS_BERGRUNN, "Vurder varig endret arbeidssituasjon for bruker som er midlertidig inaktiv"),
    FASTSETT_BG_SN(KodeKonstanter.AB_FASTSETT_BG_SN, MANUELL, BeregningSteg.FORTS_FORS_BERGRUNN, "Fastsett beregningsgrunnlag for selvstendig næringsdrivende"),
    FORDEL_BG(KodeKonstanter.AB_FORDEL_BG, MANUELL, BeregningSteg.FORDEL_BERGRUNN, "Fordel beregningsgrunnlag"),
    FASTSETT_BG_TB_ARB(KodeKonstanter.AB_FASTSETT_BG_TB_ARB, MANUELL, BeregningSteg.FORS_BERGRUNN, "Fastsett beregningsgrunnlag for tidsbegrenset arbeidsforhold"),
    VURDER_NYTT_INNTKTSFRHLD(KodeKonstanter.AB_VURDER_NYTT_INNTKTSFRHLD, MANUELL, BeregningSteg.VURDER_TILKOMMET_INNTEKT, "Vurder nytt inntektsforhold"),
    VURDER_REPRESENTERER_STORTINGET(KodeKonstanter.AB_VURDER_REPRESENTERER_STORTINGET, MANUELL, BeregningSteg.FORDEL_BERGRUNN, "Vurder om bruker representerer stortinget i perioden"),

    FASTSETT_BG_SN_NY_I_ARB_LIVT(KodeKonstanter.AB_FASTSETT_BG_SN_NY_I_ARB_LIVT, MANUELL, BeregningSteg.FORTS_FORS_BERGRUNN, "Fastsett beregningsgrunnlag for SN som er ny i arbeidslivet"),
    VURDER_GRADERING_UTEN_BG(KodeKonstanter.AB_VURDER_GRADERING_UTEN_BG, MANUELL, BeregningSteg.FAST_BERGRUNN, "Vurder gradering på andel uten beregningsgrunnlag"), // TODO: Er denne i bruk - hvis kun finnes historisk i DB - flytt ned til deprecated?
    AVKLAR_AKTIVITETER(KodeKonstanter.AB_AVKLAR_AKTIVITETER, MANUELL, BeregningSteg.FASTSETT_STP_BER, "Avklar aktivitet for beregning"),
    VURDER_FAKTA_ATFL_SN(KodeKonstanter.AB_VURDER_FAKTA_ATFL_SN, MANUELL, BeregningSteg.KOFAKBER, "Vurder fakta for arbeidstaker, frilans og selvstendig næringsdrivende"),
    VURDER_REFUSJONSKRAV(KodeKonstanter.AB_VURDER_REFUSJONSKRAV, MANUELL, BeregningSteg.VURDER_REF_BERGRUNN, "Vurder refusjonskrav for beregningen"),

    // 6000 overstyring
    OVST_BEREGNINGSAKTIVITETER(KodeKonstanter.OVST_BEREGNINGSAKTIVITETER, OVERSTYRING, BeregningSteg.FASTSETT_STP_BER, "Overstyring av beregningsaktiviteter"),
    OVST_INNTEKT(KodeKonstanter.OVST_INNTEKT, OVERSTYRING, BeregningSteg.KOFAKBER, "Overstyring av beregningsgrunnlag"),

    // 7000 automatisk satt på vent
    AUTO_VENT_PÅ_INNTKT_RAP_FRST("AUTO_VENT_PAA_INNTKT_RAP_FRST", AUTOPUNKT, BeregningSteg.FASTSETT_STP_BER, "Vent på rapporteringsfrist for inntekt"),
    AUTO_VENT_PÅ_SISTE_AAP_DP_MELDKRT("AUTO_VENT_PAA_SISTE_AAP_DP_MELDKRT", AUTOPUNKT, BeregningSteg.FASTSETT_STP_BER, "Vent på siste meldekort for AAP eller DP-mottaker"),

    // 8000 frisinn
    AUTO_VENT_FRISINN("AUTO_VENT_FRISINN", AUTOPUNKT, BeregningSteg.FASTSETT_STP_BER, "Sak settes på vent på grunn av manglende funksjonalitet"),
    INGEN_AKTIVITETER("INGEN_AKTIVITETER", AUTOPUNKT, BeregningSteg.FASTSETT_STP_BER, "Gir avslag"),

    // Punkter som ikke lenger utledes, tas vare på så vi vet hvilke koder kalkulus har brukt før
    @Deprecated
    AUTO_VENT_ULIKE_STARTDTR_SVP("AUTO_VENT_ULIKE_STARTDTR_SVP", AUTOPUNKT, null, "Autopunkt ulike startdatoer svangerskapspenger"),
    @Deprecated
    AUTO_VENT_DELVS_TILRTLGGNG_REFSJN_SVP("AUTO_VENT_DELVS_TILRTLGGNG_REFSJN_SVP", AUTOPUNKT, null, "Autopunkt delvis SVP og refusjon"),
    @Deprecated
    AUTO_VENT_PÅ_MANGLND_ARB_FHLD_KOMNRFRM("AUTO_VENT_PAA_MANGLND_ARB_FHLD_KOMNRFRM", AUTOPUNKT, null, "Sak settes på vent pga kommune- og fylkesammenslåing."),
    ;

    private static final Map<String, AvklaringsbehovDefinisjon> KODER = new LinkedHashMap<>();

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
    @JsonIgnore
    private final AvklaringsbehovType avklaringsbehovType;
    @JsonIgnore
    private final BeregningSteg stegFunnet;


    AvklaringsbehovDefinisjon(String kode, AvklaringsbehovType avklaringsbehovType, BeregningSteg stegFunnet, String navn) {
        this.kode = Objects.requireNonNull(kode);
        this.stegFunnet = stegFunnet;
        this.navn = navn;
        this.avklaringsbehovType = avklaringsbehovType;
    }

    @Override
    public String getKode() {
        return kode;
    }


    public AvklaringsbehovType getAvklaringsbehovType() {
        return avklaringsbehovType;
    }

    public BeregningSteg getStegFunnet() {
        return stegFunnet;
    }

    @JsonCreator(mode = Mode.DELEGATING)
    @Deprecated
    public static AvklaringsbehovDefinisjon fraKode(Object node) {
        if (node == null) {
            return null;
        }
        String kode = TempAvledeKode.getVerdi(AvklaringsbehovDefinisjon.class, node, "kode");
        var ny = KODER.get(kode);
        if (ny == null) {
            throw new IllegalArgumentException("Ukjent BeregningAvklaringsbehovDefinisjon: " + kode);
        }
        return ny;
    }

    public static AvklaringsbehovDefinisjon fraDatabaseKode(String databaseKode) {
        return fraKode(databaseKode);
    }

    public boolean erVentepunkt() {
        return AUTOPUNKT.equals(this.avklaringsbehovType);
    }

    public boolean erOverstyring() {
        return OVERSTYRING.equals(this.avklaringsbehovType);
    }
}
