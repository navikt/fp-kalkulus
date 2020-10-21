package no.nav.folketrygdloven.kalkulator.steg.fordeling;

import static no.nav.folketrygdloven.kalkulator.output.RegelSporingAggregat.konkatiner;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.omfordeling.OmfordelBeregningsgrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.periodisering.FordelPerioderTjeneste;

@ApplicationScoped
public class FordelBeregningsgrunnlagTjeneste {

    private FordelPerioderTjeneste fordelPerioderTjeneste;
    private OmfordelBeregningsgrunnlagTjeneste omfordelTjeneste;

    public FordelBeregningsgrunnlagTjeneste() {
        // CDI
    }

    @Inject
    public FordelBeregningsgrunnlagTjeneste(FordelPerioderTjeneste fordelPerioderTjeneste,
                                            OmfordelBeregningsgrunnlagTjeneste omfordelTjeneste) {
        this.fordelPerioderTjeneste = fordelPerioderTjeneste;
        this.omfordelTjeneste = omfordelTjeneste;
    }

    public BeregningsgrunnlagRegelResultat fordelBeregningsgrunnlag(BeregningsgrunnlagInput input, BeregningsgrunnlagDto beregningsgrunnlag) {
        var resultatFraPeriodisering = fordelPerioderTjeneste.fastsettPerioderForRefusjonOgGradering(input, beregningsgrunnlag);
        var resultatFraOmfordeling = omfordelTjeneste.omfordel(input, resultatFraPeriodisering.getBeregningsgrunnlag());
        return new BeregningsgrunnlagRegelResultat(resultatFraOmfordeling.getBeregningsgrunnlag(),
                konkatiner(resultatFraPeriodisering.getRegelsporinger().orElse(null), resultatFraOmfordeling.getRegelsporinger().orElse(null)));
    }

    // TODO TSF-1315 vi bør kunne slette #fordelBeregningsgrunnlag og kun bruke denne etter at k9sak har tatt ibruk nytt steg for refusjon
    public BeregningsgrunnlagRegelResultat omfordelBeregningsgrunnlag(BeregningsgrunnlagInput input) {
        var resultatFraOmfordeling = omfordelTjeneste.omfordel(input, input.getBeregningsgrunnlag());
        return new BeregningsgrunnlagRegelResultat(resultatFraOmfordeling.getBeregningsgrunnlag(),
                resultatFraOmfordeling.getRegelsporinger().orElse(null));
    }

}
