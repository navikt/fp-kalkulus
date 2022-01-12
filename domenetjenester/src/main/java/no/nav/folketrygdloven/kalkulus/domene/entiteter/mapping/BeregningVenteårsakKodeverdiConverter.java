package no.nav.folketrygdloven.kalkulus.domene.entiteter.mapping;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningVenteårsak;

@Converter(autoApply = true)
public class BeregningVenteårsakKodeverdiConverter implements AttributeConverter<BeregningVenteårsak, String> {
    @Override
    public String convertToDatabaseColumn(BeregningVenteårsak attribute) {
        return attribute == null ? null : attribute.getKode();
    }

    @Override
    public BeregningVenteårsak convertToEntityAttribute(String dbData) {
        return dbData == null ? null : BeregningVenteårsak.fraKode(dbData);
    }
}
