package no.nav.folketrygdloven.kalkulus.domene.entiteter.mapping;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;

@Converter(autoApply = true)
public class AvklaringsbehovDefinisjonKodeverdiConverter implements AttributeConverter<AvklaringsbehovDefinisjon, String> {
    @Override
    public String convertToDatabaseColumn(AvklaringsbehovDefinisjon attribute) {
        return attribute == null ? null : attribute.getKodeNy();
    }

    @Override
    public AvklaringsbehovDefinisjon convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        if (AvklaringsbehovDefinisjon.KODER.containsKey(dbData)) {
            return AvklaringsbehovDefinisjon.fraKode(dbData);
        }
        return  AvklaringsbehovDefinisjon.fraKode(dbData);
    }
}
