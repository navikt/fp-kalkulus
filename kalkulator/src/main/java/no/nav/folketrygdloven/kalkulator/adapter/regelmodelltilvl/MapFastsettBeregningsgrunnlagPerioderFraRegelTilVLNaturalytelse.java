package no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.EksisterendeAndel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SplittetPeriode;
import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.kodeverk.MapPeriodeÅrsakFraRegelTilVL;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagRegelType;

@ApplicationScoped
public class MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLNaturalytelse extends MapFastsettBeregningsgrunnlagPerioderFraRegelTilVL {

    @Override
    protected void mapAndeler(BeregningsgrunnlagDto nyttBeregningsgrunnlag, SplittetPeriode splittetPeriode, List<BeregningsgrunnlagPrStatusOgAndelDto> andelListe, BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode) {
        andelListe.forEach(eksisterendeAndel -> mapEksisterendeAndel(splittetPeriode, beregningsgrunnlagPeriode, eksisterendeAndel));
    }

    @Override
    protected BeregningsgrunnlagRegelType getRegelType() {
        return BeregningsgrunnlagRegelType.PERIODISERING_NATURALYTELSE;
    }

    @Override
    protected void mapSplittetPeriode(BeregningsgrunnlagDto nyttBeregningsgrunnlag,
                                      SplittetPeriode splittetPeriode,
                                      BeregningsgrunnlagDto beregningsgrunnlag) {
        LocalDate periodeTom = utledPeriodeTom(splittetPeriode);

        var originalPeriode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().stream()
            .filter(p -> p.getPeriode().inkluderer(splittetPeriode.getPeriode().getFom()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Ingen matchende perioder"));
        var andelListe = originalPeriode.getBeregningsgrunnlagPrStatusOgAndelList();
        var bgPeriodeBuilder = BeregningsgrunnlagPeriodeDto.builder()
            .medBeregningsgrunnlagPeriode(splittetPeriode.getPeriode().getFom(), periodeTom);
        splittetPeriode.getPeriodeÅrsaker().stream()
            .map(MapPeriodeÅrsakFraRegelTilVL::map)
            .forEach(bgPeriodeBuilder::leggTilPeriodeÅrsak);
        var beregningsgrunnlagPeriode = bgPeriodeBuilder.build(nyttBeregningsgrunnlag);
        mapAndeler(nyttBeregningsgrunnlag, splittetPeriode, andelListe, beregningsgrunnlagPeriode);
    }

    private void mapEksisterendeAndel(SplittetPeriode splittetPeriode, BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode, BeregningsgrunnlagPrStatusOgAndelDto eksisterendeAndel) {
        BeregningsgrunnlagPrStatusOgAndelDto.Builder andelBuilder = BeregningsgrunnlagPrStatusOgAndelDto.kopier(eksisterendeAndel);

        Optional<EksisterendeAndel> regelMatchOpt = splittetPeriode.getEksisterendePeriodeAndeler().stream()
            .filter(andel -> andel.getAndelNr().equals(eksisterendeAndel.getAndelsnr()))
            .findFirst();
        regelMatchOpt.ifPresent(regelAndel -> {
            BGAndelArbeidsforholdDto.Builder andelArbeidsforholdBuilder = andelBuilder.getBgAndelArbeidsforholdDtoBuilder()
                .medNaturalytelseBortfaltPrÅr(regelAndel.getNaturalytelseBortfaltPrÅr().orElse(null))
                .medNaturalytelseTilkommetPrÅr(regelAndel.getNaturalytelseTilkommetPrÅr().orElse(null));
            andelBuilder.medBGAndelArbeidsforhold(andelArbeidsforholdBuilder);
        });
        andelBuilder
            .build(beregningsgrunnlagPeriode);
    }

}
