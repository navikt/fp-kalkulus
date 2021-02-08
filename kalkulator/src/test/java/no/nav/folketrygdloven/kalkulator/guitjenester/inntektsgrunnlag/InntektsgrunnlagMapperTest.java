package no.nav.folketrygdloven.kalkulator.guitjenester.inntektsgrunnlag;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.modell.iay.InntektDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektspostDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektskildeType;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektspostType;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.inntektsgrunnlag.InntektsgrunnlagDto;

class InntektsgrunnlagMapperTest {
    private static final LocalDate STP = LocalDate.now();

    @Test
    public void skal_teste_at_korrekte_inntekter_mappes() {
        InntektsgrunnlagMapper mapper = new InntektsgrunnlagMapper(LocalDate.now(), Collections.emptyList());
        InntektDtoBuilder feilKilde = lagInntekt("123", InntektskildeType.INNTEKT_BEREGNING);
        InntektDtoBuilder korrektKilde = lagInntekt("123", InntektskildeType.INNTEKT_SAMMENLIGNING);
        feilKilde.leggTilInntektspost(lagInntektspost(feilKilde, 5000, månederFør(3)));
        feilKilde.leggTilInntektspost(lagInntektspost(feilKilde, 5000, månederFør(2)));
        korrektKilde.leggTilInntektspost(lagInntektspost(korrektKilde, 5000, månederFør(3)));
        korrektKilde.leggTilInntektspost(lagInntektspost(korrektKilde, 5000, månederFør(2)));

        Optional<InntektsgrunnlagDto> dto = mapper.map(Arrays.asList(feilKilde.build(), korrektKilde.build()));

        assertThat(dto).isPresent();
        assertThat(dto.get().getMåneder()).hasSize(2);
        assertThat(dto.get().getMåneder().get(0).getInntekter()).hasSize(1);
        assertThat(dto.get().getMåneder().get(1).getInntekter()).hasSize(1);
    }

    @Test
    public void skal_teste_at_frilans_merkes_korrekt() {
        InntektsgrunnlagMapper mapper = new InntektsgrunnlagMapper(LocalDate.now(), Collections.singletonList(Arbeidsgiver.virksomhet("321")));
        InntektDtoBuilder inntektFL = lagInntekt("321", InntektskildeType.INNTEKT_SAMMENLIGNING);
        InntektDtoBuilder inntektAT = lagInntekt("123", InntektskildeType.INNTEKT_SAMMENLIGNING);
        inntektFL.leggTilInntektspost(lagInntektspost(inntektFL, 3000, månederFør(3)));
        inntektFL.leggTilInntektspost(lagInntektspost(inntektFL, 3000, månederFør(2)));
        inntektAT.leggTilInntektspost(lagInntektspost(inntektAT, 5000, månederFør(3)));
        inntektAT.leggTilInntektspost(lagInntektspost(inntektAT, 5000, månederFør(2)));

        Optional<InntektsgrunnlagDto> dto = mapper.map(Arrays.asList(inntektFL.build(), inntektAT.build()));

        assertThat(dto).isPresent();
        assertThat(dto.get().getMåneder()).hasSize(2);
        assertThat(dto.get().getMåneder().get(0).getInntekter()).hasSize(2);

        assertThat(dto.get().getMåneder().get(0).getInntekter()).hasSize(2);
        assertThat(dto.get().getMåneder().get(0).getInntekter().stream()
                .anyMatch(innt -> innt.getAktivitetStatus().erFrilanser()
                        && innt.getBeløp().intValue() == 3000))
                .isTrue();
        assertThat(dto.get().getMåneder().get(0).getInntekter().stream()
                .anyMatch(innt -> innt.getAktivitetStatus().erArbeidstaker()
                        && innt.getBeløp().intValue() == 5000))
                .isTrue();

        assertThat(dto.get().getMåneder().get(1).getInntekter()).hasSize(2);
        assertThat(dto.get().getMåneder().get(1).getInntekter().stream()
                .anyMatch(innt -> innt.getAktivitetStatus().erFrilanser()
                        && innt.getBeløp().intValue() == 3000))
                .isTrue();
        assertThat(dto.get().getMåneder().get(1).getInntekter().stream()
                .anyMatch(innt -> innt.getAktivitetStatus().erArbeidstaker()
                        && innt.getBeløp().intValue() == 5000))
                .isTrue();


    }


    private InntektspostDtoBuilder lagInntektspost(InntektDtoBuilder builder, int inntekt, LocalDate fom) {
        return builder.getInntektspostBuilder()
                .medPeriode(fom, fom.with(TemporalAdjusters.lastDayOfMonth()))
                .medBeløp(BigDecimal.valueOf(inntekt))
                .medInntektspostType(InntektspostType.LØNN);
    }

    private LocalDate månederFør(int månederFør) {
        return STP.minusMonths(månederFør).withDayOfMonth(1);
    }

    private InntektDtoBuilder lagInntekt(String orgnr, InntektskildeType kilde) {
        return InntektDtoBuilder.oppdatere(Optional.empty()).medInntektsKilde(kilde).medArbeidsgiver(Arbeidsgiver.virksomhet(orgnr));
    }

}
