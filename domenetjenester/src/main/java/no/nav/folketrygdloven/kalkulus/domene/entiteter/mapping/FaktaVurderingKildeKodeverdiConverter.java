package no.nav.folketrygdloven.kalkulus.domene.entiteter.mapping;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaVurderingKilde;

@Converter(autoApply = true)
public class FaktaVurderingKildeKodeverdiConverter implements AttributeConverter<FaktaVurderingKilde, String> {
    @Override
    public String convertToDatabaseColumn(FaktaVurderingKilde attribute) {
        return attribute == null ? null : attribute.getKode();
    }

    @Override
    public FaktaVurderingKilde convertToEntityAttribute(String dbData) {
        return dbData == null ? null : FaktaVurderingKilde.fraKode(dbData);
    }

}
