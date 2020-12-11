package no.nav.folketrygdloven.kalkulus.domene.entiteter.mapping;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSatsType;

@Converter(autoApply = true)
public class BeregningSatsTypeKodeverdiConverter implements AttributeConverter<BeregningSatsType, String> {

    @Override
    public String convertToDatabaseColumn(BeregningSatsType attribute) {
        return attribute == null ? null : attribute.getKode();
    }

    @Override
    public BeregningSatsType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : BeregningSatsType.fraKode(dbData);
    }
}