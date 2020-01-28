package no.nav.folketrygdloven.kalkulator.rest.fakta;

import static no.nav.folketrygdloven.kalkulator.rest.MapBeregningsgrunnlagFraDomeneTilRest.mapArbeidsgiver;
import static no.nav.folketrygdloven.kalkulator.rest.MapBeregningsgrunnlagFraRestTilDomene.mapBeregningsgrunnlag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.kontrollerfakta.KontrollerFaktaBeregningFrilanserTjeneste;
import no.nav.folketrygdloven.kalkulator.kontrollerfakta.KontrollerFaktaBeregningTjeneste;
import no.nav.folketrygdloven.kalkulator.kontrollerfakta.LønnsendringTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.behandling.BehandlingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.AktørId;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.ArbeidsgiverMedNavn;
import no.nav.folketrygdloven.kalkulator.rest.BeregningsgrunnlagDtoUtil;
import no.nav.folketrygdloven.kalkulator.rest.dto.ATogFLISammeOrganisasjonDto;
import no.nav.folketrygdloven.kalkulator.rest.dto.FaktaOmBeregningAndelDto;

// TODO (Safir) Denne bør splittes opp til tre klasser: ei klasse for ATFL i samme org, ei for frilanser og ei for AT med lønnsendring (uten inntektsmelding)
@ApplicationScoped
public class FaktaOmBeregningAndelDtoTjeneste {

    static Optional<FaktaOmBeregningAndelDto> lagFrilansAndelDto(BeregningsgrunnlagRestDto beregningsgrunnlag, InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag) {
        if (beregningsgrunnlag.getBeregningsgrunnlagPerioder().isEmpty()) {
            return Optional.empty();
        }
        BeregningsgrunnlagPeriodeRestDto førstePeriode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPrStatusOgAndelRestDto frilansAndel = førstePeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .filter(a -> a.getAktivitetStatus().equals(AktivitetStatus.FRILANSER))
            .findFirst()
            .orElse(null);
        if (frilansAndel != null) {
            FaktaOmBeregningAndelDto dto = new FaktaOmBeregningAndelDto();
            BeregningsgrunnlagDtoUtil.lagArbeidsforholdDto(frilansAndel, Optional.empty(), inntektArbeidYtelseGrunnlag)
                .ifPresent(dto::setArbeidsforhold);
            dto.setInntektskategori(frilansAndel.getInntektskategori());
            dto.setAndelsnr(frilansAndel.getAndelsnr());
            return Optional.of(dto);
        }
        return Optional.empty();
    }

