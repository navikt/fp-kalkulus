package no.nav.folketrygdloven.kalkulus.mappers;

import static no.nav.folketrygdloven.kalkulus.mappers.MapIAYTilKalulator.mapArbeidsforholdRef;
import static no.nav.folketrygdloven.kalkulus.mappers.OmsorgspengeGrunnlagMapper.mapOmsorgspengegrunnlag;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.AktivitetStatusV2;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.ForeldrepengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.PleiepengerSyktBarnGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.PleiepengerNærståendeGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.StandardGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.SvangerskapspengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.konfig.KonfigTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.behandling.Skjæringstidspunkt;
import no.nav.folketrygdloven.kalkulator.modell.gradering.AktivitetGradering;
import no.nav.folketrygdloven.kalkulator.modell.gradering.AndelGradering;
import no.nav.folketrygdloven.kalkulator.modell.gradering.AndelGradering.Builder;
import no.nav.folketrygdloven.kalkulator.modell.iay.KravperioderPrArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittOpptjeningDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.PerioderForKravDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.RefusjonsperiodeDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.steg.besteberegning.Ytelseandel;
import no.nav.folketrygdloven.kalkulator.steg.besteberegning.Ytelsegrunnlag;
import no.nav.folketrygdloven.kalkulator.steg.besteberegning.Ytelseperiode;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.beregningsperiode.BeregningsperiodeTjeneste;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulator.ytelse.frisinn.FrisinnGrunnlag;
import no.nav.folketrygdloven.kalkulator.ytelse.frisinn.FrisinnPeriode;
import no.nav.folketrygdloven.kalkulus.beregning.v1.AktivitetGraderingDto;
import no.nav.folketrygdloven.kalkulus.beregning.v1.KravperioderPrArbeidsforhold;
import no.nav.folketrygdloven.kalkulus.beregning.v1.OmsorgspengerGrunnlag;
import no.nav.folketrygdloven.kalkulus.beregning.v1.PerioderForKrav;
import no.nav.folketrygdloven.kalkulus.beregning.v1.RefusjonskravDatoDto;
import no.nav.folketrygdloven.kalkulus.beregning.v1.Refusjonsperiode;
import no.nav.folketrygdloven.kalkulus.beregning.v1.UtbetalingsgradPrAktivitetDto;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.felles.jpa.IntervallEntitet;
import no.nav.folketrygdloven.kalkulus.felles.v1.Aktør;
import no.nav.folketrygdloven.kalkulus.felles.v1.KalkulatorInputDto;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.iay.v1.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.kodeverk.FrisinnBehandlingType;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseTyperKalkulusStøtterKontrakt;
import no.nav.folketrygdloven.kalkulus.mapFraEntitet.BehandlingslagerTilKalkulusMapper;
import no.nav.folketrygdloven.kalkulus.opptjening.v1.MidlertidigInaktivType;
import no.nav.folketrygdloven.kalkulus.opptjening.v1.OpptjeningAktiviteterDto;
import no.nav.folketrygdloven.kalkulus.typer.AktørId;

public class MapFraKalkulator {

    private static final String TOGGLE_SPLITTE_SAMMENLIGNINGSGRUNNLAG = "fpsak.splitteSammenligningATFL";
    private static final String TOGGLE_AUTOMATISK_BESTEBEREGNING = "automatisk-besteberegning";
    private static final String TOGGLE_TSF_1715 = "feilretting-tsf-1715";


    public static Arbeidsgiver mapArbeidsgiver(Aktør arbeidsgiver) {
        if (arbeidsgiver == null) {
            return null;
        }
        return arbeidsgiver.getErOrganisasjon() ? Arbeidsgiver.virksomhet(arbeidsgiver.getIdent()) : Arbeidsgiver.person(new AktørId(arbeidsgiver.getIdent()));

    }

