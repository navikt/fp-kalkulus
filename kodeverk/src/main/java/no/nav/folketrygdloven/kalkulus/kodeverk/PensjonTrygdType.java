package no.nav.folketrygdloven.kalkulus.kodeverk;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonValue;


@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum PensjonTrygdType implements Kodeverdi, YtelseType {

    UDEFINERT("-"),
    ALDERSPENSJON("ALDERSPENSJON"),
    ANNET("ANNET"),
    AFP("AFP"),
    BARNEPENSJON("BARNEPENSJON"),
    BARNEPENSJON_ANDRE("BARNEPENSJON_ANDRE"),
    BIL("BIL"),
    BOLIG("BOLIG"),
    EKTEFELLE("EKTEFELLE"),
    ELEKTRONISK_KOMMUNIKASJON("ELEKTRONISK_KOMMUNIKASJON"),
    INNSKUDDS_ENGANGS("INNSKUDDS_ENGANGS"),
    ETTERLATTE_PENSJON("ETTERLATTE_PENSJON"),
    ETTERLØNN("ETTERLØNN"),
    ETTERLØNN_OG_ETTERPENSJON("ETTERLØNN_OG_ETTERPENSJON"),
    FØDERÅD("FØDERÅD"),
    INTRODUKSJONSSTØNAD("INTRODUKSJONSSTØNAD"),
    IPA_IPS_BARNEPENSJON("IPA_IPS_BARNEPENSJON"),
    IPA_IPS_ENGANGSUTBETALING("IPA_IPS_ENGANGSUTBETALING"),
    IPA_IPS_PERIODISKE("IPA_IPS_PERIODISKE"),
    IPA_IPS_UFØRE("IPA_IPS_UFØRE"),
    KRIGSPENSJON("KRIGSPENSJON"),
    KVALIFISERINGSSTØNAD("KVALIFISERINGSSTØNAD"),
    NY_AFP("NY_AFP"),
    NYE_LIVRENTER("NYE_LIVRENTER"
    ),
    OVERGANGSSTØNAD_ENSLIG("OVERGANGSSTØNAD_ENSLIG"
    ),
    OVERGANGSSTØNAD_EKTEFELLE("OVERGANGSSTØNAD_EKTEFELLE"),
    PENSJON_DØDSMÅNED("PENSJON_DØDSMÅNED"),
    LIVRENTER("LIVRENTER"),
    RENTEFORDEL_LÅN("RENTEFORDEL_LÅN"),
    SUPPLERENDE_STØNAD("SUPPLERENDE_STØNAD"),
    UFØREPENSJON("UFØREPENSJON"),
    UFØREPENSJON_ANDRE("UFØREPENSJON_ANDRE"),
    UFØREPENSJON_ANDRE_ETTEROPPGJØR("UFØREPENSJON_ANDRE_ETTEROPPGJØR"
    ),
    UNDERHOLDNINGSBIDRAG("UNDERHOLDNINGSBIDRAG"),
    ;

    private static final Map<String, PensjonTrygdType> KODER = new LinkedHashMap<>();

    public static final String KODEVERK = "PENSJON_TRYGD_BESKRIVELSE";

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    @JsonValue
    private final String kode;

    PensjonTrygdType(String kode) {
        this.kode = kode;
    }

    @JsonCreator(mode = Mode.DELEGATING)
    public static PensjonTrygdType fraKode(Object node) {
        if (node == null) {
            return null;
        }
        String kode = TempAvledeKode.getVerdi(PensjonTrygdType.class, node, "kode");
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent PensjonTrygdType: " + kode);
        }
        return ad;
    }

    public static Map<String, PensjonTrygdType> kodeMap() {
        return Collections.unmodifiableMap(KODER);
    }

    @Override
    public String getKodeverk() {
        return KODEVERK;
    }

    @Override
    public String getKode() {
        return kode;
    }

}
