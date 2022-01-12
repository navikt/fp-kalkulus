package no.nav.folketrygdloven.kalkulus.domene.entiteter.mapping;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import no.nav.folketrygdloven.kalkulus.kodeverk.SammenligningsgrunnlagType;

@Converter(autoApply = true)
public class SammenligningsgrunnlagTypeKodeverdiConverter implements AttributeConverter<SammenligningsgrunnlagType, String> {
    @Override
    public String convertToDatabaseColumn(SammenligningsgrunnlagType attribute) {
        return attribute == null ? null : attribute.getKode();
    }

    @Override
    public SammenligningsgrunnlagType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : SammenligningsgrunnlagType.fraKode(dbData);
    }
}
