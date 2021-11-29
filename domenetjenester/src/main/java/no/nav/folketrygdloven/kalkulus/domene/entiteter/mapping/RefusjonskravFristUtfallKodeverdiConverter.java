package no.nav.folketrygdloven.kalkulus.domene.entiteter.mapping;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import no.nav.folketrygdloven.kalkulus.kodeverk.Utfall;

@Converter(autoApply = true)
public class RefusjonskravFristUtfallKodeverdiConverter implements AttributeConverter<Utfall, String> {
    @Override
    public String convertToDatabaseColumn(Utfall attribute) {
        return attribute == null ? null : attribute.getKode();
    }

    @Override
    public Utfall convertToEntityAttribute(String dbData) {
        return dbData == null ? null : Utfall.fraKode(dbData);
    }

}
