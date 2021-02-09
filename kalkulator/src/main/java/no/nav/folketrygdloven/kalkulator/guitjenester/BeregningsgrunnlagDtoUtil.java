package no.nav.folketrygdloven.kalkulator.guitjenester;

import static no.nav.vedtak.konfig.Tid.TIDENES_ENDE;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.gradering.AktivitetGradering;
import no.nav.folketrygdloven.kalkulator.modell.gradering.AndelGradering.Gradering;
import no.nav.folketrygdloven.kalkulator.modell.iay.ArbeidsgiverOpplysningerDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.EksternArbeidsforholdRef;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.aksjonpunkt.FordelingGraderingTjeneste;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.felles.v1.AktørIdPersonident;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.kodeverk.Organisasjonstype;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.BeregningsgrunnlagArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.FaktaOmBeregningAndelDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.FordelBeregningsgrunnlagArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulus.typer.AktørId;
import no.nav.folketrygdloven.kalkulus.typer.OrgNummer;

public class BeregningsgrunnlagDtoUtil {

    private BeregningsgrunnlagDtoUtil() {
        // Skjul
    }

    static FaktaOmBeregningAndelDto lagFaktaOmBeregningAndel(BeregningsgrunnlagPrStatusOgAndelDto andel,
                                                             AktivitetGradering aktivitetGradering,
                                                             InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag, BeregningsgrunnlagPeriodeDto periode) {
        FaktaOmBeregningAndelDto andelDto = settVerdierForAndel(andel, aktivitetGradering, inntektArbeidYtelseGrunnlag, periode);
        andelDto.setAndelsnr(andel.getAndelsnr());
        return andelDto;
    }

    private static FaktaOmBeregningAndelDto settVerdierForAndel(BeregningsgrunnlagPrStatusOgAndelDto andel, AktivitetGradering aktivitetGradering, InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag, BeregningsgrunnlagPeriodeDto periode) {
        FaktaOmBeregningAndelDto andelDto = new FaktaOmBeregningAndelDto();
        andelDto.setAktivitetStatus(andel.getAktivitetStatus());
        andelDto.setInntektskategori(andel.getInntektskategori());
        andelDto.setFastsattAvSaksbehandler(andel.getFastsattAvSaksbehandler());
        andelDto.setLagtTilAvSaksbehandler(andel.erLagtTilAvSaksbehandler());
        andelDto.setKilde(andel.getKilde());
        List<Gradering> graderingForAndelIPeriode = FordelingGraderingTjeneste.hentGraderingerForAndelIPeriode(andel, aktivitetGradering, periode.getPeriode()).stream()
            .sorted().collect(Collectors.toList());
        finnArbeidsprosenterIPeriode(graderingForAndelIPeriode, andel.getBeregningsgrunnlagPeriode().getPeriode()).forEach(andelDto::leggTilAndelIArbeid);
        lagArbeidsforholdDto(andel, Optional.empty(), inntektArbeidYtelseGrunnlag)
            .ifPresent(andelDto::setArbeidsforhold);
        return andelDto;
    }


    public static Optional<BeregningsgrunnlagArbeidsforholdDto> lagArbeidsforholdDto(BeregningsgrunnlagPrStatusOgAndelDto andel, Optional<InntektsmeldingDto> inntektsmeldingOptional, InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag) {
        BeregningsgrunnlagArbeidsforholdDto dto = new BeregningsgrunnlagArbeidsforholdDto();
        return lagBeregningsgrunnlagArbeidsforholdDto(andel, dto, inntektsmeldingOptional, inntektArbeidYtelseGrunnlag);
    }

    public static Optional<BeregningsgrunnlagArbeidsforholdDto> lagArbeidsforholdEndringDto(BeregningsgrunnlagPrStatusOgAndelDto andel, InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag) {
        BeregningsgrunnlagArbeidsforholdDto dto = new FordelBeregningsgrunnlagArbeidsforholdDto();
        return lagBeregningsgrunnlagArbeidsforholdDto(andel, dto, Optional.empty(), inntektArbeidYtelseGrunnlag);
    }

    private static Optional<BeregningsgrunnlagArbeidsforholdDto> lagBeregningsgrunnlagArbeidsforholdDto(BeregningsgrunnlagPrStatusOgAndelDto andel,
                                                                                                        BeregningsgrunnlagArbeidsforholdDto arbeidsforhold,
                                                                                                        Optional<InntektsmeldingDto> inntektsmeldingOptional, InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag) {
        if (skalIkkeOppretteArbeidsforhold(andel)) {
            return Optional.empty();
        }
        mapBgAndelArbeidsforhold(andel, arbeidsforhold, inntektsmeldingOptional, inntektArbeidYtelseGrunnlag);
        arbeidsforhold.setArbeidsforholdType(andel.getArbeidsforholdType());
        return Optional.of(arbeidsforhold);
    }

    private static boolean skalIkkeOppretteArbeidsforhold(BeregningsgrunnlagPrStatusOgAndelDto andel) {
        boolean arbeidsforholdTypeErIkkeSatt = andel.getArbeidsforholdType() == null
            || OpptjeningAktivitetType.UDEFINERT.equals(andel.getArbeidsforholdType());
        return arbeidsforholdTypeErIkkeSatt && !andel.getBgAndelArbeidsforhold().isPresent();

    }

