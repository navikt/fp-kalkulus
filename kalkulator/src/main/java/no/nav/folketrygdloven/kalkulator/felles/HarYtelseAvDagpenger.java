package no.nav.folketrygdloven.kalkulator.felles;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.Arbeidskategori;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;

public class HarYtelseAvDagpenger {

    public static Boolean harSykepengerPåGrunnlagAvDagpenger(Collection<YtelseDto> ytelser, LocalDate dato) {
        return ytelser.stream()
                .filter(y -> y.getPeriode().inkluderer(dato))
                .filter(y -> y.getRelatertYtelseType().equals(FagsakYtelseType.SYKEPENGER))
                .flatMap(y -> y.getYtelseGrunnlag().stream())
                .map(YtelseGrunnlagDto::getArbeidskategori)
                .anyMatch(arbeidskategori -> Arbeidskategori.DAGPENGER.equals(arbeidskategori) ||
                        Arbeidskategori.KOMBINASJON_ARBEIDSTAKER_OG_DAGPENGER.equals(arbeidskategori));
    }

}