    /// ATFL I samme organisasjon
    static List<ATogFLISammeOrganisasjonDto> lagATogFLISAmmeOrganisasjonListe(BehandlingReferanse behandlingReferanse,
                                                                       BeregningsgrunnlagRestDto beregningsgrunnlag,
                                                                       Collection<InntektsmeldingDto> inntektsmeldinger,
                                                                       InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag) {
        Set<Arbeidsgiver> arbeidsgivere = KontrollerFaktaBeregningFrilanserTjeneste
            .brukerErArbeidstakerOgFrilanserISammeOrganisasjon(behandlingReferanse.getAktørId(), mapBeregningsgrunnlag(beregningsgrunnlag), inntektArbeidYtelseGrunnlag);
        if (arbeidsgivere.isEmpty()) {
            return Collections.emptyList();
        }

        Set<String> arbeidsgivereSomErVirksomheter = arbeidsgivere
            .stream()
            .filter(Arbeidsgiver::getErVirksomhet)
            .map(Arbeidsgiver::getOrgnr)
            .collect(Collectors.toSet());

        Map<String, List<InntektsmeldingDto>> inntektsmeldingMap = KontrollerFaktaBeregningTjeneste
            .hentInntektsmeldingerForVirksomheter(arbeidsgivereSomErVirksomheter, inntektsmeldinger);
        List<BeregningsgrunnlagPrStatusOgAndelRestDto> andeler = beregningsgrunnlag.getBeregningsgrunnlagPerioder()
            .get(0)
            .getBeregningsgrunnlagPrStatusOgAndelList()
            .stream()
            .filter(andel -> andel.getAktivitetStatus().erArbeidstaker())
            .collect(Collectors.toList());

        List<ATogFLISammeOrganisasjonDto> resultatListe = new ArrayList<>();
        for (Arbeidsgiver arbeidsgiver : arbeidsgivere) {
            andeler.stream()
                .filter(
                    andel -> andel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdRestDto::getArbeidsgiver).map(a -> a.equals(mapArbeidsgiver(arbeidsgiver))).orElse(false))
                .forEach(andel -> resultatListe.add(lagATogFLISAmmeOrganisasjon(andel, inntektsmeldingMap, inntektArbeidYtelseGrunnlag)));
        }
        return resultatListe;
    }

    private static ATogFLISammeOrganisasjonDto lagATogFLISAmmeOrganisasjon(BeregningsgrunnlagPrStatusOgAndelRestDto andel,
                                                                           Map<String, List<InntektsmeldingDto>> inntektsmeldingMap,
                                                                           InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag) {
        ATogFLISammeOrganisasjonDto dto = new ATogFLISammeOrganisasjonDto();
        BeregningsgrunnlagDtoUtil.lagArbeidsforholdDto(andel, Optional.empty(), inntektArbeidYtelseGrunnlag)
            .ifPresent(dto::setArbeidsforhold);
        dto.setAndelsnr(andel.getAndelsnr());
        dto.setInntektskategori(andel.getInntektskategori());

        // Privapersoner sender ikke inntektsmelding, disse må alltid fastsettes
        if (andel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdRestDto::getArbeidsgiver).map(ArbeidsgiverMedNavn::getErVirksomhet).orElse(false)) {
            Optional<InntektsmeldingDto> inntektsmelding = andel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdRestDto::getArbeidsgiver)
                .filter(ArbeidsgiverMedNavn::getErVirksomhet)
                .flatMap(arbeidsgiver -> finnRiktigInntektsmelding(
                    inntektsmeldingMap,
                    arbeidsgiver.getOrgnr(),
                    andel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdRestDto::getArbeidsforholdRef)));
            inntektsmelding.ifPresent(im -> dto.setInntektPrMnd(im.getInntektBeløp().getVerdi()));
        }
        return dto;
    }

    /// Arbeidsforhold uten inntektsmelding
    static List<FaktaOmBeregningAndelDto> lagArbeidsforholdUtenInntektsmeldingDtoList(AktørId aktørId,
                                                                               BeregningsgrunnlagRestDto beregningsgrunnlag,
                                                                               InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag) {
        List<YrkesaktivitetDto> aktiviteterMedLønnsendring = LønnsendringTjeneste.finnAlleAktiviteterMedLønnsendringUtenInntektsmelding(aktørId,
            mapBeregningsgrunnlag(beregningsgrunnlag), inntektArbeidYtelseGrunnlag);
        if (aktiviteterMedLønnsendring.isEmpty()) {
            return Collections.emptyList();
        }
        List<BeregningsgrunnlagPrStatusOgAndelRestDto> andeler = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList()
            .stream()
            .filter(andel -> andel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdRestDto::getArbeidsgiver).filter(ArbeidsgiverMedNavn::getErVirksomhet).isPresent())
            .collect(Collectors.toList());
        List<FaktaOmBeregningAndelDto> arbeidsforholdMedLønnsendringUtenIMDtoList = new ArrayList<>();
        for (YrkesaktivitetDto yrkesaktivitet : aktiviteterMedLønnsendring) {
            BeregningsgrunnlagPrStatusOgAndelRestDto korrektAndel = finnKorrektAndelFraArbeidsgiver(andeler, yrkesaktivitet.getArbeidsgiver());
            FaktaOmBeregningAndelDto dto = lagArbeidsforholdUtenInntektsmeldingDto(korrektAndel, inntektArbeidYtelseGrunnlag);
            arbeidsforholdMedLønnsendringUtenIMDtoList.add(dto);
        }
        return arbeidsforholdMedLønnsendringUtenIMDtoList;
    }

    private static FaktaOmBeregningAndelDto lagArbeidsforholdUtenInntektsmeldingDto(BeregningsgrunnlagPrStatusOgAndelRestDto andel, InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag) {
        FaktaOmBeregningAndelDto dto = new FaktaOmBeregningAndelDto();
        BeregningsgrunnlagDtoUtil.lagArbeidsforholdDto(andel, Optional.empty(), inntektArbeidYtelseGrunnlag)
            .ifPresent(dto::setArbeidsforhold);
        dto.setAndelsnr(andel.getAndelsnr());
        dto.setInntektskategori(andel.getInntektskategori());
        return dto;
    }

    private static BeregningsgrunnlagPrStatusOgAndelRestDto finnKorrektAndelFraArbeidsgiver(List<BeregningsgrunnlagPrStatusOgAndelRestDto> andeler, Arbeidsgiver arbeidsgiver) {
        return andeler.stream()
            .filter(andel -> andel.getBgAndelArbeidsforhold()
                .map(BGAndelArbeidsforholdRestDto::getArbeidsgiver)
                .map(ag -> ag.equals(mapArbeidsgiver(arbeidsgiver)))
                .orElse(false))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Utviklerfeil: Finner ikke korrekt andel for yrkesaktiviteten"));
    }

    private static Optional<InntektsmeldingDto> finnRiktigInntektsmelding(Map<String, List<InntektsmeldingDto>> inntektsmeldingMap, String virksomhetOrgnr,
                                                                          Optional<InternArbeidsforholdRefDto> arbeidsforholdRef) {
        if (!inntektsmeldingMap.containsKey(virksomhetOrgnr)) {
            return Optional.empty();
        }
        Collection<InntektsmeldingDto> inntektsmeldinger = inntektsmeldingMap.get(virksomhetOrgnr);
        if (inntektsmeldinger.size() == 1) {
            return Optional.ofNullable(List.copyOf(inntektsmeldinger).get(0));
        }
        return inntektsmeldinger.stream()
            .filter(im -> arbeidsforholdRef.map(ref -> ref.equals(im.getArbeidsforholdRef()))
                .orElse(false))
            .findFirst();
    }

}
