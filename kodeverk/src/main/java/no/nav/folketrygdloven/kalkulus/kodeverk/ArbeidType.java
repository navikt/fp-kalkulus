package no.nav.folketrygdloven.kalkulus.kodeverk;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonValue;


@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum ArbeidType implements Kodeverdi, KontraktKode {

    ETTERLØNN_SLUTTPAKKE,
    FORENKLET_OPPGJØRSORDNING,
    FRILANSER, // Frilanser, samlet aktivitet
    FRILANSER_OPPDRAGSTAKER, // Frilansere/oppdragstakere/honorar/mm, register
    LØNN_UNDER_UTDANNING,
    MARITIMT_ARBEIDSFORHOLD,
    MILITÆR_ELLER_SIVILTJENESTE,
    ORDINÆRT_ARBEIDSFORHOLD,
    PENSJON_OG_ANDRE_TYPER_YTELSER_UTEN_ANSETTELSESFORHOLD(
    ),
    NÆRING, // Selvstendig næringsdrivende
    UTENLANDSK_ARBEIDSFORHOLD,
    VENTELØNN_VARTPENGER,
    VANLIG,
    UDEFINERT,
    ;

    public static final Set<ArbeidType> AA_REGISTER_TYPER = Set.of(
            ArbeidType.ORDINÆRT_ARBEIDSFORHOLD,
            ArbeidType.MARITIMT_ARBEIDSFORHOLD,
            ArbeidType.FORENKLET_OPPGJØRSORDNING);

    private static final Map<String, ArbeidType> KODER = new LinkedHashMap<>();

    static {
        KODER.putIfAbsent(KodeKonstanter.UDEFINERT, UDEFINERT);
        for (var v : values()) {
            if (KODER.putIfAbsent(v.name(), v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.name());
            }
        }
    }


    @JsonCreator(mode = Mode.DELEGATING)
    public static ArbeidType fraKode(Object node) {
        if (node == null) {
            return null;
        }
        String kode = TempAvledeKode.getVerdi(ArbeidType.class, node, "kode");
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent ArbeidType: " + kode);
        }
        return ad;
    }

    @Override
    @JsonValue
    public String getKode() {
        return this == UDEFINERT ? KodeKonstanter.UDEFINERT : name();
    }

}
