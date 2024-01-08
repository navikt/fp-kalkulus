package no.nav.folketrygdloven.kalkulus.domene.entiteter.mapping;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;

@Converter(autoApply = true)
public class FaktaOmBeregningTilfelleKodeverdiConverter implements AttributeConverter<FaktaOmBeregningTilfelle, String> {

    @Override
    public String convertToDatabaseColumn(FaktaOmBeregningTilfelle attribute) {
        return attribute == null ? null : AktivitetStatus.UDEFINERT.equals(attribute) ?
                KodeKonstanter.UDEFINERT : attribute.getDatabaseKode();
    }

    @Override
    public FaktaOmBeregningTilfelle convertToEntityAttribute(String dbData) {
        return dbData == null ? null : KodeKonstanter.UDEFINERT.equals(dbData) ?
                FaktaOmBeregningTilfelle.UDEFINERT : FaktaOmBeregningTilfelle.fraDatabaseKode(dbData);
    }
}
