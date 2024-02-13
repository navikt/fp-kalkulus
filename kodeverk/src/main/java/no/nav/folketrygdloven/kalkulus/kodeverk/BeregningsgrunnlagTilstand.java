package no.nav.folketrygdloven.kalkulus.kodeverk;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonValue;


@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum BeregningsgrunnlagTilstand implements Kodeverdi, DatabaseKode, KontraktKode {

    OPPRETTET,
    FASTSATT_BEREGNINGSAKTIVITETER,
    OPPDATERT_MED_ANDELER,
    KOFAKBER_UT,
    BESTEBEREGNET,
    FORESLÅTT,
    FORESLÅTT_UT,
    FORESLÅTT_DEL_2,
    FORESLÅTT_DEL_2_UT,
    VURDERT_VILKÅR,
    VURDERT_TILKOMMET_INNTEKT,
    VURDERT_TILKOMMET_INNTEKT_UT,
    VURDERT_REFUSJON,
    VURDERT_REFUSJON_UT,
    OPPDATERT_MED_REFUSJON_OG_GRADERING,
    FASTSATT_INN,
    FASTSATT,
    UDEFINERT,
    ;

    private static final Map<String, BeregningsgrunnlagTilstand> KODER = new LinkedHashMap<>();

    /**
     * Rekkefølge tilstandene opptrer i løsningen.
     * <p>
     * IKKE ENDRE REKKEFØLGE AV TILSTANDER UTEN Å ENDRE REKKEFØLGE AV LAGRING.
     */
    private static final List<BeregningsgrunnlagTilstand> tilstandRekkefølge = List.of(
            OPPRETTET,
            FASTSATT_BEREGNINGSAKTIVITETER,
            OPPDATERT_MED_ANDELER,
            KOFAKBER_UT,
            FORESLÅTT,
            FORESLÅTT_UT,
            FORESLÅTT_DEL_2,
            FORESLÅTT_DEL_2_UT,
            BESTEBEREGNET,
            VURDERT_VILKÅR,
            VURDERT_TILKOMMET_INNTEKT,
            VURDERT_TILKOMMET_INNTEKT_UT,
            VURDERT_REFUSJON,
            VURDERT_REFUSJON_UT,
            OPPDATERT_MED_REFUSJON_OG_GRADERING,
            FASTSATT_INN,
            FASTSATT
    );

    static {
        KODER.putIfAbsent(KodeKonstanter.UDEFINERT, UDEFINERT);
        for (var v : values()) {
            if (KODER.putIfAbsent(v.name(), v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.name());
            }
        }
    }


    public static List<BeregningsgrunnlagTilstand> getTilstandRekkefølge() {
        return tilstandRekkefølge;
    }

    @JsonCreator(mode = Mode.DELEGATING)
    public static BeregningsgrunnlagTilstand fraKode(Object node) {
        if (node == null) {
            return null;
        }
        String kode = TempAvledeKode.getVerdi(BeregningsgrunnlagTilstand.class, node, "kode");
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent BeregningsgrunnlagTilstand: " + kode);
        }
        return ad;
    }

    public static BeregningsgrunnlagTilstand finnFørste() {
        return tilstandRekkefølge.getFirst();
    }


    public static Optional<BeregningsgrunnlagTilstand> finnForrigeTilstand(BeregningsgrunnlagTilstand tilstand) {
        int tilstandIndex = tilstandRekkefølge.indexOf(tilstand);
        if (tilstandIndex == 0) {
            return Optional.empty();
        }
        BeregningsgrunnlagTilstand forrigeTilstand = tilstandRekkefølge.get(tilstandIndex - 1);
        return Optional.of(forrigeTilstand);
    }

    public static Optional<BeregningsgrunnlagTilstand> finnNesteTilstand(BeregningsgrunnlagTilstand tilstand) {
        int tilstandIndex = tilstandRekkefølge.indexOf(tilstand);
        if (tilstandIndex == tilstandRekkefølge.size() - 1) {
            return Optional.empty();
        }
        BeregningsgrunnlagTilstand forrigeTilstand = tilstandRekkefølge.get(tilstandIndex + 1);
        return Optional.of(forrigeTilstand);
    }


    public boolean erFør(BeregningsgrunnlagTilstand that) {
        int thisIndex = tilstandRekkefølge.indexOf(this);
        int thatIndex = tilstandRekkefølge.indexOf(that);
        return thisIndex < thatIndex;
    }

    public boolean erEtter(BeregningsgrunnlagTilstand that) {
        int thisIndex = tilstandRekkefølge.indexOf(this);
        int thatIndex = tilstandRekkefølge.indexOf(that);
        return thisIndex > thatIndex;
    }

    @Override
    @JsonValue
    public String getKode() {
        return this == UDEFINERT ? KodeKonstanter.UDEFINERT : name();
    }

    public static BeregningsgrunnlagTilstand fraDatabaseKode(String databaseKode) {
        return fraKode(databaseKode);
    }

}
