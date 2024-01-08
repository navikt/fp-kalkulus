package no.nav.folketrygdloven.kalkulus.domene.entiteter.mapping;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagPeriodeRegelType;

@Converter(autoApply = true)
public class BeregningsgrunnlagPeriodeRegelTypeKodeverdiConverter implements AttributeConverter<BeregningsgrunnlagPeriodeRegelType, String> {

    @Override
    public String convertToDatabaseColumn(BeregningsgrunnlagPeriodeRegelType attribute) {
        return attribute == null ? null : BeregningsgrunnlagPeriodeRegelType.UDEFINERT.equals(attribute) ?
                KodeKonstanter.UDEFINERT : attribute.getDatabaseKode();
    }

    @Override
    public BeregningsgrunnlagPeriodeRegelType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : KodeKonstanter.UDEFINERT.equals(dbData) ?
                BeregningsgrunnlagPeriodeRegelType.UDEFINERT : BeregningsgrunnlagPeriodeRegelType.fraDatabaseKode(dbData);
    }
}
