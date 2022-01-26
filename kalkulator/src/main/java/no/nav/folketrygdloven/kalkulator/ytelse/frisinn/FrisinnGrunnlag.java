package no.nav.folketrygdloven.kalkulator.ytelse.frisinn;

import java.time.LocalDate;
import java.util.List;

import no.nav.folketrygdloven.kalkulator.input.UtbetalingsgradGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktiviteterDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.FrisinnBehandlingType;

public class FrisinnGrunnlag extends UtbetalingsgradGrunnlag implements YtelsespesifiktGrunnlag {

    private final int DEKNINGSGRAD_80 = 80;
    private final int DEKNINGSGRAD_60 = 60;
    private final int DEKNINGSGRAD_70 = 70;
    private final LocalDate FØRSTE_DAG_MED_REDUSERT_DEKNINGSGRAD = LocalDate.of(2020,11,1);
    private final LocalDate ENDRET_DEKNINGSGRAD_DATO_70_PROSENT = LocalDate.of(2022,1,1);

    private Integer grunnbeløpMilitærHarKravPå = 2;
    private final List<FrisinnPeriode> frisinnPerioder;
    private FrisinnBehandlingType frisinnBehandlingType;

    public FrisinnGrunnlag(List<UtbetalingsgradPrAktivitetDto> utbetalingsgradPrAktivitet, List<FrisinnPeriode> frisinnPerioder, FrisinnBehandlingType frisinnBehandlingType) {
        super(utbetalingsgradPrAktivitet);
        this.frisinnPerioder = frisinnPerioder;
        this.frisinnBehandlingType = frisinnBehandlingType;
    }

    @Override
    public int getDekningsgrad(BeregningsgrunnlagDto vlBeregningsgrunnlag, OpptjeningAktiviteterDto opptjeningAktiviteterDto) {
        return DEKNINGSGRAD_80;
    }

    /**
     * Det er besluttet at frisinnsøknader som gjelder de to siste månedene det er mulig å søke (november og desember)
     * skal utbetales med redusert dekningsgrad.
     * https://jira.adeo.no/browse/TSF-1370
     */
    public int getDekningsgradForDato(LocalDate dato) {
        if (dato.isBefore(FØRSTE_DAG_MED_REDUSERT_DEKNINGSGRAD)) {
            return DEKNINGSGRAD_80;
        } else if (dato.isBefore(ENDRET_DEKNINGSGRAD_DATO_70_PROSENT)) {
            return DEKNINGSGRAD_60;
        } else {
            return DEKNINGSGRAD_70;
        }
    }

    @Override
    public int getGrunnbeløpMilitærHarKravPå() {
        return grunnbeløpMilitærHarKravPå;
    }

    @Override
    public void setGrunnbeløpMilitærHarKravPå(int grunnbeløpMilitærHarKravPå) {
        this.grunnbeløpMilitærHarKravPå = grunnbeløpMilitærHarKravPå;
    }

    public boolean getSøkerYtelseForFrilans() {
        return frisinnPerioder.stream().anyMatch(FrisinnPeriode::getSøkerFrilans);
    }

    public boolean getSøkerYtelseForFrilans(LocalDate dato) {
        return frisinnPerioder.stream().anyMatch(p -> p.getSøkerFrilans() && p.getPeriode().inkluderer(dato));
    }

    public boolean getSøkerYtelseForNæring() {
        return frisinnPerioder.stream().anyMatch(FrisinnPeriode::getSøkerNæring);
    }

    public boolean getSøkerYtelseForNæring(LocalDate dato) {
        return frisinnPerioder.stream().anyMatch(p -> p.getSøkerNæring() && p.getPeriode().inkluderer(dato));
    }

    public FrisinnBehandlingType getFrisinnBehandlingType() {
        return frisinnBehandlingType;
    }

    public List<FrisinnPeriode> getFrisinnPerioder() {
        return frisinnPerioder;
    }
}
