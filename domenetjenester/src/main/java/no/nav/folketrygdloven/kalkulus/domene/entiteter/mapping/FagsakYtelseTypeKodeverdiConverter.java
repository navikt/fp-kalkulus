package no.nav.folketrygdloven.kalkulus.domene.entiteter.mapping;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;

@Converter(autoApply = true)
public class FagsakYtelseTypeKodeverdiConverter implements AttributeConverter<FagsakYtelseType, String> {
    @Override
    public String convertToDatabaseColumn(FagsakYtelseType attribute) {
        return attribute == null || FagsakYtelseType.UDEFINERT.equals(attribute ) ? null : attribute.getDatabaseKode();
    }

    @Override
    public FagsakYtelseType convertToEntityAttribute(String dbData) {
        return KodeKonstanter.fraDatabasekode(dbData, FagsakYtelseType.UDEFINERT, FagsakYtelseType::fraKode);
    }
}
