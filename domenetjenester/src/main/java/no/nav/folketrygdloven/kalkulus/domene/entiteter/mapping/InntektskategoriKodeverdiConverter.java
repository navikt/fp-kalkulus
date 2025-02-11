package no.nav.folketrygdloven.kalkulus.domene.entiteter.mapping;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;

@Converter(autoApply = true)
public class InntektskategoriKodeverdiConverter implements AttributeConverter<Inntektskategori, String> {
    @Override
    public String convertToDatabaseColumn(Inntektskategori attribute) {
        return KodeKonstanter.tilDatabasekode(attribute, Inntektskategori.UDEFINERT);
    }

    @Override
    public Inntektskategori convertToEntityAttribute(String dbData) {
        return KodeKonstanter.fraDatabasekode(dbData, Inntektskategori.UDEFINERT, Inntektskategori::valueOf);
    }

}
