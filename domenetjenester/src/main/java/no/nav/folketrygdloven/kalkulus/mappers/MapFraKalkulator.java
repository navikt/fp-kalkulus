package no.nav.folketrygdloven.kalkulus.mappers;

import static no.nav.folketrygdloven.kalkulus.mappers.ForeldrepengerGrunnlagMapper.mapForeldrepengerGrunnlag;
import static no.nav.folketrygdloven.kalkulus.mappers.MapIAYTilKalulator.mapArbeidsforholdRef;
import static no.nav.folketrygdloven.kalkulus.mappers.SvangerskapspengerGrunnlagMapper.mapSvangerskapspengerGrunnlag;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.guitjenester.ModellTyperMapper;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.behandling.Skjæringstidspunkt;
import no.nav.folketrygdloven.kalkulator.modell.iay.KravperioderPrArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.PerioderForKravDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.RefusjonsperiodeDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.beregning.v1.ForeldrepengerGrunnlag;
import no.nav.folketrygdloven.kalkulus.beregning.v1.KravperioderPrArbeidsforhold;
import no.nav.folketrygdloven.kalkulus.beregning.v1.PerioderForKrav;
import no.nav.folketrygdloven.kalkulus.beregning.v1.RefusjonskravDatoDto;
import no.nav.folketrygdloven.kalkulus.beregning.v1.Refusjonsperiode;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.felles.v1.Aktør;
import no.nav.folketrygdloven.kalkulus.felles.v1.KalkulatorInputDto;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.iay.v1.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.kodeverk.MidlertidigInaktivType;
import no.nav.folketrygdloven.kalkulus.mapFraEntitet.BehandlingslagerTilKalkulusMapper;
import no.nav.folketrygdloven.kalkulus.opptjening.v1.OpptjeningAktiviteterDto;
import no.nav.folketrygdloven.kalkulus.typer.AktørId;
import no.nav.foreldrepenger.konfig.Environment;

public class MapFraKalkulator {
    public static final String INNTEKT_RAPPORTERING_FRIST_DATO = "inntekt.rapportering.frist.dato";

    public static Arbeidsgiver mapArbeidsgiver(Aktør arbeidsgiver) {
        if (arbeidsgiver == null) {
            return null;
        }
        return arbeidsgiver.getErOrganisasjon() ? Arbeidsgiver.virksomhet(arbeidsgiver.getIdent()) : Arbeidsgiver.person(
            new AktørId(arbeidsgiver.getIdent()));

    }

    public static BeregningsgrunnlagInput mapFraKalkulatorInputTilBeregningsgrunnlagInput(KoblingEntitet kobling,
                                                                                          KalkulatorInputDto input,
                                                                                          Optional<BeregningsgrunnlagGrunnlagEntitet> beregningsgrunnlagGrunnlagEntitet) {
        var koblingId = kobling.getId();

        var ytelseType = kobling.getYtelseType();
        var aktørId = new no.nav.folketrygdloven.kalkulus.typer.AktørId(kobling.getAktørId().getId());
        var build = Skjæringstidspunkt.builder()
            .medFørsteUttaksdato(finnFørsteUttaksdato(input))
            .medSkjæringstidspunktOpptjening(input.getSkjæringstidspunkt())
            .build();

        var ref = KoblingReferanse.fra(ytelseType, aktørId, koblingId, kobling.getKoblingReferanse().getReferanse(), Optional.empty(), build);

        var iayGrunnlag = input.getIayGrunnlag();
        var opptjeningAktiviteter = input.getOpptjeningAktiviteter();
        var kravPrArbeidsforhold = input.getRefusjonskravPrArbeidsforhold();

        var iayGrunnlagMappet = mapIAYGrunnlag(iayGrunnlag);
        var stp = beregningsgrunnlagGrunnlagEntitet.flatMap(BeregningsgrunnlagGrunnlagEntitet::getBeregningsgrunnlag)
            .map(BeregningsgrunnlagEntitet::getSkjæringstidspunkt)
            .orElse(input.getSkjæringstidspunkt());
        BeregningsgrunnlagInput utenGrunnbeløp = new BeregningsgrunnlagInput(ref, iayGrunnlagMappet, mapOpptjeningsaktiviteter(opptjeningAktiviteter),
            mapKravperioder(kravPrArbeidsforhold, iayGrunnlag, stp),
            mapYtelsespesifiktGrunnlag(kobling.getYtelseType(), input, beregningsgrunnlagGrunnlagEntitet));

        utenGrunnbeløp.leggTilKonfigverdi(INNTEKT_RAPPORTERING_FRIST_DATO, 5);
        utenGrunnbeløp.leggTilToggle("aap.praksisendring", erAAPPraksisendringAktiv());
        return beregningsgrunnlagGrunnlagEntitet.map(BehandlingslagerTilKalkulusMapper::mapGrunnlag)
            .map(utenGrunnbeløp::medBeregningsgrunnlagGrunnlag)
            .orElse(utenGrunnbeløp);
    }

