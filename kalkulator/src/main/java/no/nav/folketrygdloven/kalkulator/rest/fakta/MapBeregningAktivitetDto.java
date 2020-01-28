package no.nav.folketrygdloven.kalkulator.rest.fakta;

import java.util.List;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetRestDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.ArbeidsforholdInformasjonDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.ArbeidsgiverMedNavn;
import no.nav.folketrygdloven.kalkulator.rest.MapBeregningsgrunnlagFraRestTilDomene;

class MapBeregningAktivitetDto {

    private MapBeregningAktivitetDto() {
        // skjul
    }

    static no.nav.folketrygdloven.kalkulator.rest.dto.BeregningAktivitetDto mapBeregningAktivitet(BeregningAktivitetRestDto beregningAktivitet,
                                                                                                  List<BeregningAktivitetRestDto> saksbehandletAktiviteter,
                                                                                                  Optional<ArbeidsforholdInformasjonDto> arbeidsforholdInformasjon) {
        no.nav.folketrygdloven.kalkulator.rest.dto.BeregningAktivitetDto dto = new no.nav.folketrygdloven.kalkulator.rest.dto.BeregningAktivitetDto();
        if (beregningAktivitet.getArbeidsgiver() != null) {
            mapArbeidsgiver(dto, beregningAktivitet.getArbeidsgiver());
            dto.setArbeidsforholdId(beregningAktivitet.getArbeidsforholdRef().getReferanse());
            arbeidsforholdInformasjon.ifPresent(info -> {
                var eksternArbeidsforholdId = info.finnEkstern(MapBeregningsgrunnlagFraRestTilDomene.mapArbeidsgiver(beregningAktivitet.getArbeidsgiver()), beregningAktivitet.getArbeidsforholdRef());
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

    private static void mapArbeidsgiver(no.nav.folketrygdloven.kalkulator.rest.dto.BeregningAktivitetDto beregningAktivitetDto, ArbeidsgiverMedNavn arbeidsgiver) {
        if (arbeidsgiver == null) {
            return;
        }
        if (arbeidsgiver.getErVirksomhet()) {
            beregningAktivitetDto.setArbeidsgiverId(arbeidsgiver.getIdentifikator());
        } else if (arbeidsgiver.erAktørId()) {
            beregningAktivitetDto.setArbeidsgiverId(arbeidsgiver.getIdentifikator());
            beregningAktivitetDto.setAktørIdString(arbeidsgiver.getAktørId().getId());
        }
        beregningAktivitetDto.setArbeidsgiverNavn(arbeidsgiver.getNavn());
    }
}
