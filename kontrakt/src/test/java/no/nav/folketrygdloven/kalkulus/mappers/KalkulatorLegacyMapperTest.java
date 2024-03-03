package no.nav.folketrygdloven.kalkulus.mappers;


import static no.nav.folketrygdloven.kalkulus.mappers.JsonMapperUtil.READER_JSON;
import static no.nav.folketrygdloven.kalkulus.mappers.JsonMapperUtil.validateResult;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulus.felles.v1.Beløp;
import no.nav.folketrygdloven.kalkulus.felles.v1.KalkulatorInputDto;

public class KalkulatorLegacyMapperTest {

    private record MedBeløp(Beløp beløp) {}

    @Test
    void skal_deserialisere_legacy_beløp() throws Exception {

        var beløpMedHeltallsVerdi = """
                {
                   "beløp": 101
                }
                """;

        var beløpMedDesimalVerdi = """
                {
                   "beløp": 101.23
                }
                """;

        var legacyBeløpMedHeltallsVerdi = """
                {
                   "beløp": {
                      "verdi": 101
                   }
                }
                """;

        var legacyBeløpMedDesimalVerdi = """
                {
                   "beløp": {"verdi": 987.123}
                }
                """;

        var beløpReader = READER_JSON.forType(MedBeløp.class);

        var heltall = beløpReader.readValue(beløpMedHeltallsVerdi);
        var desimal = beløpReader.readValue(beløpMedDesimalVerdi);
        var legacyHeltall = beløpReader.readValue(legacyBeløpMedHeltallsVerdi);
        var legacyDesimal = beløpReader.readValue(legacyBeløpMedDesimalVerdi);

        assertThat(heltall).isEqualTo(new MedBeløp(Beløp.fra(101)));
        assertThat(desimal).isEqualTo(new MedBeløp(Beløp.fra(new BigDecimal("101.23"))));
        assertThat(legacyHeltall).isEqualTo(new MedBeløp(Beløp.fra(101)));
        assertThat(legacyDesimal).isEqualTo(new MedBeløp(Beløp.fra(BigDecimal.valueOf(987.123d))));
    }



    @Test
    void skal_lese_kalkulator_input_json() throws Exception {
        var input = Optional.ofNullable(lesEksempelfil()).orElseThrow();
        KalkulatorInputDto grunnlag = READER_JSON.forType(KalkulatorInputDto.class).readValue(input);

        assertThat(grunnlag).isNotNull();
        assertThat(grunnlag.getIayGrunnlag()).isNotNull();
        validateResult(grunnlag);
    }

    private String lesEksempelfil() throws IOException {
        try (var in = KalkulatorLegacyMapperTest.class.getResourceAsStream("/input/eksempel-input.json")) {
            return in != null ? new String(in.readAllBytes()) : null;
        }
    }

}
