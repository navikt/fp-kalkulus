package no.nav.folketrygdloven.kalkulus.domene.håndtering.mapping;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.kalkulus.kontrakt.request.håndtering.overstyring.OverstyrRefusjonHåndteringDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.håndtering.refusjon.RefusjonOverstyringAndel;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.håndtering.refusjon.RefusjonOverstyringAndel.RefusjonOverstyringPeriode;
import no.nav.folketrygdloven.kalkulus.kodeverk.RefusjonKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.RefusjonUtfallÅrsak;

class OppdatererDtoMapperTest {

    @Test
    void skal_mappe_refusjonsoverstyrte_andeler_og_perioder() {
        var dto = new OverstyrRefusjonHåndteringDto(List.of(
            new RefusjonOverstyringAndel("999999999", "arbeidsforhold-1", RefusjonKilde.SAKSBEHANDLER, List.of(
                new RefusjonOverstyringPeriode("2026-01-01", "2026-01-31", new BigDecimal("12500.50"), RefusjonUtfallÅrsak.UTMELDT),
                new RefusjonOverstyringPeriode("2026-02-01", "2026-02-28", new BigDecimal("10000.00"), null))),
            new RefusjonOverstyringAndel("888888888", null, RefusjonKilde.INNTEKTSMELDING, List.of(
                new RefusjonOverstyringPeriode("2026-03-01", "2026-03-31", new BigDecimal("9500.25"), RefusjonUtfallÅrsak.IKKE_INNENFOR_FRIST)))));

        var resultat = OppdatererDtoMapper.mapOverstyrRefusjonDto(dto);

        assertThat(resultat.getOverstyrteAndeler()).containsExactly(
            new no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.RefusjonOverstyringAndel(
                "999999999",
                "arbeidsforhold-1",
                RefusjonKilde.SAKSBEHANDLER,
                List.of(
                    new no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.RefusjonOverstyringAndel.RefusjonOverstyringPeriode(
                        "2026-01-01", "2026-01-31", new BigDecimal("12500.50"), RefusjonUtfallÅrsak.UTMELDT),
                    new no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.RefusjonOverstyringAndel.RefusjonOverstyringPeriode(
                        "2026-02-01", "2026-02-28", new BigDecimal("10000.00"), null))),
            new no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.RefusjonOverstyringAndel(
                "888888888",
                null,
                RefusjonKilde.INNTEKTSMELDING,
                List.of(
                    new no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.RefusjonOverstyringAndel.RefusjonOverstyringPeriode(
                        "2026-03-01", "2026-03-31", new BigDecimal("9500.25"), RefusjonUtfallÅrsak.IKKE_INNENFOR_FRIST))));
    }

    @Test
    void skal_mappe_tom_andelsliste() {
        var resultat = OppdatererDtoMapper.mapOverstyrRefusjonDto(new OverstyrRefusjonHåndteringDto(List.of()));

        assertThat(resultat.getOverstyrteAndeler()).isEmpty();
    }
}
