package no.nav.folketrygdloven.kalkulus.domene.entiteter.mapping;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import no.nav.folketrygdloven.kalkulus.kodeverk.PeriodeÅrsak;

@Converter(autoApply = true)
public class PeriodeÅrsakKodeverdiConverter implements AttributeConverter<PeriodeÅrsak, String> {

    @Override
    public String convertToDatabaseColumn(PeriodeÅrsak attribute) {
        return attribute == null ? null : PeriodeÅrsak.UDEFINERT.equals(attribute) ?
                KodeKonstanter.UDEFINERT : attribute.getDatabaseKode();
    }

    @Override
    public PeriodeÅrsak convertToEntityAttribute(String dbData) {
        return dbData == null ? null : KodeKonstanter.UDEFINERT.equals(dbData) ?
                PeriodeÅrsak.UDEFINERT : PeriodeÅrsak.fraDatabaseKode(dbData);
    }
}
