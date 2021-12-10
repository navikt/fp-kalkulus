package no.nav.folketrygdloven.kalkulator.felles;

import static no.nav.folketrygdloven.kalkulator.felles.BeregningstidspunktTjeneste.finnBeregningstidspunkt;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.Arbeidskategori;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektPeriodeType;

public class InfotrygdvedtakMedDagpengerTjeneste {

    public static Boolean harYtelsePåGrunnlagAvDagpenger(Collection<YtelseDto> ytelser, LocalDate skjæringstidspunkt, FagsakYtelseType ytelse) {
        LocalDate beregningstidspunkt = finnBeregningstidspunkt(skjæringstidspunkt);
        return finnYtelseBasertPåDagpenger(ytelser, beregningstidspunkt, ytelse).isPresent();
    }

    public static BigDecimal finnDagsatsFraYtelsevedtak(Collection<YtelseDto> ytelser, LocalDate skjæringstidspunkt, FagsakYtelseType ytelse) {
        LocalDate beregningstidspunkt = finnBeregningstidspunkt(skjæringstidspunkt);
        var ytelseGrunnlag = finnYtelseBasertPåDagpenger(ytelser, beregningstidspunkt, ytelse);

        var spAvDP = ytelseGrunnlag.stream()
                .flatMap(yg -> yg.getFordeling().stream())
                .filter(f -> f.getArbeidsgiver() == null)
                .findFirst();

        return spAvDP.map(f -> {
            var hyppighet = f.getHyppighet();
            // Antar dagsats ved ingen periodetype
            if (hyppighet == null || InntektPeriodeType.DAGLIG.equals(hyppighet)) {
                return f.getBeløp();
            }
            throw new IllegalArgumentException("Hånterer foreløpig kun dagsats som periodetype for ytelse av dagpenger.");
        }).orElse(BigDecimal.ZERO);
    }

    private static Optional<YtelseGrunnlagDto> finnYtelseBasertPåDagpenger(Collection<YtelseDto> ytelser, LocalDate beregningstidspunkt, FagsakYtelseType ytelse) {
        return ytelser.stream()
                .filter(y -> y.getPeriode().inkluderer(beregningstidspunkt))
                .filter(y -> y.getRelatertYtelseType().equals(ytelse))
                .flatMap(y -> y.getYtelseGrunnlag().stream())
                .filter(gr -> Arbeidskategori.DAGPENGER.equals(gr.getArbeidskategori()) ||
                        Arbeidskategori.KOMBINASJON_ARBEIDSTAKER_OG_DAGPENGER.equals(gr.getArbeidskategori()))
                .findFirst();
    }


}
