package no.nav.folketrygdloven.kalkulator.rest.fakta;

import static no.nav.folketrygdloven.kalkulator.rest.MapBeregningsgrunnlagFraRestTilDomene.mapArbeidsgiver;

import java.time.LocalDate;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.HentBekreftetPermisjon;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdRestDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.BekreftetPermisjonDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.ArbeidsgiverMedNavn;
import no.nav.folketrygdloven.kalkulator.rest.dto.PermisjonDto;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BekreftetPermisjonStatus;

class UtledBekreftetPermisjonerTilDto {

    private UtledBekreftetPermisjonerTilDto() {
        // skjul default
    }

    static Optional<PermisjonDto> utled(InntektArbeidYtelseGrunnlagDto grunnlag, LocalDate stp, BGAndelArbeidsforholdRestDto bgAndelArbeidsforhold) {
        ArbeidsgiverMedNavn arbeidsgiver = bgAndelArbeidsforhold.getArbeidsgiver();
        InternArbeidsforholdRefDto arbeidsforholdRef = bgAndelArbeidsforhold.getArbeidsforholdRef();
        Optional<BekreftetPermisjonDto> permisjonForYrkesaktivitet = HentBekreftetPermisjon.hent(grunnlag, mapArbeidsgiver(arbeidsgiver), arbeidsforholdRef);
        Optional<BekreftetPermisjonDto> bekreftetPermisjonOpt = finnBekreftetPermisjonSomOverlapperStp(stp, permisjonForYrkesaktivitet);
        if (bekreftetPermisjonOpt.isPresent()) {
            PermisjonDto dto = lagPermisjonDto(bekreftetPermisjonOpt.get());
            return Optional.of(dto);
        }
        return Optional.empty();
    }

    private static Optional<BekreftetPermisjonDto> finnBekreftetPermisjonSomOverlapperStp(LocalDate stp, Optional<BekreftetPermisjonDto> permisjonForYrkesaktivitet) {
        return permisjonForYrkesaktivitet
            .filter(perm -> perm.getStatus().equals(BekreftetPermisjonStatus.BRUK_PERMISJON))
            .filter(perm -> perm.getPeriode().inkluderer(stp));
    }

    private static PermisjonDto lagPermisjonDto(BekreftetPermisjonDto bekreftetPermisjonOpt) {
        LocalDate fomDato = bekreftetPermisjonOpt.getPeriode().getFomDato();
        LocalDate tomDato = bekreftetPermisjonOpt.getPeriode().getTomDato();
        return new PermisjonDto(fomDato, tomDato);
    }

}
