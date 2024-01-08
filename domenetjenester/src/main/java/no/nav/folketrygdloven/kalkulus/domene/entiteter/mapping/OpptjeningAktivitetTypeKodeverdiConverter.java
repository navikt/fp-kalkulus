package no.nav.folketrygdloven.kalkulus.domene.entiteter.mapping;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;

@Converter(autoApply = true)
public class OpptjeningAktivitetTypeKodeverdiConverter implements AttributeConverter<OpptjeningAktivitetType, String> {

    @Override
    public String convertToDatabaseColumn(OpptjeningAktivitetType attribute) {
        return attribute == null ? null : OpptjeningAktivitetType.UDEFINERT.equals(attribute) ?
                KodeKonstanter.UDEFINERT : attribute.getDatabaseKode();
    }

    @Override
    public OpptjeningAktivitetType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : KodeKonstanter.UDEFINERT.equals(dbData) ?
                OpptjeningAktivitetType.UDEFINERT : OpptjeningAktivitetType.fraDatabaseKode(dbData);
    }

}
