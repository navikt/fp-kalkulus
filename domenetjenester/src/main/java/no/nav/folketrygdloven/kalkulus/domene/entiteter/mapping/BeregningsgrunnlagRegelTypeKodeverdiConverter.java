package no.nav.folketrygdloven.kalkulus.domene.entiteter.mapping;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagRegelType;

@Converter(autoApply = true)
public class BeregningsgrunnlagRegelTypeKodeverdiConverter implements AttributeConverter<BeregningsgrunnlagRegelType, String> {

    @Override
    public String convertToDatabaseColumn(BeregningsgrunnlagRegelType attribute) {
        return attribute == null ? null : attribute.getKode();
    }

    @Override
    public BeregningsgrunnlagRegelType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : BeregningsgrunnlagRegelType.fraKode(dbData);
    }
}