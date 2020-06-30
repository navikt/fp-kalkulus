package no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl;


import java.util.List;
import java.util.Objects;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;

public class MapBeregningsgrunnlagFraRegelTilVLFordel extends MapBeregningsgrunnlagFraRegelTilVL {

    public BeregningsgrunnlagDto map(List<no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode> resultatPerioder, List<RegelResultat> regelResultater,
                                                     BeregningsgrunnlagDto eksisterendeVLGrunnlag) {
        Objects.requireNonNull(resultatPerioder, "resultatPerioder");
        Objects.requireNonNull(regelResultater, "regelResultater");
        if (resultatPerioder.size() != regelResultater.size()) {
            throw new IllegalArgumentException("Antall beregningsresultatperioder ("
                + resultatPerioder.size()
                + ") må være samme som antall regelresultater ("
                + regelResultater.size() + ")");
        }
        mapPerioder(regelResultater, eksisterendeVLGrunnlag, Steg.FORDEL, resultatPerioder);
        return eksisterendeVLGrunnlag;
    }

    @Override
    protected void mapAndelMedArbeidsforhold(BeregningsgrunnlagPeriodeDto mappetPeriode, BeregningsgrunnlagPrStatus regelAndel) {
        for (BeregningsgrunnlagPrArbeidsforhold regelAndelForArbeidsforhold : regelAndel.getArbeidsforhold()) {
            if (regelAndelForArbeidsforhold.getAndelNr() == null) {
                mapNyAndelForArbeidsforhold(mappetPeriode, regelAndelForArbeidsforhold);
            } else {
                mapEksisterendeAndelForArbeidsforhold(mappetPeriode, regelAndel, regelAndelForArbeidsforhold);
            }
        }
    }

    private static void mapNyAndelForArbeidsforhold(BeregningsgrunnlagPeriodeDto mappetPeriode, BeregningsgrunnlagPrArbeidsforhold regelAndelForArbeidsforhold) {
        var andelOpt = mappetPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(bgpsa -> bgpsa.getBgAndelArbeidsforhold().isPresent())
                .filter(bgpsa -> bgpsa.getBgAndelArbeidsforhold().get().getArbeidsgiver().getIdentifikator().equals(regelAndelForArbeidsforhold.getArbeidsgiverId()))
                .filter(bgpsa -> bgpsa.getBgAndelArbeidsforhold().get().getArbeidsforholdRef().gjelderFor(InternArbeidsforholdRefDto.ref(regelAndelForArbeidsforhold.getArbeidsforhold().getArbeidsforholdId())))
                .findFirst();

        if (andelOpt.isPresent()) {
            BeregningsgrunnlagPrStatusOgAndelDto.Builder nyAndelBuilder = BeregningsgrunnlagPrStatusOgAndelDto.ny()
                    .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER);
            if (andelOpt.get().getBeregningsperiodeFom() != null) {
                nyAndelBuilder.medBeregningsperiode(andelOpt.get().getBeregningsperiodeFom(), andelOpt.get().getBeregningsperiodeTom());
            }
            settFasteVerdier(nyAndelBuilder, regelAndelForArbeidsforhold);
            nyAndelBuilder.medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder(andelOpt.get().getBgAndelArbeidsforhold().get())
            .medNaturalytelseBortfaltPrÅr(null)
            .medNaturalytelseTilkommetPrÅr(null)
            .medRefusjonskravPrÅr(regelAndelForArbeidsforhold.getRefusjonskravPrÅr().orElse(null)))
                    .build(mappetPeriode);
        }
    }

}
