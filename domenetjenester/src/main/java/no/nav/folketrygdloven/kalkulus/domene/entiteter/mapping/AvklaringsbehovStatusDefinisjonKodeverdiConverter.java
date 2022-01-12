package no.nav.folketrygdloven.kalkulus.domene.entiteter.mapping;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovStatus;

@Converter(autoApply = true)
public class AvklaringsbehovStatusDefinisjonKodeverdiConverter implements AttributeConverter<AvklaringsbehovStatus, String> {
    @Override
    public String convertToDatabaseColumn(AvklaringsbehovStatus attribute) {
        return attribute == null ? null : attribute.getKode();
    }

    @Override
    public AvklaringsbehovStatus convertToEntityAttribute(String dbData) {
        return dbData == null ? null : AvklaringsbehovStatus.fraKode(dbData);
    }
}
