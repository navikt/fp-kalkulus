package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.ytelse.frisinn.FrisinnGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektspostDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittEgenNæringDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittFrilansDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittFrilansInntektDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittOpptjeningDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittPeriodeInntekt;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseAnvistDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.AktørId;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.Stillingsprosent;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.ArbeidType;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.FagsakYtelseType;

@ApplicationScoped
@FagsakYtelseTypeRef("FRISINN")
public class MapInntektsgrunnlagVLTilRegelFRISINN extends MapInntektsgrunnlagVLTilRegel {

    public static final int MÅNEDER_FØR_STP = 36;

    private static Periodeinntekt mapTilRegel(OppgittEgenNæringDto oppgittEgenNæringDto) {
        BigDecimal inntekt = oppgittEgenNæringDto.getBruttoInntekt();
        Intervall periode = oppgittEgenNæringDto.getPeriode();
        return Periodeinntekt.builder()
                .medInntektskildeOgPeriodeType(Inntektskilde.SØKNAD)
                .medPeriode(Periode.of(periode.getFomDato(), periode.getTomDato()))
                .medAktivitetStatus(AktivitetStatus.SN)
                .medInntekt(inntekt)
                .build();
    }

    @Override
    public Inntektsgrunnlag map(BeregningsgrunnlagInput input, LocalDate skjæringstidspunktBeregning) {
        Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
        hentInntektArbeidYtelse(input.getKoblingReferanse(), inntektsgrunnlag, input, skjæringstidspunktBeregning);
        return inntektsgrunnlag;
    }

    private void lagInntektBeregning(Inntektsgrunnlag inntektsgrunnlag,
                                     InntektFilterDto filter,
                                     Collection<YrkesaktivitetDto> yrkesaktiviteter) {
        filter.filterBeregningsgrunnlag()
                .filter(i -> i.getArbeidsgiver() != null)
                .forFilter((inntekt, inntektsposter) -> mapInntekt(inntektsgrunnlag, inntekt, inntektsposter, yrkesaktiviteter));
    }

