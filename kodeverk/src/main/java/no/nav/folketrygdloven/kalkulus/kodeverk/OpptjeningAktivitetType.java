package no.nav.folketrygdloven.kalkulus.kodeverk;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * <h3>Internt kodeverk</h3>
 * Definerer aktiviteter benyttet til å vurdere Opptjening.
 * <p>
 * Kodeverket sammenstiller data fra {@link ArbeidType}.<br>
 * Senere benyttes dette i mapping til bla. Beregningsgrunnlag.
 */

@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum OpptjeningAktivitetType implements Kodeverdi, DatabaseKode, KontraktKode {

    ARBEIDSAVKLARING("AAP"),
    ARBEID("ARBEID"),
    DAGPENGER("DAGPENGER"),
    FORELDREPENGER("FORELDREPENGER"),
    FRILANS("FRILANS"),
    MILITÆR_ELLER_SIVILTJENESTE("MILITÆR_ELLER_SIVILTJENESTE"),
    NÆRING("NÆRING"),
    OMSORGSPENGER("OMSORGSPENGER"),
    OPPLÆRINGSPENGER("OPPLÆRINGSPENGER"),
    PLEIEPENGER("PLEIEPENGER"),
    FRISINN("FRISINN"),
    ETTERLØNN_SLUTTPAKKE("ETTERLØNN_SLUTTPAKKE"),
    SVANGERSKAPSPENGER("SVANGERSKAPSPENGER"),
    SYKEPENGER("SYKEPENGER"),
    SYKEPENGER_AV_DAGPENGER("SYKEPENGER_AV_DAGPENGER"),
    PLEIEPENGER_AV_DAGPENGER("PLEIEPENGER_AV_DAGPENGER"),
    VENTELØNN_VARTPENGER("VENTELØNN_VARTPENGER"),
    VIDERE_ETTERUTDANNING("VIDERE_ETTERUTDANNING"),
    UTENLANDSK_ARBEIDSFORHOLD("UTENLANDSK_ARBEIDSFORHOLD"),

    UTDANNINGSPERMISJON("UTDANNINGSPERMISJON"),
    UDEFINERT("-"),
    ;

    private static final Map<String, OpptjeningAktivitetType> KODER = new LinkedHashMap<>();

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    @JsonValue
    private final String kode;

    OpptjeningAktivitetType(String kode) {
        this.kode = kode;
    }

    @JsonCreator(mode = Mode.DELEGATING)
    public static OpptjeningAktivitetType fraKode(Object node) {
        if (node == null) {
            return null;
        }
        String kode = TempAvledeKode.getVerdi(OpptjeningAktivitetType.class, node, "kode");
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent OpptjeningAktivitetType: " + kode);
        }
        return ad;
    }


    @Override
    public String getKode() {
        return kode;
    }

    public static OpptjeningAktivitetType fraDatabaseKode(String databaseKode) {
        return fraKode(databaseKode);
    }

}
