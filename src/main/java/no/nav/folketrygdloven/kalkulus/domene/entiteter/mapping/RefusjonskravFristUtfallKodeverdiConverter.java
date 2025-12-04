package no.nav.folketrygdloven.kalkulus.domene.entiteter.mapping;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import no.nav.folketrygdloven.kalkulus.kodeverk.Utfall;

@Converter(autoApply = true)
public class RefusjonskravFristUtfallKodeverdiConverter implements AttributeConverter<Utfall, String> {

    @Override
    public String convertToDatabaseColumn(Utfall attribute) {
        return KodeKonstanter.tilDatabasekode(attribute, Utfall.UDEFINERT);
    }

    @Override
    public Utfall convertToEntityAttribute(String dbData) {
        return KodeKonstanter.fraDatabasekode(dbData, Utfall.UDEFINERT, Utfall::valueOf);
    }

}
