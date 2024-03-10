package no.nav.folketrygdloven.kalkulus.domene.entiteter.mapping;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningAktivitetHandlingType;

@Converter(autoApply = true)
public class BeregingAktivitetHandlingTypeKodeverdiConverter implements AttributeConverter<BeregningAktivitetHandlingType, String> {

    @Override
    public String convertToDatabaseColumn(BeregningAktivitetHandlingType attribute) {
        return KodeKonstanter.tilDatabasekode(attribute, BeregningAktivitetHandlingType.UDEFINERT);
    }

    @Override
    public BeregningAktivitetHandlingType convertToEntityAttribute(String dbData) {
        return KodeKonstanter.fraDatabasekode(dbData, BeregningAktivitetHandlingType.UDEFINERT, BeregningAktivitetHandlingType::valueOf);
    }
}
