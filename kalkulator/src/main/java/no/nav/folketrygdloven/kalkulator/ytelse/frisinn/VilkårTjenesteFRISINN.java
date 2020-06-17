package no.nav.folketrygdloven.kalkulator.ytelse.frisinn;

import java.time.LocalDate;
import java.time.Month;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import org.jetbrains.annotations.NotNull;

import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.FrisinnGrunnlag;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.FrisinnPeriode;
import no.nav.folketrygdloven.kalkulator.VilkårTjeneste;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningVilkårResultat;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.Vilkårsavslagsårsak;
import no.nav.folketrygdloven.kalkulus.felles.tid.AbstractIntervall;

@ApplicationScoped
@FagsakYtelseTypeRef("FRISINN")
public class VilkårTjenesteFRISINN extends VilkårTjeneste {

    @Override
    public BeregningVilkårResultat lagVilkårResultatFordel(BeregningsgrunnlagInput input, List<BeregningVilkårResultat> beregningVilkårResultatListe) {
        boolean erAvslått = erSisteSøknadsperiodeAvslått(input, beregningVilkårResultatListe);
        Intervall vilkårsperiode = Intervall.fraOgMedTilOgMed(input.getSkjæringstidspunktForBeregning(), AbstractIntervall.TIDENES_ENDE);
        if (erAvslått) {
            Optional<BeregningVilkårResultat> avslåttVilkår = beregningVilkårResultatListe.stream().filter(vr -> !vr.getErVilkårOppfylt()).findFirst();
            return avslåttVilkår
                    .map(beregningVilkårResultat -> new BeregningVilkårResultat(false, beregningVilkårResultat.getVilkårsavslagsårsak(), vilkårsperiode))
                    .orElseGet(() -> new BeregningVilkårResultat(true, vilkårsperiode));
        } else {
            return new BeregningVilkårResultat(true, vilkårsperiode);
        }
    }

    @Override
    public Optional<BeregningVilkårResultat> lagVilkårResultatFullføre(BeregningsgrunnlagInput input, BeregningsgrunnlagDto beregningsgrunnlagDto) {
        FrisinnGrunnlag frisinnGrunnlag = input.getYtelsespesifiktGrunnlag();
        Intervall sisteSøknadsperiode = FinnSøknadsperioder.finnSisteSøknadsperiode(input.getYtelsespesifiktGrunnlag());
        boolean harAvkortetHeleSistePeriode = beregningsgrunnlagDto.getBeregningsgrunnlagPerioder().stream()
                .filter(p -> p.getPeriode().overlapper(sisteSøknadsperiode))
                .allMatch(p -> harAvkortetGrunnetAnnenInntekt(frisinnGrunnlag, p));
        return harAvkortetHeleSistePeriode ? Optional.of(new BeregningVilkårResultat(false, Vilkårsavslagsårsak.AVKORTET_GRUNNET_ANNEN_INNTEKT, sisteSøknadsperiode)) :
                Optional.empty();
    }

    private Boolean erSisteSøknadsperiodeAvslått(BeregningsgrunnlagInput input, List<BeregningVilkårResultat> beregningVilkårResultatListe) {
        Intervall sisteSøknadsperiode = FinnSøknadsperioder.finnSisteSøknadsperiode(input.getYtelsespesifiktGrunnlag());
        return beregningVilkårResultatListe.stream()
                .filter(vp -> vp.getPeriode().overlapper(sisteSøknadsperiode))
                .noneMatch(BeregningVilkårResultat::getErVilkårOppfylt);
    }

    private boolean harAvkortetGrunnetAnnenInntekt(FrisinnGrunnlag frisinnGrunnlag, BeregningsgrunnlagPeriodeDto p) {
        LocalDate fom = p.getBeregningsgrunnlagPeriodeFom();
        boolean harSøktFrilans = frisinnGrunnlag.getSøkerYtelseForFrilans(fom);
        boolean harSøktNæring = frisinnGrunnlag.getSøkerYtelseForNæring(fom);
        boolean harAvkortetSøktNæring = harAvkortetSøktNæring(p, harSøktNæring);
        boolean harAvkortetSøktFrilans = harAvkortetSøktFrilans(harSøktFrilans, p);
        if ((harSøktFrilans && harAvkortetSøktFrilans) && !harSøktNæring) {
            return true;
        }
        if ((harSøktNæring && harAvkortetSøktNæring) && !harSøktFrilans) {
            return true;
        }
        if (harAvkortetSøktFrilans && harAvkortetSøktNæring) {
            return true;
        }
        return false;
    }

    private boolean harAvkortetSøktNæring(BeregningsgrunnlagPeriodeDto periode, boolean harSøktNæring) {
        return harSøktNæring && harIkkeUtbetalingForNæring(periode);
    }

    private boolean harAvkortetSøktFrilans(boolean harSøktFrilans, BeregningsgrunnlagPeriodeDto periode) {
        return harSøktFrilans && harIkkeUtbetalingForFrilans(periode);
    }

    private Boolean harIkkeUtbetalingForFrilans(BeregningsgrunnlagPeriodeDto p) {
        return p.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(a -> a.getAktivitetStatus().erFrilanser())
                .findFirst()
                .map(a -> a.getDagsats().equals(0L))
                .orElse(true);
    }

    private Boolean harIkkeUtbetalingForNæring(BeregningsgrunnlagPeriodeDto p) {
        return p.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(a -> a.getAktivitetStatus().erSelvstendigNæringsdrivende())
                .findFirst()
                .map(a -> a.getDagsats().equals(0L))
                .orElse(true);
    }

}
