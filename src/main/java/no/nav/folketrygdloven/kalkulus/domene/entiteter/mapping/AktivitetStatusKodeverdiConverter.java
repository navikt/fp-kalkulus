package no.nav.folketrygdloven.kalkulus.domene.entiteter.mapping;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;

@Converter(autoApply = true)
public class AktivitetStatusKodeverdiConverter implements AttributeConverter<AktivitetStatus, String> {

    @Override
    public String convertToDatabaseColumn(AktivitetStatus attribute) {
        return KodeKonstanter.tilDatabasekode(attribute, AktivitetStatus.UDEFINERT);
    }

    @Override
    public AktivitetStatus convertToEntityAttribute(String dbData) {
        return KodeKonstanter.fraDatabasekode(dbData, AktivitetStatus.UDEFINERT, AktivitetStatus::fraKode);
    }
}