    private void mapInntekt(Inntektsgrunnlag inntektsgrunnlag, InntektDto inntekt, Collection<InntektspostDto> inntektsposter,
                            Collection<YrkesaktivitetDto> yrkesaktiviteter) {
        inntektsposter.forEach(inntektspost -> {

            Arbeidsforhold arbeidsgiver = mapYrkesaktivitet(inntekt.getArbeidsgiver(), yrkesaktiviteter);
            if (Objects.isNull(arbeidsgiver)) {
                throw new IllegalStateException("Arbeidsgiver må være satt.");
            } else if (Objects.isNull(inntektspost.getPeriode().getFomDato())) {
                throw new IllegalStateException("Inntektsperiode må være satt.");
            } else if (Objects.isNull(inntektspost.getBeløp().getVerdi())) {
                throw new IllegalStateException("Inntektsbeløp må være satt.");
            }
            inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder()
                    .medInntektskildeOgPeriodeType(Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING)
                    .medArbeidsgiver(arbeidsgiver)
                    .medMåned(inntektspost.getPeriode().getFomDato())
                    .medInntekt(inntektspost.getBeløp().getVerdi())
                    .build());
        });
    }

    private Arbeidsforhold mapYrkesaktivitet(Arbeidsgiver arbeidsgiver, Collection<YrkesaktivitetDto> yrkesaktiviteter) {
        return erFrilanser(arbeidsgiver, yrkesaktiviteter)
                ? Arbeidsforhold.frilansArbeidsforhold()
                : lagNyttArbeidsforholdHosArbeidsgiver(arbeidsgiver);
    }

    private boolean erFrilanser(Arbeidsgiver arbeidsgiver, Collection<YrkesaktivitetDto> yrkesaktiviteter) {
        final List<ArbeidType> arbeidType = yrkesaktiviteter
                .stream()
                .filter(it -> it.getArbeidsgiver() != null)
                .filter(it -> it.getArbeidsgiver().getIdentifikator().equals(arbeidsgiver.getIdentifikator()))
                .map(YrkesaktivitetDto::getArbeidType)
                .distinct()
                .collect(Collectors.toList());
        boolean erFrilanser = yrkesaktiviteter.stream()
                .map(YrkesaktivitetDto::getArbeidType)
                .anyMatch(ArbeidType.FRILANSER::equals);
        return (arbeidType.isEmpty() && erFrilanser) || arbeidType.contains(ArbeidType.FRILANSER_OPPDRAGSTAKER_MED_MER);
    }

    private Arbeidsforhold lagNyttArbeidsforholdHosArbeidsgiver(Arbeidsgiver arbeidsgiver) {
        if (arbeidsgiver.getErVirksomhet()) {
            return Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(arbeidsgiver.getIdentifikator());
        } else if (arbeidsgiver.erAktørId()) {
            return Arbeidsforhold.nyttArbeidsforholdHosPrivatperson(arbeidsgiver.getIdentifikator());
        }
        throw new IllegalStateException("Arbeidsgiver må være enten aktør eller virksomhet, men var: " + arbeidsgiver);
    }

    private void mapAlleYtelser(Inntektsgrunnlag inntektsgrunnlag,
                                YtelseFilterDto ytelseFilter,
                                LocalDate skjæringstidspunktOpptjening) {
        ytelseFilter.getAlleYtelser().stream()
                .filter(y -> !y.getRelatertYtelseType().equals(FagsakYtelseType.FRISINN)).
                forEach(ytelse -> ytelse.getYtelseAnvist().stream()
                    .filter(ytelseAnvistDto -> !ytelseAnvistDto.getAnvistTOM().isBefore(skjæringstidspunktOpptjening.minusMonths(MÅNEDER_FØR_STP)))
                .filter(this::harHattUtbetalingForPeriode)
                .forEach(anvist -> inntektsgrunnlag.leggTilPeriodeinntekt(byggPeriodeinntektForYtelse(anvist, ytelse.getVedtaksDagsats(), ytelse.getRelatertYtelseType()))));
    }

    private boolean harHattUtbetalingForPeriode(YtelseAnvistDto ytelse) {
        return ytelse.getUtbetalingsgradProsent()
                .map(beløp -> !beløp.erNulltall())
                .orElse(false);
    }

    private Periodeinntekt byggPeriodeinntektForYtelse(YtelseAnvistDto anvist, Optional<Beløp> vedtaksDagsats, FagsakYtelseType ytelsetype) {
        return Periodeinntekt.builder()
                .medInntektskildeOgPeriodeType(erAAPEllerDP(ytelsetype) ? Inntektskilde.TILSTØTENDE_YTELSE_DP_AAP: Inntektskilde.ANNEN_YTELSE)
                .medInntekt(finnBeløp(anvist, vedtaksDagsats))
                .medUtbetalingsgrad(anvist.getUtbetalingsgradProsent().map(Stillingsprosent::getVerdi).orElseThrow())
                .medPeriode(Periode.of(anvist.getAnvistFOM(), anvist.getAnvistTOM()))
                .build();
    }

    private boolean erAAPEllerDP(FagsakYtelseType ytelsetype) {
        return ytelsetype.equals(FagsakYtelseType.ARBEIDSAVKLARINGSPENGER) || ytelsetype.equals(FagsakYtelseType.DAGPENGER);
    }

    private BigDecimal finnBeløp(YtelseAnvistDto anvist, Optional<Beløp> vedtaksDagsats) {
        BigDecimal beløpFraYtelseAnvist = anvist.getBeløp().map(Beløp::getVerdi)
                .orElse(anvist.getDagsats().map(Beløp::getVerdi).orElse(BigDecimal.ZERO));
        return vedtaksDagsats.map(Beløp::getVerdi).orElse(beløpFraYtelseAnvist);
    }

    private void lagInntekterSN(Inntektsgrunnlag inntektsgrunnlag, InntektFilterDto filter) {
        filter.filterBeregnetSkatt().getFiltrertInntektsposter()
                .forEach(inntektspost -> inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder()
                        .medInntektskildeOgPeriodeType(Inntektskilde.SIGRUN)
                        .medAktivitetStatus(AktivitetStatus.SN)
                        .medInntekt(inntektspost.getBeløp().getVerdi())
                        .medPeriode(Periode.of(inntektspost.getPeriode().getFomDato(), inntektspost.getPeriode().getTomDato()))
                        .build()));
    }

    private void hentInntektArbeidYtelse(KoblingReferanse referanse, Inntektsgrunnlag inntektsgrunnlag, BeregningsgrunnlagInput input, LocalDate skjæringstidspunktBeregning) {
        AktørId aktørId = referanse.getAktørId();
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = input.getIayGrunnlag();

        var filter = new InntektFilterDto(iayGrunnlag.getAktørInntektFraRegister(aktørId));
        var aktørArbeid = iayGrunnlag.getAktørArbeidFraRegister(aktørId);
        var filterYaRegister = new YrkesaktivitetFilterDto(iayGrunnlag.getArbeidsforholdInformasjon(), aktørArbeid).før(skjæringstidspunktBeregning);

        YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag = input.getYtelsespesifiktGrunnlag();
        if (!(ytelsespesifiktGrunnlag instanceof FrisinnGrunnlag)) {
            throw new IllegalStateException("Ytelsesgrunnlag må være FRISINNgrunnlag for å beregne FRISINN ytelse");
        }
        FrisinnGrunnlag frisinnGrunnlag = (FrisinnGrunnlag) ytelsespesifiktGrunnlag;

        if (!filter.isEmpty()) {
            List<YrkesaktivitetDto> yrkesaktiviteter = new ArrayList<>();
            yrkesaktiviteter.addAll(filterYaRegister.getYrkesaktiviteterForBeregning());
            yrkesaktiviteter.addAll(filterYaRegister.getFrilansOppdrag());

            lagInntektBeregning(inntektsgrunnlag, filter, yrkesaktiviteter);
            lagInntekterSN(inntektsgrunnlag, filter);
        }

        var ytelseFilter = new YtelseFilterDto(iayGrunnlag.getAktørYtelseFraRegister(aktørId)).før(input.getSkjæringstidspunktOpptjening());
        if (!ytelseFilter.getFiltrertYtelser().isEmpty()) {
            mapAlleYtelser(inntektsgrunnlag, ytelseFilter, referanse.getSkjæringstidspunktOpptjening());
        }

        Optional<OppgittOpptjeningDto> oppgittOpptjeningOpt = iayGrunnlag.getOppgittOpptjening();
        oppgittOpptjeningOpt.ifPresent(oppgittOpptjening ->
                mapOppgittOpptjening(inntektsgrunnlag, frisinnGrunnlag, oppgittOpptjening, input.getSkjæringstidspunktOpptjening()));

    }

    private void mapOppgittOpptjening(Inntektsgrunnlag inntektsgrunnlag, FrisinnGrunnlag frisinnGrunnlag,
                                      OppgittOpptjeningDto oppgittOpptjening, LocalDate skjæringstidspunktBeregning) {
        mapOppgittNæringsinntekt(inntektsgrunnlag, frisinnGrunnlag, oppgittOpptjening, skjæringstidspunktBeregning);
        mapOppgittFrilansinntekt(inntektsgrunnlag, oppgittOpptjening, skjæringstidspunktBeregning);
        mapOppgittArbeidsinntekt(inntektsgrunnlag, oppgittOpptjening);
    }

    private void mapOppgittArbeidsinntekt(Inntektsgrunnlag inntektsgrunnlag, OppgittOpptjeningDto oppgittOpptjening) {
        List<OppgittPeriodeInntekt> inntekter = oppgittOpptjening.getOppgittArbeidsforhold()
                .stream().map(oppgittArbeidsforholdDto -> (OppgittPeriodeInntekt) oppgittArbeidsforholdDto)
                .collect(Collectors.toList());
        inntekter.forEach(inntekt -> inntektsgrunnlag.leggTilPeriodeinntekt(byggOppgittInntektForStatus(inntekt, AktivitetStatus.AT)));
    }

    private void mapOppgittFrilansinntekt(Inntektsgrunnlag inntektsgrunnlag, OppgittOpptjeningDto oppgittOpptjening, LocalDate skjæringstidspunktBeregning) {
        List<OppgittFrilansInntektDto> flInntekter = oppgittOpptjening.getFrilans()
                .map(OppgittFrilansDto::getOppgittFrilansInntekt)
                .orElse(Collections.emptyList());
        List<OppgittFrilansInntektDto> oppgittFLInntekt = flInntekter.stream()
                .filter(inntekt -> oppgittForPeriodeEtterSTP(inntekt.getPeriode(), skjæringstidspunktBeregning))
                .collect(Collectors.toList());
        oppgittFLInntekt.forEach(inntekt -> inntektsgrunnlag.leggTilPeriodeinntekt(byggOppgittInntektForStatus(inntekt, AktivitetStatus.FL)));
    }

    private Periodeinntekt byggOppgittInntektForStatus(OppgittPeriodeInntekt periodeInntekt, AktivitetStatus aktivitetStatus) {
        return Periodeinntekt.builder()
                .medInntektskildeOgPeriodeType(Inntektskilde.SØKNAD)
                .medPeriode(Periode.of(periodeInntekt.getPeriode().getFomDato(), periodeInntekt.getPeriode().getTomDato()))
                .medInntekt(periodeInntekt.getInntekt())
                .medAktivitetStatus(aktivitetStatus)
                .build();
    }

    private boolean oppgittForPeriodeEtterSTP(Intervall periode, LocalDate skjæringstidspunktBeregning) {
        return !periode.getFomDato().isBefore(skjæringstidspunktBeregning);
    }

    private void mapOppgittNæringsinntekt(Inntektsgrunnlag inntektsgrunnlag,
                                          FrisinnGrunnlag frisinnGrunnlag,
                                          OppgittOpptjeningDto oppgittOpptjening, LocalDate skjæringstidspunktBeregning) {
        if (!oppgittOpptjening.getEgenNæring().isEmpty()) {
            Optional<OppgittEgenNæringDto> oppgittInntektFørStp = oppgittOpptjening.getEgenNæring().stream()
                    .filter(en -> skjæringstidspunktBeregning.isAfter(en.getFraOgMed()))
                    .filter(en -> en.getBruttoInntekt() != null)
                    .findFirst();
            if (oppgittInntektFørStp.isEmpty() && frisinnGrunnlag.getSøkerYtelseForNæring()) {
                throw new IllegalStateException("Kunne ikke finne oppgitt næringsinntekt før skjæringstidspunkt, ugyldig tilstand for ytelse FRISINN");
            } else if (frisinnGrunnlag.getSøkerYtelseForNæring() && oppgittInntektFørStp.isPresent()) {
                inntektsgrunnlag.leggTilPeriodeinntekt(mapTilRegel(oppgittInntektFørStp.get()));
            }
            oppgittOpptjening.getEgenNæring().stream()
                    .filter(en -> !skjæringstidspunktBeregning.isAfter(en.getFraOgMed()))
                    .filter(en -> en.getBruttoInntekt() != null)
                    .map(MapInntektsgrunnlagVLTilRegelFRISINN::mapTilRegel)
                    .forEach(inntektsgrunnlag::leggTilPeriodeinntekt);
        }
    }
}