    private static LocalDate finnFørsteUttaksdato(KalkulatorInputDto input) {
        if (input.getYtelsespesifiktGrunnlag() instanceof ForeldrepengerGrunnlag fg) {
            return fg.getFørsteUttaksdato() == null ? input.getSkjæringstidspunkt() : fg.getFørsteUttaksdato();
        }
        return input.getSkjæringstidspunkt();
    }

    public static List<KravperioderPrArbeidsforholdDto> mapKravperioder(List<KravperioderPrArbeidsforhold> kravPrArbeidsforhold,
                                                                        InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                                                        LocalDate stp) {
        if (iayGrunnlag.getAlleInntektsmeldingerPåSak() != null && !iayGrunnlag.getAlleInntektsmeldingerPåSak().isEmpty()) {
            return MapInntektsmeldingerTilKravperioder.map(iayGrunnlag, stp);
        } else {
            return kravPrArbeidsforhold.stream().map(MapFraKalkulator::mapKravPeriode).toList();
        }
    }

    private static KravperioderPrArbeidsforholdDto mapKravPeriode(KravperioderPrArbeidsforhold kravperioderPrArbeidsforhold) {
        return new KravperioderPrArbeidsforholdDto(mapArbeidsgiver(kravperioderPrArbeidsforhold.getArbeidsgiver()),
            mapArbeidsforholdRef(kravperioderPrArbeidsforhold.getInternreferanse()),
            kravperioderPrArbeidsforhold.getAlleSøktePerioder().stream().map(MapFraKalkulator::mapSøktPeriode).toList(),
            kravperioderPrArbeidsforhold.getSisteSøktePerioder()
                .getRefusjonsperioder()
                .stream()
                .map(Refusjonsperiode::getPeriode)
                .map(MapFraKalkulator::mapPeriode)
                .toList());
    }

    private static PerioderForKravDto mapSøktPeriode(PerioderForKrav p) {
        return new PerioderForKravDto(p.getInnsendingsdato(), p.getRefusjonsperioder().stream().map(MapFraKalkulator::mapRefusjonsperiode).toList());
    }

    private static RefusjonsperiodeDto mapRefusjonsperiode(Refusjonsperiode rp) {
        return new RefusjonsperiodeDto(mapPeriode(rp.getPeriode()), ModellTyperMapper.beløpFraDto(rp.getBeløp()));
    }

    public static YtelsespesifiktGrunnlag mapYtelsespesifiktGrunnlag(FagsakYtelseType ytelseType,
                                                                     KalkulatorInputDto input,
                                                                     Optional<BeregningsgrunnlagGrunnlagEntitet> beregningsgrunnlagGrunnlagEntitet) {
        var ytelsespesifiktGrunnlag = input.getYtelsespesifiktGrunnlag();
        return switch (ytelseType) {
            case FORELDREPENGER ->
                mapForeldrepengerGrunnlag((no.nav.folketrygdloven.kalkulus.beregning.v1.ForeldrepengerGrunnlag) ytelsespesifiktGrunnlag,
                    beregningsgrunnlagGrunnlagEntitet);
            case SVANGERSKAPSPENGER ->
                mapSvangerskapspengerGrunnlag((no.nav.folketrygdloven.kalkulus.beregning.v1.SvangerskapspengerGrunnlag) ytelsespesifiktGrunnlag);
            default -> throw new IllegalStateException("Det er ikke definert ytelsespesifikt grunnlag for ytelse " + ytelseType);
        };
    }

    public static no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktiviteterDto mapOpptjeningsaktiviteter(OpptjeningAktiviteterDto opptjeningAktiviteter) {
        return new no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktiviteterDto(opptjeningAktiviteter.getPerioder()
            .stream()
            .map(opptjeningPeriodeDto -> no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktiviteterDto.nyPeriode(
                opptjeningPeriodeDto.getOpptjeningAktivitetType(), mapPeriode(opptjeningPeriodeDto.getPeriode()),
                opptjeningPeriodeDto.getArbeidsgiver() != null && opptjeningPeriodeDto.getArbeidsgiver()
                    .getErOrganisasjon() ? opptjeningPeriodeDto.getArbeidsgiver().getIdent() : null,
                opptjeningPeriodeDto.getArbeidsgiver() != null && opptjeningPeriodeDto.getArbeidsgiver()
                    .getErPerson() ? opptjeningPeriodeDto.getArbeidsgiver().getIdent() : null,
                opptjeningPeriodeDto.getAbakusReferanse() != null ? InternArbeidsforholdRefDto.ref(
                    opptjeningPeriodeDto.getAbakusReferanse().getAbakusReferanse()) : null))
            .toList(), opptjeningAktiviteter.getMidlertidigInaktivType() != null ? MidlertidigInaktivType.valueOf(
            opptjeningAktiviteter.getMidlertidigInaktivType().name()) : null);
    }

    private static no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto mapIAYGrunnlag(InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        return MapIAYTilKalulator.mapGrunnlag(iayGrunnlag);
    }

    private static Intervall mapPeriode(Periode periode) {
        return Intervall.fraOgMedTilOgMed(periode.getFom(), periode.getTom());
    }

    private static boolean erAAPPraksisendringAktiv() {
        if (Environment.current().isProd()) {
            return !LocalDate.now().isBefore(LocalDate.of(2025, 9, 1));
        } else {
            return true;
        }
    }
}
