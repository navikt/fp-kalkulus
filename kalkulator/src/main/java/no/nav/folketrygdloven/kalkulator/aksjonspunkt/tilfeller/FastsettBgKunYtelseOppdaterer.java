package no.nav.folketrygdloven.kalkulator.aksjonspunkt.tilfeller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.FaktaOmBeregningTilfelleRef;
import no.nav.folketrygdloven.kalkulator.felles.MatchBeregningsgrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulator.aksjonspunkt.dto.FaktaBeregningLagreDto;
import no.nav.folketrygdloven.kalkulator.aksjonspunkt.dto.FastsattBrukersAndel;
import no.nav.folketrygdloven.kalkulator.aksjonspunkt.dto.FastsettBgKunYtelseDto;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAktørDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.AndelKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;


@ApplicationScoped
@FaktaOmBeregningTilfelleRef("FASTSETT_BG_KUN_YTELSE")
public class FastsettBgKunYtelseOppdaterer implements FaktaOmBeregningTilfelleOppdaterer {

    private static final int MND_I_1_ÅR = 12;

    @Override
    public void oppdater(FaktaBeregningLagreDto dto,
                         Optional<BeregningsgrunnlagDto> forrigeBg,
                         BeregningsgrunnlagInput input, BeregningsgrunnlagGrunnlagDtoBuilder grunnlagBuilder) {
        FastsettBgKunYtelseDto kunYtelseDto = dto.getKunYtelseFordeling();
        BeregningsgrunnlagPeriodeDto periode = grunnlagBuilder.getBeregningsgrunnlagBuilder().getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0);
        List<FastsattBrukersAndel> andeler = kunYtelseDto.getAndeler();
        fjernAndeler(periode, andeler.stream().map(FastsattBrukersAndel::getAndelsnr).collect(Collectors.toList()));
        Boolean skalBrukeBesteberegning = kunYtelseDto.getSkalBrukeBesteberegning();
        for (FastsattBrukersAndel andel : andeler) {
            if (andel.getNyAndel()) {
                fastsettBeløpForNyAndel(periode, andel, kunYtelseDto.getSkalBrukeBesteberegning());
            } else {
                BeregningsgrunnlagPrStatusOgAndelDto korrektAndel = getKorrektAndel(periode, andel, forrigeBg);
                settInntektskategoriOgFastsattBeløp(andel, korrektAndel, periode, skalBrukeBesteberegning);
            }
        }

