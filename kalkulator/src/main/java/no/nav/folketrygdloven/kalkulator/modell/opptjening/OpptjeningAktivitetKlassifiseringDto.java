package no.nav.folketrygdloven.kalkulator.modell.opptjening;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulator.modell.kodeverk.Kodeverdi;

public enum OpptjeningAktivitetKlassifiseringDto implements Kodeverdi {

    BEKREFTET_GODKJENT("BEKREFTET_GODKJENT", "Bekreftet godkjent"),
    BEKREFTET_AVVIST("BEKREFTET_AVVIST", "Bekreftet avvist"),
    ANTATT_GODKJENT("ANTATT_GODKJENT", "Antatt godkjent"),
    MELLOMLIGGENDE_PERIODE("MELLOMLIGGENDE_PERIODE", "Mellomliggende periode"),
    UDEFINERT("-", "UDEFINERT"),
    ;

    private static final Map<String, OpptjeningAktivitetKlassifiseringDto> KODER = new LinkedHashMap<>();

    public static final String KODEVERK = "OPPTJENING_AKTIVITET_KLASSIFISERING";

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

    private OpptjeningAktivitetKlassifiseringDto(String kode) {
        this.kode = kode;
    }

    private OpptjeningAktivitetKlassifiseringDto(String kode, String navn) {
        this.kode = kode;
        this.navn = navn;
    }

    @JsonCreator
    public static OpptjeningAktivitetKlassifiseringDto fraKode(@JsonProperty("kode") String kode) {
        if (kode == null) {
            return null;
        }
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent OpptjeningAktivitetKlassifisering: " + kode);
        }
        return ad;
    }

    public static Map<String, OpptjeningAktivitetKlassifiseringDto> kodeMap() {
        return Collections.unmodifiableMap(KODER);
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

    public static void main(String[] args) {
        System.out.println(KODER.keySet());
    }

    @Converter(autoApply = true)
    public static class KodeverdiConverter implements AttributeConverter<OpptjeningAktivitetKlassifiseringDto, String> {
        @Override
        public String convertToDatabaseColumn(OpptjeningAktivitetKlassifiseringDto attribute) {
            return attribute == null ? null : attribute.getKode();
        }

        @Override
        public OpptjeningAktivitetKlassifiseringDto convertToEntityAttribute(String dbData) {
            return dbData == null ? null : fraKode(dbData);
        }
    }
}