    public static BeregningsgrunnlagInput mapFraKalkulatorInputTilBeregningsgrunnlagInput(KoblingEntitet kobling,
                                                                                          KalkulatorInputDto input,
                                                                                          Optional<BeregningsgrunnlagGrunnlagEntitet> beregningsgrunnlagGrunnlagEntitet,
                                                                                          List<IntervallEntitet> forlengelseperioder) {
        var koblingId = kobling.getId();
        var skjæringstidspunkt = input.getSkjæringstidspunkt();

        var ytelseType = FagsakYtelseType.fraKode(kobling.getYtelseTyperKalkulusStøtter().getKode());
        var aktørId = new AktørId(kobling.getAktørId().getId());
        var build = Skjæringstidspunkt.builder()
                .medSkjæringstidspunktOpptjening(skjæringstidspunkt).build();

        var ref = KoblingReferanse.fra(ytelseType, aktørId, koblingId, kobling.getKoblingReferanse().getReferanse(), Optional.empty(), build);

        var iayGrunnlag = input.getIayGrunnlag();
        var opptjeningAktiviteter = input.getOpptjeningAktiviteter();
        var kravPrArbeidsforhold = input.getRefusjonskravPrArbeidsforhold();

        var iayGrunnlagMappet = mapFraDto(iayGrunnlag);
        BeregningsgrunnlagInput utenGrunnbeløp = new BeregningsgrunnlagInput(ref,
                iayGrunnlagMappet,
                mapFraDto(opptjeningAktiviteter),
                mapFraDto(kravPrArbeidsforhold, input.getRefusjonskravDatoer(), iayGrunnlag, input.getSkjæringstidspunkt()),
                mapFraDto(kobling.getYtelseTyperKalkulusStøtter(),
                        input,
                        iayGrunnlagMappet,
                        beregningsgrunnlagGrunnlagEntitet));

        utenGrunnbeløp.leggTilKonfigverdi(BeregningsperiodeTjeneste.INNTEKT_RAPPORTERING_FRIST_DATO, 5);
        utenGrunnbeløp.leggTilToggle(TOGGLE_SPLITTE_SAMMENLIGNINGSGRUNNLAG, false);
        utenGrunnbeløp.leggTilToggle(TOGGLE_TSF_1715, false);
        utenGrunnbeløp.leggTilToggle(TOGGLE_AUTOMATISK_BESTEBEREGNING, true); // Legger til toggle for å kunne teste verdikjede
        utenGrunnbeløp.setForlengelseperioder(mapPerioder(forlengelseperioder));
        return beregningsgrunnlagGrunnlagEntitet.map(BehandlingslagerTilKalkulusMapper::mapGrunnlag)
                .map(utenGrunnbeløp::medBeregningsgrunnlagGrunnlag)
                .orElse(utenGrunnbeløp);
    }

    public static List<Intervall> mapPerioder(List<IntervallEntitet> forlengelseperioder) {
        return forlengelseperioder.stream().map(p -> Intervall.fraOgMedTilOgMed(p.getFomDato(), p.getTomDato())).toList();
    }

    public static List<KravperioderPrArbeidsforholdDto> mapFraDto(List<KravperioderPrArbeidsforhold> kravPrArbeidsforhold, List<RefusjonskravDatoDto> refusjonskravDatoer, InntektArbeidYtelseGrunnlagDto iayGrunnlag, LocalDate stp) {
        if (kravPrArbeidsforhold == null) {
            // TODO: Fjern når alle konsumenter har tatt i bruk ny struktur
            kravPrArbeidsforhold = LagKravperioder.lagKravperioderPrArbeidsforhold(refusjonskravDatoer, iayGrunnlag, stp);
        };
        return kravPrArbeidsforhold.stream().map(MapFraKalkulator::mapKravPerioder).collect(Collectors.toList());
    }

    private static KravperioderPrArbeidsforholdDto mapKravPerioder(KravperioderPrArbeidsforhold kravperioderPrArbeidsforhold) {
        return new KravperioderPrArbeidsforholdDto(mapArbeidsgiver(kravperioderPrArbeidsforhold.getArbeidsgiver()),
                mapArbeidsforholdRef(kravperioderPrArbeidsforhold.getInternreferanse()),
                kravperioderPrArbeidsforhold.getAlleSøktePerioder()
                        .stream().map(MapFraKalkulator::mapSøktPeriode)
                        .collect(Collectors.toList()),
                kravperioderPrArbeidsforhold.getSisteSøktePerioder().getRefusjonsperioder().stream()
                        .map(Refusjonsperiode::getPeriode)
                        .map(MapFraKalkulator::mapPeriode)
                        .collect(Collectors.toList()));
    }

    private static PerioderForKravDto mapSøktPeriode(PerioderForKrav p) {
        return new PerioderForKravDto(p.getInnsendingsdato(), p.getRefusjonsperioder()
                .stream().map(MapFraKalkulator::mapRefusjonsperiode)
                .collect(Collectors.toList()));
    }

