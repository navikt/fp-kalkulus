package no.nav.folketrygdloven.kalkulus.domene.entiteter.mapping;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningAktivitetHandlingType;

@Converter(autoApply = true)
public class BeregingAktivitetHandlingTypeKodeverdiConverter implements AttributeConverter<BeregningAktivitetHandlingType, String> {

    @Override
    public String convertToDatabaseColumn(BeregningAktivitetHandlingType attribute) {
        return attribute == null ? null : BeregningAktivitetHandlingType.UDEFINERT.equals(attribute) ?
                KodeKonstanter.UDEFINERT : attribute.getDatabaseKode();
    }

    @Override
    public BeregningAktivitetHandlingType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : KodeKonstanter.UDEFINERT.equals(dbData) ?
                BeregningAktivitetHandlingType.UDEFINERT : BeregningAktivitetHandlingType.fraDatabaseKode(dbData);
    }
}
