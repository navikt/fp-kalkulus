package no.nav.folketrygdloven.kalkulus.domene.entiteter.mapping;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.YtelseTyperKalkulusStøtter;

@Converter(autoApply = true)
public class YtelseTyperKalkulusStøtterKodeverdiConverter implements AttributeConverter<YtelseTyperKalkulusStøtter, String> {
    @Override
    public String convertToDatabaseColumn(YtelseTyperKalkulusStøtter attribute) {
        return attribute == null ? null : attribute.getKode();
    }

    @Override
    public YtelseTyperKalkulusStøtter convertToEntityAttribute(String dbData) {
        return dbData == null ? null : YtelseTyperKalkulusStøtter.fraKode(dbData);
    }
}