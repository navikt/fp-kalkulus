package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell;

import java.time.LocalDate;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittEgenNæringDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittOpptjeningDto;

@ApplicationScoped
@FagsakYtelseTypeRef("*")
public class MapInntektsgrunnlagVLTilRegelGenerell extends MapInntektsgrunnlagVLTilRegel {

    @Inject
    public MapInntektsgrunnlagVLTilRegelGenerell() {
        // Skjul meg
    }

    void mapOppgittOpptjening(Inntektsgrunnlag inntektsgrunnlag, OppgittOpptjeningDto oppgittOpptjening) {
        oppgittOpptjening.getEgenNæring().stream()
                .filter(en -> en.getNyoppstartet() || en.getVarigEndring())
                .filter(en -> en.getBruttoInntekt() != null)
                .forEach(en -> inntektsgrunnlag.leggTilPeriodeinntekt(byggPeriodeinntektEgenNæring(en)));
    }
    private Periodeinntekt byggPeriodeinntektEgenNæring(OppgittEgenNæringDto en) {
        LocalDate datoForInntekt;
        if (en.getVarigEndring()) {
            datoForInntekt = en.getEndringDato();
        } else {
            datoForInntekt = en.getFraOgMed();
        }
        if (datoForInntekt == null) {
            throw new IllegalStateException("Søker har oppgitt varig endret eller nyoppstartet næring men har ikke oppgitt endringsdato eller oppstartsdato");
        }
        return Periodeinntekt.builder()
                .medInntektskildeOgPeriodeType(Inntektskilde.SØKNAD)
                .medMåned(datoForInntekt)
                .medInntekt(en.getBruttoInntekt())
                .build();
    }

}
