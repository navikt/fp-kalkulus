package no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.felles.kodeverk.Kodeverdi;


@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum BeregningsgrunnlagTilstand implements Kodeverdi {

    OPPRETTET("OPPRETTET", "Opprettet", true),
    FASTSATT_BEREGNINGSAKTIVITETER("FASTSATT_BEREGNINGSAKTIVITETER", "Fastsatt beregningsaktiviteter", false),
    OPPDATERT_MED_ANDELER("OPPDATERT_MED_ANDELER", "Oppdatert med andeler", true),
    KOFAKBER_UT("KOFAKBER_UT", "Kontroller fakta beregningsgrunnlag - Ut", false),
    FORESLÅTT("FORESLÅTT", "Foreslått", true),
    FORESLÅTT_UT("FORESLÅTT_UT", "Foreslått ut", false),
    OPPDATERT_MED_REFUSJON_OG_GRADERING("OPPDATERT_MED_REFUSJON_OG_GRADERING", "Tilstand for splittet periode med refusjon og gradering", true),
    FASTSATT_INN("FASTSATT_INN", "Fastsatt - Inn", false),
    FASTSATT("FASTSATT", "Fastsatt", true),
    UDEFINERT("-", "Ikke definert", false),
    ;
    public static final String KODEVERK = "BEREGNINGSGRUNNLAG_TILSTAND";

    @Deprecated
    public static final String DISCRIMINATOR = "BEREGNINGSGRUNNLAG_TILSTAND";

    private static final Map<String, BeregningsgrunnlagTilstand> KODER = new LinkedHashMap<>();

    /**
     * Rekkefølge tilstandene opptrer i løsningen.
     *
     * IKKE ENDRE REKKEFØLGE AV TILSTANDER UTEN Å ENDRE REKKEFØLGE AV LAGRING.
     */
    private static final List<BeregningsgrunnlagTilstand> tilstandRekkefølge = Collections.unmodifiableList(
    List.of(
        OPPRETTET,
        FASTSATT_BEREGNINGSAKTIVITETER,
        OPPDATERT_MED_ANDELER,
        KOFAKBER_UT,
        FORESLÅTT,
        FORESLÅTT_UT,
        OPPDATERT_MED_REFUSJON_OG_GRADERING,
        FASTSATT_INN,
        FASTSATT
    ));

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
    private boolean obligatoriskTilstand;

    BeregningsgrunnlagTilstand(String kode, String navn, boolean obligatoriskTilstand) {
        this.kode = kode;
        this.navn = navn;
        this.obligatoriskTilstand = obligatoriskTilstand;
    }

    @JsonCreator
    public static BeregningsgrunnlagTilstand fraKode(@JsonProperty("kode") String kode) {
        if (kode == null) {
            return null;
        }
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent BeregningsgrunnlagTilstand: " + kode);
        }
        return ad;
    }

    public static Map<String, BeregningsgrunnlagTilstand> kodeMap() {
        return Collections.unmodifiableMap(KODER);
    }

    public static void main(String[] args) {
        System.out.println(KODER.keySet().stream().map(k -> "'" + k + "'").collect(Collectors.toList()));
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
        BeregningsgrunnlagTilstand forrigeTilstand = tilstandRekkefølge.get(tilstandIndex-1);
        return Optional.of(forrigeTilstand);
    }



    public boolean erFør(BeregningsgrunnlagTilstand that) {
        int thisIndex = tilstandRekkefølge.indexOf(this);
        int thatIndex = tilstandRekkefølge.indexOf(that);
        return thisIndex < thatIndex;
    }

    @Override
    public String getNavn() {
        return navn;
    }

    @JsonProperty
    @Override
    public String getKodeverk() {
        return KODEVERK;
    }

    @JsonProperty
    @Override
    public String getKode() {
        return kode;
    }

    @Override
    public String getOffisiellKode() {
        return getKode();
    }

    public boolean erObligatoriskTilstand() {
        return this.obligatoriskTilstand;
    }

    @Converter(autoApply = true)
    public static class KodeverdiConverter implements AttributeConverter<BeregningsgrunnlagTilstand, String> {

        @Override
        public String convertToDatabaseColumn(BeregningsgrunnlagTilstand attribute) {
            return attribute == null ? null : attribute.getKode();
        }

        @Override
        public BeregningsgrunnlagTilstand convertToEntityAttribute(String dbData) {
            return dbData == null ? null : fraKode(dbData);
        }
    }
}
