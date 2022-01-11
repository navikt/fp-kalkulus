package no.nav.folketrygdloven.kalkulus.domene.entiteter.mapping;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;

@Converter(autoApply = true)
public class AktivitetStatusKodeverdiConverter implements AttributeConverter<AktivitetStatus, String> {

    @Override
    public String convertToDatabaseColumn(AktivitetStatus attribute) {
        return attribute == null ? null : attribute.getKode();
    }

    @Override
    public AktivitetStatus convertToEntityAttribute(String dbData) {
        return dbData == null ? null : AktivitetStatus.fraKode(dbData);
    }
}