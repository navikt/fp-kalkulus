package no.nav.folketrygdloven.kalkulus.kodeverk;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonValue;


@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum Hjemmel implements Kodeverdi, DatabaseKode, KontraktKode {

    F_9_9, // folketrygdloven § 9-9

    F_9_8_8_28, // folketrygdloven §§ 9-8 og 8-28
    F_9_9_8_28_8_30, // folketrygdloven §§ 9-9, 8-28 og 8-30
    F_9_9_8_35, // folketrygdloven §§ 9-9 og 8-35
    F_9_9_8_38, // folketrygdloven §§ 9-9 og 8-38
    F_9_9_8_40, // folketrygdloven §§ 9-9 og 8-40
    F_9_9_8_41, // folketrygdloven §§ 9-9 og 8-41
    F_9_9_8_42, // folketrygdloven §§ 9-9 og 8-42
    F_9_9_8_43, // folketrygdloven §§ 9-9 og 8-43
    F_9_9_8_47, // folketrygdloven §§ 9-9 og 8-47
    F_9_9_8_49, // folketrygdloven §§ 9-9 og 8-49

    F_14_7, // folketrygdloven § 14-7
    F_14_7_8_30, // folketrygdloven §§ 14-7 og 8-30
    F_14_7_8_28_8_30, // folketrygdloven §§ 14-7, 8-28 og 8-30
    F_14_7_8_35, // folketrygdloven §§ 14-7 og 8-35
    F_14_7_8_38, // folketrygdloven §§ 14-7 og 8-38
    F_14_7_8_40, // folketrygdloven §§ 14-7 og 8-40
    F_14_7_8_41, // folketrygdloven §§ 14-7 og 8-41
    F_14_7_8_42, // folketrygdloven §§ 14-7 og 8-42
    F_14_7_8_43, // folketrygdloven §§ 14-7 og 8-43
    F_14_7_8_47, // folketrygdloven §§ 14-7 og 8-47
    F_14_7_8_49, // folketrygdloven §§ 14-7 og 8-49

    F_22_13_6, // folketrygdloven §§ 22-13 sjette ledd
    COV_1_5, // Midlertidig forskrift om unntak fra folketrygdloven og arbeidsmiljøloven i forbindelse med covid-19-pandemien § 1-5

    KORONALOVEN_3, // koronaloven § 3


    UDEFINERT,
    ;

    private static final Map<String, Hjemmel> KODER = new LinkedHashMap<>();

    static {
        KODER.putIfAbsent(KodeKonstanter.UDEFINERT, UDEFINERT);
        for (var v : values()) {
            if (KODER.putIfAbsent(v.name(), v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.name());
            }
        }
    }

    @JsonCreator(mode = Mode.DELEGATING)
    public static Hjemmel fraKode(Object node) {
        if (node == null) {
            return null;
        }
        String kode = TempAvledeKode.getVerdi(Hjemmel.class, node, "kode");
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent Hjemmel: " + kode);
        }
        return ad;
    }

    @Override
    @JsonValue
    public String getKode() {
        return this == UDEFINERT ? KodeKonstanter.UDEFINERT : name();
    }

    public static Hjemmel fraDatabaseKode(String databaseKode) {
        return fraKode(databaseKode);
    }

}
