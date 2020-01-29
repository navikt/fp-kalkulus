package no.nav.folketrygdloven.kalkulus.felles.verktøy;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/** JPA konverterer for å skrive J/N for boolske verdier. */
@Converter
public class BooleanToStringConverter implements AttributeConverter<Boolean, String> {

    @Override
    public String convertToDatabaseColumn(Boolean attribute) {
        if (attribute == null) {
            return null;
        } else {
            return attribute ? "J" : "N";
        }
    }

    @Override
    public Boolean convertToEntityAttribute(String dbData) {
        if (dbData == null)
            return null;
        return "J".equals(dbData);
    }
}

