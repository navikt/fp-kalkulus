package no.nav.folketrygdloven.kalkulator.rest.fakta;

import static no.nav.folketrygdloven.kalkulator.rest.VisningsnavnForAktivitetTjeneste.finnArbeidsgiverNavn;

import java.util.List;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.kontrakt.v1.ArbeidsgiverOpplysningerDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.ArbeidsforholdInformasjonDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;

class MapBeregningAktivitetDto {

    private MapBeregningAktivitetDto() {
        // skjul
    }

    static no.nav.folketrygdloven.kalkulator.rest.dto.BeregningAktivitetDto mapBeregningAktivitet(BeregningAktivitetDto beregningAktivitet,
                                                                                                  List<BeregningAktivitetDto> saksbehandletAktiviteter,
                                                                                                  Optional<ArbeidsforholdInformasjonDto> arbeidsforholdInformasjon, List<ArbeidsgiverOpplysningerDto> arbeidsgiverOpplysninger) {
        no.nav.folketrygdloven.kalkulator.rest.dto.BeregningAktivitetDto dto = new no.nav.folketrygdloven.kalkulator.rest.dto.BeregningAktivitetDto();
        if (beregningAktivitet.getArbeidsgiver() != null) {
            mapArbeidsgiver(dto, beregningAktivitet.getArbeidsgiver(), arbeidsgiverOpplysninger);
            dto.setArbeidsforholdId(beregningAktivitet.getArbeidsforholdRef().getReferanse());
            arbeidsforholdInformasjon.ifPresent(info -> {
                var eksternArbeidsforholdId = info.finnEkstern(beregningAktivitet.getArbeidsgiver(), beregningAktivitet.getArbeidsforholdRef());
                if (eksternArbeidsforholdId != null) {
                    dto.setEksternArbeidsforholdId(eksternArbeidsforholdId.getReferanse());
                }
            });
        }
        dto.setArbeidsforholdType(beregningAktivitet.getOpptjeningAktivitetType());
        dto.setFom(beregningAktivitet.getPeriode().getFomDato());
        dto.setTom(beregningAktivitet.getPeriode().getTomDato());
        if (!saksbehandletAktiviteter.isEmpty()) {
            dto.setSkalBrukes(saksbehandletAktiviteter.contains(beregningAktivitet));
        }
        return dto;
    }

    private static void mapArbeidsgiver(no.nav.folketrygdloven.kalkulator.rest.dto.BeregningAktivitetDto beregningAktivitetDto, Arbeidsgiver arbeidsgiver, List<ArbeidsgiverOpplysningerDto> arbeidsgiverOpplysninger) {
        if (arbeidsgiver == null) {
            return;
        }
        if (arbeidsgiver.getErVirksomhet()) {
            beregningAktivitetDto.setArbeidsgiverId(arbeidsgiver.getIdentifikator());
        } else if (arbeidsgiver.erAktørId()) {
            beregningAktivitetDto.setArbeidsgiverId(arbeidsgiver.getIdentifikator());
            beregningAktivitetDto.setAktørIdString(arbeidsgiver.getAktørId().getId());
        }
        finnArbeidsgiverNavn(arbeidsgiver, arbeidsgiverOpplysninger).ifPresent(beregningAktivitetDto::setArbeidsgiverNavn);
    }
}
