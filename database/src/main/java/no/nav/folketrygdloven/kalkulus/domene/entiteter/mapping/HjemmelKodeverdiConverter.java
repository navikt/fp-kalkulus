package no.nav.folketrygdloven.kalkulus.domene.entiteter.mapping;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import no.nav.folketrygdloven.kalkulus.kodeverk.Hjemmel;

@Converter(autoApply = true)
public class HjemmelKodeverdiConverter implements AttributeConverter<Hjemmel, String> {
    @Override
    public String convertToDatabaseColumn(Hjemmel attribute) {
        return attribute == null ? null : attribute.getKode();
    }

    @Override
    public Hjemmel convertToEntityAttribute(String dbData) {
        return dbData == null ? null : Hjemmel.fraKode(dbData);
    }
}