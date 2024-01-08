package no.nav.folketrygdloven.kalkulus.domene.entiteter.mapping;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import no.nav.folketrygdloven.kalkulus.kodeverk.Utfall;

@Converter(autoApply = true)
public class RefusjonskravFristUtfallKodeverdiConverter implements AttributeConverter<Utfall, String> {

    @Override
    public String convertToDatabaseColumn(Utfall attribute) {
        return attribute == null ? null : Utfall.UDEFINERT.equals(attribute) ?
                KodeKonstanter.UDEFINERT : attribute.getDatabaseKode();
    }

    @Override
    public Utfall convertToEntityAttribute(String dbData) {
        return dbData == null ? null : KodeKonstanter.UDEFINERT.equals(dbData) ?
                Utfall.UDEFINERT : Utfall.fraDatabaseKode(dbData);
    }

}
