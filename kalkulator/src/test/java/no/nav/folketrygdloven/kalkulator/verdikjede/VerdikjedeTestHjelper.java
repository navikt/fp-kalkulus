package no.nav.folketrygdloven.kalkulator.verdikjede;

import static no.nav.folketrygdloven.kalkulator.GrunnbeløpMock.GRUNNBELØPSATSER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.FastsettBeregningAktiviteter;
import no.nav.folketrygdloven.kalkulator.FastsettSkjæringstidspunktOgStatuser;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.BehandlingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.SammenligningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektspostDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.NaturalYtelseDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittAnnenAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittOpptjeningDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.OpptjeningsnøkkelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.VersjonTypeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulator.modell.typer.AktørId;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.ArbeidType;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.Hjemmel;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.InntektsKilde;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.InntektspostType;


public class VerdikjedeTestHjelper {

    static final LocalDate SKJÆRINGSTIDSPUNKT_OPPTJENING = LocalDate.of(2018, Month.APRIL, 10);

    public VerdikjedeTestHjelper() {
    }

    void verifiserPeriode(BeregningsgrunnlagPeriodeDto periode, LocalDate fom, LocalDate tom, int antallAndeler) {
        verifiserPeriode(periode, fom, tom, antallAndeler, null);
    }

    void verifiserPeriode(BeregningsgrunnlagPeriodeDto periode, LocalDate fom, LocalDate tom, int antallAndeler, Long dagsats) {
        assertThat(periode.getBeregningsgrunnlagPeriodeFom()).isEqualTo(fom);
        assertThat(periode.getBeregningsgrunnlagPeriodeTom()).isEqualTo(tom);
        assertThat(periode.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(antallAndeler);
        assertThat(periode.getDagsats()).isEqualTo(dagsats);
    }

    void verifiserBeregningsgrunnlagBasis(BeregningsgrunnlagDto beregningsgrunnlag, Hjemmel hjemmel) {
        assertThat(beregningsgrunnlag).isNotNull();
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder()).hasSize(1);
        assertThat(beregningsgrunnlag.getHjemmel()).isEqualTo(hjemmel);
    }

    void verifiserBeregningsgrunnlagBasis(BeregningsgrunnlagRegelResultat beregningsgrunnlagRegelResultat, Hjemmel hjemmel) {
        assertThat(beregningsgrunnlagRegelResultat.getAksjonspunkter()).isEmpty();
        verifiserBeregningsgrunnlagBasis(beregningsgrunnlagRegelResultat.getBeregningsgrunnlag(), hjemmel);

    }

    void verifiserSammenligningsgrunnlag(SammenligningsgrunnlagDto sammenligningsgrunnlag, double rapportertPrÅr, LocalDate fom, LocalDate tom,
                                         BigDecimal avvikPromille) {
        assertThat(sammenligningsgrunnlag.getRapportertPrÅr().doubleValue()).isEqualTo(rapportertPrÅr, within(0.01));
        assertThat(sammenligningsgrunnlag.getSammenligningsperiodeFom()).isEqualTo(fom);
        assertThat(sammenligningsgrunnlag.getSammenligningsperiodeTom()).isEqualTo(tom);
        assertThat(sammenligningsgrunnlag.getAvvikPromilleNy().compareTo(avvikPromille)).isEqualTo(0);
    }

    void verifiserBGATførAvkorting(BeregningsgrunnlagPeriodeDto periode, List<Double> bgListe, List<String> virksomheterOrgnr) {
        List<BeregningsgrunnlagPrStatusOgAndelDto> bgpsaListe = statusliste(periode, AktivitetStatus.ARBEIDSTAKER);
        for (int ix = 0; ix < bgpsaListe.size(); ix++) {
            BeregningsgrunnlagPrStatusOgAndelDto bgpsa = bgpsaListe.get(ix);
            assertThat(bgpsa.getAktivitetStatus()).isEqualTo(AktivitetStatus.ARBEIDSTAKER);
            final int index = ix;
            assertThat(bgpsa.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getArbeidsgiver))
                .hasValueSatisfying(arbeidsgiver -> assertThat(arbeidsgiver.getOrgnr()).isEqualTo(virksomheterOrgnr.get(index)));
            assertThat(bgpsa.getBgAndelArbeidsforhold()
                .map(BGAndelArbeidsforholdDto::getArbeidsforholdRef)
                .map(InternArbeidsforholdRefDto::gjelderForSpesifiktArbeidsforhold).orElse(false))
                    .as("gjelderSpesifiktArbeidsforhold").isFalse();
            assertThat(bgpsa.getArbeidsforholdType()).isEqualTo(OpptjeningAktivitetType.ARBEID);
            assertThat(bgpsa.getBeregnetPrÅr().doubleValue()).isEqualTo(bgListe.get(ix), within(0.01));
            assertThat(bgpsa.getBruttoPrÅr().doubleValue()).isEqualTo(bgListe.get(ix), within(0.01));

            assertThat(bgpsa.getOverstyrtPrÅr()).isNull();
            assertThat(bgpsa.getAvkortetPrÅr()).isNull();
            assertThat(bgpsa.getRedusertPrÅr()).isNull();

            assertThat(bgpsa.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforholdDto::getNaturalytelseBortfaltPrÅr)).isEmpty();

