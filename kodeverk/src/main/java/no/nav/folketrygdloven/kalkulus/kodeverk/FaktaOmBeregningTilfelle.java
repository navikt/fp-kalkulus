package no.nav.folketrygdloven.kalkulus.kodeverk;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonValue;


/**
 * Et tilfelle som kan oppstå i fakta om beregning. Hvert tilfelle beskriver en spesifikk situasjon der informasjon må innhentes eller manuell vurdering
 * må gjøres av saksbehandler.
 */
@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum FaktaOmBeregningTilfelle implements Kodeverdi, DatabaseKode, KontraktKode {

    VURDER_TIDSBEGRENSET_ARBEIDSFORHOLD,
    VURDER_SN_NY_I_ARBEIDSLIVET, // Vurder om søker er SN og ny i arbeidslivet
    VURDER_NYOPPSTARTET_FL, // Vurder nyoppstartet frilans
    FASTSETT_MAANEDSINNTEKT_FL, // Fastsett månedsinntekt frilans
    FASTSETT_BG_ARBEIDSTAKER_UTEN_INNTEKTSMELDING, // Fastsette beregningsgrunnlag for arbeidstaker uten inntektsmelding
    VURDER_LØNNSENDRING,
    FASTSETT_MÅNEDSLØNN_ARBEIDSTAKER_UTEN_INNTEKTSMELDING,
    VURDER_AT_OG_FL_I_SAMME_ORGANISASJON, // Vurder om bruker er arbeidstaker og frilanser i samme organisasjon
    FASTSETT_BESTEBEREGNING_FØDENDE_KVINNE,
    VURDER_ETTERLØNN_SLUTTPAKKE, // Vurder om søker har etterlønn og/eller sluttpakke
    FASTSETT_ETTERLØNN_SLUTTPAKKE, // Fastsett søkers beregningsgrunnlag for etterlønn og/eller sluttpakke andel
    VURDER_MOTTAR_YTELSE, // Vurder om søker mottar ytelse for aktivitet
    VURDER_BESTEBEREGNING, // Vurder om søker skal ha besteberegning
    VURDER_MILITÆR_SIVILTJENESTE, // Vurder om søker har hatt militær- eller siviltjeneste i opptjeningsperioden
    VURDER_REFUSJONSKRAV_SOM_HAR_KOMMET_FOR_SENT, // Vurder refusjonskrav fremsatt for sent skal være med i beregning
    FASTSETT_BG_KUN_YTELSE, // Fastsett beregningsgrunnlag for kun ytelse uten arbeidsforhold
    TILSTØTENDE_YTELSE, // Avklar beregningsgrunnlag og inntektskategori for tilstøtende ytelse
    FASTSETT_ENDRET_BEREGNINGSGRUNNLAG, // Fastsette endring i beregningsgrunnlag
    UDEFINERT,
    ;
    private static final Map<String, FaktaOmBeregningTilfelle> KODER = new LinkedHashMap<>();

    static {
        KODER.putIfAbsent(KodeKonstanter.UDEFINERT, UDEFINERT);
        for (var v : values()) {
            if (KODER.putIfAbsent(v.name(), v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.name());
            }
        }
    }

    @JsonCreator(mode = Mode.DELEGATING)
    public static FaktaOmBeregningTilfelle fraKode(Object node) {
        if (node == null) {
            return null;
        }
        String kode = TempAvledeKode.getVerdi(FaktaOmBeregningTilfelle.class, node, "kode");
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent FaktaOmBeregningTilfelle: " + kode);
        }
        return ad;
    }

    @Override
    @JsonValue
    public String getKode() {
        return this == UDEFINERT ? KodeKonstanter.UDEFINERT : name();
    }

    public static FaktaOmBeregningTilfelle fraDatabaseKode(String databaseKode) {
        return fraKode(databaseKode);
    }

}
