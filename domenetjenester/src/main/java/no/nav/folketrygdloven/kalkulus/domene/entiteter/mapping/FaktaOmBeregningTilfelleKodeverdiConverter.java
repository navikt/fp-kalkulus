package no.nav.folketrygdloven.kalkulus.domene.entiteter.mapping;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;

@Converter(autoApply = true)
public class FaktaOmBeregningTilfelleKodeverdiConverter implements AttributeConverter<FaktaOmBeregningTilfelle, String> {

    @Override
    public String convertToDatabaseColumn(FaktaOmBeregningTilfelle attribute) {
        return attribute == null ? null : attribute.getKode();
    }

    @Override
    public FaktaOmBeregningTilfelle convertToEntityAttribute(String dbData) {
        return dbData == null ? null : FaktaOmBeregningTilfelle.fraKode(dbData);
    }
}