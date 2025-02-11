package no.nav.folketrygdloven.kalkulus.domene.entiteter.mapping;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import no.nav.folketrygdloven.kalkulus.kodeverk.PeriodeÅrsak;

@Converter(autoApply = true)
public class PeriodeÅrsakKodeverdiConverter implements AttributeConverter<PeriodeÅrsak, String> {

    @Override
    public String convertToDatabaseColumn(PeriodeÅrsak attribute) {
        return KodeKonstanter.tilDatabasekode(attribute, PeriodeÅrsak.UDEFINERT);
    }

    @Override
    public PeriodeÅrsak convertToEntityAttribute(String dbData) {
        return KodeKonstanter.fraDatabasekode(dbData, PeriodeÅrsak.UDEFINERT, PeriodeÅrsak::valueOf);
    }
}
