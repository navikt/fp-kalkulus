package no.nav.folketrygdloven.kalkulus.domene.entiteter.mapping;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSatsType;

@Converter(autoApply = true)
public class BeregningSatsTypeKodeverdiConverter implements AttributeConverter<BeregningSatsType, String> {

    @Override
    public String convertToDatabaseColumn(BeregningSatsType attribute) {
        return KodeKonstanter.tilDatabasekode(attribute, BeregningSatsType.UDEFINERT);
    }

    @Override
    public BeregningSatsType convertToEntityAttribute(String dbData) {
        return KodeKonstanter.fraDatabasekode(dbData, BeregningSatsType.UDEFINERT, BeregningSatsType::valueOf);
    }
}
