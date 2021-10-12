package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell;

import static no.nav.folketrygdloven.kalkulator.felles.BeregningstidspunktTjeneste.finnBeregningstidspunkt;
import static no.nav.folketrygdloven.kalkulator.felles.InfotrygdvedtakMedDagpengerTjeneste.finnDagsatsFraSykepengervedtak;
import static no.nav.folketrygdloven.kalkulator.felles.InfotrygdvedtakMedDagpengerTjeneste.harSykepengerPåGrunnlagAvDagpenger;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.KonfigurasjonVerdi;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.FinnAnsettelsesPeriode;
import no.nav.folketrygdloven.kalkulator.felles.MeldekortUtils;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektspostDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittEgenNæringDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittOpptjeningDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseAnvistDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.Stillingsprosent;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;

@ApplicationScoped
@FagsakYtelseTypeRef()
public class MapInntektsgrunnlagVLTilRegelFelles extends MapInntektsgrunnlagVLTilRegel {
    private static final String TOGGLE_SPLITTE_SAMMENLIGNING = "fpsak.splitteSammenligningATFL";
    private static final String INNTEKT_RAPPORTERING_FRIST_DATO = "inntekt.rapportering.frist.dato";

    @Inject
    public MapInntektsgrunnlagVLTilRegelFelles() {
        // CDI
    }

    @Override
    public Inntektsgrunnlag map(BeregningsgrunnlagInput input, LocalDate skjæringstidspunktBeregning) {
        Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
        inntektsgrunnlag.setInntektRapporteringFristDag((Integer) input.getKonfigVerdi(INNTEKT_RAPPORTERING_FRIST_DATO));
        hentInntektArbeidYtelse(inntektsgrunnlag, input, skjæringstidspunktBeregning);

        return inntektsgrunnlag;
    }

