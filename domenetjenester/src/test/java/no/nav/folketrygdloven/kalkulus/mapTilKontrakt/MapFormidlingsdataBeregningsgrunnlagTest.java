package no.nav.folketrygdloven.kalkulus.mapTilKontrakt;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import no.nav.folketrygdloven.kalkulus.typer.Beløp;

import no.nav.folketrygdloven.kalkulus.typer.Utbetalingsgrad;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.input.OmsorgspengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.PleiepengerSyktBarnGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.UtbetalingsgradGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.behandling.Skjæringstidspunkt;
import no.nav.folketrygdloven.kalkulator.modell.svp.AktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.kodeverk.UttakArbeidType;
import no.nav.folketrygdloven.kalkulus.response.v1.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.detaljert.BGAndelArbeidsforhold;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.detaljert.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.detaljert.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.detaljert.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.detaljert.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulus.typer.AktørId;
import no.nav.folketrygdloven.kalkulus.typer.Beløp;

class MapFormidlingsdataBeregningsgrunnlagTest {
    private static final Beløp GRUNNBELØP = Beløp.fra(100000);
    private static final LocalDate STP_DATO = LocalDate.of(2021, 6, 1);
    private static final Skjæringstidspunkt STP = Skjæringstidspunkt.builder().medFørsteUttaksdato(STP_DATO)
            .medSkjæringstidspunktOpptjening(STP_DATO).build();
    private List<UtbetalingsgradPrAktivitetDto> utbGrader = new ArrayList<>();
    private List<BeregningsgrunnlagPeriodeDto> bgPerioder = new ArrayList<>();

    @Test
    public void skal_teste_utbetaling_i_periode_uten_andre_inntekter() {
        String orgnr = "999999999";
        UUID ref = UUID.randomUUID();
        lagBGPeriode(STP_DATO, etterSTP(10),
                lagBGAndel(AktivitetStatus.ARBEIDSTAKER, orgnr, ref, 500000));
        lagUtbGrunnlasg(lagYGArbeid(orgnr, ref, UttakArbeidType.ORDINÆRT_ARBEID), lagYGPeriode(STP_DATO, etterSTP(10), 100));

        // Test for omsorgspenger
        BeregningsgrunnlagGrunnlagDto res = mapForOmsorgspenger();
        assertThat(res.getBeregningsgrunnlag()).isNotNull();
        assertThat(res.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder()).hasSize(1);
        assertAndel(res.getBeregningsgrunnlag(), STP_DATO, AktivitetStatus.ARBEIDSTAKER, orgnr, ref, 500000);

        // Test for pleiepenger
        res = mapForPleiepenger();
        assertThat(res.getBeregningsgrunnlag()).isNotNull();
        assertThat(res.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder()).hasSize(1);
        assertAndel(res.getBeregningsgrunnlag(), STP_DATO, AktivitetStatus.ARBEIDSTAKER, orgnr, ref, 500000);
    }

    @Test
    public void skal_teste_utbetaling_i_periode_med_andre_inntekter() {
        String orgnr = "999999999";
        UUID ref = UUID.randomUUID();
        lagBGPeriode(STP_DATO, etterSTP(10),
                lagBGAndel(AktivitetStatus.ARBEIDSTAKER, orgnr, ref, 500000),
                lagBGAndel(AktivitetStatus.FRILANSER, null, null, 500000));
        lagUtbGrunnlasg(lagYGArbeid(orgnr, ref, UttakArbeidType.ORDINÆRT_ARBEID), lagYGPeriode(STP_DATO, etterSTP(10), 100));

        // Omsorgspenger
        BeregningsgrunnlagGrunnlagDto res = mapForOmsorgspenger();
        assertThat(res.getBeregningsgrunnlag()).isNotNull();
        assertThat(res.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder()).hasSize(1);
        assertAndel(res.getBeregningsgrunnlag(), STP_DATO, AktivitetStatus.ARBEIDSTAKER, orgnr, ref, 100000);

        // Pleiepenger
        res = mapForPleiepenger();
        assertThat(res.getBeregningsgrunnlag()).isNotNull();
        assertThat(res.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder()).hasSize(1);
        assertAndel(res.getBeregningsgrunnlag(), STP_DATO, AktivitetStatus.ARBEIDSTAKER, orgnr, ref, 100000);
    }

