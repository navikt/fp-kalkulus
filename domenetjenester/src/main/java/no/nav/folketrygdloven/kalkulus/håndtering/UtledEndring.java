package no.nav.folketrygdloven.kalkulus.håndtering;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.håndtering.faktaberegning.UtledFaktaOmBeregningVurderinger;
import no.nav.folketrygdloven.kalkulus.håndtering.foreslå.UtledVarigEndringEllerNyoppstartetVurderinger;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.HåndterBeregningDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta.FaktaOmBeregningHåndteringDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.foreslå.VurderVarigEndretArbeidssituasjonHåndteringDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.foreslå.VurderVarigEndringEllerNyoppstartetSNHåndteringDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.BeregningsgrunnlagEndring;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.BeregningsgrunnlagPeriodeEndring;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.Endringer;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.FaktaOmBeregningVurderinger;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.VarigEndretArbeidssituasjonEndring;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.VarigEndretEllerNyoppstartetNæringEndring;

public class UtledEndring {

    private boolean tillatSplittPeriode;

    private UtledEndring(boolean tillatSplittPeriode) {
        this.tillatSplittPeriode = tillatSplittPeriode;
    }

    public static UtledEndring standard() {
        return new UtledEndring(false);
    }

    public static UtledEndring forTilkommetInntekt() {
        return new UtledEndring(true);
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
                .map(endringBuilder::medBeregningAktiviteterEndring);
        mapFaktaOmBeregningEndring(beregningsgrunnlagGrunnlagDto, forrigeGrunnlag, dto, endringBuilder)
                .map(endringBuilder::medFaktaOmBeregningVurderinger);
        mapVarigEndretNæringEndring(forrigeGrunnlag, dto, endringBuilder, beregningsgrunnlagDto, iayGrunnlag)
                .map(endringBuilder::medVarigEndretNæringEndring);
        mapVarigEndretArbeidssituasjonEndring(forrigeGrunnlag, dto, endringBuilder, beregningsgrunnlagDto, iayGrunnlag)
                .map(endringBuilder::medVarigEndretArbeidssituasjonEndring);
        return endringBuilder.build();
    }

    private static Optional<VarigEndretEllerNyoppstartetNæringEndring> mapVarigEndretNæringEndring(Optional<BeregningsgrunnlagGrunnlagDto> forrigeGrunnlag, HåndterBeregningDto dto, Endringer.Builder endringBuilder, BeregningsgrunnlagDto beregningsgrunnlagDto, InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        if (dto instanceof VurderVarigEndringEllerNyoppstartetSNHåndteringDto) {
            return Optional.of(UtledVarigEndringEllerNyoppstartetVurderinger.utledForVarigEndretEllerNyoppstartetNæring(
                    beregningsgrunnlagDto,
                    forrigeGrunnlag.flatMap(BeregningsgrunnlagGrunnlagDto::getBeregningsgrunnlagHvisFinnes),
                    iayGrunnlag
            ));
        }
        return Optional.empty();
    }

    private static Optional<VarigEndretArbeidssituasjonEndring> mapVarigEndretArbeidssituasjonEndring(Optional<BeregningsgrunnlagGrunnlagDto> forrigeGrunnlag, HåndterBeregningDto dto, Endringer.Builder endringBuilder, BeregningsgrunnlagDto beregningsgrunnlagDto, InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        if (dto instanceof VurderVarigEndretArbeidssituasjonHåndteringDto) {
            var endring = UtledVarigEndringEllerNyoppstartetVurderinger.utledEndring(
                    beregningsgrunnlagDto,
                    forrigeGrunnlag.flatMap(BeregningsgrunnlagGrunnlagDto::getBeregningsgrunnlagHvisFinnes),
                    UtledEndring::finnBrukersAndel);
            return Optional.of(new VarigEndretArbeidssituasjonEndring(endring));
        }
        return Optional.empty();
    }

    private static BeregningsgrunnlagPrStatusOgAndelDto finnBrukersAndel(BeregningsgrunnlagPeriodeDto bgPeriode) {
        return bgPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(a -> a.getAktivitetStatus().equals(AktivitetStatus.BRUKERS_ANDEL))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Forventet å finne brukersAndel"));
    }

    private static Optional<FaktaOmBeregningVurderinger> mapFaktaOmBeregningEndring(BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlagDto, Optional<BeregningsgrunnlagGrunnlagDto> forrigeGrunnlag, HåndterBeregningDto dto, Endringer.Builder endringBuilder) {
        if (dto instanceof FaktaOmBeregningHåndteringDto faktaOmBeregningHåndteringDto) {
            return Optional.ofNullable(UtledFaktaOmBeregningVurderinger.utled(
                    faktaOmBeregningHåndteringDto,
                    beregningsgrunnlagGrunnlagDto.getFaktaAggregat(),
                    forrigeGrunnlag.flatMap(BeregningsgrunnlagGrunnlagDto::getFaktaAggregat)));
        }
        return Optional.empty();
    }

    private BeregningsgrunnlagEndring utledBeregningsgrunnlagEndring(BeregningsgrunnlagDto beregningsgrunnlagEntitet, BeregningsgrunnlagDto bgFraSteg, Optional<BeregningsgrunnlagDto> forrigeBeregningsgrunnlagOpt) {
        List<BeregningsgrunnlagPeriodeDto> perioder = beregningsgrunnlagEntitet.getBeregningsgrunnlagPerioder();
        List<BeregningsgrunnlagPeriodeDto> perioderFraSteg = bgFraSteg.getBeregningsgrunnlagPerioder();
        List<BeregningsgrunnlagPeriodeDto> forrigePerioder = forrigeBeregningsgrunnlagOpt.map(BeregningsgrunnlagDto::getBeregningsgrunnlagPerioder).orElse(Collections.emptyList());
        List<BeregningsgrunnlagPeriodeEndring> beregningsgrunnlagPeriodeEndringer = tillatSplittPeriode
                ? utledPeriodeEndringerMedSplittingTillatt(perioder, perioderFraSteg, forrigePerioder)
                : utledPeriodeEndringerUtenSplittingTillatt(perioder, perioderFraSteg, forrigePerioder);
        return beregningsgrunnlagPeriodeEndringer.isEmpty() ? null : new BeregningsgrunnlagEndring(beregningsgrunnlagPeriodeEndringer);
    }

    private List<BeregningsgrunnlagPeriodeEndring> utledPeriodeEndringerUtenSplittingTillatt(List<BeregningsgrunnlagPeriodeDto> perioder, List<BeregningsgrunnlagPeriodeDto> perioderFraSteg, List<BeregningsgrunnlagPeriodeDto> forrigePerioder) {
        return perioder.stream()
                .map(p -> {
                    BeregningsgrunnlagPeriodeDto periodeFraSteg = finnPeriodeEksaktMatchMotFom(perioderFraSteg, p.getBeregningsgrunnlagPeriodeFom())
                            .orElseThrow(() -> new IllegalStateException("Skal ikke ha endring i periode fra steg. Fant ikke periode med fom " + p.getBeregningsgrunnlagPeriodeFom() + ", har " + perioderFraSteg.stream().map(BeregningsgrunnlagPeriodeDto::getBeregningsgrunnlagPeriodeFom).toList()));
                    Optional<BeregningsgrunnlagPeriodeDto> forrigePeriode = finnPeriodeEksaktMatchMotFom(forrigePerioder, p.getBeregningsgrunnlagPeriodeFom());
                    return UtledEndringIPeriode.utled(p, periodeFraSteg, forrigePeriode);
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
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
                .collect(Collectors.toList());
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
