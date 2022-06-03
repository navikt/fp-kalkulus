package no.nav.folketrygdloven.kalkulator.input;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.gradering.AktivitetGradering;
import no.nav.folketrygdloven.kalkulator.steg.besteberegning.BesteberegningVurderingGrunnlag;
import no.nav.folketrygdloven.kalkulator.steg.besteberegning.Ytelsegrunnlag;

public class ForeldrepengerGrunnlag implements YtelsespesifiktGrunnlag {

    private int dekningsgrad = 100;

    private boolean kvalifisererTilBesteberegning = false;

    private BesteberegningVurderingGrunnlag besteberegningVurderingGrunnlag;

    private Integer grunnbeløpMilitærHarKravPå;

    /** Aktiviteter for gradering av uttak. */
    private AktivitetGradering aktivitetGradering = AktivitetGradering.INGEN_GRADERING;


    /** Når siste søkte uttaksdag er, skal brukes for å vurdere refusjon til arbeidsforhold som er avsluttet. */
    private LocalDate sisteSøkteUttaksdag;

    /** Når vi start behandlingen av saken, skal brukes for å vurdere refusjon til arbeidsforhold som er avsluttet */
    private LocalDate behandlingstidspunkt;

    /**  Brukes kun for besteberegninger som skal gjøres automatisk */
    private List<Ytelsegrunnlag> besteberegningYtelsegrunnlag;

    ForeldrepengerGrunnlag() {
    }

    public ForeldrepengerGrunnlag(int dekningsgrad, boolean kvalifisererTilBesteberegning) {
        this.dekningsgrad = dekningsgrad;
        this.kvalifisererTilBesteberegning = kvalifisererTilBesteberegning;
    }

    public ForeldrepengerGrunnlag(int dekningsgrad, boolean kvalifisererTilBesteberegning, AktivitetGradering aktivitetGradering) {
        this.dekningsgrad = dekningsgrad;
        this.kvalifisererTilBesteberegning = kvalifisererTilBesteberegning;
        this.aktivitetGradering = aktivitetGradering;
    }

    public ForeldrepengerGrunnlag(int dekningsgrad, BesteberegningVurderingGrunnlag besteberegningVurderingGrunnlag) {
        this.dekningsgrad = dekningsgrad;
        this.kvalifisererTilBesteberegning = besteberegningVurderingGrunnlag != null;
        this.besteberegningVurderingGrunnlag = besteberegningVurderingGrunnlag;
    }

    @Override
    public int getDekningsgrad() {
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


    public void setAktivitetGradering(AktivitetGradering aktivitetGradering) {
        this.aktivitetGradering = aktivitetGradering;
    }

    public AktivitetGradering getAktivitetGradering() {
        return aktivitetGradering;
    }

    public boolean isKvalifisererTilBesteberegning() {
        return kvalifisererTilBesteberegning;
    }

    public BesteberegningVurderingGrunnlag getBesteberegningVurderingGrunnlag() {
        return besteberegningVurderingGrunnlag;
    }

    public Optional<LocalDate> getSisteSøkteUttaksdag() {
        return Optional.ofNullable(sisteSøkteUttaksdag);
    }

    public void setSisteSøkteUttaksdag(LocalDate sisteSøkteUttaksdag) {
        this.sisteSøkteUttaksdag = sisteSøkteUttaksdag;
    }

    public Optional<LocalDate> getBehandlingstidspunkt() {
        return Optional.ofNullable(behandlingstidspunkt);
    }

    public void setBehandlingstidspunkt(LocalDate behandlingstidspunkt) {
        this.behandlingstidspunkt = behandlingstidspunkt;
    }

    public List<Ytelsegrunnlag> getBesteberegningYtelsegrunnlag() {
        return besteberegningYtelsegrunnlag;
    }

    public void setBesteberegningYtelsegrunnlag(List<Ytelsegrunnlag> besteberegningYtelsegrunnlag) {
        this.besteberegningYtelsegrunnlag = besteberegningYtelsegrunnlag;
    }
}