    @Test
    public void skal_teste_utbetaling_i_over_flere_perioder() {
        String orgnr = "999999999";
        UUID ref = UUID.randomUUID();
        lagBGPeriode(STP_DATO, etterSTP(10),
                lagBGAndel(AktivitetStatus.ARBEIDSTAKER, orgnr, ref, 300000),
                lagBGAndel(AktivitetStatus.FRILANSER, null, null, 200000),
                lagBGAndel(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, null, null, 200000));
        lagBGPeriode(etterSTP(11), etterSTP(50),
                lagBGAndel(AktivitetStatus.ARBEIDSTAKER, orgnr, ref, 300000),
                lagBGAndel(AktivitetStatus.FRILANSER, null, null, 200000),
                lagBGAndel(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, null, null, 200000));
        lagUtbGrunnlasg(lagYGArbeid(orgnr, ref, UttakArbeidType.ORDINÆRT_ARBEID),
                lagYGPeriode(STP_DATO, etterSTP(10), 100));
        lagUtbGrunnlasg(lagYGArbeid(null, null, UttakArbeidType.SELVSTENDIG_NÆRINGSDRIVENDE),
                lagYGPeriode(etterSTP(11), etterSTP(50), 100));

        // Omsorgspenger
        BeregningsgrunnlagGrunnlagDto res = mapForOmsorgspenger();

        assertThat(res.getBeregningsgrunnlag()).isNotNull();
        assertThat(res.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder()).hasSize(2);

        // Assert første periode - omsorgspenger
        assertAndel(res.getBeregningsgrunnlag(), STP_DATO, AktivitetStatus.ARBEIDSTAKER, orgnr, ref, 200000);
        assertAndel(res.getBeregningsgrunnlag(), STP_DATO, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, null, null, 0);

        // Assert andre periode - omsorgspenger
        assertAndel(res.getBeregningsgrunnlag(), etterSTP(11), AktivitetStatus.ARBEIDSTAKER, orgnr, ref, 0);
        assertAndel(res.getBeregningsgrunnlag(), etterSTP(11), AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, null, null, 100000);

        // Pleiepenger
        res = mapForPleiepenger();

        assertThat(res.getBeregningsgrunnlag()).isNotNull();
        assertThat(res.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder()).hasSize(2);

        // Assert første periode - pleiepenger
        assertAndel(res.getBeregningsgrunnlag(), STP_DATO, AktivitetStatus.ARBEIDSTAKER, orgnr, ref, 200000);
        assertAndel(res.getBeregningsgrunnlag(), STP_DATO, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, null, null, 0);

        // Assert andre periode - pleiepenger
        assertAndel(res.getBeregningsgrunnlag(), etterSTP(11), AktivitetStatus.ARBEIDSTAKER, orgnr, ref, 0);
        assertAndel(res.getBeregningsgrunnlag(), etterSTP(11), AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, null, null, 100000);
    }

