package no.nav.folketrygdloven.kalkulus.kodeverk;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum TemaUnderkategori implements Kodeverdi, KontraktKode {

    FORELDREPENGER("FP", "Foreldrepenger"),
    FORELDREPENGER_FODSEL("FØ", "Foreldrepenger fødsel"),
    FORELDREPENGER_ADOPSJON("AP", "Foreldrepenger adopsjon"),
    FORELDREPENGER_SVANGERSKAPSPENGER("SV", "Svangerskapspenger"),
    SYKEPENGER_SYKEPENGER("SP", "Sykepenger"),
    PÅRØRENDE_OMSORGSPENGER("OM", "Pårørende omsorgsmpenger"),
    PÅRØRENDE_OPPLÆRINGSPENGER("OP", "Pårørende opplæringspenger"),
    PÅRØRENDE_PLEIETRENGENDE_SYKT_BARN("PB", "Pårørende pleietrengende sykt barn"),
    PÅRØRENDE_PLEIETRENGENDE("PI", "Pårørende pleietrengende"),
    PÅRØRENDE_PLEIETRENGENDE_PÅRØRENDE("PP", "Pårørende pleietrengende pårørende"),
    PÅRØRENDE_PLEIEPENGER("PN", "Pårørende pleiepenger"),
    SYKEPENGER_FORSIKRINGSRISIKO("SU", "Sykepenger utenlandsopphold"),
    SYKEPENGER_REISETILSKUDD("RT", "Reisetilskudd"),
    SYKEPENGER_UTENLANDSOPPHOLD("RS", "Forsikr.risiko sykefravær"),
    OVERGANGSSTØNAD("OG", "Overgangsstønad"),
    FORELDREPENGER_FODSEL_UTLAND("FU", "Foreldrepenger fødsel, utland"),
    ENGANGSSTONAD_ADOPSJON("AE", "Adopsjon engangsstønad"),
    ENGANGSSTONAD_FODSEL("FE", "Fødsel engangsstønad"),

    BT("BT", "Stønad til barnetilsyn"),
    FL("FL", "Tilskudd til flytting"),
    UT("UT", "Skolepenger"),

    UDEFINERT("-", "Udefinert"),
    ;

    private static final Map<String, TemaUnderkategori> KODER = new LinkedHashMap<>();

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

    TemaUnderkategori(String kode, String navn) {
        this.kode = kode;
        this.navn = navn;
    }

    @JsonCreator(mode = Mode.DELEGATING)
    public static TemaUnderkategori fraKode(Object node) {
        if (node == null) {
            return null;
        }
        String kode = TempAvledeKode.getVerdi(TemaUnderkategori.class, node, "kode");
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent TemaUnderkategori: " + kode);
        }
        return ad;
    }


    @Override
    public String getKode() {
        return kode;
    }

}