            assertThat(bgpsa.getAvkortetBrukersAndelPrÅr()).isNull();
            assertThat(bgpsa.getRedusertBrukersAndelPrÅr()).isNull();

            assertThat(bgpsa.getMaksimalRefusjonPrÅr()).isNull();
            assertThat(bgpsa.getAvkortetRefusjonPrÅr()).isNull();
            assertThat(bgpsa.getRedusertRefusjonPrÅr()).isNull();
        }
    }

    void verifiserBGATetterAvkorting(BeregningsgrunnlagPeriodeDto periode,
                                     List<Double> beregnetListe,
                                     List<Double> bruttoBgListe, List<String> virksomheteneOrgnr,
                                     List<Double> avkortetListe,
                                     List<Double> maksimalRefusjonListe,
                                     List<Double> avkortetRefusjonListe,
                                     List<Double> avkortetBrukersAndelListe, boolean overstyrt) {
        List<BeregningsgrunnlagPrStatusOgAndelDto> bgpsaListe = statusliste(periode, AktivitetStatus.ARBEIDSTAKER);
        assertThat(beregnetListe).hasSameSizeAs(bgpsaListe);
        assertThat(avkortetListe).hasSameSizeAs(bgpsaListe);
        assertThat(maksimalRefusjonListe).hasSameSizeAs(bgpsaListe);
        assertThat(avkortetRefusjonListe).hasSameSizeAs(bgpsaListe);
        assertThat(avkortetBrukersAndelListe).hasSameSizeAs(bgpsaListe);
        for (int ix = 0; ix < bgpsaListe.size(); ix++) {
            BeregningsgrunnlagPrStatusOgAndelDto bgpsa = bgpsaListe.get(ix);
            assertThat(bgpsa.getAktivitetStatus()).isEqualTo(AktivitetStatus.ARBEIDSTAKER);
            final int index = ix;
            assertThat(bgpsa.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getArbeidsgiver))
                .hasValueSatisfying(arbeidsgiver -> assertThat(arbeidsgiver.getOrgnr()).isEqualTo(virksomheteneOrgnr.get(index)));
            assertThat(bgpsa.getBgAndelArbeidsforhold()
                .map(BGAndelArbeidsforholdDto::getArbeidsforholdRef)
                .map(InternArbeidsforholdRefDto::gjelderForSpesifiktArbeidsforhold).orElse(false))
                    .as("gjelderSpesifiktArbeidsforhold").isFalse();
            assertThat(bgpsa.getArbeidsforholdType()).isEqualTo(OpptjeningAktivitetType.ARBEID);
            assertThat(bgpsa.getBeregnetPrÅr().doubleValue()).isEqualTo(beregnetListe.get(ix), within(0.01));
            assertThat(bgpsa.getBruttoPrÅr().doubleValue()).isEqualTo(bruttoBgListe.get(ix), within(0.01));

            if (!overstyrt) {
                assertThat(bgpsa.getOverstyrtPrÅr()).isNull();
            }
            assertThat(bgpsa.getAvkortetPrÅr().doubleValue()).isCloseTo(avkortetListe.get(ix), within(0.01));
            assertThat(bgpsa.getRedusertPrÅr().doubleValue()).isCloseTo(avkortetListe.get(ix), within(0.01));

            assertThat(bgpsa.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforholdDto::getNaturalytelseBortfaltPrÅr)).isEmpty();

            assertThat(bgpsa.getMaksimalRefusjonPrÅr().doubleValue()).as("MaksimalRefusjonPrÅr")
                .isCloseTo(maksimalRefusjonListe.get(ix), within(0.01));
            assertThat(bgpsa.getAvkortetRefusjonPrÅr().doubleValue()).as("AvkortetRefusjonPrÅr")
                .isCloseTo(avkortetRefusjonListe.get(ix), within(0.01));
            assertThat(bgpsa.getRedusertRefusjonPrÅr().doubleValue()).as("RedusertRefusjonPrÅr")
                .isCloseTo(avkortetRefusjonListe.get(ix), within(0.01));

            assertThat(bgpsa.getAvkortetBrukersAndelPrÅr().doubleValue()).as("AvkortetBrukersAndelPrÅr")
                .isCloseTo(avkortetBrukersAndelListe.get(ix), within(0.01));
            assertThat(bgpsa.getRedusertBrukersAndelPrÅr().doubleValue()).as("RedusertBrukersAndelPrÅr")
                .isCloseTo(avkortetBrukersAndelListe.get(ix), within(0.01));
        }
    }

    void verifiserFLførAvkorting(BeregningsgrunnlagPeriodeDto periode, Double bgFL) {
        List<BeregningsgrunnlagPrStatusOgAndelDto> bgpsaListe = statusliste(periode, AktivitetStatus.FRILANSER);
        assertThat(bgpsaListe).hasSize(1);
        for (BeregningsgrunnlagPrStatusOgAndelDto bgpsa : bgpsaListe) {
            assertThat(bgpsa.getAktivitetStatus()).isEqualTo(AktivitetStatus.FRILANSER);
            assertThat(bgpsa.getBeregnetPrÅr().doubleValue()).isEqualTo(bgFL);
            assertThat(bgpsa.getBruttoPrÅr().doubleValue()).isEqualTo(bgFL);

            assertThat(bgpsa.getBeregningsperiodeFom()).isEqualTo(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(3).withDayOfMonth(1));
            assertThat(bgpsa.getBeregningsperiodeTom()).isEqualTo(SKJÆRINGSTIDSPUNKT_OPPTJENING.withDayOfMonth(1).minusDays(1));

            assertThat(bgpsa.getOverstyrtPrÅr()).isNull();
            assertThat(bgpsa.getAvkortetPrÅr()).isNull();
            assertThat(bgpsa.getRedusertPrÅr()).isNull();

            assertThat(bgpsa.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforholdDto::getNaturalytelseBortfaltPrÅr)).isEmpty();

            assertThat(bgpsa.getAvkortetBrukersAndelPrÅr()).isNull();
            assertThat(bgpsa.getRedusertBrukersAndelPrÅr()).isNull();

            assertThat(bgpsa.getMaksimalRefusjonPrÅr()).isNull();
            assertThat(bgpsa.getAvkortetRefusjonPrÅr()).isNull();
            assertThat(bgpsa.getRedusertRefusjonPrÅr()).isNull();
        }
    }

    void verifiserFLetterAvkorting(BeregningsgrunnlagPeriodeDto periode, Double beregnetFL, Double bgFL, Double avkortetBgFL, Double brukersAndelFL) {
        List<BeregningsgrunnlagPrStatusOgAndelDto> bgpsaListe = statusliste(periode, AktivitetStatus.FRILANSER);
        assertThat(bgpsaListe).hasSize(1);
        for (BeregningsgrunnlagPrStatusOgAndelDto bgpsa : bgpsaListe) {
            assertThat(bgpsa.getAktivitetStatus()).isEqualTo(AktivitetStatus.FRILANSER);
            assertThat(bgpsa.getBeregnetPrÅr().doubleValue()).isEqualTo(beregnetFL);
            assertThat(bgpsa.getBruttoPrÅr().doubleValue()).isEqualTo(bgFL);

            assertThat(bgpsa.getBeregningsperiodeFom()).isEqualTo(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(3).withDayOfMonth(1));
            assertThat(bgpsa.getBeregningsperiodeTom()).isEqualTo(SKJÆRINGSTIDSPUNKT_OPPTJENING.withDayOfMonth(1).minusDays(1));

            assertThat(bgpsa.getOverstyrtPrÅr()).isNull();
            assertThat(bgpsa.getAvkortetPrÅr().doubleValue()).isEqualTo(avkortetBgFL, within(0.01));
            assertThat(bgpsa.getRedusertPrÅr().doubleValue()).isEqualTo(avkortetBgFL, within(0.01));

            assertThat(bgpsa.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforholdDto::getNaturalytelseBortfaltPrÅr)).isEmpty();

            assertThat(bgpsa.getAvkortetBrukersAndelPrÅr().doubleValue()).isCloseTo(brukersAndelFL, within(0.01));
            assertThat(bgpsa.getRedusertBrukersAndelPrÅr().doubleValue()).isCloseTo(brukersAndelFL, within(0.01));

            assertThat(bgpsa.getMaksimalRefusjonPrÅr().doubleValue()).isEqualTo(0.0d);
            assertThat(bgpsa.getAvkortetRefusjonPrÅr().doubleValue()).isEqualTo(0.0d);
            assertThat(bgpsa.getRedusertRefusjonPrÅr().doubleValue()).isEqualTo(0.0d);
        }
    }

    void verifiserBGSNførAvkorting(BeregningsgrunnlagPeriodeDto periode, double forventetBrutto, double forventetBeregnet, int sisteÅr) {
        List<BeregningsgrunnlagPrStatusOgAndelDto> andeler = statusliste(periode, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);
        assertThat(andeler).hasSize(1);
        BeregningsgrunnlagPrStatusOgAndelDto andel = andeler.get(0);
        assertThat(andel.getAktivitetStatus()).isEqualTo(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);
        assertThat(andel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getArbeidsgiver)).isEmpty();
        assertThat(andel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getArbeidsforholdRef)).isEmpty();
        assertThat(andel.getArbeidsforholdType()).isEqualTo(OpptjeningAktivitetType.NÆRING);
        assertThat(andel.getBeregnetPrÅr().doubleValue()).isEqualTo(forventetBeregnet, within(0.2));
        assertThat(andel.getBruttoPrÅr().doubleValue()).isEqualTo(forventetBrutto, within(0.2));

        assertThat(andel.getBeregningsperiodeFom()).isEqualTo(LocalDate.of(sisteÅr - 2, Month.JANUARY, 1));
        assertThat(andel.getBeregningsperiodeTom()).isEqualTo(LocalDate.of(sisteÅr, Month.DECEMBER, 31));

        assertThat(andel.getOverstyrtPrÅr()).isNull();
        assertThat(andel.getAvkortetPrÅr()).isNull();
        assertThat(andel.getRedusertPrÅr()).isNull();

        assertThat(andel.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforholdDto::getNaturalytelseBortfaltPrÅr)).isEmpty();

        assertThat(andel.getAvkortetBrukersAndelPrÅr()).isNull();
        assertThat(andel.getRedusertBrukersAndelPrÅr()).isNull();

        assertThat(andel.getMaksimalRefusjonPrÅr()).isNull();
        assertThat(andel.getAvkortetRefusjonPrÅr()).isNull();
        assertThat(andel.getRedusertRefusjonPrÅr()).isNull();

        assertThat(andel.getPgiSnitt()).isNotNull();
        assertThat(andel.getPgi1()).isNotNull();
        assertThat(andel.getPgi2()).isNotNull();
        assertThat(andel.getPgi3()).isNotNull();
    }

    void verifiserBGSNetterAvkorting(BeregningsgrunnlagPeriodeDto periode, double forventetBeregnet, double forventetBrutto,
                                     double forventetAvkortet, double forventetRedusert, int sisteÅr) {
        List<BeregningsgrunnlagPrStatusOgAndelDto> andeler = statusliste(periode, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);
        assertThat(andeler).hasSize(1);
        BeregningsgrunnlagPrStatusOgAndelDto andel = andeler.get(0);
        assertThat(andel.getAktivitetStatus()).isEqualTo(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);
        assertThat(andel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getArbeidsgiver)).isEmpty();
        assertThat(andel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getArbeidsforholdRef)).isEmpty();
        assertThat(andel.getArbeidsforholdType()).isEqualTo(OpptjeningAktivitetType.NÆRING);
        assertThat(andel.getBeregnetPrÅr().doubleValue()).isEqualTo(forventetBeregnet, within(0.2));
        assertThat(andel.getBruttoPrÅr().doubleValue()).isEqualTo(forventetBrutto, within(0.2));

        assertThat(andel.getBeregningsperiodeFom()).isEqualTo(LocalDate.of(sisteÅr - 2, Month.JANUARY, 1));
        assertThat(andel.getBeregningsperiodeTom()).isEqualTo(LocalDate.of(sisteÅr, Month.DECEMBER, 31));

        assertThat(andel.getOverstyrtPrÅr()).isNull();
        assertThat(andel.getAvkortetPrÅr().doubleValue()).isEqualTo(forventetAvkortet, within(0.2));
        assertThat(andel.getRedusertPrÅr().doubleValue()).isEqualTo(forventetRedusert, within(0.2));

        assertThat(andel.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforholdDto::getNaturalytelseBortfaltPrÅr)).isEmpty();

        assertThat(andel.getMaksimalRefusjonPrÅr().doubleValue()).isEqualTo(0.0);
        assertThat(andel.getAvkortetRefusjonPrÅr().doubleValue()).isEqualTo(0.0);
        assertThat(andel.getRedusertRefusjonPrÅr().doubleValue()).isEqualTo(0.0);

        assertThat(andel.getAvkortetBrukersAndelPrÅr().doubleValue()).isEqualTo(forventetAvkortet, within(0.2));
        assertThat(andel.getRedusertBrukersAndelPrÅr().doubleValue()).isEqualTo(forventetRedusert, within(0.2));

        assertThat(andel.getPgiSnitt()).isNotNull();
        assertThat(andel.getPgi1()).isNotNull();
        assertThat(andel.getPgi2()).isNotNull();
        assertThat(andel.getPgi3()).isNotNull();
    }

    private List<BeregningsgrunnlagPrStatusOgAndelDto> statusliste(BeregningsgrunnlagPeriodeDto periode, AktivitetStatus status) {
        return periode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .filter(bpsa -> status.equals(bpsa.getAktivitetStatus()))
            .sorted(Comparator.comparing(bga -> bga.getBgAndelArbeidsforhold().get().getArbeidsforholdOrgnr()))
            .collect(Collectors.toList());
    }

    private InntektsmeldingDto lagInntektsmelding(BigDecimal beløp,
                                                  Arbeidsgiver arbeidsgiver,
                                                  BigDecimal refusjonskrav, NaturalYtelseDto naturalYtelse) {
        InntektsmeldingDtoBuilder inntektsmeldingBuilder = InntektsmeldingDtoBuilder.builder();
        inntektsmeldingBuilder.medStartDatoPermisjon(SKJÆRINGSTIDSPUNKT_OPPTJENING);
        inntektsmeldingBuilder.medBeløp(beløp);
        if (naturalYtelse != null) {
            inntektsmeldingBuilder.leggTil(naturalYtelse);
        }
        if (refusjonskrav != null) {
            inntektsmeldingBuilder.medRefusjon(refusjonskrav);
        }
        inntektsmeldingBuilder.medArbeidsgiver(arbeidsgiver);
        return inntektsmeldingBuilder.build();
    }

    public InntektArbeidYtelseAggregatBuilder initBehandlingFor_AT_SN(BigDecimal skattbarInntekt,
                                                                      int førsteÅr, LocalDate skjæringstidspunkt, String virksomhetOrgnr,
                                                                      BigDecimal inntektSammenligningsgrunnlag,
                                                                      BigDecimal inntektBeregningsgrunnlag, BehandlingReferanse behandlingReferanse,
                                                                      InntektArbeidYtelseGrunnlagDtoBuilder inntektArbeidYtelseBuilder) {
        InntektArbeidYtelseAggregatBuilder register = InntektArbeidYtelseAggregatBuilder.oppdatere(inntektArbeidYtelseBuilder.getKladd().getRegisterVersjon(), VersjonTypeDto.REGISTER);
        for (LocalDate året = LocalDate.of(førsteÅr, Month.JANUARY, 1); året.getYear() < førsteÅr + 3; året = året.plusYears(1)) {
            lagInntektForSN(register, behandlingReferanse.getAktørId(), året, skattbarInntekt);
        }
        LocalDate fraOgMed = skjæringstidspunkt.minusYears(1).withDayOfMonth(1);
        LocalDate tilOgMed = fraOgMed.plusYears(1);
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(virksomhetOrgnr);
        lagAktørArbeid(register, behandlingReferanse.getAktørId(), arbeidsgiver, fraOgMed, tilOgMed, ArbeidType.ORDINÆRT_ARBEIDSFORHOLD);

        for (LocalDate dt = fraOgMed; dt.isBefore(tilOgMed); dt = dt.plusMonths(1)) {
            lagInntektForSammenligning(register, behandlingReferanse.getAktørId(), dt, dt.plusMonths(1), inntektSammenligningsgrunnlag,
                arbeidsgiver);
            lagInntektForArbeidsforhold(register, behandlingReferanse.getAktørId(), dt, dt.plusMonths(1), inntektBeregningsgrunnlag,
                arbeidsgiver);
            lagInntektForOpptjening(register, behandlingReferanse.getAktørId(), dt, dt.plusMonths(1), inntektBeregningsgrunnlag,
                virksomhetOrgnr);
        }
        return register;
    }

    public void lagBehandlingForSN(BigDecimal skattbarInntekt,
                                   int førsteÅr, BehandlingReferanse behandlingReferanse, InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder) {
        for (LocalDate året = LocalDate.of(førsteÅr, Month.JANUARY, 1); året.getYear() < førsteÅr + 3; året = året.plusYears(1)) {
            lagInntektForSN(inntektArbeidYtelseAggregatBuilder, behandlingReferanse.getAktørId(), året, skattbarInntekt);
        }
    }

    private void lagInntektForSN(InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder, AktørId aktørId,
                                 LocalDate år, BigDecimal årsinntekt) {
        InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder aktørInntektBuilder = inntektArbeidYtelseAggregatBuilder.getAktørInntektBuilder(aktørId);
        InntektDtoBuilder inntektBuilder = aktørInntektBuilder.getInntektBuilder(InntektsKilde.SIGRUN, null);
        InntektspostDtoBuilder inntektspost = InntektspostDtoBuilder.ny()
            .medBeløp(årsinntekt)
            .medPeriode(år.withMonth(1).withDayOfMonth(1), år.withMonth(12).withDayOfMonth(31))
            .medInntektspostType(InntektspostType.SELVSTENDIG_NÆRINGSDRIVENDE);
        inntektBuilder.leggTilInntektspost(inntektspost);
        aktørInntektBuilder.leggTilInntekt(inntektBuilder);
        inntektArbeidYtelseAggregatBuilder.leggTilAktørInntekt(aktørInntektBuilder);
    }

    public InntektArbeidYtelseAggregatBuilder initBehandlingFL(BigDecimal inntektSammenligningsgrunnlag,
                                                               BigDecimal inntektFrilans,
                                                               String virksomhetOrgnr, LocalDate fraOgMed, LocalDate tilOgMed, AktørId aktørId, InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder) {

        Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(virksomhetOrgnr);
        lagAktørArbeid(inntektArbeidYtelseAggregatBuilder, aktørId, arbeidsgiver, fraOgMed, tilOgMed, ArbeidType.FRILANSER_OPPDRAGSTAKER_MED_MER);
        for (LocalDate dt = fraOgMed; dt.isBefore(tilOgMed); dt = dt.plusMonths(1)) {
            lagInntektForArbeidsforhold(inntektArbeidYtelseAggregatBuilder, aktørId, dt, dt.plusMonths(1), inntektFrilans,
                arbeidsgiver);
            lagInntektForSammenligning(inntektArbeidYtelseAggregatBuilder, aktørId, dt, dt.plusMonths(1), inntektSammenligningsgrunnlag,
                arbeidsgiver);
            lagInntektForOpptjening(inntektArbeidYtelseAggregatBuilder, aktørId, dt, dt.plusMonths(1), inntektSammenligningsgrunnlag,
                virksomhetOrgnr);
        }
        return inntektArbeidYtelseAggregatBuilder;
    }

    public YrkesaktivitetDtoBuilder lagAktørArbeid(InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder, AktørId aktørId,
                                                   Arbeidsgiver arbeidsgiver,
                                                   LocalDate fom, LocalDate tom, ArbeidType arbeidType) {
        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder = inntektArbeidYtelseAggregatBuilder
            .getAktørArbeidBuilder(aktørId);

        OpptjeningsnøkkelDto opptjeningsnøkkel = OpptjeningsnøkkelDto.forArbeidsgiver(arbeidsgiver);

        YrkesaktivitetDtoBuilder yrkesaktivitetBuilder = aktørArbeidBuilder
            .getYrkesaktivitetBuilderForNøkkelAvType(opptjeningsnøkkel, arbeidType);
        AktivitetsAvtaleDtoBuilder aktivitetsAvtaleBuilder = yrkesaktivitetBuilder.getAktivitetsAvtaleBuilder();

        AktivitetsAvtaleDtoBuilder aktivitetsAvtale = aktivitetsAvtaleBuilder.medPeriode(Intervall.fraOgMedTilOgMed(fom, tom));
        yrkesaktivitetBuilder.leggTilAktivitetsAvtale(aktivitetsAvtale)
            .medArbeidType(arbeidType)
            .medArbeidsgiver(arbeidsgiver);

        aktørArbeidBuilder.leggTilYrkesaktivitet(yrkesaktivitetBuilder);
        inntektArbeidYtelseAggregatBuilder.leggTilAktørArbeid(aktørArbeidBuilder);

        return yrkesaktivitetBuilder;
    }

    public void lagInntektForSammenligning(InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder,
                                           AktørId aktørId, LocalDate fom,
                                           LocalDate tom, BigDecimal månedsbeløp, Arbeidsgiver arbeidsgiver) {
        OpptjeningsnøkkelDto opptjeningsnøkkel = OpptjeningsnøkkelDto.forArbeidsgiver(arbeidsgiver);

        InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder aktørInntektBuilder = inntektArbeidYtelseAggregatBuilder.getAktørInntektBuilder(aktørId);

        InntektsKilde kilde = InntektsKilde.INNTEKT_SAMMENLIGNING;
        InntektDtoBuilder inntektBuilder = aktørInntektBuilder.getInntektBuilder(kilde, opptjeningsnøkkel);
        InntektspostDtoBuilder inntektspost = InntektspostDtoBuilder.ny()
            .medBeløp(månedsbeløp)
            .medPeriode(fom, tom)
            .medInntektspostType(InntektspostType.LØNN);
        inntektBuilder.leggTilInntektspost(inntektspost).medArbeidsgiver(arbeidsgiver);
        aktørInntektBuilder.leggTilInntekt(inntektBuilder);
        inntektArbeidYtelseAggregatBuilder.leggTilAktørInntekt(aktørInntektBuilder);
    }

    public void lagInntektForArbeidsforhold(InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder,
                                            AktørId aktørId, LocalDate fom,
                                            LocalDate tom, BigDecimal månedsbeløp, Arbeidsgiver arbeidsgiver) {
        OpptjeningsnøkkelDto opptjeningsnøkkel = OpptjeningsnøkkelDto.forArbeidsgiver(arbeidsgiver);

        InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder aktørInntektBuilder = inntektArbeidYtelseAggregatBuilder.getAktørInntektBuilder(aktørId);

        InntektsKilde kilde = InntektsKilde.INNTEKT_BEREGNING;
        InntektDtoBuilder inntektBuilder = aktørInntektBuilder.getInntektBuilder(kilde, opptjeningsnøkkel);
        InntektspostDtoBuilder inntektspost = InntektspostDtoBuilder.ny()
            .medBeløp(månedsbeløp)
            .medPeriode(fom, tom)
            .medInntektspostType(InntektspostType.LØNN);
        inntektBuilder.leggTilInntektspost(inntektspost).medArbeidsgiver(arbeidsgiver);
        aktørInntektBuilder.leggTilInntekt(inntektBuilder);
        inntektArbeidYtelseAggregatBuilder.leggTilAktørInntekt(aktørInntektBuilder);
    }

    public InntektArbeidYtelseGrunnlagDto opprettIAYforOrg(String orgNr, LocalDate stp) {
        Intervall alltidGyldigArbeidsforhold = Intervall.fraOgMedTilOgMed(stp.minusYears(3), stp.plusYears(3));

        InntektArbeidYtelseGrunnlagDtoBuilder builder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        InntektArbeidYtelseAggregatBuilder aggregatBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);

        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder = aggregatBuilder.getAktørArbeidBuilder(AktørId.dummy());
        YrkesaktivitetDtoBuilder yrkesaktivitetBuilder = aktørArbeidBuilder.getYrkesaktivitetBuilderForType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD);
        yrkesaktivitetBuilder.medArbeidsgiver(Arbeidsgiver.virksomhet(orgNr));
        yrkesaktivitetBuilder.medArbeidsforholdId(InternArbeidsforholdRefDto.nullRef());
        AktivitetsAvtaleDtoBuilder ansettelse = yrkesaktivitetBuilder.getAktivitetsAvtaleBuilder();
        ansettelse.medPeriode(alltidGyldigArbeidsforhold);
        ansettelse.medErAnsettelsesPeriode(true);
        AktivitetsAvtaleDtoBuilder stillingsprosent = yrkesaktivitetBuilder.getAktivitetsAvtaleBuilder();
        stillingsprosent.medPeriode(alltidGyldigArbeidsforhold);
        stillingsprosent.medErAnsettelsesPeriode(false);
        stillingsprosent.medProsentsats(BigDecimal.valueOf(100));
        yrkesaktivitetBuilder.leggTilAktivitetsAvtale(ansettelse);
        yrkesaktivitetBuilder.leggTilAktivitetsAvtale(stillingsprosent);

        aktørArbeidBuilder.leggTilYrkesaktivitet(yrkesaktivitetBuilder);
        aggregatBuilder.leggTilAktørArbeid(aktørArbeidBuilder);

        return builder.medData(aggregatBuilder).build();
    }

    void lagInntektForOpptjening(InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder,
                                 AktørId aktørId, LocalDate fom,
                                 LocalDate tom, BigDecimal månedsbeløp, String virksomhetOrgnr) {
        OpptjeningsnøkkelDto opptjeningsnøkkel = OpptjeningsnøkkelDto.forOrgnummer(virksomhetOrgnr);

        InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder aktørInntektBuilder = inntektArbeidYtelseAggregatBuilder.getAktørInntektBuilder(aktørId);

        InntektsKilde kilde = InntektsKilde.INNTEKT_OPPTJENING;
        InntektDtoBuilder inntektBuilder = aktørInntektBuilder.getInntektBuilder(kilde, opptjeningsnøkkel);
        InntektspostDtoBuilder inntektspost = InntektspostDtoBuilder.ny()
            .medBeløp(månedsbeløp)
            .medPeriode(fom, tom)
            .medInntektspostType(InntektspostType.LØNN);
        inntektBuilder.leggTilInntektspost(inntektspost)
            .medArbeidsgiver(aktørId == null ? Arbeidsgiver.virksomhet(virksomhetOrgnr) : Arbeidsgiver.person(aktørId));
        aktørInntektBuilder.leggTilInntekt(inntektBuilder);
        inntektArbeidYtelseAggregatBuilder.leggTilAktørInntekt(aktørInntektBuilder);
    }

    InntektsmeldingDto opprettInntektsmeldingMedRefusjonskrav(Arbeidsgiver arbeidsgiver, BigDecimal inntektInntektsmelding,
                                                              BigDecimal refusjonskrav) {
        return opprettInntektsmeldingMedRefusjonskrav(arbeidsgiver, inntektInntektsmelding, null, refusjonskrav);
    }

    public InntektsmeldingDto opprettInntektsmeldingMedRefusjonskrav(Arbeidsgiver arbeidsgiver, BigDecimal inntektInntektsmelding,
                                                                     NaturalYtelseDto naturalYtelse,
                                                                     BigDecimal refusjonskrav) {
        return lagInntektsmelding(inntektInntektsmelding,
            arbeidsgiver,
            refusjonskrav,
            naturalYtelse);
    }

    BeregningsgrunnlagGrunnlagDto kjørStegOgLagreGrunnlag(BeregningsgrunnlagInput input,
                                                          BeregningTjenesteWrapper beregningTjenesteWrapper) {
        var ref = input.getBehandlingReferanse();
        BeregningAktivitetAggregatDto beregningAktivitetAggregat = FastsettBeregningAktiviteter.fastsettAktiviteter(input);

        BeregningsgrunnlagDto beregningsgrunnlag = FastsettSkjæringstidspunktOgStatuser.fastsett(ref,
            beregningAktivitetAggregat, input.getIayGrunnlag(), GRUNNBELØPSATSER);
        BeregningsgrunnlagGrunnlagDto grunnlag = lagGrunnlag(beregningAktivitetAggregat, beregningsgrunnlag,
            BeregningsgrunnlagTilstand.FASTSATT_BEREGNINGSAKTIVITETER);
        var newInput = input.medBeregningsgrunnlagGrunnlag(grunnlag);
        beregningsgrunnlag = beregningTjenesteWrapper.getFastsettBeregningsgrunnlagPerioderTjeneste().fastsettPerioderForNaturalytelse(newInput, beregningsgrunnlag);
        return lagGrunnlag(beregningAktivitetAggregat, beregningsgrunnlag, BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);
    }

    private BeregningsgrunnlagGrunnlagDto lagGrunnlag(BeregningAktivitetAggregatDto beregningAktivitetAggregat,
                                                      BeregningsgrunnlagDto beregningsgrunnlag, BeregningsgrunnlagTilstand tilstand) {
        return BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
            .medRegisterAktiviteter(beregningAktivitetAggregat)
            .medBeregningsgrunnlag(beregningsgrunnlag)
            .build(tilstand);
    }

    InntektArbeidYtelseGrunnlagDtoBuilder lagBehandlingATogFLogSN(List<BigDecimal> inntektBeregningsgrunnlag,
                                                                  List<String> beregningVirksomhetOrgnr,
                                                                  BigDecimal inntektFrilans,
                                                                  List<BigDecimal> årsinntekterSN,
                                                                  int førsteÅr,
                                                                  BigDecimal årsinntektVarigEndring, BehandlingReferanse behandlingReferanse) {
        LocalDate fraOgMed = SKJÆRINGSTIDSPUNKT_OPPTJENING.minusYears(1);
        LocalDate tilOgMed = fraOgMed.plusYears(1);

        InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);

        beregningVirksomhetOrgnr
            .forEach(orgnr -> lagAktørArbeid(inntektArbeidYtelseBuilder, behandlingReferanse.getAktørId(),
                Arbeidsgiver.virksomhet(orgnr), fraOgMed, tilOgMed, ArbeidType.ORDINÆRT_ARBEIDSFORHOLD));

        String dummyVirksomhetOrgnr = "999";
        Arbeidsgiver dummyVirksomhet = Arbeidsgiver.virksomhet(dummyVirksomhetOrgnr);
        if (inntektFrilans != null) {
            lagAktørArbeid(inntektArbeidYtelseBuilder, behandlingReferanse.getAktørId(), dummyVirksomhet, fraOgMed,
                tilOgMed, ArbeidType.FRILANSER_OPPDRAGSTAKER_MED_MER);
        }

        for (LocalDate dt = fraOgMed; dt.isBefore(tilOgMed); dt = dt.plusMonths(1)) {
            for (int i = 0; i < beregningVirksomhetOrgnr.size(); i++) {
                String virksomhetOrgnr_i = beregningVirksomhetOrgnr.get(i);
                lagInntektForArbeidsforhold(inntektArbeidYtelseBuilder,
                    behandlingReferanse.getAktørId(),
                    dt, dt.plusMonths(1), inntektBeregningsgrunnlag.get(i),
                    Arbeidsgiver.virksomhet(virksomhetOrgnr_i));
                lagInntektForOpptjening(inntektArbeidYtelseBuilder,
                    behandlingReferanse.getAktørId(),
                    dt, dt.plusMonths(1), inntektBeregningsgrunnlag.get(i),
                    virksomhetOrgnr_i);
            }
            if (inntektFrilans != null) {
                lagInntektForArbeidsforhold(inntektArbeidYtelseBuilder, behandlingReferanse.getAktørId(), dt, dt.plusMonths(1), inntektFrilans,
                    dummyVirksomhet);
                lagInntektForOpptjening(inntektArbeidYtelseBuilder, behandlingReferanse.getAktørId(), dt, dt.plusMonths(1), inntektFrilans,
                    dummyVirksomhetOrgnr);
            }
        }

        if (årsinntekterSN != null) {
            for (int ix = 0; ix < 3; ix++) {
                lagInntektForSN(inntektArbeidYtelseBuilder, behandlingReferanse.getAktørId(), LocalDate.of(førsteÅr + ix, Month.JANUARY, 1),
                    årsinntekterSN.get(ix));
            }
        }
        OppgittOpptjeningDtoBuilder oppgittOpptjeningBuilder = OppgittOpptjeningDtoBuilder.ny();
        OppgittOpptjeningDtoBuilder.EgenNæringBuilder egenNæringBuilder = OppgittOpptjeningDtoBuilder.EgenNæringBuilder.ny()
            .medBruttoInntekt(årsinntektVarigEndring)
            .medVarigEndring(årsinntektVarigEndring != null)
            .medPeriode(Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(1), SKJÆRINGSTIDSPUNKT_OPPTJENING))
            .medEndringDato(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(1));
        oppgittOpptjeningBuilder
            .leggTilEgneNæringer(List.of(egenNæringBuilder));
        if (inntektFrilans != null) {
            OppgittAnnenAktivitetDto frilanserAktivitet = new OppgittAnnenAktivitetDto(Intervall.fraOgMedTilOgMed(fraOgMed, tilOgMed),
                ArbeidType.FRILANSER);
            oppgittOpptjeningBuilder.leggTilAnnenAktivitet(frilanserAktivitet);
        }

        return InntektArbeidYtelseGrunnlagDtoBuilder.oppdatere(Optional.empty())
            .medData(inntektArbeidYtelseBuilder)
            .medOppgittOpptjening(oppgittOpptjeningBuilder);

    }
}
