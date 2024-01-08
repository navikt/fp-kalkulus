package no.nav.folketrygdloven.kalkulus.domene.entiteter.mapping;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagRegelType;

@Converter(autoApply = true)
public class BeregningsgrunnlagRegelTypeKodeverdiConverter implements AttributeConverter<BeregningsgrunnlagRegelType, String> {

    @Override
    public String convertToDatabaseColumn(BeregningsgrunnlagRegelType attribute) {
        return attribute == null ? null : BeregningsgrunnlagRegelType.UDEFINERT.equals(attribute) ?
                KodeKonstanter.UDEFINERT : attribute.getDatabaseKode();
    }

    @Override
    public BeregningsgrunnlagRegelType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : KodeKonstanter.UDEFINERT.equals(dbData) ?
                BeregningsgrunnlagRegelType.UDEFINERT : BeregningsgrunnlagRegelType.fraDatabaseKode(dbData);
    }
}