    private void lagInntektBeregning(Inntektsgrunnlag inntektsgrunnlag, InntektFilterDto filter, Collection<YrkesaktivitetDto> yrkesaktiviteter) {
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

    private void mapInntektsmelding(Inntektsgrunnlag inntektsgrunnlag,
                                    Collection<InntektsmeldingDto> inntektsmeldinger,
                                    YrkesaktivitetFilterDto filterYaRegister,
                                    LocalDate skjæringstidspunktBeregning) {
        inntektsmeldinger.stream()
            .filter(im -> slutterPåEllerEtterSkjæringstidspunkt(im, filterYaRegister, skjæringstidspunktBeregning))
            .map(this::mapNaturalYtelse)
            .forEach(inntektsgrunnlag::leggTilPeriodeinntekt);
    }

    private Periodeinntekt mapNaturalYtelse(InntektsmeldingDto im) {

        try {
            Arbeidsforhold arbeidsforhold = MapArbeidsforholdFraVLTilRegel.mapForInntektsmelding(im);
            BigDecimal inntekt = im.getInntektBeløp().getVerdi();
            List<no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.NaturalYtelse> naturalytelser = im.getNaturalYtelser().stream()
                .map(ny -> new no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.NaturalYtelse(ny.getBeloepPerMnd().getVerdi(),
                    ny.getPeriode().getFomDato(), ny.getPeriode().getTomDato()))
                .collect(Collectors.toList());
            Periodeinntekt.Builder naturalYtelserBuilder = Periodeinntekt.builder()
                .medInntektskildeOgPeriodeType(Inntektskilde.INNTEKTSMELDING)
                .medArbeidsgiver(arbeidsforhold)
                .medInntekt(inntekt)
                .medNaturalYtelser(naturalytelser);
            im.getStartDatoPermisjon().ifPresent(dato -> naturalYtelserBuilder.medMåned(dato.minusMonths(1).withDayOfMonth(1)));
            return naturalYtelserBuilder.build();
        } catch (RuntimeException e) {
            throw new IllegalArgumentException(String.format("Kunne ikke mappe inntektsmelding [journalpostId=%s, kanalreferanse=%s]: %s",
                im.getJournalpostId(), im.getKanalreferanse(), e.getMessage()), e);
        }
    }

    private boolean slutterPåEllerEtterSkjæringstidspunkt(InntektsmeldingDto im, YrkesaktivitetFilterDto filterYaRegister, LocalDate skjæringstidspunktBeregning) {
        return filterYaRegister.getYrkesaktiviteter().stream()
            .filter(ya -> ya.gjelderFor(im))
            .anyMatch(ya -> FinnAnsettelsesPeriode.finnMinMaksPeriode(ya.getAlleAktivitetsAvtaler(), skjæringstidspunktBeregning)
                .map(periode -> !periode.getTom().isBefore(skjæringstidspunktBeregning)).orElse(false));
    }

    private void mapTilstøtendeYtelseAAP(Inntektsgrunnlag inntektsgrunnlag,
                                                     YtelseFilterDto ytelseFilter,
                                                     LocalDate skjæringstidspunkt,
                                                     FagsakYtelseType fagsakYtelseType) {

        Optional<YtelseDto> nyesteVedtakForDagsats = MeldekortUtils.sisteVedtakFørStpForType(ytelseFilter, skjæringstidspunkt, Set.of(FagsakYtelseType.ARBEIDSAVKLARINGSPENGER));

        if (nyesteVedtakForDagsats.isEmpty()) {
            return;
        }

        Periodeinntekt aap = mapYtelseFraArenavedtak(ytelseFilter, skjæringstidspunkt, fagsakYtelseType, nyesteVedtakForDagsats.get(), FagsakYtelseType.ARBEIDSAVKLARINGSPENGER);
        inntektsgrunnlag.leggTilPeriodeinntekt(aap);
    }

    private Periodeinntekt mapYtelseFraArenavedtak(YtelseFilterDto ytelseFilter, LocalDate skjæringstidspunkt, FagsakYtelseType fagsakYtelseType, YtelseDto nyesteVedtakForDagsats, FagsakYtelseType arbeidsavklaringspenger) {
        Optional<YtelseAnvistDto> sisteUtbetalingFørStp = MeldekortUtils.sisteHeleMeldekortFørStp(ytelseFilter, nyesteVedtakForDagsats,
                skjæringstidspunkt,
                Set.of(arbeidsavklaringspenger), fagsakYtelseType);
        BigDecimal dagsats = nyesteVedtakForDagsats.getVedtaksDagsats().map(Beløp::getVerdi)
                .orElse(sisteUtbetalingFørStp.flatMap(YtelseAnvistDto::getDagsats).map(Beløp::getVerdi).orElse(BigDecimal.ZERO));
        BigDecimal utbetalingsgradProsent = sisteUtbetalingFørStp.flatMap(YtelseAnvistDto::getUtbetalingsgradProsent)
                .map(Stillingsprosent::getVerdi).orElse(MeldekortUtils.MAX_UTBETALING_PROSENT_AAP_DAG);

        return Periodeinntekt.builder()
                .medInntektskildeOgPeriodeType(Inntektskilde.TILSTØTENDE_YTELSE_DP_AAP)
                .medInntekt(dagsats)
                .medMåned(skjæringstidspunkt)
                .medUtbetalingsgrad(utbetalingsgradProsent)
                .build();
    }

    private void mapTilstøtendeYtelseDagpenger(Inntektsgrunnlag inntektsgrunnlag,
                                                     YtelseFilterDto ytelseFilter,
                                                     LocalDate skjæringstidspunkt,
                                                     FagsakYtelseType fagsakYtelseType) {

        Collection<YtelseDto> ytelser = ytelseFilter.getFiltrertYtelser();
        Boolean harSPAvDP = harSykepengerPåGrunnlagAvDagpenger(ytelser, skjæringstidspunkt);

        if (harSPAvDP && KonfigurasjonVerdi.get("BEREGNE_DAGPENGER_FRA_SYKEPENGER", false)) {
            var dagpenger = mapDagpengerFraInfotrygdvedtak(skjæringstidspunkt, ytelser);
            inntektsgrunnlag.leggTilPeriodeinntekt(dagpenger);
        } else {
            Optional<YtelseDto> nyesteVedtakForDagsats = MeldekortUtils.sisteVedtakFørStpForType(ytelseFilter, skjæringstidspunkt, Set.of(FagsakYtelseType.DAGPENGER));
            if (nyesteVedtakForDagsats.isEmpty()) {
                return;
            }
            Periodeinntekt dagpenger = mapYtelseFraArenavedtak(ytelseFilter, skjæringstidspunkt, fagsakYtelseType, nyesteVedtakForDagsats.get(), FagsakYtelseType.DAGPENGER);
            inntektsgrunnlag.leggTilPeriodeinntekt(dagpenger);
        }
    }

    private Periodeinntekt mapDagpengerFraInfotrygdvedtak(LocalDate skjæringstidspunkt, Collection<YtelseDto> ytelser) {
        BigDecimal dagsats = finnDagsatsFraSykepengervedtak(ytelser, skjæringstidspunkt);

        return Periodeinntekt.builder()
                .medInntektskildeOgPeriodeType(Inntektskilde.TILSTØTENDE_YTELSE_DP_AAP)
                .medInntekt(dagsats)
                .medMåned(skjæringstidspunkt)
                // Antar alltid full utbetaling ved overgang fra dagpenger til sykepenger
                .medUtbetalingsgrad(MeldekortUtils.MAX_UTBETALING_PROSENT_AAP_DAG)
                .build();
    }

    private void lagInntektSammenligning(Inntektsgrunnlag inntektsgrunnlag, InntektFilterDto filter) {
        Map<LocalDate, BigDecimal> månedsinntekter = filter.filterSammenligningsgrunnlag().getFiltrertInntektsposter().stream()
            .collect(Collectors.groupingBy(ip -> ip.getPeriode().getFomDato(), Collectors.reducing(BigDecimal.ZERO,
                ip -> ip.getBeløp().getVerdi(), BigDecimal::add)));

        månedsinntekter.forEach((måned, inntekt) -> inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder()
            .medInntektskildeOgPeriodeType(Inntektskilde.INNTEKTSKOMPONENTEN_SAMMENLIGNING)
            .medMåned(måned)
            .medInntekt(inntekt)
            .build()));
    }

