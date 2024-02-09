package no.nav.folketrygdloven.kalkulus.kodeverk;

import java.time.Period;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum InntektPeriodeType implements Kodeverdi, KontraktKode {

    DAGLIG("DAGLG", Period.ofDays(1)),
    UKENTLIG("UKNLG", Period.ofWeeks(1)),
    BIUKENTLIG("14DLG", Period.ofWeeks(2)),
    MÅNEDLIG("MNDLG", Period.ofMonths(1)),
    ÅRLIG("AARLG", Period.ofYears(1)),
    FASTSATT25PAVVIK("INNFS", Period.ofYears(1)),
    PREMIEGRUNNLAG("PREMGR", Period.ofYears(1)),
    UDEFINERT(KodeKonstanter.UDEFINERT, null),
    ;

    /**
     * @deprecated bruk enum konstant.
     */
    @Deprecated(forRemoval = true)
    public static final InntektPeriodeType PER_ÅR = ÅRLIG;
    /**
     * @deprecated bruk enum konstant.
     */
    @Deprecated(forRemoval = true)
    public static final InntektPeriodeType PER_DAG = DAGLIG;
    /**
     * @deprecated bruk enum konstant.
     */
    @Deprecated(forRemoval = true)
    public static final InntektPeriodeType PER_MÅNED = MÅNEDLIG;
    /**
     * @deprecated bruk enum konstant.
     */
    @Deprecated(forRemoval = true)
    public static final InntektPeriodeType PER_UKE = UKENTLIG;
    /**
     * @deprecated bruk enum konstant.
     */
    @Deprecated(forRemoval = true)
    public static final InntektPeriodeType PER_14DAGER = BIUKENTLIG;
    /**
     * @deprecated bruk enum konstant.
     */
    @Deprecated(forRemoval = true)
    public static final InntektPeriodeType PREMIEGRUNNLAG_OPPDRAGSGIVER = PREMIEGRUNNLAG;
    /**
     * @deprecated bruk enum konstant.
     */
    @Deprecated(forRemoval = true)
    public static final InntektPeriodeType FASTSATT_ETTER_AVVIKHÅNDTERING = FASTSATT25PAVVIK;

    private static final Map<String, InntektPeriodeType> KODER = new LinkedHashMap<>();

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
    private final Period periode;

    InntektPeriodeType(String kode, Period periode) {
        this.kode = kode;
        this.periode = periode;
    }

    @JsonCreator(mode = Mode.DELEGATING)
    public static InntektPeriodeType fraKode(Object node) {
        if (node == null) {
            return null;
        }
        String kode = TempAvledeKode.getVerdi(InntektPeriodeType.class, node, "kode");
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent InntektPeriodeType: " + kode);
        }
        return ad;
    }


    @Override
    public String getKode() {
        return kode;
    }

    public Period getPeriode() {
        return periode;
    }


}
