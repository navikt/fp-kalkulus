package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import static no.nav.folketrygdloven.kalkulator.guitjenester.VisningsnavnForAktivitetTjeneste.finnArbeidsgiverNavn;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.kontrakt.v1.ArbeidsgiverOpplysningerDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.ArbeidsforholdInformasjonDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.felles.v1.AktørId;
import no.nav.folketrygdloven.kalkulus.felles.v1.AktørIdPersonident;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;

class MapBeregningAktivitetDto {

    private MapBeregningAktivitetDto() {
        // skjul
    }

    static no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.BeregningAktivitetDto mapBeregningAktivitet(BeregningAktivitetDto beregningAktivitet,
                                                                                                  List<BeregningAktivitetDto> saksbehandletAktiviteter,
                                                                                                  Optional<ArbeidsforholdInformasjonDto> arbeidsforholdInformasjon,
                                                                                                                          List<ArbeidsgiverOpplysningerDto> arbeidsgiverOpplysninger) {
        no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.BeregningAktivitetDto dto = new no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.BeregningAktivitetDto();
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
        dto.setArbeidsforholdType(new OpptjeningAktivitetType(beregningAktivitet.getOpptjeningAktivitetType().getKode()));
        dto.setFom(beregningAktivitet.getPeriode().getFomDato());
        dto.setTom(beregningAktivitet.getPeriode().getTomDato());
        if (!saksbehandletAktiviteter.isEmpty()) {
            dto.setSkalBrukes(saksbehandletAktiviteter.contains(beregningAktivitet));
        }
        return dto;
    }

    private static void mapArbeidsgiver(no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.BeregningAktivitetDto beregningAktivitetDto,
                                        Arbeidsgiver arbeidsgiver,
                                        List<ArbeidsgiverOpplysningerDto> arbeidsgiverOpplysninger) {
        if (arbeidsgiver == null) {
            return;
        }

        if (arbeidsgiver.getErVirksomhet()) {
            beregningAktivitetDto.setArbeidsgiverId(arbeidsgiver.getIdentifikator());
        } else if (arbeidsgiver.erAktørId()) {
            setArbeidsgiverIdForVisning(beregningAktivitetDto, arbeidsgiver, arbeidsgiverOpplysninger);
            beregningAktivitetDto.setArbeidsgiverId(arbeidsgiver.getIdentifikator());
            beregningAktivitetDto.setAktørIdString(arbeidsgiver.getAktørId().getId());
        }
        finnArbeidsgiverNavn(arbeidsgiver, arbeidsgiverOpplysninger).ifPresent(beregningAktivitetDto::setArbeidsgiverNavn);
    }

    private static void setArbeidsgiverIdForVisning(no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.BeregningAktivitetDto beregningAktivitetDto, Arbeidsgiver arbeidsgiver, List<ArbeidsgiverOpplysningerDto> arbeidsgiverOpplysninger) {
        Optional<ArbeidsgiverOpplysningerDto> opplysningerDto = arbeidsgiverOpplysninger.stream().filter(aOppl -> aOppl.getIdentifikator().equals(arbeidsgiver.getIdentifikator()))
                .findFirst();
        Optional<LocalDate> fødselsdato = opplysningerDto.map(ArbeidsgiverOpplysningerDto::getFødselsdato);
        if (fødselsdato.isPresent()) {
            String formatertDato = fødselsdato.get().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
            beregningAktivitetDto.setArbeidsgiverIdVisning(formatertDato);
        }
    }
}
