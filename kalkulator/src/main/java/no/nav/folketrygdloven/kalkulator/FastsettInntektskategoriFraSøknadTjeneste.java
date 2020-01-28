package no.nav.folketrygdloven.kalkulator;

import static no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittEgenNæringDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittOpptjeningDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.kodeverk.VirksomhetType;


public class FastsettInntektskategoriFraSøknadTjeneste {

   private FastsettInntektskategoriFraSøknadTjeneste() {
        // hide me
    }

    public static void fastsettInntektskategori(BeregningsgrunnlagDto beregningsgrunnlag, InntektArbeidYtelseGrunnlagDto grunnlag) {
        beregningsgrunnlag.getBeregningsgrunnlagPerioder().stream()
            .flatMap(p -> p.getBeregningsgrunnlagPrStatusOgAndelList().stream())
            .forEach(andel -> BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(Optional.of(andel))
                .medInntektskategori(finnInntektskategoriForStatus(andel.getAktivitetStatus(), grunnlag)));
    }

    static Optional<Inntektskategori> finnHøyestPrioriterteInntektskategoriForSN(List<Inntektskategori> inntektskategorier) {
        if (inntektskategorier.isEmpty()) { //NOSONAR Style - Method excessively uses methods of another class. Klassen fastsetter inntektskategori og prioritet av inntektskategori
            return Optional.empty();
        }
        if (inntektskategorier.size() == 1) {
            return Optional.of(inntektskategorier.get(0));
        }
        if (inntektskategorier.contains(Inntektskategori.FISKER)) {
            return Optional.of(Inntektskategori.FISKER);
        }
        if (inntektskategorier.contains(Inntektskategori.JORDBRUKER)) {
            return Optional.of(Inntektskategori.JORDBRUKER);
        }
        if (inntektskategorier.contains(Inntektskategori.DAGMAMMA)) {
            return Optional.of(Inntektskategori.DAGMAMMA);
        }
        return Optional.of(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE);
    }

    private static Inntektskategori finnInntektskategoriForStatus(AktivitetStatus aktivitetStatus, InntektArbeidYtelseGrunnlagDto grunnlag) {
        if (SELVSTENDIG_NÆRINGSDRIVENDE == aktivitetStatus) {
            return finnInntektskategoriForSelvstendigNæringsdrivende(grunnlag);
        }
        return aktivitetStatus.getInntektskategori();
    }

    private static Inntektskategori finnInntektskategoriForSelvstendigNæringsdrivende(InntektArbeidYtelseGrunnlagDto grunnlag) {
        Optional<OppgittOpptjeningDto> oppgittOpptjening = grunnlag.getOppgittOpptjening();
        if (oppgittOpptjening.isPresent() && !oppgittOpptjening.get().getEgenNæring().isEmpty()) {
            Set<VirksomhetType> virksomhetTypeSet = oppgittOpptjening.get().getEgenNæring().stream()
                .map(OppgittEgenNæringDto::getVirksomhetType)
                .collect(Collectors.toSet());

            List<Inntektskategori> inntektskategorier = virksomhetTypeSet.stream()
                .map(v -> {
                    if (v.getInntektskategori() == Inntektskategori.UDEFINERT) {
                        return Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE;
                    }
                    return v.getInntektskategori();
                })
                .collect(Collectors.toList());

            return finnHøyestPrioriterteInntektskategoriForSN(inntektskategorier)
                .orElse(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE);
        }
        return Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE;
    }
}
