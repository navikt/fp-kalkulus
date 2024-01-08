package no.nav.folketrygdloven.kalkulus.kodeverk;

import static no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand.BESTEBEREGNET;
import static no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER;
import static no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand.OPPDATERT_MED_REFUSJON_OG_GRADERING;
import static no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand.VURDERT_REFUSJON;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;


@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum BeregningsgrunnlagRegelType implements Kodeverdi, DatabaseKode {

    SKJÆRINGSTIDSPUNKT("SKJÆRINGSTIDSPUNKT", "Fastsette skjæringstidspunkt", OPPDATERT_MED_ANDELER),
    BRUKERS_STATUS("BRUKERS_STATUS", "Fastsette brukers status/aktivitetstatus", OPPDATERT_MED_ANDELER),
    // Skal ikke lagres til men eksisterer fordi det finnes entries med denne i databasen (før ble det kun lagret 1 sporing for periodisering)
    @Deprecated
    PERIODISERING("PERIODISERING", "Periodiser beregningsgrunnlag", OPPDATERT_MED_REFUSJON_OG_GRADERING),

    PERIODISERING_NATURALYTELSE("PERIODISERING_NATURALYTELSE", "Periodiser beregningsgrunnlag pga naturalytelse", OPPDATERT_MED_ANDELER),
    PERIODISERING_REFUSJON("PERIODISERING_REFUSJON", "Periodiser beregningsgrunnlag pga refusjon", VURDERT_REFUSJON),
    // Vurder om perioder i databasen skal migreres over til PERIODISERING_UTBETALINGSGRAD før fp-sak flytter til kalkulus
    PERIODISERING_GRADERING("PERIODISERING_GRADERING", "Periodiser beregningsgrunnlag pga gradering", VURDERT_REFUSJON),
    PERIODISERING_UTBETALINGSGRAD("PERIODISERING_UTBETALINGSGRAD", "Periodiser beregningsgrunnlag pga endring i utbetalingsgrad", VURDERT_REFUSJON),
    BESTEBEREGNING("BESTEBEREGNING", "Sammenligner beregning etter kap 8 med beregning ved besteberegning.", BESTEBEREGNET),

    UDEFINERT("-", "Ikke definert", BeregningsgrunnlagTilstand.UDEFINERT),
    ;

    private static final Map<String, BeregningsgrunnlagRegelType> KODER = new LinkedHashMap<>();

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    @JsonIgnore
    private final String navn;
    @JsonValue
    private final String kode;
    @JsonIgnore
    private final BeregningsgrunnlagTilstand lagretTilstand;

    BeregningsgrunnlagRegelType(String kode, String navn, BeregningsgrunnlagTilstand lagretTilstand) {
        this.kode = kode;
        this.navn = navn;
        this.lagretTilstand = lagretTilstand;
    }

    @JsonCreator(mode = Mode.DELEGATING)
    public static BeregningsgrunnlagRegelType fraKode(Object node) {
        if (node == null) {
            return null;
        }
        String kode = TempAvledeKode.getVerdi(BeregningsgrunnlagRegelType.class, node, "kode");
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent BeregningsgrunnlagRegelType: " + kode);
        }
        return ad;
    }

    @Override
    public String getKode() {
        return kode;
    }

    public static BeregningsgrunnlagRegelType fraDatabaseKode(String databaseKode) {
        return fraKode(databaseKode);
    }


    public BeregningsgrunnlagTilstand getLagretTilstand() {
        return lagretTilstand;
    }

}
