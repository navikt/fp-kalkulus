package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering;

import java.time.LocalDate;
import java.util.Optional;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.kalkulator.modell.iay.BekreftetPermisjonDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.BekreftetPermisjonStatus;
import no.nav.vedtak.konfig.Tid;

public class FinnFørsteDagEtterBekreftetPermisjon {
    private FinnFørsteDagEtterBekreftetPermisjon() {
        // skjul public constructor
    }

    /**
     * @return Dersom det finnes en bekreftet permisjon som gjelder på skjæringstidspunkt for beregning, returner første dag etter den bekreftede permisjonen.
     * Ellers: Returner første dag i ansettelsesperioden til arbeidsforholdet. Dette kan være enten før eller etter første uttaksdag.
     */
  public static Optional<LocalDate> finn(InntektArbeidYtelseGrunnlagDto iayGrunnlag, YrkesaktivitetDto ya, Periode ansettelsesPeriode, LocalDate skjæringstidspunktBeregning) {
        Optional<BekreftetPermisjonDto> permisjonForYrkesaktivitet = HentBekreftetPermisjon.hent(iayGrunnlag, ya);
        Optional<BekreftetPermisjonDto> bekreftetPermisjonOpt = permisjonForYrkesaktivitet
            .filter(perm -> perm.getStatus().equals(BekreftetPermisjonStatus.BRUK_PERMISJON))
            .filter(perm -> perm.getPeriode().inkluderer(skjæringstidspunktBeregning));
        if (bekreftetPermisjonOpt.isEmpty()) {
            return Optional.of(ansettelsesPeriode.getFom());
        }
        BekreftetPermisjonDto bekreftetPermisjon = bekreftetPermisjonOpt.get();
        LocalDate sisteDagMedPermisjon = bekreftetPermisjon.getPeriode().getTomDato();
        if (sisteDagMedPermisjon.equals(Tid.TIDENES_ENDE)) {
            return Optional.empty();
        }
        LocalDate dagenEtterBekreftetPermisjon = sisteDagMedPermisjon.plusDays(1);
        return Optional.of(dagenEtterBekreftetPermisjon);
    }
}