    @Test
    public void skal_teste_utbetaling_i_flere_andeler_samme_periode() {
        String orgnr = "999999999";
        UUID ref = UUID.randomUUID();
        lagBGPeriode(STP_DATO, etterSTP(10),
                lagBGAndel(AktivitetStatus.ARBEIDSTAKER, orgnr, ref, 200000),
                lagBGAndel(AktivitetStatus.FRILANSER, null, null, 300000),
                lagBGAndel(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, null, null, 500000));
        lagUtbGrunnlasg(lagYGArbeid(orgnr, ref, UttakArbeidType.ORDINÆRT_ARBEID),
                lagYGPeriode(STP_DATO, etterSTP(10), 100));
        lagUtbGrunnlasg(lagYGArbeid(null, null, UttakArbeidType.SELVSTENDIG_NÆRINGSDRIVENDE),
                lagYGPeriode(STP_DATO, etterSTP(10), 100));

        // Omsorgspenger
        BeregningsgrunnlagGrunnlagDto res = mapForOmsorgspenger();

        assertThat(res.getBeregningsgrunnlag()).isNotNull();
        assertThat(res.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder()).hasSize(1);

        // Assert første periode - omsorgspenger
        assertAndel(res.getBeregningsgrunnlag(), STP_DATO, AktivitetStatus.ARBEIDSTAKER, orgnr, ref, 200000);
        assertAndel(res.getBeregningsgrunnlag(), STP_DATO, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, null, null, 100000);

        // Pleiepenger
        res = mapForPleiepenger();

        assertThat(res.getBeregningsgrunnlag()).isNotNull();
        assertThat(res.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder()).hasSize(1);

        // Assert første periode - pleiepenger
        assertAndel(res.getBeregningsgrunnlag(), STP_DATO, AktivitetStatus.ARBEIDSTAKER, orgnr, ref, 200000);
        assertAndel(res.getBeregningsgrunnlag(), STP_DATO, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, null, null, 100000);
    }

    @Test
    public void skal_teste_utbetaling_i_flere_andeler_samme_periode_en_andel_avkortes_til_0() {
        String orgnr = "999999999";
        UUID ref = UUID.randomUUID();
        lagBGPeriode(STP_DATO, etterSTP(10),
                lagBGAndel(AktivitetStatus.ARBEIDSTAKER, orgnr, ref, 400000),
                lagBGAndel(AktivitetStatus.FRILANSER, null, null, 300000),
                lagBGAndel(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, null, null, 500000));
        lagUtbGrunnlasg(lagYGArbeid(orgnr, ref, UttakArbeidType.ORDINÆRT_ARBEID),
                lagYGPeriode(STP_DATO, etterSTP(10), 100));
        lagUtbGrunnlasg(lagYGArbeid(orgnr, ref, UttakArbeidType.FRILANS),
                lagYGPeriode(STP_DATO, etterSTP(10), 100));
        lagUtbGrunnlasg(lagYGArbeid(null, null, UttakArbeidType.SELVSTENDIG_NÆRINGSDRIVENDE),
                lagYGPeriode(STP_DATO, etterSTP(10), 100));

        // Omsorgspenger
        BeregningsgrunnlagGrunnlagDto res = mapForOmsorgspenger();

        assertThat(res.getBeregningsgrunnlag()).isNotNull();
        assertThat(res.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder()).hasSize(1);

        // Assert første periode - omsorgspenger
        assertAndel(res.getBeregningsgrunnlag(), STP_DATO, AktivitetStatus.ARBEIDSTAKER, orgnr, ref, 400000);
        assertAndel(res.getBeregningsgrunnlag(), STP_DATO, AktivitetStatus.FRILANSER, null, null, 200000);
        assertAndel(res.getBeregningsgrunnlag(), STP_DATO, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, null, null, 0);

        // Pleiepenger
        res = mapForPleiepenger();

        assertThat(res.getBeregningsgrunnlag()).isNotNull();
        assertThat(res.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder()).hasSize(1);

        // Assert første periode - pleiepenger
        assertAndel(res.getBeregningsgrunnlag(), STP_DATO, AktivitetStatus.ARBEIDSTAKER, orgnr, ref, 400000);
        assertAndel(res.getBeregningsgrunnlag(), STP_DATO, AktivitetStatus.FRILANSER, null, null, 200000);
        assertAndel(res.getBeregningsgrunnlag(), STP_DATO, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, null, null, 0);
    }

