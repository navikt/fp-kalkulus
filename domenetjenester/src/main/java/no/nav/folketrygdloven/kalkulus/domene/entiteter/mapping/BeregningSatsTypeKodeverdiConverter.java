package no.nav.folketrygdloven.kalkulus.domene.entiteter.mapping;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSatsType;

@Converter(autoApply = true)
public class BeregningSatsTypeKodeverdiConverter implements AttributeConverter<BeregningSatsType, String> {

    @Override
    public String convertToDatabaseColumn(BeregningSatsType attribute) {
        return attribute == null ? null : BeregningSatsType.UDEFINERT.equals(attribute) ?
                KodeKonstanter.UDEFINERT : attribute.getDatabaseKode();
    }

    @Override
    public BeregningSatsType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : KodeKonstanter.UDEFINERT.equals(dbData) ?
                BeregningSatsType.UDEFINERT : BeregningSatsType.fraDatabaseKode(dbData);
    }
}
