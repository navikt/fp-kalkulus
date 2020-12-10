package no.nav.folketrygdloven.kalkulus.domene.entiteter.mapping;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.PeriodeÅrsak;

@Converter(autoApply = true)
public class PeriodeÅrsakKodeverdiConverter implements AttributeConverter<PeriodeÅrsak, String> {
    @Override
    public String convertToDatabaseColumn(PeriodeÅrsak attribute) {
        return attribute == null ? null : attribute.getKode();
    }

    @Override
    public PeriodeÅrsak convertToEntityAttribute(String dbData) {
        return dbData == null ? null : PeriodeÅrsak.fraKode(dbData);
    }
}