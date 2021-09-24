package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import static no.nav.folketrygdloven.kalkulator.guitjenester.VisningsnavnForAktivitetTjeneste.finnArbeidsgiverNavn;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.iay.ArbeidsgiverOpplysningerDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.ArbeidsforholdInformasjonDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;

class MapBeregningAktivitetDto {

    private MapBeregningAktivitetDto() {
        // skjul
    }

    static no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.BeregningAktivitetDto mapBeregningAktivitet(BeregningAktivitetDto beregningAktivitet,
                                                                                                                          List<BeregningAktivitetDto> saksbehandletAktiviteter,
                                                                                                                          Optional<ArbeidsforholdInformasjonDto> arbeidsforholdInformasjon,
                                                                                                                          List<ArbeidsgiverOpplysningerDto> arbeidsgiverOpplysninger) {
        var dto = new no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.BeregningAktivitetDto();
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
            Optional<BeregningAktivitetDto> matchetAktivitet = saksbehandletAktiviteter.stream()
                .filter(a -> a.getNøkkel().equals(beregningAktivitet.getNøkkel())).findFirst();
            matchetAktivitet.ifPresentOrElse(aktivitet -> {
                dto.setSkalBrukes(true);
                dto.setTom(aktivitet.getPeriode().getTomDato());
            }, () -> dto.setSkalBrukes(false));
        }
        return dto;
    }

    private static void mapArbeidsgiver(no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.BeregningAktivitetDto beregningAktivitetDto,
                                        Arbeidsgiver arbeidsgiver,
                                        List<ArbeidsgiverOpplysningerDto> arbeidsgiverOpplysninger) {
        if (arbeidsgiver == null) {
            return;
        }
        beregningAktivitetDto.setArbeidsgiverIdent(arbeidsgiver.getIdentifikator());
        beregningAktivitetDto.setArbeidsgiverId(arbeidsgiver.getIdentifikator());

        if (arbeidsgiver.erAktørId()) {
            setArbeidsgiverIdForVisning(beregningAktivitetDto, arbeidsgiver, arbeidsgiverOpplysninger);
            beregningAktivitetDto.setAktørIdString(arbeidsgiver.getAktørId().getId());
        }
        finnArbeidsgiverNavn(arbeidsgiver, arbeidsgiverOpplysninger).ifPresent(beregningAktivitetDto::setArbeidsgiverNavn);
    }

    private static void setArbeidsgiverIdForVisning(no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.BeregningAktivitetDto beregningAktivitetDto,
                                                    Arbeidsgiver arbeidsgiver, List<ArbeidsgiverOpplysningerDto> arbeidsgiverOpplysninger) {
        Optional<ArbeidsgiverOpplysningerDto> opplysningerDto = arbeidsgiverOpplysninger.stream()
            .filter(aOppl -> aOppl.getIdentifikator().equals(arbeidsgiver.getIdentifikator()))
            .findFirst();
        Optional<LocalDate> fødselsdato = opplysningerDto.map(ArbeidsgiverOpplysningerDto::getFødselsdato);
        if (fødselsdato.isPresent()) {
            String formatertDato = fødselsdato.get().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
            beregningAktivitetDto.setArbeidsgiverIdVisning(formatertDato);
        }
    }
}
