package no.nav.folketrygdloven.kalkulus.domene.entiteter.mapping;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;

@Converter(autoApply = true)
public class FaktaOmBeregningTilfelleKodeverdiConverter implements AttributeConverter<FaktaOmBeregningTilfelle, String> {

    @Override
    public String convertToDatabaseColumn(FaktaOmBeregningTilfelle attribute) {
        return KodeKonstanter.tilDatabasekode(attribute, FaktaOmBeregningTilfelle.UDEFINERT);
    }

    @Override
    public FaktaOmBeregningTilfelle convertToEntityAttribute(String dbData) {
        return KodeKonstanter.fraDatabasekode(dbData, FaktaOmBeregningTilfelle.UDEFINERT, FaktaOmBeregningTilfelle::valueOf);
    }
}