    @Test
    public void flere_at_andeler_søkes_for_i_ulike_perioder() {
        String orgnr1 = "999999999";
        UUID ref1 = UUID.randomUUID();
        String orgnr2 = "999999998";
        UUID ref2 = UUID.randomUUID();
        lagBGPeriode(STP_DATO, etterSTP(10),
                lagBGAndel(AktivitetStatus.ARBEIDSTAKER, orgnr1, ref1, 400000),
                lagBGAndel(AktivitetStatus.ARBEIDSTAKER, orgnr2, ref2, 300000),
                lagBGAndel(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, null, null, 200000));
        lagBGPeriode(etterSTP(11), etterSTP(40),
                lagBGAndel(AktivitetStatus.ARBEIDSTAKER, orgnr1, ref1, 400000),
                lagBGAndel(AktivitetStatus.ARBEIDSTAKER, orgnr2, ref2, 300000),
                lagBGAndel(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, null, null, 200000));
        lagUtbGrunnlasg(lagYGArbeid(orgnr1, ref1, UttakArbeidType.ORDINÆRT_ARBEID),
                lagYGPeriode(STP_DATO, etterSTP(10), 100));
        lagUtbGrunnlasg(lagYGArbeid(orgnr2, ref2, UttakArbeidType.ORDINÆRT_ARBEID),
                lagYGPeriode(etterSTP(11), etterSTP(40), 100));

        // Omsorgspenger
        BeregningsgrunnlagGrunnlagDto res = mapForOmsorgspenger();

        assertThat(res.getBeregningsgrunnlag()).isNotNull();
        assertThat(res.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder()).hasSize(2);

        // Assert første periode - omsorgspenger
        assertAndel(res.getBeregningsgrunnlag(), STP_DATO, AktivitetStatus.ARBEIDSTAKER, orgnr1, ref1, 100000);
        assertAndel(res.getBeregningsgrunnlag(), STP_DATO, AktivitetStatus.ARBEIDSTAKER, orgnr2, ref2, 0);
        assertAndel(res.getBeregningsgrunnlag(), STP_DATO, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, null, null, 0);

        // Assert andre periode - omsorgspenger
        assertAndel(res.getBeregningsgrunnlag(), etterSTP(11), AktivitetStatus.ARBEIDSTAKER, orgnr1, ref1, 0);
        assertAndel(res.getBeregningsgrunnlag(), etterSTP(11), AktivitetStatus.ARBEIDSTAKER, orgnr2, ref2, 0);
        assertAndel(res.getBeregningsgrunnlag(), etterSTP(11), AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, null, null, 0);

        // Pleiepenger
        res = mapForPleiepenger();

        assertThat(res.getBeregningsgrunnlag()).isNotNull();
        assertThat(res.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder()).hasSize(2);

        // Assert første periode - pleiepenger
        assertAndel(res.getBeregningsgrunnlag(), STP_DATO, AktivitetStatus.ARBEIDSTAKER, orgnr1, ref1, 100000);
        assertAndel(res.getBeregningsgrunnlag(), STP_DATO, AktivitetStatus.ARBEIDSTAKER, orgnr2, ref2, 0);
        assertAndel(res.getBeregningsgrunnlag(), STP_DATO, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, null, null, 0);

        // Assert andre periode - pleiepenger
        assertAndel(res.getBeregningsgrunnlag(), etterSTP(11), AktivitetStatus.ARBEIDSTAKER, orgnr1, ref1, 0);
        assertAndel(res.getBeregningsgrunnlag(), etterSTP(11), AktivitetStatus.ARBEIDSTAKER, orgnr2, ref2, 0);
        assertAndel(res.getBeregningsgrunnlag(), etterSTP(11), AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, null, null, 0);
    }

    private void assertAndel(BeregningsgrunnlagDto bg, LocalDate periodeFom, AktivitetStatus status, String orgnr, UUID ref, int inntektstak) {
        List<BeregningsgrunnlagPrStatusOgAndelDto> andeler = bg.getBeregningsgrunnlagPerioder().stream()
                .filter(bgp -> bgp.getBeregningsgrunnlagPeriodeFom().equals(periodeFom))
                .findFirst()
                .map(BeregningsgrunnlagPeriodeDto::getBeregningsgrunnlagPrStatusOgAndelList)
                .orElse(Collections.emptyList());

        assertThat(andeler).isNotEmpty();
        BeregningsgrunnlagPrStatusOgAndelDto andel = andeler.stream()
                .filter(a -> a.getAktivitetStatus().equals(status))
                .filter(a -> matcherAG(a.getBgAndelArbeidsforhold(), orgnr, ref))
                .findFirst()
                .orElse(null);
        assertThat(andel).isNotNull();
        assertThat(andel.getAvkortetMotInntektstak()).isNotNull();
        assertThat(andel.getAvkortetMotInntektstak().compareTo(Beløp.fra(inntektstak))).isEqualTo(0);
    }

