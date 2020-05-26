package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.frisinn;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Aktivitet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapBeregningAktiviteterFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.kodeverk.MapOpptjeningAktivitetTypeFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittOpptjeningDto;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.OrgNummer;
import no.nav.folketrygdloven.kalkulator.opptjening.OpptjeningAktiviteterDto;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivPeriode;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModell;

@ApplicationScoped
@FagsakYtelseTypeRef("FRISINN")
public class MapBeregningAktiviteterFraVLTilRegelFRISINN extends MapBeregningAktiviteterFraVLTilRegel {

    public static final String INGEN_AKTIVITET_MELDING = "Må ha aktiviteter for å sette status.";

    @Override
    public AktivitetStatusModell mapForSkjæringstidspunkt(BeregningsgrunnlagInput input) {
        LocalDate opptjeningSkjæringstidspunkt = input.getSkjæringstidspunktOpptjening();

        AktivitetStatusModell modell = new AktivitetStatusModell();
        modell.setSkjæringstidspunktForOpptjening(opptjeningSkjæringstidspunkt);

        var relevanteAktiviteter = input.getOpptjeningAktiviteterForBeregning();

        Optional<OppgittOpptjeningDto> oppgittOpptjening = input.getIayGrunnlag().getOppgittOpptjening();
        boolean harFLEtterStp = harOppgittFLEtterStpOpptjening(opptjeningSkjæringstidspunkt, oppgittOpptjening);
        boolean harSNEtterStp = harOppgittSNEtterStpOpptjening(opptjeningSkjæringstidspunkt, oppgittOpptjening);
        boolean harATEtterSTP = harOppgittArbeidsinntektEtterSTP(opptjeningSkjæringstidspunkt, oppgittOpptjening);
        if (relevanteAktiviteter.isEmpty()) { // For enklere feilsøking når det mangler aktiviteter
            throw new IllegalStateException(INGEN_AKTIVITET_MELDING);
        } else {
            relevanteAktiviteter.forEach(opptjeningsperiode -> modell.leggTilEllerOppdaterAktivPeriode(
                    lagAktivPeriode(input.getInntektsmeldinger(), opptjeningsperiode, harFLEtterStp, harSNEtterStp, opptjeningSkjæringstidspunkt)));
        }


        // Legger til 48 mnd med frilans og næring rundt stp om det ikkje finnes, legger også til arbeidsaktivitet om det ikke finnes fra før og er oppgitt
        Periode hardkodetOpptjeningsperiode = Periode.of(opptjeningSkjæringstidspunkt.minusMonths(36), opptjeningSkjæringstidspunkt.plusMonths(12));
        if (relevanteAktiviteter.stream().noneMatch(a -> a.getType().equals(OpptjeningAktivitetType.FRILANS)) && harFLEtterStp) {
            modell.leggTilEllerOppdaterAktivPeriode(AktivPeriode.forFrilanser(hardkodetOpptjeningsperiode));
        }
        if (relevanteAktiviteter.stream().noneMatch(a -> a.getType().equals(OpptjeningAktivitetType.NÆRING)) && harSNEtterStp) {
            modell.leggTilEllerOppdaterAktivPeriode(AktivPeriode.forAndre(Aktivitet.NÆRINGSINNTEKT, hardkodetOpptjeningsperiode));
        }
        if (relevanteAktiviteter.stream().noneMatch(a -> a.getType().equals(OpptjeningAktivitetType.ARBEID)) && harATEtterSTP) {
            modell.leggTilEllerOppdaterAktivPeriode(AktivPeriode.forArbeidstakerHosVirksomhet(hardkodetOpptjeningsperiode, OrgNummer.KUNSTIG_ORG, null));
        }
        return modell;
    }

    private boolean harOppgittArbeidsinntektEtterSTP(LocalDate opptjeningSkjæringstidspunkt, Optional<OppgittOpptjeningDto> oppgittOpptjening) {
        List<OppgittArbeidsforholdDto> oppgitteArbfor = oppgittOpptjening.map(OppgittOpptjeningDto::getOppgittArbeidsforhold).orElse(Collections.emptyList());
        return oppgitteArbfor.stream().anyMatch(oa -> !oa.getFraOgMed().isBefore(opptjeningSkjæringstidspunkt));
    }

    private boolean harOppgittFLEtterStpOpptjening(LocalDate opptjeningSkjæringstidspunkt, Optional<OppgittOpptjeningDto> oppgittOpptjening) {
        return oppgittOpptjening.flatMap(OppgittOpptjeningDto::getFrilans)
                    .filter(fl -> fl.getOppgittFrilansInntekt().stream()
                            .anyMatch(ip -> !ip.getPeriode().getTomDato().isBefore(opptjeningSkjæringstidspunkt)))
                    .isPresent();
    }

    private boolean harOppgittSNEtterStpOpptjening(LocalDate opptjeningSkjæringstidspunkt, Optional<OppgittOpptjeningDto> oppgittOpptjening) {
        return oppgittOpptjening.map(OppgittOpptjeningDto::getEgenNæring)
                .filter(fl -> fl.stream()
                        .anyMatch(ip -> !ip.getPeriode().getTomDato().isBefore(opptjeningSkjæringstidspunkt)))
                .isPresent();
    }


    private AktivPeriode lagAktivPeriode(Collection<InntektsmeldingDto> inntektsmeldinger,
                                         OpptjeningAktiviteterDto.OpptjeningPeriodeDto opptjeningsperiode,
                                         boolean harFLEtterStp,
                                         boolean harSNEtterStp,
                                         LocalDate opptjeningSkjæringstidspunkt) {
        Aktivitet aktivitetType = MapOpptjeningAktivitetTypeFraVLTilRegel.map(opptjeningsperiode.getOpptjeningAktivitetType());
        Periode gjeldendePeriode = opptjeningsperiode.getPeriode();
        Periode utvidetPeriode = Periode.of(opptjeningSkjæringstidspunkt.minusMonths(36), opptjeningSkjæringstidspunkt.plusMonths(12));

        if (Aktivitet.FRILANSINNTEKT.equals(aktivitetType)) {
            return AktivPeriode.forFrilanser(harFLEtterStp ? utvidetPeriode : gjeldendePeriode);
        } else if (Aktivitet.ARBEIDSTAKERINNTEKT.equals(aktivitetType)) {
            var opptjeningArbeidsgiverAktørId = opptjeningsperiode.getArbeidsgiverAktørId();
            var opptjeningArbeidsgiverOrgnummer = opptjeningsperiode.getArbeidsgiverOrgNummer();
            var opptjeningArbeidsforhold = Optional.ofNullable(opptjeningsperiode.getArbeidsforholdId()).orElse(InternArbeidsforholdRefDto.nullRef());
            return lagAktivPeriodeForArbeidstaker(inntektsmeldinger, gjeldendePeriode, opptjeningArbeidsgiverAktørId,
                    opptjeningArbeidsgiverOrgnummer, opptjeningArbeidsforhold);
        } if (Aktivitet.NÆRINGSINNTEKT.equals(aktivitetType)) {
            return AktivPeriode.forAndre(aktivitetType, harSNEtterStp ? utvidetPeriode : gjeldendePeriode);
        } else {
            return AktivPeriode.forAndre(aktivitetType, gjeldendePeriode);
        }
    }


}
