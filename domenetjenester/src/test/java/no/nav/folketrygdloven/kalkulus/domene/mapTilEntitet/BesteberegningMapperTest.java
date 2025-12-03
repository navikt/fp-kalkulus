package no.nav.folketrygdloven.kalkulus.domene.mapTilEntitet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.steg.besteberegning.BesteberegningMånedGrunnlag;
import no.nav.folketrygdloven.kalkulator.steg.besteberegning.BesteberegningVurderingGrunnlag;
import no.nav.folketrygdloven.kalkulator.steg.besteberegning.Inntekt;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;

class BesteberegningMapperTest {

    protected static final BigDecimal AVVIK = BigDecimal.valueOf(1234.34);
    protected static final BigDecimal INNTEKT = BigDecimal.valueOf(30000);

    @Test
    void test_mapping() {
        var entitet = BesteberegningMapper.mapBestebergninggrunnlag(lagGrunnlag(6));

        assertThat(entitet.getAvvik()).isPresent();
        assertThat(entitet.getAvvik().get().getVerdi()).isEqualTo(AVVIK);

        assertThat(entitet.getSeksBesteMåneder()).isNotEmpty().hasSize(6);

        assertThat(entitet.getSeksBesteMåneder()).allSatisfy(måned -> {
            assertThat(måned.getInntekter()).isNotEmpty().hasSize(1);
            assertThat(måned.getInntekter().getFirst().getInntekt().getVerdi()).isEqualTo(INNTEKT);
            assertThat(måned.getInntekter().getFirst().getOpptjeningAktivitetType()).isEqualTo(OpptjeningAktivitetType.AAP);
        });
    }

    @Test
    void test_exception_hvis_ikke_6_måneder() {
        var besteberegningVurderingGrunnlag = lagGrunnlag(7);
        assertThrows(IllegalStateException.class, () -> BesteberegningMapper.mapBestebergninggrunnlag(besteberegningVurderingGrunnlag));
    }

    private BesteberegningVurderingGrunnlag lagGrunnlag(int antall) {
        return new BesteberegningVurderingGrunnlag(lagMånedGrunnlag(antall), Beløp.fra(AVVIK));
    }

    private List<BesteberegningMånedGrunnlag> lagMånedGrunnlag(int antall) {
        List<BesteberegningMånedGrunnlag> månedGrunnlag = new ArrayList<>();
        for (int i = 0; i < antall; i++) {
            månedGrunnlag.add(new BesteberegningMånedGrunnlag(List.of(new Inntekt(OpptjeningAktivitetType.AAP, Beløp.fra(INNTEKT))), YearMonth.now().minusMonths(i)));
        }
        return månedGrunnlag;
    }

}