    private void lagInntektSammenligningPrStatus(Inntektsgrunnlag inntektsgrunnlag, InntektFilterDto filter, Collection<YrkesaktivitetDto> yrkesaktiviteter) {
        Map<LocalDate, BigDecimal> månedsinntekterFrilans = filter.filterSammenligningsgrunnlag().getAlleInntektSammenligningsgrunnlag().stream()
            .filter(inntekt -> inntekt.getArbeidsgiver() != null)
            .filter(inntekt -> erFrilanser(inntekt.getArbeidsgiver(), yrkesaktiviteter))
            .flatMap(i -> i.getAlleInntektsposter().stream())
            .collect(Collectors.groupingBy(ip -> ip.getPeriode().getFomDato(), Collectors.reducing(BigDecimal.ZERO,
                ip -> ip.getBeløp().getVerdi(), BigDecimal::add)));

        månedsinntekterFrilans.forEach((måned, inntekt) -> inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder()
            .medInntektskildeOgPeriodeType(Inntektskilde.INNTEKTSKOMPONENTEN_SAMMENLIGNING)
            .medMåned(måned)
            .medInntekt(inntekt)
            .medAktivitetStatus(AktivitetStatus.FL)
            .build()));

        Map<LocalDate, BigDecimal> månedsinntekterArbeidstaker = filter.filterSammenligningsgrunnlag().getAlleInntektSammenligningsgrunnlag().stream()
            .filter(inntekt -> inntekt.getArbeidsgiver() != null)
            .filter(inntekt -> !erFrilanser(inntekt.getArbeidsgiver(), yrkesaktiviteter))
            .flatMap(i -> i.getAlleInntektsposter().stream())
            .collect(Collectors.groupingBy(ip -> ip.getPeriode().getFomDato(), Collectors.reducing(BigDecimal.ZERO,
                ip -> ip.getBeløp().getVerdi(), BigDecimal::add)));

