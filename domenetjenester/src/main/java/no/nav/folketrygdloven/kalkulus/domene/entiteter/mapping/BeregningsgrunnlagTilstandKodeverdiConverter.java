package no.nav.folketrygdloven.kalkulus.domene.entiteter.mapping;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;

@Converter(autoApply = true)
public class BeregningsgrunnlagTilstandKodeverdiConverter implements AttributeConverter<BeregningsgrunnlagTilstand, String> {

    @Override
    public String convertToDatabaseColumn(BeregningsgrunnlagTilstand attribute) {
        return attribute == null ? null : attribute.getKode();
    }

    @Override
    public BeregningsgrunnlagTilstand convertToEntityAttribute(String dbData) {
        return dbData == null ? null : BeregningsgrunnlagTilstand.fraKode(dbData);
    }
}
