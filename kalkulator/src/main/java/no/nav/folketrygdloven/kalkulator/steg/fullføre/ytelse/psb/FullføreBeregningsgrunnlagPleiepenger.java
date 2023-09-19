package no.nav.folketrygdloven.kalkulator.steg.fullføre.ytelse.psb;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.Beregningsgrunnlag;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.KonfigurasjonVerdi;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.fastsett.MapBeregningsgrunnlagFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.steg.fullføre.ytelse.utbgrad.FullføreBeregningsgrunnlagUtbgrad;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.regelmodelloversetter.KalkulusRegler;

@FagsakYtelseTypeRef(FagsakYtelseType.PLEIEPENGER_SYKT_BARN)
@FagsakYtelseTypeRef(FagsakYtelseType.OPPLÆRINGSPENGER)
@FagsakYtelseTypeRef(FagsakYtelseType.PLEIEPENGER_NÆRSTÅENDE)
@ApplicationScoped
public class FullføreBeregningsgrunnlagPleiepenger extends FullføreBeregningsgrunnlagUtbgrad {

    public FullføreBeregningsgrunnlagPleiepenger() {
        // CDI
    }

    @Inject
    public FullføreBeregningsgrunnlagPleiepenger(MapBeregningsgrunnlagFraVLTilRegel mapBeregningsgrunnlagFraVLTilRegel) {
        super(mapBeregningsgrunnlagFraVLTilRegel);
    }

    protected List<String> kjørRegelFinnGrenseverdi(Beregningsgrunnlag beregningsgrunnlagRegel) {
        var graderingMotInntektEnabled = KonfigurasjonVerdi.get("GRADERING_MOT_INNTEKT", false);
        return beregningsgrunnlagRegel.getBeregningsgrunnlagPerioder().stream()
                .map(periode -> {
                    if (graderingMotInntektEnabled) {
                        return KalkulusRegler.finnGrenseverdiUtenFordeling(periode).getRegelSporing().sporing();
                    }
                    return KalkulusRegler.finnGrenseverdi(periode).getRegelSporing().sporing();
                })
                .collect(Collectors.toList());
    }


}