        // Setter fakta aggregat
        if (skalBrukeBesteberegning != null) {
            FaktaAggregatDto.Builder faktaBuilder = grunnlagBuilder.getFaktaAggregatBuilder();
            FaktaAktørDto.Builder faktaAktørBuilder = faktaBuilder.getFaktaAktørBuilder();
            faktaAktørBuilder.medSkalBesteberegnes(skalBrukeBesteberegning);
            faktaBuilder.medFaktaAktør(faktaAktørBuilder.build());
            grunnlagBuilder.medFaktaAggregat(faktaBuilder.build());
        }
    }


    private void fjernAndeler(BeregningsgrunnlagPeriodeDto periode, List<Long> andelsnrListe) {
        BeregningsgrunnlagPeriodeDto.builder(periode).fjernBeregningsgrunnlagPrStatusOgAndelerSomIkkeLiggerIListeAvAndelsnr(andelsnrListe);
    }


    private void settInntektskategoriOgFastsattBeløp(FastsattBrukersAndel andel, BeregningsgrunnlagPrStatusOgAndelDto korrektAndel,
                                                     BeregningsgrunnlagPeriodeDto periode, Boolean skalBrukeBesteberegning) {
        Inntektskategori inntektskategori = andel.getInntektskategori();
        BigDecimal fastsattBeløp = BigDecimal.valueOf(andel.getFastsattBeløp() * (long) MND_I_1_ÅR);
        if (andel.getNyAndel()) {
            BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(korrektAndel)
                    .medBeregnetPrÅr(fastsattBeløp)
                    .medBesteberegningPrÅr(Boolean.TRUE.equals(skalBrukeBesteberegning) ? fastsattBeløp : null)
                    .medInntektskategori(inntektskategori)
                    .medFastsattAvSaksbehandler(true)
                    .nyttAndelsnr(periode)
                    .medKilde(AndelKilde.SAKSBEHANDLER_KOFAKBER).build(periode);
        } else {
            Optional<BeregningsgrunnlagPrStatusOgAndelDto> matchetAndel = periode.getBeregningsgrunnlagPrStatusOgAndelList()
                    .stream().filter(bgAndel -> bgAndel.equals(korrektAndel)).findFirst();
            matchetAndel.ifPresentOrElse(endreEksisterende(skalBrukeBesteberegning, inntektskategori, fastsattBeløp),
                    leggTilFraForrige(korrektAndel, periode, skalBrukeBesteberegning, inntektskategori, fastsattBeløp)
            );
        }
    }

    private Runnable leggTilFraForrige(BeregningsgrunnlagPrStatusOgAndelDto korrektAndel, BeregningsgrunnlagPeriodeDto periode, Boolean skalBrukeBesteberegning, Inntektskategori inntektskategori, BigDecimal fastsattBeløp) {
        return () -> BeregningsgrunnlagPrStatusOgAndelDto.kopier(korrektAndel)
                .medBeregnetPrÅr(fastsattBeløp)
                .medBesteberegningPrÅr(Boolean.TRUE.equals(skalBrukeBesteberegning) ? fastsattBeløp : null)
                .medInntektskategori(inntektskategori)
                .medFastsattAvSaksbehandler(true).build(periode);
    }

    private Consumer<BeregningsgrunnlagPrStatusOgAndelDto> endreEksisterende(Boolean skalBrukeBesteberegning, Inntektskategori inntektskategori, BigDecimal fastsattBeløp) {
        return match -> BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(match)
                .medBeregnetPrÅr(fastsattBeløp)
                .medBesteberegningPrÅr(Boolean.TRUE.equals(skalBrukeBesteberegning) ? fastsattBeløp : null)
                .medInntektskategori(inntektskategori)
                .medFastsattAvSaksbehandler(true);
    }


    private void fastsettBeløpForNyAndel(BeregningsgrunnlagPeriodeDto periode,
                                         FastsattBrukersAndel andel, Boolean skalBrukeBesteberegning) {
        BigDecimal fastsatt = BigDecimal.valueOf(andel.getFastsattBeløp() * (long) MND_I_1_ÅR);// NOSONAR
        BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
                .medAktivitetStatus(AktivitetStatus.BRUKERS_ANDEL)
                .medInntektskategori(andel.getInntektskategori())
                .medBeregnetPrÅr(fastsatt)
                .medBesteberegningPrÅr(Boolean.TRUE.equals(skalBrukeBesteberegning) ? fastsatt : null)
                .medFastsattAvSaksbehandler(true)
                .medKilde(AndelKilde.SAKSBEHANDLER_KOFAKBER)
                .build(periode);
    }


    private BeregningsgrunnlagPrStatusOgAndelDto getKorrektAndel(BeregningsgrunnlagPeriodeDto periode, FastsattBrukersAndel andel, Optional<BeregningsgrunnlagDto> forrigeBg) {
        if (andel.getLagtTilAvSaksbehandler() && !andel.getNyAndel()) {
            return finnAndelFraForrigeGrunnlag(periode, andel, forrigeBg);
        }
        return periode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(a -> a.getAndelsnr().equals(andel.getAndelsnr()))
                .findFirst()
                .orElseThrow(() -> FastsettBGKunYtelseOppdatererFeil.FACTORY.finnerIkkeAndelFeil().toException());
    }

    private BeregningsgrunnlagPrStatusOgAndelDto finnAndelFraForrigeGrunnlag(BeregningsgrunnlagPeriodeDto periode, FastsattBrukersAndel andel, Optional<BeregningsgrunnlagDto> forrigeBg) {
        List<BeregningsgrunnlagPeriodeDto> matchendePerioder = forrigeBg.stream()
                .flatMap(bg -> bg.getBeregningsgrunnlagPerioder().stream())
                .filter(periodeIGjeldendeGrunnlag -> periodeIGjeldendeGrunnlag.getPeriode().overlapper(periode.getPeriode())).collect(Collectors.toList());
        if (matchendePerioder.size() != 1) {
            throw MatchBeregningsgrunnlagTjeneste.MatchBeregningsgrunnlagTjenesteFeil.FACTORY.fantFlereEnn1Periode().toException();
        }
        Optional<BeregningsgrunnlagPrStatusOgAndelDto> andelIForrigeGrunnlag = matchendePerioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(a -> a.getAndelsnr().equals(andel.getAndelsnr()))
                .findFirst();
        return andelIForrigeGrunnlag
                .orElseGet(() -> MatchBeregningsgrunnlagTjeneste
                        .matchMedAndelFraPeriode(periode, andel.getAndelsnr(), null));
    }
}
