package no.nav.folketrygdloven.kalkulus.domene.entiteter.mapping;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagRegelType;

@Converter(autoApply = true)
public class BeregningsgrunnlagRegelTypeKodeverdiConverter implements AttributeConverter<BeregningsgrunnlagRegelType, String> {

    @Override
    public String convertToDatabaseColumn(BeregningsgrunnlagRegelType attribute) {
        return KodeKonstanter.tilDatabasekode(attribute, BeregningsgrunnlagRegelType.UDEFINERT);
    }

    @Override
    public BeregningsgrunnlagRegelType convertToEntityAttribute(String dbData) {
        return KodeKonstanter.fraDatabasekode(dbData, BeregningsgrunnlagRegelType.UDEFINERT, BeregningsgrunnlagRegelType::valueOf);
    }
}
