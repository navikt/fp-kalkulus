package no.nav.folketrygdloven.kalkulator.steg.besteberegning;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.RelatertYtelseType;
import no.nav.folketrygdloven.besteberegning.modell.BesteberegningRegelmodell;
import no.nav.folketrygdloven.besteberegning.modell.input.BesteberegningInput;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.ForeslåBesteberegningInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektspostDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittEgenNæringDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseAnvistDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.kodeverk.OffentligYtelseType;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseType;

public class MapTilBesteberegningRegelmodell {


    public static BesteberegningRegelmodell map(ForeslåBesteberegningInput input) {
        BesteberegningInput besteberegningInput = lagInput(input);
        return new BesteberegningRegelmodell(besteberegningInput);
    }

    private static BesteberegningInput lagInput(ForeslåBesteberegningInput input) {
        List<Periodeinntekt> inntekter = mapInntekterForBesteberegning(input);
        Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
        inntekter.forEach(inntektsgrunnlag::leggTilPeriodeinntekt);
        List<Periode> perioderMedNæringsvirksomhet = finnPerioderMedOppgittNæring(input);
        return new BesteberegningInput(inntektsgrunnlag,
                input.getGrunnbeløpsatser(),
                finnGrunnbeløp(input),
                input.getSkjæringstidspunktOpptjening(),
                perioderMedNæringsvirksomhet,
                finnTotalBruttoUtenNaturalytelseFørstePeriode(input));
    }

    /** Finner total brutto beregningsgrunnlag i første periode.
     * Tar ikke med naturalytelse ettersom filteret som brukes til å beregne de seks beste månedene heller ikke
     * tar med naturalytelse.
     *
     * @param input Input til foreslå besteberegning
     * @return Total brutto beregningsgrunnlag uten naturalytelse
     */
    private static BigDecimal finnTotalBruttoUtenNaturalytelseFørstePeriode(ForeslåBesteberegningInput input) {
        List<BeregningsgrunnlagPeriodeDto> perioder = input.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder();
        if (perioder.isEmpty()) {
            throw new IllegalStateException("Liste med perioder skal ikke vere tom");
        }
        BeregningsgrunnlagPeriodeDto førstePeriode = perioder.get(0);
        return førstePeriode.getBruttoPrÅr();
    }

    private static List<Periode> finnPerioderMedOppgittNæring(ForeslåBesteberegningInput input) {
        return input.getIayGrunnlag().getOppgittOpptjening().stream()
                    .flatMap(oo -> oo.getEgenNæring().stream())
                    .map(OppgittEgenNæringDto::getPeriode)
                    .map(p -> Periode.of(p.getFomDato(), p.getTomDato()))
                    .collect(Collectors.toList());
    }

    private static BigDecimal finnGrunnbeløp(ForeslåBesteberegningInput input) {
        return input.getBeregningsgrunnlagGrunnlag()
                .getBeregningsgrunnlag().map(BeregningsgrunnlagDto::getGrunnbeløp).map(Beløp::getVerdi).orElseThrow(() -> new IllegalStateException("Forventer grunnbeløp"));
    }

    private static List<Periodeinntekt> mapInntekterForBesteberegning(BeregningsgrunnlagInput input) {
        var iayGrunnlag = input.getIayGrunnlag();
        var inntektFilterDto = new InntektFilterDto(iayGrunnlag.getAktørInntektFraRegister());
        var ytelseFilterDto = new YtelseFilterDto(iayGrunnlag.getAktørYtelseFraRegister());
        YrkesaktivitetFilterDto yrkesaktivitetFilterDto = new YrkesaktivitetFilterDto(iayGrunnlag.getArbeidsforholdInformasjon(), iayGrunnlag.getAktørArbeidFraRegister());
        List<Periodeinntekt> inntekterATFLSN = finnInntekterATFLSN(input.getSkjæringstidspunktForBeregning(), inntektFilterDto, yrkesaktivitetFilterDto);
        List<Periodeinntekt> ytelserFraSammenligningsgrunnlaget = lagInntektForYtelseFraSammenligningsgrunnlag(inntektFilterDto);
        List<Periodeinntekt> ytelserDPOgAAP = lagYtelseDagpengerArbeidsavklaringspenger(ytelseFilterDto);
        List<Periodeinntekt> alleInntekter = new ArrayList<>();
        alleInntekter.addAll(inntekterATFLSN);
        alleInntekter.addAll(ytelserFraSammenligningsgrunnlaget);
        alleInntekter.addAll(ytelserDPOgAAP);
        return alleInntekter;
    }