        månedsinntekterArbeidstaker.forEach((måned, inntekt) -> inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder()
            .medInntektskildeOgPeriodeType(Inntektskilde.INNTEKTSKOMPONENTEN_SAMMENLIGNING)
            .medMåned(måned)
            .medInntekt(inntekt)
            .medAktivitetStatus(AktivitetStatus.AT)
            .build()));
    }

    private void lagInntekterSN(Inntektsgrunnlag inntektsgrunnlag, InntektFilterDto filter) {
        filter.filterBeregnetSkatt().getFiltrertInntektsposter()
            .forEach(inntektspost -> inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder()
                .medInntektskildeOgPeriodeType(Inntektskilde.SIGRUN)
                .medInntekt(inntektspost.getBeløp().getVerdi())
                .medPeriode(Periode.of(inntektspost.getPeriode().getFomDato(), inntektspost.getPeriode().getTomDato()))
                .build()));
    }

    private void hentInntektArbeidYtelse(Inntektsgrunnlag inntektsgrunnlag, BeregningsgrunnlagInput input, LocalDate skjæringstidspunktBeregning) {
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = input.getIayGrunnlag();
        Collection<InntektsmeldingDto> inntektsmeldinger = input.getInntektsmeldinger();

        var filter = new InntektFilterDto(iayGrunnlag.getAktørInntektFraRegister()).før(skjæringstidspunktBeregning);
        var aktørArbeid = iayGrunnlag.getAktørArbeidFraRegister();
        var filterYaRegister = new YrkesaktivitetFilterDto(iayGrunnlag.getArbeidsforholdInformasjon(), aktørArbeid);

        if (!filter.isEmpty()) {
            List<YrkesaktivitetDto> yrkesaktiviteter = new ArrayList<>();
            yrkesaktiviteter.addAll(filterYaRegister.getYrkesaktiviteterForBeregning());
            yrkesaktiviteter.addAll(filterYaRegister.getFrilansOppdrag());

            var bekreftetAnnenOpptjening = iayGrunnlag.getBekreftetAnnenOpptjening();
            var filterYaBekreftetAnnenOpptjening = new YrkesaktivitetFilterDto(iayGrunnlag.getArbeidsforholdInformasjon(), bekreftetAnnenOpptjening)
                .før(skjæringstidspunktBeregning);
            yrkesaktiviteter.addAll(filterYaBekreftetAnnenOpptjening.getYrkesaktiviteterForBeregning());

            lagInntektBeregning(inntektsgrunnlag, filter, yrkesaktiviteter);
            if (!input.isEnabled(TOGGLE_SPLITTE_SAMMENLIGNING, false)) {
                lagInntektSammenligning(inntektsgrunnlag, filter);
            } else {
                lagInntektSammenligningPrStatus(inntektsgrunnlag, filter, yrkesaktiviteter);
            }
            lagInntekterSN(inntektsgrunnlag, filter);
        }

        mapInntektsmelding(inntektsgrunnlag, inntektsmeldinger, filterYaRegister, skjæringstidspunktBeregning);

        var ytelseFilter = new YtelseFilterDto(iayGrunnlag.getAktørYtelseFraRegister()).før(skjæringstidspunktBeregning);
        if (!ytelseFilter.getFiltrertYtelser().isEmpty()) {
            mapTilstøtendeYtelseAAP(inntektsgrunnlag, ytelseFilter, skjæringstidspunktBeregning, input.getFagsakYtelseType());
            mapTilstøtendeYtelseDagpenger(inntektsgrunnlag, ytelseFilter, skjæringstidspunktBeregning, input.getFagsakYtelseType());
        }

        Optional<OppgittOpptjeningDto> oppgittOpptjeningOpt = iayGrunnlag.getOppgittOpptjening();
        oppgittOpptjeningOpt.ifPresent(oppgittOpptjening -> mapOppgittOpptjening(inntektsgrunnlag, oppgittOpptjening));

    }

    void mapOppgittOpptjening(Inntektsgrunnlag inntektsgrunnlag, OppgittOpptjeningDto oppgittOpptjening) {
        oppgittOpptjening.getEgenNæring().stream()
            .filter(en -> en.getNyoppstartet() || en.getVarigEndring())
            .filter(en -> en.getBruttoInntekt() != null)
            .forEach(en -> inntektsgrunnlag.leggTilPeriodeinntekt(byggPeriodeinntektEgenNæring(en)));
    }

    private Periodeinntekt byggPeriodeinntektEgenNæring(OppgittEgenNæringDto en) {
        LocalDate datoForInntekt;
        if (en.getVarigEndring()) {
            datoForInntekt = en.getEndringDato();
        } else {
            datoForInntekt = en.getFraOgMed();
        }
        if (datoForInntekt == null) {
            throw new IllegalStateException("Søker har oppgitt varig endret eller nyoppstartet næring men har ikke oppgitt endringsdato eller oppstartsdato");
        }
        return Periodeinntekt.builder()
            .medInntektskildeOgPeriodeType(Inntektskilde.SØKNAD)
            .medMåned(datoForInntekt)
            .medInntekt(en.getBruttoInntekt())
            .build();
    }

}