    private BeregningsgrunnlagGrunnlagDto mapForOmsorgspenger() {
        OmsorgspengerGrunnlag ompGr = new OmsorgspengerGrunnlag(utbGrader, List.of());
        return map(ompGr);
    }

    private BeregningsgrunnlagGrunnlagDto mapForPleiepenger() {
        PleiepengerSyktBarnGrunnlag pleieGr = new PleiepengerSyktBarnGrunnlag(utbGrader);
        return map(pleieGr);
    }

    private BeregningsgrunnlagGrunnlagDto map(UtbetalingsgradGrunnlag grunnlag) {
        BeregningsgrunnlagGUIInput input = new BeregningsgrunnlagGUIInput(KoblingReferanse.fra(FagsakYtelseType.OMSORGSPENGER, AktørId.dummy(), 1L, UUID.randomUUID(),
                Optional.empty(), STP), null, null, (YtelsespesifiktGrunnlag) grunnlag);

        BeregningsgrunnlagDto bg = new BeregningsgrunnlagDto(null, null, bgPerioder,
                null, null, false, GRUNNBELØP);

        BeregningsgrunnlagGrunnlagDto gr = new BeregningsgrunnlagGrunnlagDto(bg, null, null,
                null, null, null, BeregningsgrunnlagTilstand.FASTSATT);

        return MapFormidlingsdataBeregningsgrunnlag.mapMedBrevfelt(gr, input);
    }

    private boolean matcherAG(BGAndelArbeidsforhold bga, String orgnr, UUID ref) {
        String andelOrgnr = bga == null ? null : bga.getArbeidsgiver().getArbeidsgiverOrgnr();
        UUID andelRef = bga == null ? null : bga.getArbeidsforholdRef();
        return Objects.equals(orgnr, andelOrgnr) && Objects.equals(ref, andelRef);
    }

    private LocalDate etterSTP(int i) {
        return STP_DATO.plusDays(i);
    }

    private AktivitetDto lagYGArbeid(String orgnr, UUID ref, UttakArbeidType uttakArbeidType) {
        if (orgnr == null) {
            return new AktivitetDto(null, InternArbeidsforholdRefDto.nullRef(), uttakArbeidType);
        }
        return new AktivitetDto(no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver.virksomhet(orgnr),
                InternArbeidsforholdRefDto.ref(ref), uttakArbeidType);
    }

    private PeriodeMedUtbetalingsgradDto lagYGPeriode(LocalDate fom, LocalDate tom, int utbGrad) {
        return new PeriodeMedUtbetalingsgradDto(Intervall.fraOgMedTilOgMed(fom, tom), Utbetalingsgrad.valueOf(utbGrad));
    }

    private void lagUtbGrunnlasg(AktivitetDto arbfor, PeriodeMedUtbetalingsgradDto... utbPerioder) {
        utbGrader.add(new UtbetalingsgradPrAktivitetDto(arbfor, Arrays.asList(utbPerioder)));
    }

    private void lagBGPeriode(LocalDate fom, LocalDate tom, BeregningsgrunnlagPrStatusOgAndelDto... andeler) {
        bgPerioder.add(new BeregningsgrunnlagPeriodeDto(Arrays.asList(andeler), new Periode(fom, tom),
                null, null, null, null, null, null, null, null));
    }

    private BeregningsgrunnlagPrStatusOgAndelDto lagBGAndel(AktivitetStatus status, String orgnr, UUID ref, int brutto) {
        BGAndelArbeidsforhold arb = null;
        if (orgnr != null) {
            arb = new BGAndelArbeidsforhold(new Arbeidsgiver(orgnr, null), ref);
        }
        return new BeregningsgrunnlagPrStatusOgAndelDto.Builder()
                .medAktivitetStatus(status)
                .medBruttoPrÅr(Beløp.fra(brutto))
                .medBgAndelArbeidsforhold(arb)
                .build();
    }

}
