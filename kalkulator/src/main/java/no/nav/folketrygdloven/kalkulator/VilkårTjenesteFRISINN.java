package no.nav.folketrygdloven.kalkulator;

import static no.nav.folketrygdloven.kalkulator.ytelse.frisinn.HarFrilansUtenInntekt.harKunFrilansUtenInntekt;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.FrisinnGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningVilkårResultat;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.Vilkårsavslagsårsak;
import no.nav.folketrygdloven.kalkulus.felles.tid.AbstractIntervall;

@ApplicationScoped
@FagsakYtelseTypeRef("FRISINN")
public class VilkårTjenesteFRISINN extends VilkårTjeneste {


    @Override
    public List<BeregningVilkårResultat> lagVilkårResultatFordel(BeregningsgrunnlagInput input, BeregningsgrunnlagRegelResultat beregningsgrunnlagRegelResultat) {
        BeregningsgrunnlagDto beregningsgrunnlag = beregningsgrunnlagRegelResultat.getBeregningsgrunnlag();
        FrisinnGrunnlag frisinnGrunnlag = input.getYtelsespesifiktGrunnlag();
        BeregningsgrunnlagPeriodeDto førstePeriode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().stream()
                .min(Comparator.comparing(BeregningsgrunnlagPeriodeDto::getBeregningsgrunnlagPeriodeFom))
                .orElseThrow();
        List<BeregningVilkårResultat> søktFLIngenInntektPerioder = beregningsgrunnlag.getBeregningsgrunnlagPerioder().stream()
                .filter(p -> harKunFrilansUtenInntekt(frisinnGrunnlag, p.getBeregningsgrunnlagPeriodeFom(), førstePeriode))
                .map(p -> new BeregningVilkårResultat(false, Vilkårsavslagsårsak.SØKT_FL_INGEN_FL_INNTEKT, p.getPeriode()))
                .collect(Collectors.toList());
        List<BeregningVilkårResultat> beregningVilkårListe = beregningsgrunnlagRegelResultat.getVilkårsresultat().stream()
                .filter(p -> frisinnGrunnlag.getFrisinnPerioder().stream().anyMatch(fp -> fp.getPeriode().overlapper(p.getPeriode())))
                .filter(vp -> søktFLIngenInntektPerioder.stream().noneMatch(p -> p.getPeriode().overlapper(vp.getPeriode())))
                .collect(Collectors.toList());
        beregningVilkårListe.addAll(søktFLIngenInntektPerioder);
        return beregningVilkårListe;
    }

    @Override
    public List<BeregningVilkårResultat> lagVilkårResultatFullføre(BeregningsgrunnlagInput input, BeregningsgrunnlagDto beregningsgrunnlagDto) {
        FrisinnGrunnlag frisinnGrunnlag = input.getYtelsespesifiktGrunnlag();
        return beregningsgrunnlagDto.getBeregningsgrunnlagPerioder().stream()
                .filter(p -> harAvkortetGrunnetAnnenInntekt(frisinnGrunnlag, p))
                .map(p -> new BeregningVilkårResultat(false, Vilkårsavslagsårsak.AVKORTET_GRUNNET_ANNEN_INNTEKT, p.getPeriode()))
                .collect(Collectors.toList());

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