    private static RefusjonsperiodeDto mapRefusjonsperiode(Refusjonsperiode rp) {
        return new RefusjonsperiodeDto(mapPeriode(rp.getPeriode()), rp.getBeløp());
    }

    public static YtelsespesifiktGrunnlag mapFraDto(YtelseTyperKalkulusStøtterKontrakt ytelseType,
                                                    KalkulatorInputDto input,
                                                    no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                                    Optional<BeregningsgrunnlagGrunnlagEntitet> beregningsgrunnlagGrunnlagEntitet) {
        var yt = YtelseTyperKalkulusStøtterKontrakt.fraKode(ytelseType.getKode());
        var ytelsespesifiktGrunnlag = input.getYtelsespesifiktGrunnlag();

        switch (yt) {
            case FORELDREPENGER:
                ForeldrepengerGrunnlag foreldrepengerGrunnlag = new ForeldrepengerGrunnlag(ytelsespesifiktGrunnlag.getDekningsgrad().intValue(),
                        ytelsespesifiktGrunnlag.getKvalifisererTilBesteberegning());
                // TODO(OJR) lag builder?
                foreldrepengerGrunnlag.setGrunnbeløpMilitærHarKravPå(KonfigTjeneste.forYtelse(FagsakYtelseType.FORELDREPENGER).getAntallGMilitærHarKravPå().intValue());
                no.nav.folketrygdloven.kalkulus.beregning.v1.ForeldrepengerGrunnlag fpKontraktGrunnlag = (no.nav.folketrygdloven.kalkulus.beregning.v1.ForeldrepengerGrunnlag) ytelsespesifiktGrunnlag;
                var aktivitetGradering = fpKontraktGrunnlag.getAktivitetGradering();
                foreldrepengerGrunnlag.setAktivitetGradering(aktivitetGradering == null ? AktivitetGradering.INGEN_GRADERING : mapFraDto(aktivitetGradering));
                foreldrepengerGrunnlag.setSisteSøkteUttaksdag(fpKontraktGrunnlag.getSisteSøkteUttaksdag());
                foreldrepengerGrunnlag.setBesteberegningYtelsegrunnlag(mapYtelsegrunnlag(fpKontraktGrunnlag.getYtelsegrunnlagForBesteberegning()));
                return foreldrepengerGrunnlag;
            case SVANGERSKAPSPENGER:
                no.nav.folketrygdloven.kalkulus.beregning.v1.SvangerskapspengerGrunnlag svangerskapspengerGrunnlag = (no.nav.folketrygdloven.kalkulus.beregning.v1.SvangerskapspengerGrunnlag) ytelsespesifiktGrunnlag;
                SvangerskapspengerGrunnlag svpGrunnlag = new SvangerskapspengerGrunnlag(
                        UtbetalingsgradMapper.mapUtbetalingsgrad(svangerskapspengerGrunnlag.getUtbetalingsgradPrAktivitet()));
                svpGrunnlag
                        .setGrunnbeløpMilitærHarKravPå(KonfigTjeneste.forYtelse(FagsakYtelseType.SVANGERSKAPSPENGER).getAntallGMilitærHarKravPå().intValue());
                return svpGrunnlag;
            case PLEIEPENGER_SYKT_BARN:
                no.nav.folketrygdloven.kalkulus.beregning.v1.PleiepengerSyktBarnGrunnlag pleiepengerYtelsesGrunnlag = (no.nav.folketrygdloven.kalkulus.beregning.v1.PleiepengerSyktBarnGrunnlag) ytelsespesifiktGrunnlag;
                validerForMidlertidigInaktivTypeA(pleiepengerYtelsesGrunnlag.getUtbetalingsgradPrAktivitet());
                PleiepengerSyktBarnGrunnlag pleiepengerSyktBarnGrunnlag = new PleiepengerSyktBarnGrunnlag(
                        UtbetalingsgradMapper.mapUtbetalingsgrad(pleiepengerYtelsesGrunnlag.getUtbetalingsgradPrAktivitet()));
                pleiepengerSyktBarnGrunnlag
                        .setGrunnbeløpMilitærHarKravPå(KonfigTjeneste.forYtelse(FagsakYtelseType.PLEIEPENGER_SYKT_BARN).getAntallGMilitærHarKravPå().intValue());
                return pleiepengerSyktBarnGrunnlag;
            case PLEIEPENGER_NÆRSTÅENDE:
                no.nav.folketrygdloven.kalkulus.beregning.v1.PleiepengerNærståendeGrunnlag ppnYtelsesGrunnlag = (no.nav.folketrygdloven.kalkulus.beregning.v1.PleiepengerNærståendeGrunnlag) ytelsespesifiktGrunnlag;
                validerForMidlertidigInaktivTypeA(ppnYtelsesGrunnlag.getUtbetalingsgradPrAktivitet());
                var pleiepengerNærståendeGrunnlag = new PleiepengerNærståendeGrunnlag(
                        UtbetalingsgradMapper.mapUtbetalingsgrad(ppnYtelsesGrunnlag.getUtbetalingsgradPrAktivitet()));
                pleiepengerNærståendeGrunnlag
                        .setGrunnbeløpMilitærHarKravPå(KonfigTjeneste.forYtelse(FagsakYtelseType.PLEIEPENGER_NÆRSTÅENDE).getAntallGMilitærHarKravPå().intValue());
                return pleiepengerNærståendeGrunnlag;
            case FRISINN:
                no.nav.folketrygdloven.kalkulus.beregning.v1.FrisinnGrunnlag frisinnGrunnlag = (no.nav.folketrygdloven.kalkulus.beregning.v1.FrisinnGrunnlag) ytelsespesifiktGrunnlag;
                List<FrisinnPeriode> frisinnPerioder = mapFraKontrakt(frisinnGrunnlag, iayGrunnlag.getOppgittOpptjening());
                return new FrisinnGrunnlag(UtbetalingsgradMapperFRISINN.map(iayGrunnlag, beregningsgrunnlagGrunnlagEntitet, frisinnPerioder),
                        frisinnPerioder, frisinnGrunnlag.getFrisinnBehandlingType() == null ? FrisinnBehandlingType.NY_SØKNADSPERIODE
                        : FrisinnBehandlingType.fraKode(frisinnGrunnlag.getFrisinnBehandlingType().getKode()));
            case OMSORGSPENGER:
                OmsorgspengerGrunnlag omsorgspengerGrunnlag = (OmsorgspengerGrunnlag) ytelsespesifiktGrunnlag;
                validerForMidlertidigInaktivTypeA(omsorgspengerGrunnlag.getUtbetalingsgradPrAktivitet());
                return mapOmsorgspengegrunnlag(omsorgspengerGrunnlag);
            default:
                return new StandardGrunnlag();
        }
    }