    private static void mapBgAndelArbeidsforhold(BeregningsgrunnlagPrStatusOgAndelDto andel,
                                                 BeregningsgrunnlagArbeidsforholdDto arbeidsforhold,
                                                 Optional<InntektsmeldingDto> inntektsmelding, InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag) {
        andel.getBgAndelArbeidsforhold().ifPresent(bga -> {
            Arbeidsgiver arbeidsgiver = bga.getArbeidsgiver();
            arbeidsforhold.setStartdato(bga.getArbeidsperiodeFom());
            arbeidsforhold.setOpphoersdato(finnKorrektOpphørsdato(andel));
            arbeidsforhold.setArbeidsforholdId(bga.getArbeidsforholdRef().getReferanse());
            arbeidsforhold.setRefusjonPrAar(bga.getGjeldendeRefusjonPrÅr());
            arbeidsforhold.setNaturalytelseBortfaltPrÅr(bga.getNaturalytelseBortfaltPrÅr().orElse(null));
            arbeidsforhold.setNaturalytelseTilkommetPrÅr(bga.getNaturalytelseTilkommetPrÅr().orElse(null));
            inntektsmelding.ifPresent(im -> arbeidsforhold.setBelopFraInntektsmeldingPrMnd(im.getInntektBeløp().getVerdi()));
            mapArbeidsgiver(arbeidsforhold, arbeidsgiver, inntektArbeidYtelseGrunnlag);
            finnEksternArbeidsforholdId(andel, inntektArbeidYtelseGrunnlag).ifPresent(ref -> arbeidsforhold.setEksternArbeidsforholdId(ref.getReferanse()));
        });
    }

    private static Optional<EksternArbeidsforholdRef> finnEksternArbeidsforholdId(BeregningsgrunnlagPrStatusOgAndelDto andel, InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        Optional<Arbeidsgiver> agOpt = andel.getArbeidsgiver();
        Optional<InternArbeidsforholdRefDto> refOpt = andel.getArbeidsforholdRef();
        if (agOpt.isEmpty() || refOpt.isEmpty()) {
            return Optional.empty();
        }
        return iayGrunnlag.getArbeidsforholdInformasjon()
                .map(d -> d.finnEkstern(agOpt.get(), refOpt.get()));
    }

    private static void mapArbeidsgiver(BeregningsgrunnlagArbeidsforholdDto arbeidsforhold, Arbeidsgiver arbeidsgiver, InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag) {
        Optional<ArbeidsgiverOpplysningerDto> opplysningerDto = inntektArbeidYtelseGrunnlag.getArbeidsgiverOpplysninger()
                .stream().filter(arbeidsgiverOpplysningerDto -> arbeidsgiver.getIdentifikator().equals(arbeidsgiverOpplysningerDto.getIdentifikator()))
                .findFirst();
        if (arbeidsgiver != null) {
                arbeidsforhold.setArbeidsgiverIdent(arbeidsgiver.getIdentifikator());
                arbeidsforhold.setArbeidsgiverId(arbeidsgiver.getIdentifikator());
            if (OrgNummer.erKunstig(arbeidsgiver.getOrgnr())) {
                arbeidsforhold.setOrganisasjonstype(Organisasjonstype.KUNSTIG);
            }
            if (!arbeidsgiver.getErVirksomhet()) {
                arbeidsforhold.setAktørId(new AktørId(arbeidsgiver.getAktørId().getId()));
                arbeidsforhold.setAktørIdPersonIdent(new AktørIdPersonident(arbeidsgiver.getAktørId().getId()));
            }
        }
        if (opplysningerDto.isPresent()) {
            if (arbeidsgiver.getErVirksomhet()) {
                arbeidsforhold.setArbeidsgiverIdVisning(opplysningerDto.get().getIdentifikator());
                arbeidsforhold.setArbeidsgiverNavn(opplysningerDto.get().getNavn());
            } else if (arbeidsgiver.erAktørId()) {
                LocalDate fødselsdato = opplysningerDto.get().getFødselsdato();
                if (fødselsdato != null) {
                    String formatertDato = fødselsdato.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                    arbeidsforhold.setArbeidsgiverId(formatertDato);
                    arbeidsforhold.setArbeidsgiverIdVisning(formatertDato);
                }
                arbeidsforhold.setArbeidsgiverNavn(opplysningerDto.get().getNavn());
            }
        }
    }

    private static LocalDate finnKorrektOpphørsdato(BeregningsgrunnlagPrStatusOgAndelDto andel) {
        return andel.getBgAndelArbeidsforhold()
            .flatMap(BGAndelArbeidsforholdDto::getArbeidsperiodeTom)
            .filter(tom -> !TIDENES_ENDE.equals(tom))
            .orElse(null);
    }

    public static List<BigDecimal> finnArbeidsprosenterIPeriode(List<Gradering> graderingForAndelIPeriode, Intervall periode) {
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
