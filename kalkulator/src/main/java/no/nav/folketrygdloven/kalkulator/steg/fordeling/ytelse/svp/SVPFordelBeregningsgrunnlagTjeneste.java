package no.nav.folketrygdloven.kalkulator.steg.fordeling.ytelse.svp;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.KonfigurasjonVerdi;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.FordelBeregningsgrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.omfordeling.OmfordelBeregningsgrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.omfordeling.OmfordelingUtenRefusjonskravTjeneste;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.ytelse.psb.TilkommetInntektPeriodeTjeneste;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;

@ApplicationScoped
@FagsakYtelseTypeRef(FagsakYtelseType.SVANGERSKAPSPENGER)
public class SVPFordelBeregningsgrunnlagTjeneste implements FordelBeregningsgrunnlagTjeneste {

    private OmfordelBeregningsgrunnlagTjeneste omfordelTjeneste;
    private final TilkommetInntektPeriodeTjeneste periodeTjeneste = new TilkommetInntektPeriodeTjeneste();
    private boolean graderingMotInntektEnabled;
    private boolean fordelingUtenKravEnabled;

    public SVPFordelBeregningsgrunnlagTjeneste() {
        // CDI
    }

    @Inject
    public SVPFordelBeregningsgrunnlagTjeneste(OmfordelBeregningsgrunnlagTjeneste omfordelTjeneste) {
        this.omfordelTjeneste = omfordelTjeneste;
        this.graderingMotInntektEnabled = KonfigurasjonVerdi.get("GRADERING_MOT_INNTEKT", false);
        this.fordelingUtenKravEnabled = KonfigurasjonVerdi.get("FORDELING_UTEN_KRAV", false);
    }

    @Override
    public BeregningsgrunnlagRegelResultat omfordelBeregningsgrunnlag(BeregningsgrunnlagInput input) {
        var resultatFraOmfordeling = omfordelTjeneste.omfordel(input, input.getBeregningsgrunnlag());
        if (fordelingUtenKravEnabled) {
            resultatFraOmfordeling = new BeregningsgrunnlagRegelResultat(OmfordelingUtenRefusjonskravTjeneste.omfordel(resultatFraOmfordeling.getBeregningsgrunnlag(), input.getYtelsespesifiktGrunnlag()),
                    resultatFraOmfordeling.getRegelsporinger().orElse(null));
        }
        if (!graderingMotInntektEnabled) {
            return new BeregningsgrunnlagRegelResultat(resultatFraOmfordeling.getBeregningsgrunnlag(),
                    resultatFraOmfordeling.getRegelsporinger().orElse(null));
        } else {
            var splittetTilkommetInntektBg = periodeTjeneste.splittPerioderVedTilkommetInntekt(input, resultatFraOmfordeling.getBeregningsgrunnlag());
            return new BeregningsgrunnlagRegelResultat(splittetTilkommetInntektBg,
                    resultatFraOmfordeling.getRegelsporinger().orElse(null));
        }

    }

}
