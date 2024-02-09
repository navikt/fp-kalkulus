package no.nav.folketrygdloven.kalkulus.kodeverk;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;


@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum BeregningsgrunnlagTilstand implements Kodeverdi, DatabaseKode, KontraktKode {

    OPPRETTET("OPPRETTET", true),
    FASTSATT_BEREGNINGSAKTIVITETER("FASTSATT_BEREGNINGSAKTIVITETER", false),
    OPPDATERT_MED_ANDELER("OPPDATERT_MED_ANDELER", true),
    KOFAKBER_UT("KOFAKBER_UT", false),
    BESTEBEREGNET("BESTEBEREGNET", false),
    FORESLÅTT("FORESLÅTT", true),
    FORESLÅTT_UT("FORESLÅTT_UT", false),
    FORESLÅTT_2("FORESLÅTT_DEL_2", false),
    FORESLÅTT_2_UT("FORESLÅTT_DEL_2_UT", false),
    VURDERT_VILKÅR("VURDERT_VILKÅR", true),
    VURDERT_TILKOMMET_INNTEKT("VURDERT_TILKOMMET_INNTEKT", false),
    VURDERT_TILKOMMET_INNTEKT_UT("VURDERT_TILKOMMET_INNTEKT_UT", false),
    VURDERT_REFUSJON("VURDERT_REFUSJON", true),
    VURDERT_REFUSJON_UT("VURDERT_REFUSJON_UT", false),
    OPPDATERT_MED_REFUSJON_OG_GRADERING("OPPDATERT_MED_REFUSJON_OG_GRADERING", true),
    FASTSATT_INN("FASTSATT_INN", false),
    FASTSATT("FASTSATT", true),
    UDEFINERT(KodeKonstanter.UDEFINERT, false),
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
            FORESLÅTT_2,
            FORESLÅTT_2_UT,
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
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    @JsonValue
    private final String kode;

    @JsonIgnore
    private final boolean obligatoriskTilstand;

    BeregningsgrunnlagTilstand(String kode, boolean obligatoriskTilstand) {
        this.kode = kode;
        this.obligatoriskTilstand = obligatoriskTilstand;
    }

    public static List<BeregningsgrunnlagTilstand> getTilstandRekkefølge() {
        return Collections.unmodifiableList(tilstandRekkefølge);
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
        return tilstandRekkefølge.get(0);
    }


    public static Optional<BeregningsgrunnlagTilstand> finnForrigeObligatoriskTilstand(BeregningsgrunnlagTilstand tilstand) {
        int tilstandIndex = tilstandRekkefølge.indexOf(tilstand);
        if (tilstandIndex == 0) {
            return Optional.empty();
        }
        int id = tilstandIndex - 1;
        while (!tilstandRekkefølge.get(id).obligatoriskTilstand && id > 0) {
            id = id - 1;
        }
        BeregningsgrunnlagTilstand forrigeObligatoriskTilstand = tilstandRekkefølge.get(id);
        return Optional.of(forrigeObligatoriskTilstand);
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
    public String getKode() {
        return kode;
    }

    public static BeregningsgrunnlagTilstand fraDatabaseKode(String databaseKode) {
        return fraKode(databaseKode);
    }


    public boolean erObligatoriskTilstand() {
        return this.obligatoriskTilstand;
    }
}