    private static List<Periodeinntekt> lagYtelseDagpengerArbeidsavklaringspenger(YtelseFilterDto ytelseFilter) {
        Set<FagsakYtelseType> ytelseTyper = Set.of(FagsakYtelseType.DAGPENGER, FagsakYtelseType.ARBEIDSAVKLARINGSPENGER);
        return ytelseFilter.getAlleYtelser().stream()
                .filter(yt-> ytelseTyper.contains(yt.getRelatertYtelseType()))
                .map(MapTilBesteberegningRegelmodell::mapYtelseTilPeriodeinntekt)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private static List<Periodeinntekt> mapYtelseTilPeriodeinntekt(YtelseDto yt) {
        // Mapper alle ytelser til DP sidan man ikkje kan ha både AAP og DP på skjæringstidspunktet
        return yt.getYtelseAnvist().stream()
                .map(meldekort -> Periodeinntekt.builder()
                        .medInntektskildeOgPeriodeType(Inntektskilde.TILSTØTENDE_YTELSE_DP_AAP) // OBS: Utbetaling er eit eingangsbeløp og skjer ikkje daglig
                        .medInntekt(meldekort.getBeløp().map(Beløp::getVerdi).orElse(BigDecimal.ZERO))
                        .medPeriode(utledMeldekortperiode(meldekort, yt.getPeriode()))
                        .build())
                .collect(Collectors.toList());
    }

    private static Periode utledMeldekortperiode(YtelseAnvistDto meldekort, Intervall vedtaksperiode) {
        LocalDate fom = meldekort.getAnvistFOM().isBefore(vedtaksperiode.getFomDato()) ? vedtaksperiode.getFomDato() : meldekort.getAnvistFOM();
        LocalDate tom = meldekort.getAnvistTOM().isAfter(vedtaksperiode.getTomDato()) ? vedtaksperiode.getTomDato() : meldekort.getAnvistTOM();
        return Periode.of(fom, tom);
    }


    /** Henter ut ytelser fra sammenlignigsgrunnlaget:
     * - FORELDREPENGER
     * - SVANGERSKAPSPENGER
     * - SYKEPENGER
     * - SYKEPENGER_FISKER
     *
     * @param filter Inntektsfilter
     * @return periodeinntekter for ytelser
     */
    private static List<Periodeinntekt> lagInntektForYtelseFraSammenligningsgrunnlag(InntektFilterDto filter) {
        List<InntektspostDto> ytelseposter = filter.filterSammenligningsgrunnlag().getFiltrertInntektsposter().stream()
                .filter(ip -> ip.getYtelseType() != null && !ip.getYtelseType().equals(OffentligYtelseType.UDEFINERT))
                .collect(Collectors.toList());

        return ytelseposter.stream().map(e -> Periodeinntekt.builder()
                .medInntektskildeOgPeriodeType(Inntektskilde.INNTEKTSKOMPONENTEN_SAMMENLIGNING)
                .medAktivitetStatus(AktivitetStatus.KUN_YTELSE)
                .medYtelse(mapTilRegelytelse(e.getYtelseType()))
                .medMåned(e.getPeriode().getFomDato())
                .medInntekt(e.getBeløp().getVerdi())
                .build()).collect(Collectors.toList());
    }

    private static RelatertYtelseType mapTilRegelytelse(YtelseType ytelseType) {
        if (!(ytelseType instanceof OffentligYtelseType)) {
            throw new IllegalStateException("Støtte på ukjent ytelse under besteberegning " + ytelseType.getKode());
        }
        var ytelse = (OffentligYtelseType) ytelseType;
        switch(ytelse) {
            case SVANGERSKAPSPENGER:
                return RelatertYtelseType.SVANGERSKAPSPENGER;
            case FORELDREPENGER:
                return RelatertYtelseType.FORELDREPENGER;
            case SYKEPENGER:
            case SYKEPENGER_FISKER:
                return RelatertYtelseType.SYKEPENGER;
            default:
                throw new IllegalStateException("Støtte på ukjent ytelse under besteberegning " + ytelseType.getKode());
        }
    }


    private static List<Periodeinntekt> finnInntekterATFLSN(LocalDate skjæringstidspunktBeregning, InntektFilterDto filter, YrkesaktivitetFilterDto yrkesaktivitetFilterDto) {
        var filterYaRegister = yrkesaktivitetFilterDto.før(skjæringstidspunktBeregning);
        List<Periodeinntekt> samletInntekterATFLSN = new ArrayList<>();
        if (!filter.isEmpty()) {
            List<YrkesaktivitetDto> yrkesaktiviteter = new ArrayList<>();
            yrkesaktiviteter.addAll(filterYaRegister.getYrkesaktiviteterForBeregning());
            yrkesaktiviteter.addAll(filterYaRegister.getFrilansOppdrag());

            List<Periodeinntekt> inntekterATFL = lagInntektBeregning(filter, yrkesaktiviteter);
            List<Periodeinntekt> inntektNæring = lagInntekterSN(filter);

            samletInntekterATFLSN = Stream.concat(inntekterATFL.stream(), inntektNæring.stream()).collect(Collectors.toList());

        }
        return samletInntekterATFLSN;
    }

    private static List<Periodeinntekt> lagInntektBeregning(InntektFilterDto filter,
                                                            Collection<YrkesaktivitetDto> yrkesaktiviteter) {
        List<Periodeinntekt> inntekter = new ArrayList<>();
        filter.filterBeregningsgrunnlag()
                .filter(i -> i.getArbeidsgiver() != null)
                .forFilter((inntekt, inntektsposter) -> inntekter.addAll(mapInntekt(inntekt, inntektsposter, yrkesaktiviteter)));
        return inntekter;
    }

    private static List<Periodeinntekt> mapInntekt(InntektDto inntekt, Collection<InntektspostDto> inntektsposter,
                                                   Collection<YrkesaktivitetDto> yrkesaktiviteter) {
        return inntektsposter.stream().map(inntektspost -> {

            Arbeidsforhold arbeidsgiver = mapYrkesaktivitet(inntekt.getArbeidsgiver(), yrkesaktiviteter);
            if (Objects.isNull(arbeidsgiver)) {
                throw new IllegalStateException("Arbeidsgiver må være satt.");
            } else if (Objects.isNull(inntektspost.getPeriode()) || Objects.isNull(inntektspost.getPeriode().getFomDato())) {
                throw new IllegalStateException("Inntektsperiode må være satt.");
            } else if (Objects.isNull(inntektspost.getBeløp().getVerdi())) {
                throw new IllegalStateException("Inntektsbeløp må være satt.");
            }
            return Periodeinntekt.builder()
                    .medInntektskildeOgPeriodeType(Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING)
                    .medArbeidsgiver(arbeidsgiver)
                    .medMåned(inntektspost.getPeriode().getFomDato())
                    .medInntekt(inntektspost.getBeløp().getVerdi())
                    .build();
        }).collect(Collectors.toList());
    }

    private static Arbeidsforhold mapYrkesaktivitet(Arbeidsgiver arbeidsgiver, Collection<YrkesaktivitetDto> yrkesaktiviteter) {
        return erFrilanser(arbeidsgiver, yrkesaktiviteter)
                ? Arbeidsforhold.frilansArbeidsforhold()
                : lagNyttArbeidsforholdHosArbeidsgiver(arbeidsgiver);
    }

    private static boolean erFrilanser(Arbeidsgiver arbeidsgiver, Collection<YrkesaktivitetDto> yrkesaktiviteter) {
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

    private static Arbeidsforhold lagNyttArbeidsforholdHosArbeidsgiver(Arbeidsgiver arbeidsgiver) {
        if (arbeidsgiver.getErVirksomhet()) {
            return Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(arbeidsgiver.getIdentifikator());
        } else if (arbeidsgiver.erAktørId()) {
            return Arbeidsforhold.nyttArbeidsforholdHosPrivatperson(arbeidsgiver.getIdentifikator());
        }
        throw new IllegalStateException("Arbeidsgiver må være enten aktør eller virksomhet, men var: " + arbeidsgiver);
    }

    private static List<Periodeinntekt> lagInntekterSN(InntektFilterDto filter) {
        return filter.filterBeregnetSkatt().getFiltrertInntektsposter()
                .stream()
                .map(inntektspost -> Periodeinntekt.builder()
                        .medInntektskildeOgPeriodeType(Inntektskilde.SIGRUN)
                        .medAktivitetStatus(AktivitetStatus.SN)
                        .medInntekt(inntektspost.getBeløp().getVerdi())
                        .medPeriode(Periode.of(inntektspost.getPeriode().getFomDato(), inntektspost.getPeriode().getTomDato()))
                        .build()).collect(Collectors.toList());
    }


}
