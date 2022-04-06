package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.inntektskategori;

import static no.nav.folketrygdloven.kalkulator.felles.ytelseovergang.DirekteOvergangTjeneste.finnAnvisningerForDirekteOvergangFraKap8;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.KonfigurasjonVerdi;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AnvistAndel;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;

public class FastsettInntektskategoriFraYtelseVedtak {

    private FastsettInntektskategoriFraYtelseVedtak() {
    }

    public static BeregningsgrunnlagDto opprettAndelerOgSettInntektskategorierFraYtelseVedtak(BeregningsgrunnlagDto beregningsgrunnlagDto, InntektArbeidYtelseGrunnlagDto iay) {
        var nyttBg = BeregningsgrunnlagDto.builder(beregningsgrunnlagDto).build();

        if (!KonfigurasjonVerdi.get("BEREGNE_FRA_YTELSE_VEDTAK", false)) {
            return nyttBg;
        }

        if (beregningsgrunnlagDto.getAktivitetStatuser().stream().allMatch(a -> a.getAktivitetStatus().equals(AktivitetStatus.KUN_YTELSE))) {
            lagAndelerForInntektskategorierFraYtelse(iay, nyttBg);
        }
        return nyttBg;
    }

    private static void lagAndelerForInntektskategorierFraYtelse(InntektArbeidYtelseGrunnlagDto iay, BeregningsgrunnlagDto nyttBg) {
        var alleInntektskategorierFraYtelse = finnAnvisningerForDirekteOvergangFraKap8(iay, nyttBg.getSkjæringstidspunkt()).stream()
                .flatMap(ya -> ya.getAnvisteAndeler().stream())
                .map(AnvistAndel::getInntektskategori)
                .sorted(Comparator.comparing(Inntektskategori::getKode))
                .collect(Collectors.toCollection(TreeSet::new));

        leggTilAndelerPrInntektskategori(nyttBg, alleInntektskategorierFraYtelse);
    }

    private static void leggTilAndelerPrInntektskategori(BeregningsgrunnlagDto nyttBg, Set<Inntektskategori> alleInntektskategorierFraYtelse) {
        int i = 0;
        var periode = nyttBg.getBeregningsgrunnlagPerioder().get(0);
        var andeler = periode.getBeregningsgrunnlagPrStatusOgAndelList();
        if (andeler.stream().anyMatch(a -> !a.getAktivitetStatus().equals(AktivitetStatus.BRUKERS_ANDEL))) {
            throw new IllegalArgumentException("Forventer kun brukers andel. Fikk " + andeler.stream().map(BeregningsgrunnlagPrStatusOgAndelDto::getAktivitetStatus).collect(Collectors.toSet()));
        }
        if (andeler.size() != 1) {
            throw new IllegalArgumentException("Forventer kun en andel. Faktisk: " + andeler.size());
        }
        for (var inntektskategori : alleInntektskategorierFraYtelse) {
            if (i == 0) {
                BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(andeler.get(0)).medInntektskategori(inntektskategori);
            } else {
                var nyAndelBuilder = BeregningsgrunnlagPrStatusOgAndelDto.kopier(andeler.get(0))
                        .medAndelsnr(null);
                nyAndelBuilder.medInntektskategori(inntektskategori);
                BeregningsgrunnlagPeriodeDto.oppdater(periode).leggTilBeregningsgrunnlagPrStatusOgAndel(nyAndelBuilder);
            }
            i++;
        }
    }

}
