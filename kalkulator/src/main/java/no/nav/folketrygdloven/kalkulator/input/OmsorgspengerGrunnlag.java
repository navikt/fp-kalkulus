package no.nav.folketrygdloven.kalkulator.input;

import java.util.Collections;
import java.util.List;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.omp.SøknadsperioderPrAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktiviteterDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;

public class OmsorgspengerGrunnlag extends UtbetalingsgradGrunnlag implements YtelsespesifiktGrunnlag {

    private final int dekningsgrad = 100;
    private Integer grunnbeløpMilitærHarKravPå;
    private final List<SøknadsperioderPrAktivitetDto> søknadsperioderPrAktivitet;

    public OmsorgspengerGrunnlag(List<UtbetalingsgradPrAktivitetDto> utbetalingsgradPrAktivitet, List<SøknadsperioderPrAktivitetDto> søknadsperioderPrAktivitet) {
        super(utbetalingsgradPrAktivitet);
        this.søknadsperioderPrAktivitet = søknadsperioderPrAktivitet;
    }

    @Override
    public int getDekningsgrad(BeregningsgrunnlagDto vlBeregningsgrunnlag, OpptjeningAktiviteterDto opptjeningAktiviteterDto) {
        return dekningsgrad;
    }

    @Override
    public int getGrunnbeløpMilitærHarKravPå() {
        return grunnbeløpMilitærHarKravPå;
    }

    @Override
    public void setGrunnbeløpMilitærHarKravPå(int grunnbeløpMilitærHarKravPå) {
        this.grunnbeløpMilitærHarKravPå = grunnbeløpMilitærHarKravPå;
    }

    public List<SøknadsperioderPrAktivitetDto> getSøknadsperioderPrAktivitet() {
        return søknadsperioderPrAktivitet == null ? Collections.emptyList() : søknadsperioderPrAktivitet;
    }

    public boolean harBrukerSøktForArbeidsgiverIPeriode(Intervall periode, Arbeidsgiver arbeidsgiver) {
        // Returnerer true for null og tom liste til vi har tatt i bruk i produksjon
        return søknadsperioderPrAktivitet == null || søknadsperioderPrAktivitet.isEmpty() || søknadsperioderPrAktivitet.stream()
                .filter(s -> s.getAktivitet().getArbeidsgiver().isPresent() && s.getAktivitet().getArbeidsgiver().get().equals(arbeidsgiver))
                .anyMatch(s -> s.getPeriode().stream().anyMatch(p -> p.overlapper(periode)));
    }

}
