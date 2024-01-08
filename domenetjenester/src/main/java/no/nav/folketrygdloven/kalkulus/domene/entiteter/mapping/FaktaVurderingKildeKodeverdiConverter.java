package no.nav.folketrygdloven.kalkulus.domene.entiteter.mapping;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaVurderingKilde;

@Converter(autoApply = true)
public class FaktaVurderingKildeKodeverdiConverter implements AttributeConverter<FaktaVurderingKilde, String> {
    @Override
    public String convertToDatabaseColumn(FaktaVurderingKilde attribute) {
        return attribute == null ? null : FaktaVurderingKilde.UDEFINERT.equals(attribute) ?
                KodeKonstanter.UDEFINERT : attribute.getDatabaseKode();
    }

    @Override
    public FaktaVurderingKilde convertToEntityAttribute(String dbData) {
        return dbData == null ? null : KodeKonstanter.UDEFINERT.equals(dbData) ?
                FaktaVurderingKilde.UDEFINERT : FaktaVurderingKilde.fraDatabaseKode(dbData);
    }

}
