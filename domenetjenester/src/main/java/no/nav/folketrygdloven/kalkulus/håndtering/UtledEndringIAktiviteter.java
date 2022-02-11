package no.nav.folketrygdloven.kalkulus.håndtering;

import java.util.List;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.HåndterBeregningDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.avklaraktiviteter.AvklarAktiviteterHåndteringDto;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.BeregningAktivitetEndring;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.BeregningAktivitetNøkkel;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.BeregningAktiviteterEndring;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.DatoEndring;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.ToggleEndring;

class UtledEndringIAktiviteter {

    static Optional<BeregningAktiviteterEndring> utedEndring(
            HåndterBeregningDto dto, BeregningAktivitetAggregatDto register,
            BeregningAktivitetAggregatDto gjeldende,
            Optional<BeregningAktivitetAggregatDto> forrigeRegister,
            Optional<BeregningAktivitetAggregatDto> forrigeGjeldende) {

        if (dto instanceof AvklarAktiviteterHåndteringDto) {

            var registerAktiviteter = register.getBeregningAktiviteter();
            var gjeldendeAktiviteter = gjeldende.getBeregningAktiviteter();

            var endringer = registerAktiviteter.stream()
                    .map(aktivitet -> utledEndring(aktivitet, gjeldendeAktiviteter, forrigeRegister, forrigeGjeldende))
                    .toList();
            return Optional.of(new BeregningAktiviteterEndring(endringer));
        }
        return Optional.empty();
    }

    private static BeregningAktivitetEndring utledEndring(BeregningAktivitetDto aktivitet, List<BeregningAktivitetDto> gjeldendeAktiviteter, Optional<BeregningAktivitetAggregatDto> forrigeRegister, Optional<BeregningAktivitetAggregatDto> forrigeGjeldende) {
        var gjeldendeAktivitet = finnKorresponderende(aktivitet, gjeldendeAktiviteter);
        var skalBrukesEndring = finnSkalBrukesEndring(aktivitet, forrigeRegister, forrigeGjeldende, gjeldendeAktivitet);
        var datoEndring = finnDatoEndring(aktivitet, gjeldendeAktivitet, forrigeGjeldende);
        return new BeregningAktivitetEndring(
                mapNøkkel(aktivitet),
                skalBrukesEndring,
                datoEndring.orElse(null)
        );
    }

    private static Optional<DatoEndring> finnDatoEndring(BeregningAktivitetDto aktivitet,
                                                         Optional<BeregningAktivitetDto> gjeldendeAktivitet,
                                                         Optional<BeregningAktivitetAggregatDto> forrigeGjeldende) {
        var forrigeKorresponderendeGjeldende = forrigeGjeldende.map(BeregningAktivitetAggregatDto::getBeregningAktiviteter)
                .flatMap(akts -> finnKorresponderende(aktivitet, akts));
        return gjeldendeAktivitet.map(a ->
                new DatoEndring(forrigeKorresponderendeGjeldende.map(BeregningAktivitetDto::getPeriode).map(Intervall::getTomDato).orElse(null),
                        a.getPeriode().getTomDato()));
    }

    private static ToggleEndring finnSkalBrukesEndring(BeregningAktivitetDto aktivitet, Optional<BeregningAktivitetAggregatDto> forrigeRegister, Optional<BeregningAktivitetAggregatDto> forrigeGjeldende, Optional<BeregningAktivitetDto> gjeldendeAktivitet) {
        boolean skalBrukesNyVerdi = gjeldendeAktivitet.isPresent();
        var skalBrukesForrige = finnSkalBrukesForrige(aktivitet, forrigeRegister, forrigeGjeldende);
        var skalBrukesEndring = new ToggleEndring(skalBrukesForrige.orElse(null), skalBrukesNyVerdi);
        return skalBrukesEndring;
    }

    private static BeregningAktivitetNøkkel mapNøkkel(BeregningAktivitetDto aktivitet) {
        return new BeregningAktivitetNøkkel(aktivitet.getOpptjeningAktivitetType(),
                aktivitet.getPeriode().getFomDato(),
                aktivitet.getArbeidsgiver() != null ? aktivitet.getArbeidsgiver().getIdentifikator() : null,
                aktivitet.getArbeidsforholdRef() != null ? aktivitet.getArbeidsforholdRef().getReferanse() : null);
    }

    private static Optional<Boolean> finnSkalBrukesForrige(BeregningAktivitetDto aktivitet, Optional<BeregningAktivitetAggregatDto> forrigeRegister, Optional<BeregningAktivitetAggregatDto> forrigeGjeldende) {
        var forrigeKorresponderendeGjeldende = forrigeGjeldende.map(BeregningAktivitetAggregatDto::getBeregningAktiviteter)
                .flatMap(akts -> finnKorresponderende(aktivitet, akts));
        var forrigeKorresponderendeRegister = forrigeRegister.map(BeregningAktivitetAggregatDto::getBeregningAktiviteter)
                .flatMap(registerAktiviteter -> finnKorresponderende(aktivitet, registerAktiviteter));
        return forrigeKorresponderendeRegister.map(a -> forrigeKorresponderendeGjeldende.isPresent());
    }

    private static Optional<BeregningAktivitetDto> finnKorresponderende(BeregningAktivitetDto aktivitet, List<BeregningAktivitetDto> gjeldendeAktiviteter) {
        return gjeldendeAktiviteter.stream()
                .filter(a -> aktivitet.getNøkkel().equals(a.getNøkkel()))
                .findFirst();
    }

}
