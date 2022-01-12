package no.nav.folketrygdloven.kalkulus.domene.entiteter.mapping;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import no.nav.folketrygdloven.kalkulus.kodeverk.AndelKilde;

@Converter(autoApply = true)
public class AndelKildeKodeverdiConverter implements AttributeConverter<AndelKilde, String> {
    @Override
    public String convertToDatabaseColumn(AndelKilde attribute) {
        return attribute == null ? null : attribute.getKode();
    }

    @Override
    public AndelKilde convertToEntityAttribute(String dbData) {
        return dbData == null ? null : AndelKilde.fraKode(dbData);
    }

}
