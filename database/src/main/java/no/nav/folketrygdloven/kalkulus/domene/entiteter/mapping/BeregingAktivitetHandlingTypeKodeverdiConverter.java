package no.nav.folketrygdloven.kalkulus.domene.entiteter.mapping;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningAktivitetHandlingType;

@Converter(autoApply = true)
public class BeregingAktivitetHandlingTypeKodeverdiConverter implements AttributeConverter<BeregningAktivitetHandlingType, String> {

    @Override
    public String convertToDatabaseColumn(BeregningAktivitetHandlingType attribute) {
        return attribute == null ? null : attribute.getKode();
    }

    @Override
    public BeregningAktivitetHandlingType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : BeregningAktivitetHandlingType.fraKode(dbData);
    }
}