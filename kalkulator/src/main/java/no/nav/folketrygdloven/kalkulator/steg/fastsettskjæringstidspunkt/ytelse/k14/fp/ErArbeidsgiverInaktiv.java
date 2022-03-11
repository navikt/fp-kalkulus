package no.nav.folketrygdloven.kalkulator.steg.fastsettskjæringstidspunkt.ytelse.k14.fp;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktørArbeidDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktørInntektDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktørYtelseDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektspostDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseFordelingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektskildeType;

public class ErArbeidsgiverInaktiv {

    private ErArbeidsgiverInaktiv() {
        // SKjuler default
    }

    private static final Set<String> NØDNUMRE = Set.of("971278420", "971278439", "971248106", "971373032", "871400172");
    private static final Set<FagsakYtelseType> YTELSER_SOM_IKKE_PÅVIRKER_IM = Set.of(FagsakYtelseType.PLEIEPENGER_NÆRSTÅENDE, FagsakYtelseType.ARBEIDSAVKLARINGSPENGER, FagsakYtelseType.DAGPENGER, FagsakYtelseType.OMSORGSPENGER);
    private static final int AKTIVE_MÅNEDER_FØR_STP = 10;
    private static final int NYOPPSTARTEDE_ARBEIDSFORHOLD_ALDER_I_MND = 4;

    private static final Logger LOG = LoggerFactory.getLogger(ErArbeidsgiverInaktiv.class);

    public static boolean erInaktivt(Arbeidsgiver arbeidsgiverSomSjekkes, InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag, LocalDate stp) {
        if (inntektArbeidYtelseGrunnlag == null) {
            return false;
        }
        if (erNyoppstartet(arbeidsgiverSomSjekkes, inntektArbeidYtelseGrunnlag, stp)) {
            return false;
        }
        if (harHattYtelseForArbeidsgiver(arbeidsgiverSomSjekkes, inntektArbeidYtelseGrunnlag, stp)) {
            return false;
        }
        return !harHattInntektIPeriode(arbeidsgiverSomSjekkes, inntektArbeidYtelseGrunnlag, stp);
    }

    private static boolean harHattYtelseForArbeidsgiver(Arbeidsgiver arbeidsgiver, InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag, LocalDate stp) {
        var periodeViDefinererSomAkivt = Intervall.fraOgMedTilOgMed(stp.minusMonths(AKTIVE_MÅNEDER_FØR_STP), stp);
        Collection<YtelseDto> ytelser = inntektArbeidYtelseGrunnlag.getAktørYtelseFraRegister()
                .map(AktørYtelseDto::getAlleYtelser)
                .orElse(Collections.emptyList());
        return ytelser.stream()
                .filter(yt-> !YTELSER_SOM_IKKE_PÅVIRKER_IM.contains(yt.getYtelseType()))
                .filter(yt -> harYtelseIPeriode(yt, periodeViDefinererSomAkivt))
                .anyMatch(yt -> erYtelseForAG(yt, arbeidsgiver));
    }

    private static boolean harHattInntektIPeriode(Arbeidsgiver arbeidsgiver, InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag, LocalDate stp) {
        var periodeViDefinererSomAkivt = Intervall.fraOgMedTilOgMed(stp.minusMonths(AKTIVE_MÅNEDER_FØR_STP), stp);
        var inntekter = inntektArbeidYtelseGrunnlag.getAktørInntektFraRegister()
                .map(AktørInntektDto::getInntekt)
                .orElse(Collections.emptyList());
        return inntekter.stream()
                .filter(innt -> innt.getInntektsKilde().equals(InntektskildeType.INNTEKT_BEREGNING))
                .filter(innt -> innt.getArbeidsgiver() != null && innt.getArbeidsgiver().equals(arbeidsgiver))
                .anyMatch(innt -> harInntektIPeriode(innt.getAlleInntektsposter(), periodeViDefinererSomAkivt));
    }

    private static boolean erNyoppstartet(Arbeidsgiver arbeidsgiver, InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag, LocalDate stp) {
        var alleArbeidsforhold = inntektArbeidYtelseGrunnlag.getAktørArbeidFraRegister()
                .map(AktørArbeidDto::hentAlleYrkesaktiviteter)
                .orElse(Collections.emptyList());
        return alleArbeidsforhold.stream()
                .filter(arb -> arb.getArbeidsgiver() != null && arb.getArbeidsgiver().equals(arbeidsgiver))
                .noneMatch(arb -> erEldreEnnGrense(arb, stp));
    }

    private static boolean erEldreEnnGrense(YrkesaktivitetDto arb, LocalDate stp) {
        LocalDate fomDefinertSomNyoppstartet = stp.minusMonths(NYOPPSTARTEDE_ARBEIDSFORHOLD_ALDER_I_MND);
        return arb.getAlleAktivitetsAvtaler().stream()
                .filter(AktivitetsAvtaleDto::erAnsettelsesPeriode)
                .anyMatch(aa -> aa.getPeriode().getFomDato().isBefore(fomDefinertSomNyoppstartet));
    }

    private static boolean harInntektIPeriode(Collection<InntektspostDto> alleInntektsposter, Intervall periodeViDefinererSomAkivt) {
        return alleInntektsposter.stream()
                .filter(post -> post.getPeriode().overlapper(periodeViDefinererSomAkivt))
                .anyMatch(post -> !post.getBeløp().erNullEllerNulltall());
    }

    private static boolean erYtelseForAG(YtelseDto ytelse, Arbeidsgiver arbeidsgiver) {
        List<YtelseFordelingDto> ytelsefordenlinger = ytelse.getYtelseGrunnlag()
                .map(YtelseGrunnlagDto::getFordeling)
                .orElse(Collections.emptyList());
        if (ytelsefordenlinger.isEmpty()) {
            LOG.info("Kan ikke finne ut av hvem som mottar ytelse, defaulter til true");
            return true;
        }
        return ytelsefordenlinger.stream().anyMatch(yf -> kanMatcheAG(arbeidsgiver, yf));
    }

    private static boolean kanMatcheAG(Arbeidsgiver arbeidsgiver, YtelseFordelingDto yf) {
        // Hvis orgnr mangler kan det være at det gjelder for arbeidsgiver, så hvis det mangler returnerer vi true for sikkerhets skyld
        boolean matcherOrgnr = Objects.equals(yf.getArbeidsgiver(), arbeidsgiver);
        if (matcherOrgnr) {
            return true;
        }
        return NØDNUMRE.contains(arbeidsgiver.getIdentifikator());
    }

    private static boolean harYtelseIPeriode(YtelseDto ytelse, Intervall periode) {
        if (ytelse.getYtelseAnvist().isEmpty()) {
            return ytelse.getPeriode().overlapper(periode);
        }
        return ytelse.getYtelseAnvist().stream()
                .anyMatch(ya -> periode.inkluderer(ya.getAnvistFOM()) || periode.inkluderer(ya.getAnvistTOM()));
    }
}