    public static void validerForMidlertidigInaktivTypeA(List<UtbetalingsgradPrAktivitetDto> utbPrAktList) {
        boolean innholderMinstEnInaktivStatus = utbPrAktList.stream().anyMatch(matchMedAktivitetstatusInaktiv());
        boolean innholderKunInaktivStatus = utbPrAktList.stream().allMatch(matchMedAktivitetstatusInaktiv());

        if (innholderMinstEnInaktivStatus && !innholderKunInaktivStatus) {
            throw new IllegalArgumentException("Det skal ikke være mulig å ha status INAKTIV med i kombinasjon med andre aktiviteter: " + utbPrAktList);
        }
    }

    private static Predicate<UtbetalingsgradPrAktivitetDto> matchMedAktivitetstatusInaktiv() {
        return dto -> dto.getUtbetalingsgradArbeidsforholdDto().getUttakArbeidType().getKode().equals(AktivitetStatusV2.IN.getBeskrivelse());
    }

    private static List<Ytelsegrunnlag> mapYtelsegrunnlag(List<no.nav.folketrygdloven.kalkulus.beregning.v1.besteberegning.Ytelsegrunnlag> ytelsegrunnlagForBesteberegning) {
        if (ytelsegrunnlagForBesteberegning == null) {
            return Collections.emptyList();
        }
        return ytelsegrunnlagForBesteberegning.stream()
                .map(yg -> new Ytelsegrunnlag(FagsakYtelseType.fraKode(yg.getYtelse().getKode()), mapYtelseperioder(yg.getPerioder())))
                .collect(Collectors.toList());
    }

    private static List<Ytelseperiode> mapYtelseperioder(List<no.nav.folketrygdloven.kalkulus.beregning.v1.besteberegning.Ytelseperiode> perioder) {
        return perioder.stream()
                .map(p -> new Ytelseperiode(mapPeriode(p.getPeriode()), mapYtelseandeler(p.getAndeler())))
                .collect(Collectors.toList());
    }

