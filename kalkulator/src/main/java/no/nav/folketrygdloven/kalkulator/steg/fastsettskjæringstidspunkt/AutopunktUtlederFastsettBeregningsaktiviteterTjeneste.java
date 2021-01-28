package no.nav.folketrygdloven.kalkulator.steg.fastsettskjæringstidspunkt;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;


import no.nav.folketrygdloven.kalkulator.felles.BeregningstidspunktTjeneste;
import no.nav.folketrygdloven.kalkulator.felles.MeldekortUtils;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktørYtelseDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseAnvistDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseFilterDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;

public class AutopunktUtlederFastsettBeregningsaktiviteterTjeneste {

    private AutopunktUtlederFastsettBeregningsaktiviteterTjeneste() {
        // Skjuler default konstruktør
    }

    /**
     * Utleder om det skal ventes på siste meldekort for AAP for å kunne beregne riktig beregningsgrunnlag.
     * Skal vente om:
     * - Har annen aktivitetstatus enn AAP
     * - Har løpende AAP på skjæringstidspunktet
     * - Har sendt inn meldekort for AAP de siste 4 mnd før skjæringstidspunkt for opptjening
     *
     *
     * @param aktørYtelse aktørytelse for søker
     * @param beregningsgrunnlag beregningsgrunnlaget
     * @param dagensdato Dagens dato/ idag
     * @param fagsakYtelseType Fagsakytelsetype
     * @return Optional som innholder ventefrist om autopunkt skal opprettes, Optional.empty ellers
     */
    static Optional<LocalDate> skalVenteTilDatoPåMeldekortAAPellerDP(Optional<AktørYtelseDto> aktørYtelse, BeregningsgrunnlagDto beregningsgrunnlag, LocalDate dagensdato, FagsakYtelseType fagsakYtelseType) {
        if (!harLøpendeVedtakOgSendtInnMeldekortNylig(aktørYtelse, beregningsgrunnlag.getSkjæringstidspunkt()))
            return Optional.empty();

        if(erSisteMeldekortMottatt(aktørYtelse, beregningsgrunnlag.getSkjæringstidspunkt(), fagsakYtelseType)){
            return Optional.empty();
        }

        return utledVenteFrist(beregningsgrunnlag.getSkjæringstidspunkt(), dagensdato);
    }

    private static boolean erSisteMeldekortMottatt(Optional<AktørYtelseDto> aktørYtelse, LocalDate skjæringstidspunkt, FagsakYtelseType fagsakYtelseType) {
        var ytelseFilterVedtak = new YtelseFilterDto(aktørYtelse).før(skjæringstidspunkt);
        Optional<YtelseDto> nyligsteVedtak = MeldekortUtils.sisteVedtakFørStpForType(ytelseFilterVedtak, skjæringstidspunkt, Set.of(FagsakYtelseType.ARBEIDSAVKLARINGSPENGER, FagsakYtelseType.DAGPENGER));

        var ytelseFilterMeldekort = new YtelseFilterDto(aktørYtelse);


        if(BeregningstidspunktTjeneste.finnBeregningstidspunkt(skjæringstidspunkt, fagsakYtelseType).isBefore(skjæringstidspunkt) && opphørerYtelseDagenFørStp(nyligsteVedtak.get(), skjæringstidspunkt)){
            Optional<YtelseAnvistDto> meldekortOpphørtYtelse = MeldekortUtils.finnMeldekortSomInkludererGittDato(ytelseFilterMeldekort, nyligsteVedtak.get(),
                    Set.of(nyligsteVedtak.get().getRelatertYtelseType()), skjæringstidspunkt.minusDays(1));
            return meldekortOpphørtYtelse.isPresent();
        }

        Optional<YtelseAnvistDto> meldekortLøpendeYtelse = MeldekortUtils.finnMeldekortSomInkludererGittDato(ytelseFilterMeldekort, nyligsteVedtak.get(),
                Set.of(nyligsteVedtak.get().getRelatertYtelseType()), skjæringstidspunkt);
        return meldekortLøpendeYtelse.isPresent();
    }

    private static boolean harLøpendeVedtakOgSendtInnMeldekortNylig(Optional<AktørYtelseDto> aktørYtelse, LocalDate skjæringstidspunkt) {
        List<YtelseDto> aapOgDPYtelser = getAAPogDPYtelser(aktørYtelse, skjæringstidspunkt);
        boolean hattAAPSiste4Mnd = hattGittYtelseIGittPeriode(aapOgDPYtelser, skjæringstidspunkt.minusMonths(4).withDayOfMonth(1),
                FagsakYtelseType.ARBEIDSAVKLARINGSPENGER);
        Predicate<List<YtelseDto>> hattDPSiste10Mnd = ytelser -> hattGittYtelseIGittPeriode(ytelser, skjæringstidspunkt.minusMonths(10), FagsakYtelseType.DAGPENGER);

        if (!hattAAPSiste4Mnd && !hattDPSiste10Mnd.test(aapOgDPYtelser)) {
            return false;
        }

        FagsakYtelseType ytelseType = hattAAPSiste4Mnd ? FagsakYtelseType.ARBEIDSAVKLARINGSPENGER : FagsakYtelseType.DAGPENGER;
        return aapOgDPYtelser.stream()
            .filter(ytelse -> ytelseType.equals(ytelse.getRelatertYtelseType()))
            .anyMatch(ytelse -> ytelse.getPeriode().getFomDato().isBefore(skjæringstidspunkt)
                && !ytelse.getPeriode().getTomDato().isBefore(skjæringstidspunkt.minusDays(1)));
    }

    private static boolean hattGittYtelseIGittPeriode(List<YtelseDto> aapOgDPYtelser, LocalDate hattYtelseFom, FagsakYtelseType ytelseType) {
        return aapOgDPYtelser.stream()
            .filter(ytelse -> ytelseType.equals(ytelse.getRelatertYtelseType()))
            .flatMap(ytelse -> ytelse.getYtelseAnvist().stream())
            .anyMatch(ya -> !ya.getAnvistTOM().isBefore(hattYtelseFom));
    }

    private static List<YtelseDto> getAAPogDPYtelser(Optional<AktørYtelseDto> aktørYtelse, LocalDate skjæringstidspunkt) {
        var filter = new YtelseFilterDto(aktørYtelse).før(skjæringstidspunkt);
        var ytelser = filter.getFiltrertYtelser().stream()
            .filter(ytelse -> FagsakYtelseType.ARBEIDSAVKLARINGSPENGER.equals(ytelse.getRelatertYtelseType()) || FagsakYtelseType.DAGPENGER.equals(ytelse.getRelatertYtelseType()))
            .collect(Collectors.toList());
        return ytelser;
    }

    private static Optional<LocalDate> utledVenteFrist(LocalDate skjæringstidspunktOpptjening, LocalDate dagensdato) {
        if (!dagensdato.isAfter(skjæringstidspunktOpptjening)) {
            return Optional.of(skjæringstidspunktOpptjening.plusDays(1));
        }
        if (!dagensdato.isAfter(skjæringstidspunktOpptjening.plusDays(14))) {
            return Optional.of(dagensdato.plusDays(1));
        }
        return Optional.empty();
    }

    private static boolean opphørerYtelseDagenFørStp(YtelseDto nyligsteVedtak, LocalDate skjæringstidspunkt){
        return nyligsteVedtak.getPeriode().getTomDato().isEqual(skjæringstidspunkt.minusDays(1));
    }
}
