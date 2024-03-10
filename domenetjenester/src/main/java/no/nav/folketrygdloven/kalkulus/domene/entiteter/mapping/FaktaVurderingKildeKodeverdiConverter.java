package no.nav.folketrygdloven.kalkulus.domene.entiteter.mapping;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaVurderingKilde;

@Converter(autoApply = true)
public class FaktaVurderingKildeKodeverdiConverter implements AttributeConverter<FaktaVurderingKilde, String> {
    @Override
    public String convertToDatabaseColumn(FaktaVurderingKilde attribute) {
        return KodeKonstanter.tilDatabasekode(attribute, FaktaVurderingKilde.UDEFINERT);
    }

    @Override
    public FaktaVurderingKilde convertToEntityAttribute(String dbData) {
        return KodeKonstanter.fraDatabasekode(dbData, FaktaVurderingKilde.UDEFINERT, FaktaVurderingKilde::valueOf);
    }

}
