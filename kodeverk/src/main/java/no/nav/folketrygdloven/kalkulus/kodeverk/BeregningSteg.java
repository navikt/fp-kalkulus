package no.nav.folketrygdloven.kalkulus.kodeverk;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonValue;


@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum BeregningSteg implements Kodeverdi, KontraktKode {

    FASTSETT_STP_BER, // Fastsett skjæringstidspunkt beregning
    KOFAKBER, // Kontroller fakta for beregning
    FORS_BERGRUNN, // Foreslå beregningsgrunnlag
    FORS_BERGRUNN_2, // Fortsett foreslå beregningsgrunnlag

    // Kun for foreldrepenger
    FORS_BESTEBEREGNING, // Foreslå besteberegning

    VURDER_VILKAR_BERGRUNN, // Vurder beregningsgrunnlagsvilkår

    VURDER_TILKOMMET_INNTEKT, // Vurder tilkommet inntekt

    VURDER_REF_BERGRUNN, // Vurder refusjon for beregningsgrunnlaget
    FORDEL_BERGRUNN, // Fordel beregningsgrunnlag
    FAST_BERGRUNN; // Fastsett beregningsgrunnlag

    /**
     * Rekkefølge stegene opptrer i løsningen.
     * <p>
     * IKKE ENDRE REKKEFØLGE AV STEG UTEN Å SYNKE MED KONSUMENTER.
     */
    private static final List<BeregningSteg> stegRekkefølge = List.of(
            FASTSETT_STP_BER,
            KOFAKBER,
            FORS_BERGRUNN,
            FORS_BERGRUNN_2,
            FORS_BESTEBEREGNING,
            VURDER_VILKAR_BERGRUNN,
            VURDER_TILKOMMET_INNTEKT,
            VURDER_REF_BERGRUNN,
            FORDEL_BERGRUNN,
            FAST_BERGRUNN);

    private static final Map<String, BeregningSteg> KODER = new LinkedHashMap<>();

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.name(), v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.name());
            }
        }
    }
    @JsonCreator(mode = Mode.DELEGATING)
    public static BeregningSteg fraKode(Object node) {
        if (node == null) {
            return null;
        }
        String kode = TempAvledeKode.getVerdi(BeregningSteg.class, node, "kode");
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent BeregningSteg: " + kode);
        }
        return ad;
    }

    @Override
    @JsonValue
    public String getKode() {
        return name();
    }


    public boolean erFør(BeregningSteg that) {
        int thisIndex = stegRekkefølge.indexOf(this);
        int thatIndex = stegRekkefølge.indexOf(that);
        return thisIndex < thatIndex;
    }

    public boolean erEtter(BeregningSteg that) {
        int thisIndex = stegRekkefølge.indexOf(this);
        int thatIndex = stegRekkefølge.indexOf(that);
        return thisIndex > thatIndex;
    }


}
