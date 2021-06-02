package no.nav.folketrygdloven.kalkulus.domene.entiteter.mapping;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import no.nav.folketrygdloven.kalkulus.kodeverk.AksjonspunktDefinisjon;

@Converter(autoApply = true)
public class AksjonspunktDefinisjonKodeverdiConverter implements AttributeConverter<AksjonspunktDefinisjon, String> {
    @Override
    public String convertToDatabaseColumn(AksjonspunktDefinisjon attribute) {
        return attribute == null ? null : attribute.getKode();
    }

    @Override
    public AksjonspunktDefinisjon convertToEntityAttribute(String dbData) {
        return dbData == null ? null : AksjonspunktDefinisjon.fraKode(dbData);
    }
}
