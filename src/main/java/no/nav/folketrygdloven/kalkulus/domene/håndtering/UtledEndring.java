package no.nav.folketrygdloven.kalkulus.domene.håndtering;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.refusjon.FordelingGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.refusjon.Refusjonsperiode;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulus.domene.håndtering.faktaberegning.UtledFaktaOmBeregningVurderinger;
import no.nav.folketrygdloven.kalkulus.domene.håndtering.foreslå.UtledVarigEndringEllerNyoppstartetVurderinger;
import no.nav.folketrygdloven.kalkulus.kodeverk.RefusjonKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.RefusjonUtfallÅrsak;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.håndtering.HåndterBeregningDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.håndtering.fakta.FaktaOmBeregningHåndteringDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.håndtering.foreslå.VurderVarigEndringEllerNyoppstartetSNHåndteringDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.håndtering.overstyring.OverstyrRefusjonHåndteringDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.håndtering.refusjon.RefusjonOverstyringAndel;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.håndtering.refusjon.RefusjonOverstyringAndel.RefusjonOverstyringPeriode;
import no.nav.foreldrepenger.kalkulus.kontrakt.response.håndtering.BeregningsgrunnlagEndring;
import no.nav.foreldrepenger.kalkulus.kontrakt.response.håndtering.BeregningsgrunnlagPeriodeEndring;
import no.nav.foreldrepenger.kalkulus.kontrakt.response.håndtering.Endringer;
import no.nav.foreldrepenger.kalkulus.kontrakt.response.håndtering.FaktaOmBeregningVurderinger;
import no.nav.foreldrepenger.kalkulus.kontrakt.response.håndtering.OverstyrtRefusjonEndringDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.response.håndtering.RefusjonOverstyrtAndelEndringDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.response.håndtering.VarigEndretEllerNyoppstartetNæringEndring;

public class UtledEndring {

    public static UtledEndring standard() {
        return new UtledEndring();
    }

    public Endringer utled(BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlagDto,
                           BeregningsgrunnlagGrunnlagDto grunnlagFraSteg,
                           Optional<BeregningsgrunnlagGrunnlagDto> forrigeGrunnlag,
                           HåndterBeregningDto dto,
                           InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        var endringBuilder = Endringer.ny();
        BeregningsgrunnlagDto beregningsgrunnlagDto = beregningsgrunnlagGrunnlagDto.getBeregningsgrunnlagHvisFinnes()
                .orElseThrow(() -> new IllegalArgumentException("Skal ha beregningsgrunnlag her"));
        BeregningsgrunnlagDto bgFraSteg = grunnlagFraSteg.getBeregningsgrunnlagHvisFinnes()
                .orElseThrow(() -> new IllegalArgumentException("Skal ha beregningsgrunnlag her"));
        Optional<BeregningsgrunnlagDto> forrigeBeregningsgrunnlagOpt = forrigeGrunnlag.flatMap(BeregningsgrunnlagGrunnlagDto::getBeregningsgrunnlagHvisFinnes);
        BeregningsgrunnlagEndring beregningsgrunnlagEndring = utledBeregningsgrunnlagEndring(beregningsgrunnlagDto, bgFraSteg, forrigeBeregningsgrunnlagOpt);
        endringBuilder.medBeregningsgrunnlagEndring(beregningsgrunnlagEndring);
        UtledEndringIAktiviteter.utedEndring(dto, beregningsgrunnlagGrunnlagDto.getRegisterAktiviteter(),
                        beregningsgrunnlagGrunnlagDto.getGjeldendeAktiviteter(),
                        forrigeGrunnlag.map(BeregningsgrunnlagGrunnlagDto::getRegisterAktiviteter),
                        forrigeGrunnlag.map(BeregningsgrunnlagGrunnlagDto::getGjeldendeAktiviteter))
                .ifPresent(endringBuilder::medBeregningAktiviteterEndring);
        mapFaktaOmBeregningEndring(beregningsgrunnlagGrunnlagDto, forrigeGrunnlag, dto)
                .ifPresent(endringBuilder::medFaktaOmBeregningVurderinger);
        mapVarigEndretNæringEndring(forrigeGrunnlag, dto, beregningsgrunnlagDto, iayGrunnlag)
                .ifPresent(endringBuilder::medVarigEndretNæringEndring);
        return endringBuilder.build();
    }

    public Endringer utled(FordelingGrunnlag forrigeGrunnlag, HåndterBeregningDto dto) {
        var endringBuilder = Endringer.ny();
        mapOverstyrtRefusjonEndringDto(forrigeGrunnlag, dto).ifPresent(endringBuilder::medOverstyrtRefusjonEndringDto);
        return endringBuilder.build();
    }



