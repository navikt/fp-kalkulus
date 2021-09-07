package no.nav.folketrygdloven.kalkulus.kopiering;

import static no.nav.folketrygdloven.kalkulus.felles.tid.AbstractIntervall.TIDENES_ENDE;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.folketrygdloven.kalkulator.input.StegProsesseringInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningResultatAggregat;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.k9.felles.konfigurasjon.konfig.KonfigVerdi;

@ApplicationScoped
public class ForlengelseTjeneste {

    private boolean skalVurdereForlengelse;

    public ForlengelseTjeneste() {
    }

    @Inject
    public ForlengelseTjeneste(@KonfigVerdi(value = "SKAL_VURDERE_FORLENGELSE", defaultVerdi = "false", required = false) boolean skalVurdereForlengelse) {
        this.skalVurdereForlengelse = skalVurdereForlengelse;
    }

    public boolean erForlengelse(StegProsesseringInput input,
                                        BeregningResultatAggregat resultat) {

        if (skalVurdereForlengelse) {
            return input.getForrigeGrunnlagFraSteg()
                    .map(forrigeGr -> erForlengelse(resultat.getBeregningsgrunnlagGrunnlag(), forrigeGr))
                    .orElse(false);
        }

        return false;

    }

    private static boolean erForlengelse(BeregningsgrunnlagGrunnlagDto nyttGrunnlag,
                                        BeregningsgrunnlagGrunnlagDto forrigeGrunnlagFraSteg) {

        if (nyttGrunnlag.getBeregningsgrunnlag().isPresent() && forrigeGrunnlagFraSteg.getBeregningsgrunnlag().isPresent()) {

            BeregningsgrunnlagDto nyttBg = nyttGrunnlag.getBeregningsgrunnlag().get();
            BeregningsgrunnlagDto forrigBG = forrigeGrunnlagFraSteg.getBeregningsgrunnlag().get();
            Set<Intervall> perioderUtenDiff = BeregningsgrunnlagDiffSjekker.finnPerioderUtenDiff(nyttBg, forrigBG);

            List<BeregningsgrunnlagPeriodeDto> forrigePerioder = forrigBG.getBeregningsgrunnlagPerioder();

            List<Intervall> perioderMedDiff = forrigePerioder.stream()
                    .map(BeregningsgrunnlagPeriodeDto::getPeriode)
                    .filter(p -> perioderUtenDiff.stream().noneMatch(periodeUtenDiff -> periodeUtenDiff.overlapper(p)))
                    .collect(Collectors.toList());

            // Det er ein forlengelse om det er kun siste periode i forrige grunnlag som er ulik eller ingen er ulike
            return perioderMedDiff.size() == 0 || (perioderMedDiff.size() == 1 && perioderMedDiff.get(0).getTomDato().equals(TIDENES_ENDE));
        }

        return BeregningsgrunnlagDiffSjekker.harSignifikantDiffIAktiviteter(nyttGrunnlag.getGjeldendeAktiviteter(), forrigeGrunnlagFraSteg.getGjeldendeAktiviteter());
    }


}
