package no.nav.folketrygdloven.kalkulus.kodeverk;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;


@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum BeregningSteg implements Kodeverdi {

    FASTSETT_STP_BER("FASTSETT_STP_BER", "Fastsett skjæringstidspunkt beregning"),
    KOFAKBER("KOFAKBER", "Kontroller fakta for beregning"),
    FORS_BERGRUNN("FORS_BERGRUNN", "Foreslå beregningsgrunnlag"),

    // Kun for foreldrepenger
    FORS_BESTEBEREGNING("FORS_BESTEBEREGNING", "Foreslå besteberegning"),

    VURDER_VILKAR_BERGRUNN("VURDER_VILKAR", "Vurder beregningsgrunnlagsvilkår"),

    VURDER_REF_BERGRUNN("VURDER_REF_BERGRUNN", "Vurder refusjon for beregningsgrunnlaget"),
    FORDEL_BERGRUNN("FORDEL_BERGRUNN", "Fordel beregningsgrunnlag"),
    FAST_BERGRUNN("FAST_BERGRUNN", "Fastsett beregningsgrunnlag");

    /**
     * Rekkefølge stegene opptrer i løsningen.
     * <p>
     * IKKE ENDRE REKKEFØLGE AV STEG UTEN Å SYNKE MED KONSUMENTER.
     */
    private static final List<BeregningSteg> stegRekkefølge = List.of(
            FASTSETT_STP_BER,
            KOFAKBER,
            FORS_BERGRUNN,
            FORS_BESTEBEREGNING,
            VURDER_VILKAR_BERGRUNN,
            VURDER_REF_BERGRUNN,
            FORDEL_BERGRUNN,
            FAST_BERGRUNN);

    private static final Map<String, BeregningSteg> KODER = new LinkedHashMap<>();

    public static final String KODEVERK = "BEREGNING_STEG";

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    @JsonIgnore
    private String navn;

    private String kode;

    BeregningSteg(String kode, String navn) {
        this.kode = kode;
        this.navn = navn;
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

    public static Map<String, BeregningSteg> kodeMap() {
        return Collections.unmodifiableMap(KODER);
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
