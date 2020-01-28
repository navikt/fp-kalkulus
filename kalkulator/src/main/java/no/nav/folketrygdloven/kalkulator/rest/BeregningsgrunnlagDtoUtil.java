package no.nav.folketrygdloven.kalkulator.rest;

import static no.nav.folketrygdloven.kalkulator.rest.MapBeregningsgrunnlagFraRestTilDomene.mapAndel;
import static no.nav.vedtak.konfig.Tid.TIDENES_ENDE;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.gradering.AktivitetGradering;
import no.nav.folketrygdloven.kalkulator.gradering.AndelGradering.Gradering;
import no.nav.folketrygdloven.kalkulator.kontrakt.v1.ArbeidsgiverOpplysningerDto;
import no.nav.folketrygdloven.kalkulator.kontrollerfakta.FordelBeregningsgrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelRestDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.ArbeidsgiverMedNavn;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Organisasjonstype;
import no.nav.folketrygdloven.kalkulator.rest.dto.BeregningsgrunnlagArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.rest.dto.FaktaOmBeregningAndelDto;
import no.nav.folketrygdloven.kalkulator.rest.dto.FordelBeregningsgrunnlagArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;

public class BeregningsgrunnlagDtoUtil {

    private BeregningsgrunnlagDtoUtil() {
        // Skjul
    }

    static FaktaOmBeregningAndelDto lagFaktaOmBeregningAndel(BeregningsgrunnlagPrStatusOgAndelRestDto andel,
                                                             AktivitetGradering aktivitetGradering,
                                                             InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag, BeregningsgrunnlagPeriodeRestDto periode) {
        FaktaOmBeregningAndelDto andelDto = settVerdierForAndel(andel, aktivitetGradering, inntektArbeidYtelseGrunnlag, periode);
        andelDto.setAndelsnr(andel.getAndelsnr());
        return andelDto;
    }

    private static FaktaOmBeregningAndelDto settVerdierForAndel(BeregningsgrunnlagPrStatusOgAndelRestDto andel, AktivitetGradering aktivitetGradering, InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag, BeregningsgrunnlagPeriodeRestDto periode) {
        FaktaOmBeregningAndelDto andelDto = new FaktaOmBeregningAndelDto();
        andelDto.setAktivitetStatus(andel.getAktivitetStatus());
        andelDto.setInntektskategori(andel.getInntektskategori());
        andelDto.setFastsattAvSaksbehandler(andel.getFastsattAvSaksbehandler());
        andelDto.setLagtTilAvSaksbehandler(andel.getLagtTilAvSaksbehandler());
        List<Gradering> graderingForAndelIPeriode = FordelBeregningsgrunnlagTjeneste.hentGraderingerForAndelIPeriode(mapAndel(andel), aktivitetGradering, periode.getPeriode()).stream()
            .sorted().collect(Collectors.toList());
        finnArbeidsprosenterIPeriode(graderingForAndelIPeriode, andel.getBeregningsgrunnlagPeriode().getPeriode()).forEach(andelDto::leggTilAndelIArbeid);
        lagArbeidsforholdDto(andel, Optional.empty(), inntektArbeidYtelseGrunnlag)
            .ifPresent(andelDto::setArbeidsforhold);
        return andelDto;
    }


    public static Optional<BeregningsgrunnlagArbeidsforholdDto> lagArbeidsforholdDto(BeregningsgrunnlagPrStatusOgAndelRestDto andel, Optional<InntektsmeldingDto> inntektsmeldingOptional, InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag) {
        BeregningsgrunnlagArbeidsforholdDto dto = new BeregningsgrunnlagArbeidsforholdDto();
        return lagBeregningsgrunnlagArbeidsforholdDto(andel, dto, inntektsmeldingOptional, inntektArbeidYtelseGrunnlag);
    }

    public static Optional<BeregningsgrunnlagArbeidsforholdDto> lagArbeidsforholdEndringDto(BeregningsgrunnlagPrStatusOgAndelRestDto andel, InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag) {
        BeregningsgrunnlagArbeidsforholdDto dto = new FordelBeregningsgrunnlagArbeidsforholdDto();
        return lagBeregningsgrunnlagArbeidsforholdDto(andel, dto, Optional.empty(), inntektArbeidYtelseGrunnlag);
    }

    private static Optional<BeregningsgrunnlagArbeidsforholdDto> lagBeregningsgrunnlagArbeidsforholdDto(BeregningsgrunnlagPrStatusOgAndelRestDto andel,
                                                                                                        BeregningsgrunnlagArbeidsforholdDto arbeidsforhold,
                                                                                                        Optional<InntektsmeldingDto> inntektsmeldingOptional, InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag) {
        if (skalIkkeOppretteArbeidsforhold(andel)) {
            return Optional.empty();
        }
        mapBgAndelArbeidsforhold(andel, arbeidsforhold, inntektsmeldingOptional, inntektArbeidYtelseGrunnlag);
        arbeidsforhold.setArbeidsforholdType(andel.getArbeidsforholdType());
        return Optional.of(arbeidsforhold);
    }

    private static boolean skalIkkeOppretteArbeidsforhold(BeregningsgrunnlagPrStatusOgAndelRestDto andel) {
        boolean arbeidsforholdTypeErIkkeSatt = andel.getArbeidsforholdType() == null
            || OpptjeningAktivitetType.UDEFINERT.equals(andel.getArbeidsforholdType());
        return arbeidsforholdTypeErIkkeSatt && !andel.getBgAndelArbeidsforhold().isPresent();

    }

