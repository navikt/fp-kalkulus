package no.nav.folketrygdloven.kalkulus.kodeverk;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonValue;


@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum NaturalYtelseType implements Kodeverdi, KontraktKode {

    ELEKTRISK_KOMMUNIKASJON("ELEKTRISK_KOMMUNIKASJON"),
    AKSJER_GRUNNFONDSBEVIS_TIL_UNDERKURS("AKSJER_UNDERKURS"),
    LOSJI("LOSJI"),
    KOST_DØGN("KOST_DOEGN"),
    BESØKSREISER_HJEMMET_ANNET("BESOEKSREISER_HJEM"),
    KOSTBESPARELSE_I_HJEMMET("KOSTBESPARELSE_HJEM"),
    RENTEFORDEL_LÅN("RENTEFORDEL_LAAN"),
    BIL("BIL"),
    KOST_DAGER("KOST_DAGER"),
    BOLIG("BOLIG"),
    SKATTEPLIKTIG_DEL_FORSIKRINGER("FORSIKRINGER"),
    FRI_TRANSPORT("FRI_TRANSPORT"),
    OPSJONER("OPSJONER"),
    TILSKUDD_BARNEHAGEPLASS("TILSKUDD_BARNEHAGE"),
    ANNET("ANNET"),
    BEDRIFTSBARNEHAGEPLASS("BEDRIFTSBARNEHAGE"),
    YRKEBIL_TJENESTLIGBEHOV_KILOMETER("YRKESBIL_KILOMETER"),
    YRKEBIL_TJENESTLIGBEHOV_LISTEPRIS("YRKESBIL_LISTEPRIS"),
    INNBETALING_TIL_UTENLANDSK_PENSJONSORDNING("UTENLANDSK_PENSJONSORDNING"),
    UDEFINERT(KodeKonstanter.UDEFINERT),
    ;

    private static final Map<String, NaturalYtelseType> KODER = new LinkedHashMap<>();

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    @JsonValue
    private final String kode;

    NaturalYtelseType(String kode) {
        this.kode = kode;
    }

    @JsonCreator(mode = Mode.DELEGATING)
    public static NaturalYtelseType fraKode(Object node) {
        if (node == null) {
            return null;
        }
        String kode = TempAvledeKode.getVerdi(NaturalYtelseType.class, node, "kode");
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent NaturalYtelseType: " + kode);
        }
        return ad;
    }

    @Override
    public String getKode() {
        return kode;
    }


}
