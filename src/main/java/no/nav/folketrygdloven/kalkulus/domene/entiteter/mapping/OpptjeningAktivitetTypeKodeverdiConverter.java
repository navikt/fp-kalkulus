package no.nav.folketrygdloven.kalkulus.domene.entiteter.mapping;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;

@Converter(autoApply = true)
public class OpptjeningAktivitetTypeKodeverdiConverter implements AttributeConverter<OpptjeningAktivitetType, String> {

    @Override
    public String convertToDatabaseColumn(OpptjeningAktivitetType attribute) {
        return KodeKonstanter.tilDatabasekode(attribute, OpptjeningAktivitetType.UDEFINERT);
    }

    @Override
    public OpptjeningAktivitetType convertToEntityAttribute(String dbData) {
        return KodeKonstanter.fraDatabasekode(dbData, OpptjeningAktivitetType.UDEFINERT, OpptjeningAktivitetType::valueOf);
    }

}
