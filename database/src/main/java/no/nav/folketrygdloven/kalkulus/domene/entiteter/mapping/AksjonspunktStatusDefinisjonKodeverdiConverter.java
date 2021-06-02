package no.nav.folketrygdloven.kalkulus.domene.entiteter.mapping;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import no.nav.folketrygdloven.kalkulus.kodeverk.AksjonspunktStatus;

@Converter(autoApply = true)
public class AksjonspunktStatusDefinisjonKodeverdiConverter implements AttributeConverter<AksjonspunktStatus, String> {
    @Override
    public String convertToDatabaseColumn(AksjonspunktStatus attribute) {
        return attribute == null ? null : attribute.getKode();
    }

    @Override
    public AksjonspunktStatus convertToEntityAttribute(String dbData) {
        return dbData == null ? null : AksjonspunktStatus.fraKode(dbData);
    }
}
