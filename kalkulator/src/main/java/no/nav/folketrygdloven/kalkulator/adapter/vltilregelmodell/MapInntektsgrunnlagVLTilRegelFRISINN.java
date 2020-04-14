package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittEgenNæringDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittOpptjeningDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;

@ApplicationScoped
@FagsakYtelseTypeRef("FRISINN")
public class MapInntektsgrunnlagVLTilRegelFRISINN extends MapInntektsgrunnlagVLTilRegel {
    private static final LocalDate FOM_2019 = LocalDate.of(2019,1,1);
    private static final LocalDate TOM_2019 = LocalDate.of(2019,12,31);

    @Inject
    public MapInntektsgrunnlagVLTilRegelFRISINN() {
        // Skjul meg
    }

    void mapOppgittOpptjening(Inntektsgrunnlag inntektsgrunnlag, OppgittOpptjeningDto oppgittOpptjening) {
        if (!oppgittOpptjening.getEgenNæring().isEmpty()) {
            Optional<BigDecimal> samletNæringsinntekt2019 = oppgittOpptjening.getEgenNæring().stream()
                    .filter(en -> erInntektFor2019(en.getPeriode()))
                    .filter(en -> en.getBruttoInntekt() != null)
                    .map(OppgittEgenNæringDto::getBruttoInntekt)
                    .reduce(BigDecimal::add);
            if (samletNæringsinntekt2019.isEmpty()) {
                throw new IllegalStateException("Kunne ikke finne oppgitt næringsinntekt for 2019, ugyldig tilstand for ytelse FRISINN");
            }
            inntektsgrunnlag.leggTilPeriodeinntekt(byggOppgittNæringsinntektFor2019(samletNæringsinntekt2019.get()));
        }
    }

    private boolean erInntektFor2019(Intervall periode) {
        return periode.overlapper(Intervall.fraOgMedTilOgMed(FOM_2019, TOM_2019));
    }

    private static Periodeinntekt byggOppgittNæringsinntektFor2019(BigDecimal oppgittInntekt) {
        return Periodeinntekt.builder()
                .medInntektskildeOgPeriodeType(Inntektskilde.SØKNAD)
                .medPeriode(Periode.of(FOM_2019, TOM_2019))
                .medInntekt(oppgittInntekt)
                .build();
    }
}
