package no.nav.folketrygdloven.kalkulus.domene.entiteter.mapping;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseTyperKalkulusStøtterKontrakt;

@Converter(autoApply = true)
public class YtelseTyperKalkulusStøtterKodeverdiConverter implements AttributeConverter<YtelseTyperKalkulusStøtterKontrakt, String> {
    @Override
    public String convertToDatabaseColumn(YtelseTyperKalkulusStøtterKontrakt attribute) {
        return attribute == null ? null : attribute.getKode();
    }

    @Override
    public YtelseTyperKalkulusStøtterKontrakt convertToEntityAttribute(String dbData) {
        return dbData == null ? null : YtelseTyperKalkulusStøtterKontrakt.fraKode(dbData);
    }
}