    private static void mapBgAndelArbeidsforhold(BeregningsgrunnlagPrStatusOgAndelRestDto andel,
                                                 BeregningsgrunnlagArbeidsforholdDto arbeidsforhold,
                                                 Optional<InntektsmeldingDto> inntektsmelding, InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag) {
        andel.getBgAndelArbeidsforhold().ifPresent(bga -> {
            ArbeidsgiverMedNavn arbeidsgiver = bga.getArbeidsgiver();
            arbeidsforhold.setStartdato(bga.getArbeidsperiodeFom());
            arbeidsforhold.setOpphoersdato(finnKorrektOpphørsdato(andel));
            arbeidsforhold.setArbeidsforholdId(bga.getArbeidsforholdRef().getReferanse());
            arbeidsforhold.setRefusjonPrAar(bga.getRefusjonskravPrÅr());
            arbeidsforhold.setNaturalytelseBortfaltPrÅr(bga.getNaturalytelseBortfaltPrÅr().orElse(null));
            arbeidsforhold.setNaturalytelseTilkommetPrÅr(bga.getNaturalytelseTilkommetPrÅr().orElse(null));
            inntektsmelding.ifPresent(im -> arbeidsforhold.setBelopFraInntektsmeldingPrMnd(im.getInntektBeløp().getVerdi()));
            mapArbeidsgiver(arbeidsforhold, arbeidsgiver, inntektArbeidYtelseGrunnlag);
        });
    }

    private static void mapArbeidsgiver(BeregningsgrunnlagArbeidsforholdDto arbeidsforhold, ArbeidsgiverMedNavn arbeidsgiver, InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag) {
        Optional<ArbeidsgiverOpplysningerDto> opplysningerDto = inntektArbeidYtelseGrunnlag.getArbeidsgiverOpplysningerDto().stream().filter(opplysnigner -> arbeidsgiver.getIdentifikator().equals(opplysnigner.getIdentifikator()))
            .findFirst();
        if (opplysningerDto.isPresent()) {
            if (arbeidsgiver.getErVirksomhet()) {
                arbeidsforhold.setArbeidsgiverId(opplysningerDto.get().getIdentifikator());
                arbeidsforhold.setArbeidsgiverNavn(opplysningerDto.get().getNavn());
                if (Organisasjonstype.erKunstig(arbeidsgiver.getOrgnr())) {
                    arbeidsforhold.setOrganisasjonstype(Organisasjonstype.KUNSTIG);
                }
            } else if (arbeidsgiver.erAktørId()) {
                arbeidsforhold.setAktørId(arbeidsgiver.getAktørId());
                String fødselsdato = opplysningerDto.get().getFødselsdato().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                arbeidsforhold.setArbeidsgiverId(fødselsdato);
                arbeidsforhold.setArbeidsgiverNavn(opplysningerDto.get().getNavn());
            }
        }
    }

    private static LocalDate finnKorrektOpphørsdato(BeregningsgrunnlagPrStatusOgAndelRestDto andel) {
        return andel.getBgAndelArbeidsforhold()
            .flatMap(BGAndelArbeidsforholdRestDto::getArbeidsperiodeTom)
            .filter(tom -> !TIDENES_ENDE.equals(tom))
            .orElse(null);
    }

    static List<BigDecimal> finnArbeidsprosenterIPeriode(List<Gradering> graderingForAndelIPeriode, Intervall periode) {
        List<BigDecimal> prosentAndelerIPeriode = new ArrayList<>();
        if (graderingForAndelIPeriode.isEmpty()) {
            leggTilNullProsent(prosentAndelerIPeriode);
            return prosentAndelerIPeriode;
        }
        Gradering gradering = graderingForAndelIPeriode.get(0);
        prosentAndelerIPeriode.add(gradering.getArbeidstidProsent());
        if (graderingForAndelIPeriode.size() == 1) {
            if (graderingDekkerHeilePerioden(gradering, periode)) {
                return prosentAndelerIPeriode;
            }
            prosentAndelerIPeriode.add(BigDecimal.ZERO);
            return prosentAndelerIPeriode;
        }

        for (int i = 1; i < graderingForAndelIPeriode.size(); i++) {
            Gradering nesteGradering = graderingForAndelIPeriode.get(i);
            prosentAndelerIPeriode.add(nesteGradering.getArbeidstidProsent());
            if (!gradering.getPeriode().getFomDato().isBefore(periode.getTomDato())) {
                break;
            }
            if (gradering.getPeriode().getTomDato().isBefore(nesteGradering.getPeriode().getFomDato()) &&
                !gradering.getPeriode().getTomDato().equals(nesteGradering.getPeriode().getFomDato().minusDays(1))) {
                leggTilNullProsent(prosentAndelerIPeriode);
            }
            gradering = nesteGradering;
        }

        if (periode.getTomDato() == null || gradering.getPeriode().getTomDato().isBefore(periode.getTomDato())) {
            leggTilNullProsent(prosentAndelerIPeriode);
        }

        return prosentAndelerIPeriode;
    }

    private static void leggTilNullProsent(List<BigDecimal> prosentAndelerIPeriode) {
        if (!prosentAndelerIPeriode.contains(BigDecimal.ZERO)) {
            prosentAndelerIPeriode.add(BigDecimal.ZERO);
        }
    }

    private static boolean graderingDekkerHeilePerioden(Gradering gradering, Intervall periode) {
        return !gradering.getPeriode().getFomDato().isAfter(periode.getFomDato()) &&
            (gradering.getPeriode().getTomDato().equals(TIDENES_ENDE) ||
                (periode.getTomDato() != null &&
                    !gradering.getPeriode().getTomDato().isBefore(periode.getTomDato())));
    }

}
