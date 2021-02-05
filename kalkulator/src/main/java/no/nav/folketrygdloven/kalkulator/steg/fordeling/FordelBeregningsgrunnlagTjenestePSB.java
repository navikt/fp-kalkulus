package no.nav.folketrygdloven.kalkulator.steg.fordeling;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.omfordeling.OmfordelBeregningsgrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.ytelse.psb.TilkommetInntektPeriodeTjeneste;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
@FagsakYtelseTypeRef("PSB")
public class FordelBeregningsgrunnlagTjenestePSB implements FordelBeregningsgrunnlagTjeneste {

    private OmfordelBeregningsgrunnlagTjeneste omfordelTjeneste;
    private final TilkommetInntektPeriodeTjeneste periodeTjeneste = new TilkommetInntektPeriodeTjeneste();
    private boolean tilkommetInntektAktivert;

    public FordelBeregningsgrunnlagTjenestePSB() {
        // CDI
    }

    @Inject
    public FordelBeregningsgrunnlagTjenestePSB(OmfordelBeregningsgrunnlagTjeneste omfordelTjeneste,
                                               @KonfigVerdi(value = "PSB_TILKOMMET_INNTEKT", defaultVerdi = "true", required = false) boolean tilkommetInntektAktivert) {
        this.omfordelTjeneste = omfordelTjeneste;
        this.tilkommetInntektAktivert = tilkommetInntektAktivert;
    }

    public BeregningsgrunnlagRegelResultat omfordelBeregningsgrunnlag(BeregningsgrunnlagInput input) {
        var resultatFraOmfordeling = omfordelTjeneste.omfordel(input, input.getBeregningsgrunnlag());
        if (!tilkommetInntektAktivert) {
            return new BeregningsgrunnlagRegelResultat(resultatFraOmfordeling.getBeregningsgrunnlag(),
                    resultatFraOmfordeling.getRegelsporinger().orElse(null));
        } else {
            var splittetTilkommetInntektBg = periodeTjeneste.splittPerioderVedTilkommetInntekt(input, resultatFraOmfordeling.getBeregningsgrunnlag());
            return new BeregningsgrunnlagRegelResultat(splittetTilkommetInntektBg,
                    resultatFraOmfordeling.getRegelsporinger().orElse(null));
        }
    }

}
