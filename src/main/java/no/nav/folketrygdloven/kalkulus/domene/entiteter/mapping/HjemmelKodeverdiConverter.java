package no.nav.folketrygdloven.kalkulus.domene.entiteter.mapping;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import no.nav.folketrygdloven.kalkulus.kodeverk.Hjemmel;

@Converter(autoApply = true)
public class HjemmelKodeverdiConverter implements AttributeConverter<Hjemmel, String> {

    @Override
    public String convertToDatabaseColumn(Hjemmel attribute) {
        return KodeKonstanter.tilDatabasekode(attribute, Hjemmel.UDEFINERT);
    }

    @Override
    public Hjemmel convertToEntityAttribute(String dbData) {
        return KodeKonstanter.fraDatabasekode(dbData, Hjemmel.UDEFINERT, Hjemmel::valueOf);
    }
}
