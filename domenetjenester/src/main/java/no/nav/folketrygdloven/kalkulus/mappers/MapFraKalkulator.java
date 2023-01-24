package no.nav.folketrygdloven.kalkulus.mappers;

import static no.nav.folketrygdloven.kalkulus.mappers.ForeldrepengerGrunnlagMapper.mapForeldrepengerGrunnlag;
import static no.nav.folketrygdloven.kalkulus.mappers.FrisinnGrunnlagMapper.mapFrisinnGrunnlag;
import static no.nav.folketrygdloven.kalkulus.mappers.MapIAYTilKalulator.mapArbeidsforholdRef;
import static no.nav.folketrygdloven.kalkulus.mappers.OmsorgspengeGrunnlagMapper.mapOmsorgspengegrunnlag;
import static no.nav.folketrygdloven.kalkulus.mappers.PleiepengerNærståendeGrunnlagMapper.mapPleiepengerNærståendeGrunnlag;
import static no.nav.folketrygdloven.kalkulus.mappers.PleiepengerSyktBarnGrunnlagMapper.mapPleiepengerSyktBarnGrunnlag;
import static no.nav.folketrygdloven.kalkulus.mappers.SvangerskapspengerGrunnlagMapper.mapSvangerskapspengerGrunnlag;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.AktivitetStatusV2;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.StandardGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.behandling.Skjæringstidspunkt;
import no.nav.folketrygdloven.kalkulator.modell.iay.KravperioderPrArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.PerioderForKravDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.RefusjonsperiodeDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.beregningsperiode.BeregningsperiodeTjeneste;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.beregning.v1.KravperioderPrArbeidsforhold;
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
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseTyperKalkulusStøtterKontrakt;
import no.nav.folketrygdloven.kalkulus.mapFraEntitet.BehandlingslagerTilKalkulusMapper;
import no.nav.folketrygdloven.kalkulus.opptjening.v1.MidlertidigInaktivType;
import no.nav.folketrygdloven.kalkulus.opptjening.v1.OpptjeningAktiviteterDto;
import no.nav.folketrygdloven.kalkulus.typer.AktørId;

public class MapFraKalkulator {

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
                .medFørsteUttaksdato(skjæringstidspunkt)
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
        utenGrunnbeløp.leggTilToggle(TOGGLE_TSF_1715, false);
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
            // For å kunne mappe kall for å hente gui-dto for gamle saker
            kravPrArbeidsforhold = LagKravperioder.lagKravperioderPrArbeidsforhold(refusjonskravDatoer, iayGrunnlag, stp);
        }
        ;
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
        var ytelsespesifiktGrunnlag = input.getYtelsespesifiktGrunnlag();
        return switch (ytelseType) {
            case FORELDREPENGER -> mapForeldrepengerGrunnlag(ytelsespesifiktGrunnlag);
            case SVANGERSKAPSPENGER -> mapSvangerskapspengerGrunnlag((no.nav.folketrygdloven.kalkulus.beregning.v1.SvangerskapspengerGrunnlag) ytelsespesifiktGrunnlag);
            case PLEIEPENGER_SYKT_BARN -> mapPleiepengerSyktBarnGrunnlag((no.nav.folketrygdloven.kalkulus.beregning.v1.PleiepengerSyktBarnGrunnlag) ytelsespesifiktGrunnlag);
            case OPPLÆRINGSPENGER -> OpplæringspengerGrunnlagMapper.mapGrunnlag((no.nav.folketrygdloven.kalkulus.beregning.v1.OpplæringspengerGrunnlag) ytelsespesifiktGrunnlag);
            case PLEIEPENGER_NÆRSTÅENDE -> mapPleiepengerNærståendeGrunnlag((no.nav.folketrygdloven.kalkulus.beregning.v1.PleiepengerNærståendeGrunnlag) ytelsespesifiktGrunnlag);
            case FRISINN -> mapFrisinnGrunnlag(iayGrunnlag, beregningsgrunnlagGrunnlagEntitet, (no.nav.folketrygdloven.kalkulus.beregning.v1.FrisinnGrunnlag) ytelsespesifiktGrunnlag);
            case OMSORGSPENGER -> mapOmsorgspengegrunnlag((no.nav.folketrygdloven.kalkulus.beregning.v1.OmsorgspengerGrunnlag) ytelsespesifiktGrunnlag);
            default -> new StandardGrunnlag();
        };
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

    public static no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktiviteterDto mapFraDto(OpptjeningAktiviteterDto opptjeningAktiviteter) {
        return new no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktiviteterDto(
                opptjeningAktiviteter.getPerioder().stream()
                        .map(opptjeningPeriodeDto -> no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktiviteterDto.nyPeriode(
                                opptjeningPeriodeDto.getOpptjeningAktivitetType(),
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
        return MapIAYTilKalulator.mapGrunnlag(iayGrunnlag);
    }

    private static Intervall mapPeriode(Periode periode) {
        return Intervall.fraOgMedTilOgMed(periode.getFom(), periode.getTom());
    }

}
