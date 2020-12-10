package no.nav.folketrygdloven.kalkulus.domene.entiteter.mapping;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AndelKilde;

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