    private static Intervall mapPeriode(Periode periode) {
        return Intervall.fraOgMedTilOgMed(periode.getFom(), periode.getTom());
    }

    private static List<Ytelseandel> mapYtelseandeler(List<no.nav.folketrygdloven.kalkulus.beregning.v1.besteberegning.Ytelseandel> andeler) {
        return andeler.stream()
                .map(a -> {
                    if (a.getAktivitetStatus() != null) {
                        return new Ytelseandel(a.getAktivitetStatus(), a.getDagsats());
                    } else {
                        return new Ytelseandel(a.getArbeidskategori(), a.getDagsats());
                    }
                })
                .collect(Collectors.toList());
    }

    private static List<FrisinnPeriode> mapFraKontrakt(no.nav.folketrygdloven.kalkulus.beregning.v1.FrisinnGrunnlag frisinnGrunnlag,
                                                       Optional<OppgittOpptjeningDto> oppgittOpptjening) {
        // FIXME (Er tilfellet ved gamle saker på gamel kontrakt)
        if (frisinnGrunnlag.getPerioderMedSøkerInfo() == null) {
            return MapGammeltFrisinngrunnlag.map(frisinnGrunnlag, oppgittOpptjening);
        }

        return frisinnGrunnlag.getPerioderMedSøkerInfo().stream()
                .map(p -> new FrisinnPeriode(mapPeriode(p.getPeriode()), p.getSøkerFrilansIPeriode(),
                        p.getSøkerNæringIPeriode()))
                .collect(Collectors.toList());
    }

    public static AktivitetGradering mapFraDto(AktivitetGraderingDto aktivitetGradering) {
        List<AndelGradering> res = new ArrayList<>();
        aktivitetGradering.getAndelGraderingDto().forEach(andel -> {
            Builder builder = AndelGradering.builder();
            andel.getGraderinger()
                    .forEach(grad -> builder.medGradering(grad.getPeriode().getFom(), grad.getPeriode().getTom(), grad.getArbeidstidProsent().intValue()));
            builder.medStatus(AktivitetStatus.fraKode(andel.getAktivitetStatus().getKode()));
            builder.medArbeidsgiver(mapArbeidsgiver(andel.getArbeidsgiver()));
            no.nav.folketrygdloven.kalkulus.felles.v1.InternArbeidsforholdRefDto arbeidsforholdRef = andel.getArbeidsforholdRef();
            if (arbeidsforholdRef != null) {
                builder.medArbeidsforholdRef(InternArbeidsforholdRefDto.ref(arbeidsforholdRef.getAbakusReferanse()));
            }
            res.add(builder.build());
        });

        return new AktivitetGradering(res);
    }

    public static no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktiviteterDto mapFraDto(OpptjeningAktiviteterDto opptjeningAktiviteter) {
        return new no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktiviteterDto(
                opptjeningAktiviteter.getPerioder().stream()
                        .map(opptjeningPeriodeDto -> no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktiviteterDto.nyPeriode(
                                OpptjeningAktivitetType.fraKode(opptjeningPeriodeDto.getOpptjeningAktivitetType().getKode()),
                                mapPeriode(opptjeningPeriodeDto.getPeriode()),
                                opptjeningPeriodeDto.getArbeidsgiver() != null && opptjeningPeriodeDto.getArbeidsgiver().getErOrganisasjon()
                                        ? opptjeningPeriodeDto.getArbeidsgiver().getIdent()
                                        : null,
                                opptjeningPeriodeDto.getArbeidsgiver() != null && opptjeningPeriodeDto.getArbeidsgiver().getErPerson()
                                        ? opptjeningPeriodeDto.getArbeidsgiver().getIdent()
                                        : null,
                                opptjeningPeriodeDto.getAbakusReferanse() != null
                                        ? InternArbeidsforholdRefDto.ref(opptjeningPeriodeDto.getAbakusReferanse().getAbakusReferanse())
                                        : null))
                        .collect(Collectors.toList()), opptjeningAktiviteter.getMidlertidigInaktivType() != null ? MidlertidigInaktivType.valueOf(opptjeningAktiviteter.getMidlertidigInaktivType().name()) : null);
    }

    private static no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto mapFraDto(InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        return new MapIAYTilKalulator(iayGrunnlag).mapGrunnlag(iayGrunnlag);
    }

}
