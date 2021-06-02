package no.nav.folketrygdloven.kalkulus.domene.entiteter.mapping;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSteg;

@Converter(autoApply = true)
public class BeregningStegKodeverdiConverter implements AttributeConverter<BeregningSteg, String> {
    @Override
    public String convertToDatabaseColumn(BeregningSteg attribute) {
        return attribute == null ? null : attribute.getKode();
    }

    @Override
    public BeregningSteg convertToEntityAttribute(String dbData) {
        return dbData == null ? null : BeregningSteg.fraKode(dbData);
    }
}