    private static Optional<VarigEndretEllerNyoppstartetNæringEndring> mapVarigEndretNæringEndring(Optional<BeregningsgrunnlagGrunnlagDto> forrigeGrunnlag, HåndterBeregningDto dto,
                                                                                                   BeregningsgrunnlagDto beregningsgrunnlagDto, InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        if (dto instanceof VurderVarigEndringEllerNyoppstartetSNHåndteringDto) {
            return Optional.of(UtledVarigEndringEllerNyoppstartetVurderinger.utledForVarigEndretEllerNyoppstartetNæring(
                    beregningsgrunnlagDto,
                    forrigeGrunnlag.flatMap(BeregningsgrunnlagGrunnlagDto::getBeregningsgrunnlagHvisFinnes),
                    iayGrunnlag
            ));
        }
        return Optional.empty();
    }

    private static Optional<FaktaOmBeregningVurderinger> mapFaktaOmBeregningEndring(BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlagDto, Optional<BeregningsgrunnlagGrunnlagDto> forrigeGrunnlag, HåndterBeregningDto dto) {
        if (dto instanceof FaktaOmBeregningHåndteringDto faktaOmBeregningHåndteringDto) {
            return Optional.ofNullable(UtledFaktaOmBeregningVurderinger.utled(
                    faktaOmBeregningHåndteringDto,
                    beregningsgrunnlagGrunnlagDto.getFaktaAggregat(),
                    forrigeGrunnlag.flatMap(BeregningsgrunnlagGrunnlagDto::getFaktaAggregat)));
        }
        return Optional.empty();
    }

    private static Optional<OverstyrtRefusjonEndringDto> mapOverstyrtRefusjonEndringDto(FordelingGrunnlag forrigeGrunnlag,
                                                                                        HåndterBeregningDto dto) {
        if (dto instanceof OverstyrRefusjonHåndteringDto overstyrRefusjonHåndteringDto) {
           var endredeAndeler = overstyrRefusjonHåndteringDto.getOverstyrteAndeler().stream()
                .filter(o -> o.kilde().equals(RefusjonKilde.SAKSBEHANDLER))
                .filter(o -> erRefusjonsAndelEndret(o, forrigeGrunnlag))
               .map(o -> new RefusjonOverstyrtAndelEndringDto(o.arbeidsgiverIdent(), o.arbeidsforholdId(),
                   mapPerioder(o.refusjonsperioder())))
               .toList();
            if (!endredeAndeler.isEmpty()) {
                return Optional.of(new OverstyrtRefusjonEndringDto(endredeAndeler));
            }
            return Optional.empty();
        }
        throw new IllegalStateException("Ukjent HåndterBeregningDto type: " + dto.getClass());
    }

    private static List<RefusjonOverstyrtAndelEndringDto.RefusjonOverstyrtPeriodeDto> mapPerioder(List<RefusjonOverstyringPeriode> perioder) {
        return perioder.stream()
                .map(p -> new RefusjonOverstyrtAndelEndringDto.RefusjonOverstyrtPeriodeDto(
                        p.fom(),
                        p.tom(),
                        p.refusjonPrMnd(),
                        p.utfallÅrsak()))
                .toList();
    }

    private static boolean erRefusjonsAndelEndret(RefusjonOverstyringAndel o, FordelingGrunnlag forrigeGrunnlag) {
        // Matcher også på kilde: samme arbeidsforhold kan ha både en INNTEKTSMELDING- og en SAKSBEHANDLER-andel
        var refusjonsAndelForrigeGrunnlag = forrigeGrunnlag.refusjonsandeler().stream()
            .filter(refusjonsandel -> refusjonsandel.kilde() == o.kilde())
            .filter(refusjonsandel -> refusjonsandel.arbeidsgiver().getIdentifikator().equals(o.arbeidsgiverIdent()))
            .filter(refusjonsandel -> Objects.equals(refusjonsandel.arbeidsforholdRef().getReferanse(), o.arbeidsforholdId()))
            .findFirst();
        return refusjonsAndelForrigeGrunnlag
            .map(forrigeAndel -> !harLikeRefusjonsperioder(o.refusjonsperioder(), forrigeAndel.refusjonsperioder()))
            .orElse(true);
    }

    private static boolean harLikeRefusjonsperioder(List<RefusjonOverstyringPeriode> nyePerioder, List<Refusjonsperiode> forrigePerioder) {
        var nye = nyePerioder.stream()
            .map(p -> new SammenlignbarPeriode(LocalDate.parse(p.fom()), LocalDate.parse(p.tom()), Beløp.fra(p.refusjonPrMnd()), p.utfallÅrsak()))
            .sorted(Comparator.comparing(SammenlignbarPeriode::fom))
            .toList();
        var forrige = forrigePerioder.stream()
            .map(p -> new SammenlignbarPeriode(p.periode().getFomDato(), p.periode().getTomDato(), p.refusjonPrMnd(), p.utfallÅrsak()))
            .sorted(Comparator.comparing(SammenlignbarPeriode::fom))
            .toList();
        return nye.equals(forrige);
    }

