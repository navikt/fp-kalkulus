package no.nav.folketrygdloven.kalkulus.domene.entiteter.mapping;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;

@Converter(autoApply = true)
public class InntektskategoriKodeverdiConverter implements AttributeConverter<Inntektskategori, String> {
    @Override
    public String convertToDatabaseColumn(Inntektskategori attribute) {
        return attribute == null ? null : Inntektskategori.UDEFINERT.equals(attribute) ?
                KodeKonstanter.UDEFINERT : attribute.getDatabaseKode();
    }

    @Override
    public Inntektskategori convertToEntityAttribute(String dbData) {
        return dbData == null ? null : KodeKonstanter.UDEFINERT.equals(dbData) ?
                Inntektskategori.UDEFINERT : Inntektskategori.fraDatabaseKode(dbData);
    }

}