    private record SammenlignbarPeriode(LocalDate fom, LocalDate tom, Beløp refusjonPrMnd, RefusjonUtfallÅrsak utfallÅrsak) {
    }

    private BeregningsgrunnlagEndring utledBeregningsgrunnlagEndring(BeregningsgrunnlagDto beregningsgrunnlagEntitet, BeregningsgrunnlagDto bgFraSteg, Optional<BeregningsgrunnlagDto> forrigeBeregningsgrunnlagOpt) {
        List<BeregningsgrunnlagPeriodeDto> perioder = beregningsgrunnlagEntitet.getBeregningsgrunnlagPerioder();
        List<BeregningsgrunnlagPeriodeDto> perioderFraSteg = bgFraSteg.getBeregningsgrunnlagPerioder();
        List<BeregningsgrunnlagPeriodeDto> forrigePerioder = forrigeBeregningsgrunnlagOpt.map(BeregningsgrunnlagDto::getBeregningsgrunnlagPerioder).orElse(Collections.emptyList());
        List<BeregningsgrunnlagPeriodeEndring> beregningsgrunnlagPeriodeEndringer = utledPeriodeEndringer(perioder, perioderFraSteg, forrigePerioder);
        return beregningsgrunnlagPeriodeEndringer.isEmpty() ? null : new BeregningsgrunnlagEndring(beregningsgrunnlagPeriodeEndringer);
    }

    private List<BeregningsgrunnlagPeriodeEndring> utledPeriodeEndringer(List<BeregningsgrunnlagPeriodeDto> perioder, List<BeregningsgrunnlagPeriodeDto> perioderFraSteg, List<BeregningsgrunnlagPeriodeDto> forrigePerioder) {
        return perioder.stream()
                .map(p -> {
                    BeregningsgrunnlagPeriodeDto periodeFraSteg = finnPeriodeEksaktMatchMotFom(perioderFraSteg, p.getBeregningsgrunnlagPeriodeFom())
                            .orElseThrow(() -> new IllegalStateException("Skal ikke ha endring i periode fra steg. Fant ikke periode med fom " + p.getBeregningsgrunnlagPeriodeFom() + ", har " + perioderFraSteg.stream().map(BeregningsgrunnlagPeriodeDto::getBeregningsgrunnlagPeriodeFom).toList()));
                    Optional<BeregningsgrunnlagPeriodeDto> forrigePeriode = finnPeriodeEksaktMatchMotFom(forrigePerioder, p.getBeregningsgrunnlagPeriodeFom());
                    return UtledEndringIPeriode.utled(p, periodeFraSteg, forrigePeriode);
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    private List<BeregningsgrunnlagPeriodeEndring> utledPeriodeEndringerMedSplittingTillatt(List<BeregningsgrunnlagPeriodeDto> perioder, List<BeregningsgrunnlagPeriodeDto> perioderFraSteg, List<BeregningsgrunnlagPeriodeDto> forrigePerioder) {
        return perioder.stream()
                .map(p -> {
                    BeregningsgrunnlagPeriodeDto periodeFraSteg = finnPeriodeMedEvtSplitt(perioderFraSteg, p.getBeregningsgrunnlagPeriodeFom())
                            .orElseThrow(() -> new IllegalStateException("Fant ikke periode som overlapper med fom " + p.getBeregningsgrunnlagPeriodeFom() + ", har " + perioderFraSteg.stream().map(BeregningsgrunnlagPeriodeDto::getBeregningsgrunnlagPeriodeFom).toList()));
                    Optional<BeregningsgrunnlagPeriodeDto> forrigePeriode = finnPeriodeMedEvtSplitt(forrigePerioder, p.getBeregningsgrunnlagPeriodeFom());
                    return UtledEndringIPeriode.utled(p, periodeFraSteg, forrigePeriode);
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }


    private static Optional<BeregningsgrunnlagPeriodeDto> finnPeriodeEksaktMatchMotFom(List<BeregningsgrunnlagPeriodeDto> forrigePerioder, LocalDate beregningsgrunnlagPeriodeFom) {
        return forrigePerioder.stream().filter(p -> p.getBeregningsgrunnlagPeriodeFom().equals(beregningsgrunnlagPeriodeFom)).findFirst();
    }

    private static Optional<BeregningsgrunnlagPeriodeDto> finnPeriodeMedEvtSplitt(List<BeregningsgrunnlagPeriodeDto> perioder, LocalDate beregningsgrunnlagPeriodeFom) {
        return perioder.stream()
                .filter(p -> p.getPeriode().inkluderer(beregningsgrunnlagPeriodeFom))
                .reduce((a, b) -> {
                    throw new IllegalStateException("Fant flere enn 1 periode som overlappet med " + beregningsgrunnlagPeriodeFom);
                });
    }
}
