package no.nav.folketrygdloven.kalkulus.håndtering.mapping;

import java.util.List;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.DagpengeAndelLagtTilBesteberegningDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.OverstyrBeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.avklaraktiviteter.AvklarteAktiviteterDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.avklaraktiviteter.BeregningsaktivitetLagreDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta.ArbeidstakerandelUtenIMMottarYtelseDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta.BesteberegningFødendeKvinneAndelDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta.BesteberegningFødendeKvinneDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta.FaktaBeregningLagreDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta.FastsattBrukersAndel;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta.FastsatteAndelerTidsbegrensetDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta.FastsattePerioderTidsbegrensetDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta.FastsatteVerdierDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta.FastsettBeregningsgrunnlagAndelDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta.FastsettBgKunYtelseDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta.FastsettEtterlønnSluttpakkeDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta.FastsettMånedsinntektFLDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta.FastsettMånedsinntektUtenInntektsmeldingAndelDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta.FastsettMånedsinntektUtenInntektsmeldingDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta.MottarYtelseDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta.RedigerbarAndelDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta.RefusjonskravPrArbeidsgiverVurderingDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta.VurderATogFLiSammeOrganisasjonAndelDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta.VurderATogFLiSammeOrganisasjonDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta.VurderEtterlønnSluttpakkeDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta.VurderLønnsendringDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta.VurderMilitærDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta.VurderNyoppstartetFLDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta.VurderSelvstendigNæringsdrivendeNyIArbeidslivetDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta.VurderTidsbegrensetArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta.VurderteArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fordeling.FordelBeregningsgrunnlagAndelDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fordeling.FordelBeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fordeling.FordelBeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fordeling.FordelFastsatteVerdierDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fordeling.FordelRedigerbarAndelDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.foreslå.FastsettBGTidsbegrensetArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.foreslå.FastsettBeregningsgrunnlagATFLHåndteringDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.foreslå.FastsettBruttoBeregningsgrunnlagSNforNyIArbeidslivetDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.foreslå.InntektPrAndelDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.overstyring.OverstyrBeregningsgrunnlagHåndteringDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.refusjon.VurderRefusjonAndelBeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.refusjon.VurderRefusjonBeregningsgrunnlagDto;


public class OppdatererDtoMapper {
    public static no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FastsettBeregningsgrunnlagATFLDto mapFastsettBeregningsgrunnlagATFLDto(FastsettBeregningsgrunnlagATFLHåndteringDto tilKalkulus) {
        return new no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FastsettBeregningsgrunnlagATFLDto(mapTilInntektPrAndelListe(tilKalkulus.getInntektPrAndelList()), tilKalkulus.getInntektFrilanser());
    }

    public static no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FastsettBGTidsbegrensetArbeidsforholdDto mapFastsettBGTidsbegrensetArbeidsforholdDto(FastsettBGTidsbegrensetArbeidsforholdDto tidsbegrensetDto) {
        return new no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FastsettBGTidsbegrensetArbeidsforholdDto(
            tidsbegrensetDto.getFastsatteTidsbegrensedePerioder() == null ? null : mapTilFastsattTidsbegrensetPerioder(tidsbegrensetDto.getFastsatteTidsbegrensedePerioder()),
            tidsbegrensetDto.getFrilansInntekt());
    }

    public static no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.AvklarteAktiviteterDto mapAvklarteAktiviteterDto(AvklarteAktiviteterDto dto) {
        return new no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.AvklarteAktiviteterDto(mapTilBeregningsaktivitetLagreDtoList(dto.getBeregningsaktivitetLagreDtoList()));
    }

    public static no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FastsettBruttoBeregningsgrunnlagSNforNyIArbeidslivetDto mapFastsettBruttoBeregningsgrunnlagSNforNyIArbeidslivetDto(FastsettBruttoBeregningsgrunnlagSNforNyIArbeidslivetDto dto) {
        return new no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FastsettBruttoBeregningsgrunnlagSNforNyIArbeidslivetDto(dto.getBruttoBeregningsgrunnlag());
    }

    public static no.nav.folketrygdloven.kalkulator.avklaringsbehov.fordeling.FordelBeregningsgrunnlagDto mapFordelBeregningsgrunnlagDto(FordelBeregningsgrunnlagDto dto) {
        return new no.nav.folketrygdloven.kalkulator.avklaringsbehov.fordeling.FordelBeregningsgrunnlagDto(mapTilEndredePerioderList(dto.getEndretBeregningsgrunnlagPerioder()));
    }

    public static no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.VurderRefusjonBeregningsgrunnlagDto mapVurderRefusjonBeregningsgrunnlagDto(VurderRefusjonBeregningsgrunnlagDto dto) {
        return new no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.VurderRefusjonBeregningsgrunnlagDto(mapTilFastsatteAndeler(dto.getFastsatteAndeler()));
    }

    private static List<no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.VurderRefusjonAndelBeregningsgrunnlagDto> mapTilFastsatteAndeler(List<VurderRefusjonAndelBeregningsgrunnlagDto> fastsatteAndeler) {
        return fastsatteAndeler.stream()
                .map(a -> new no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.VurderRefusjonAndelBeregningsgrunnlagDto(
                        a.getArbeidsgiverOrgnr(),
                        a.getArbeidsgiverAktørId(),
                        a.getInternArbeidsforholdRef(),
                        a.getFullRefusjonFom(),
                        a.getDelvisRefusjonBeløpPrMnd()))
                .collect(Collectors.toList());
    }

    public static List<no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.BeregningsaktivitetLagreDto> mapOverstyrBeregningsaktiviteterDto(List<BeregningsaktivitetLagreDto> beregningsaktivitetLagreDtoList) {
        return beregningsaktivitetLagreDtoList.stream().map(OppdatererDtoMapper::mapTilBeregningsaktivitetLagreDto).collect(Collectors.toList());
    }

    public static OverstyrBeregningsgrunnlagDto mapOverstyrBeregningsgrunnlagDto(OverstyrBeregningsgrunnlagHåndteringDto dto) {
        return new OverstyrBeregningsgrunnlagDto(mapFastsettBeregningsgrunnlagPeriodeAndeler(dto.getOverstyrteAndeler()), dto.getFakta() == null ? null : mapTilFaktaOmBeregningLagreDto(dto.getFakta()));
    }

    public static no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FaktaBeregningLagreDto mapTilFaktaOmBeregningLagreDto(FaktaBeregningLagreDto fakta) {
        return new no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FaktaBeregningLagreDto(
            fakta.getVurderNyoppstartetFL() == null ? null : mapVurderNyoppstartetFLDto(fakta.getVurderNyoppstartetFL()),
            fakta.getVurderTidsbegrensetArbeidsforhold() == null ? null : mapTidsbegrensetArbeidsforhold(fakta.getVurderTidsbegrensetArbeidsforhold()),
            fakta.getVurderNyIArbeidslivet() == null ? null : mapVurderNyIArbeidslivet(fakta.getVurderNyIArbeidslivet()),
            fakta.getFastsettMaanedsinntektFL() == null ? null : mapFastsettMånedsinntektFL(fakta.getFastsettMaanedsinntektFL()),
            fakta.getVurdertLonnsendring() == null ? null : mapVurderLønnsendringDto(fakta.getVurdertLonnsendring()),
            fakta.getFastsattUtenInntektsmelding() == null ? null : mapFastsattUtenInntektsmeldingDto(fakta.getFastsattUtenInntektsmelding()),
            fakta.getVurderATogFLiSammeOrganisasjon() == null ? null : mapVurderAtOgFLiSammeOrganisasjonDto(fakta.getVurderATogFLiSammeOrganisasjon()),
            fakta.getBesteberegningAndeler() == null ? null : mapBesteberegningFødendeKvinneDto(fakta.getBesteberegningAndeler()),
            fakta.getFaktaOmBeregningTilfelleDto() == null ? null : fakta.getFaktaOmBeregningTilfelleDto().getTilfeller(),
            fakta.getKunYtelseFordeling() == null ? null : mapFastsettKunYtelseDto(fakta.getKunYtelseFordeling()),
            fakta.getVurderEtterlønnSluttpakke() == null ? null : mapVurderEtterlønnSluttpakke(fakta.getVurderEtterlønnSluttpakke()),
            fakta.getFastsettEtterlønnSluttpakke() == null ? null : mapFastsettEtterlønnSluttpakker(fakta.getFastsettEtterlønnSluttpakke()),
            fakta.getMottarYtelse() == null ? null : mapMottarYtelse(fakta.getMottarYtelse()),
            fakta.getVurderMilitaer() == null ? null : mapVurderMilitær(fakta.getVurderMilitaer()),
            fakta.getRefusjonskravGyldighet() == null ? null : mapRefusjonskravPrArbeidsgiverVurderingDto(fakta.getRefusjonskravGyldighet())
        );
    }

    private static List<no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.RefusjonskravPrArbeidsgiverVurderingDto> mapRefusjonskravPrArbeidsgiverVurderingDto(List<RefusjonskravPrArbeidsgiverVurderingDto> refusjonskravGyldighet) {
        return refusjonskravGyldighet.stream().map(OppdatererDtoMapper::mapRefusjonskravGyldighet).collect(Collectors.toList());
    }

    private static no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.RefusjonskravPrArbeidsgiverVurderingDto mapRefusjonskravGyldighet(RefusjonskravPrArbeidsgiverVurderingDto refusjonskravPrArbeidsgiverVurderingDto) {
        return new no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.RefusjonskravPrArbeidsgiverVurderingDto(
            refusjonskravPrArbeidsgiverVurderingDto.getArbeidsgiverId(),
            refusjonskravPrArbeidsgiverVurderingDto.isSkalUtvideGyldighet()
            );
    }

    private static no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.VurderMilitærDto mapVurderMilitær(VurderMilitærDto vurderMilitaer) {
        return new no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.VurderMilitærDto(vurderMilitaer.getHarMilitaer());
    }

    private static no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.MottarYtelseDto mapMottarYtelse(MottarYtelseDto mottarYtelse) {
        return new no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.MottarYtelseDto(
            mottarYtelse.getFrilansMottarYtelse(),
            mottarYtelse.getArbeidstakerUtenIMMottarYtelse() == null ? null : mapArbeidstakterUtenIMMottarYtelseListe(mottarYtelse.getArbeidstakerUtenIMMottarYtelse()));
    }

    private static List<no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.ArbeidstakerandelUtenIMMottarYtelseDto> mapArbeidstakterUtenIMMottarYtelseListe(List<ArbeidstakerandelUtenIMMottarYtelseDto> arbeidstakerUtenIMMottarYtelse) {
        return arbeidstakerUtenIMMottarYtelse.stream().map(OppdatererDtoMapper::mapArbeidstakterUtenIMMottarYtelse).collect(Collectors.toList());
    }

    private static no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.ArbeidstakerandelUtenIMMottarYtelseDto mapArbeidstakterUtenIMMottarYtelse(ArbeidstakerandelUtenIMMottarYtelseDto arbeidstakerandelUtenIMMottarYtelseDto) {
        return new no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.ArbeidstakerandelUtenIMMottarYtelseDto(
            arbeidstakerandelUtenIMMottarYtelseDto.getAndelsnr(),
            arbeidstakerandelUtenIMMottarYtelseDto.getMottarYtelse()
        );
    }

    private static no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FastsettEtterlønnSluttpakkeDto mapFastsettEtterlønnSluttpakker(FastsettEtterlønnSluttpakkeDto fastsettEtterlønnSluttpakke) {
        return new no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FastsettEtterlønnSluttpakkeDto(fastsettEtterlønnSluttpakke.getFastsattPrMnd());
    }

    private static no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.VurderEtterlønnSluttpakkeDto mapVurderEtterlønnSluttpakke(VurderEtterlønnSluttpakkeDto vurderEtterlønnSluttpakke) {
        return new no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.VurderEtterlønnSluttpakkeDto(
            vurderEtterlønnSluttpakke.getErEtterlønnSluttpakke()
        );
    }

    private static no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FastsettBgKunYtelseDto mapFastsettKunYtelseDto(FastsettBgKunYtelseDto kunYtelseFordeling) {
        return new no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FastsettBgKunYtelseDto(
            kunYtelseFordeling.getAndeler() == null ? null : mapKunYtelseAndeler(kunYtelseFordeling.getAndeler()),
            kunYtelseFordeling.getSkalBrukeBesteberegning()
        );
    }

    private static List<no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FastsattBrukersAndel> mapKunYtelseAndeler(List<FastsattBrukersAndel> andeler) {
        return andeler.stream().map(OppdatererDtoMapper::mapFastsattBrukersAndel).collect(Collectors.toList());
    }

    private static no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FastsattBrukersAndel mapFastsattBrukersAndel(FastsattBrukersAndel fastsattBrukersAndel) {
        return new no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FastsattBrukersAndel(
            fastsattBrukersAndel.getNyAndel(),
            fastsattBrukersAndel.getAndelsnr(),
            fastsattBrukersAndel.getLagtTilAvSaksbehandler(),
            fastsattBrukersAndel.getFastsattBeløp(),
            fastsattBrukersAndel.getInntektskategori()
        );
    }

    private static no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.VurderATogFLiSammeOrganisasjonDto mapVurderAtOgFLiSammeOrganisasjonDto(VurderATogFLiSammeOrganisasjonDto vurderATogFLiSammeOrganisasjon) {
        return new no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.VurderATogFLiSammeOrganisasjonDto(
            mapVurderAtOgFLiSammeOranisasjonAndelListe(vurderATogFLiSammeOrganisasjon.getVurderATogFLiSammeOrganisasjonAndelListe())
        );
    }

    private static List<no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.VurderATogFLiSammeOrganisasjonAndelDto> mapVurderAtOgFLiSammeOranisasjonAndelListe(List<VurderATogFLiSammeOrganisasjonAndelDto> vurderATogFLiSammeOrganisasjonAndelListe) {
        return vurderATogFLiSammeOrganisasjonAndelListe.stream().map(OppdatererDtoMapper::mapVurderATOgFLiSammeOrgAndel).collect(Collectors.toList());
    }

    private static no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.VurderATogFLiSammeOrganisasjonAndelDto mapVurderATOgFLiSammeOrgAndel(VurderATogFLiSammeOrganisasjonAndelDto vurderATogFLiSammeOrganisasjonAndelDto) {
        return new no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.VurderATogFLiSammeOrganisasjonAndelDto(vurderATogFLiSammeOrganisasjonAndelDto.getAndelsnr(), vurderATogFLiSammeOrganisasjonAndelDto.getArbeidsinntekt());
    }

    private static no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FastsettMånedsinntektUtenInntektsmeldingDto mapFastsattUtenInntektsmeldingDto(FastsettMånedsinntektUtenInntektsmeldingDto fastsattUtenInntektsmelding) {
        return new no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FastsettMånedsinntektUtenInntektsmeldingDto(mapFastsattUtenInntektsmeldingAndelListe(fastsattUtenInntektsmelding.getAndelListe()));
    }

    private static List<no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FastsettMånedsinntektUtenInntektsmeldingAndelDto> mapFastsattUtenInntektsmeldingAndelListe(List<FastsettMånedsinntektUtenInntektsmeldingAndelDto> andelListe) {
        return andelListe.stream().map(OppdatererDtoMapper::mapAndel).collect(Collectors.toList());
    }

    private static no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FastsettMånedsinntektUtenInntektsmeldingAndelDto mapAndel(FastsettMånedsinntektUtenInntektsmeldingAndelDto fastsettMånedsinntektUtenInntektsmeldingAndelDto) {
        return new no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FastsettMånedsinntektUtenInntektsmeldingAndelDto(
            fastsettMånedsinntektUtenInntektsmeldingAndelDto.getAndelsnr(),
                no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FastsatteVerdierDto.Builder.ny()
                .medFastsattBeløpPrMnd(fastsettMånedsinntektUtenInntektsmeldingAndelDto.getFastsattBeløp())
                .medInntektskategori(fastsettMånedsinntektUtenInntektsmeldingAndelDto.getInntektskategori())
                .build());
    }

    private static no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.VurderLønnsendringDto mapVurderLønnsendringDto(VurderLønnsendringDto vurdertLonnsendring) {
        return new no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.VurderLønnsendringDto(vurdertLonnsendring.erLønnsendringIBeregningsperioden());
    }

    private static no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FastsettMånedsinntektFLDto mapFastsettMånedsinntektFL(FastsettMånedsinntektFLDto fastsettMaanedsinntektFL) {
        return new no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FastsettMånedsinntektFLDto(fastsettMaanedsinntektFL.getMaanedsinntekt());
    }

    private static no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.VurderSelvstendigNæringsdrivendeNyIArbeidslivetDto mapVurderNyIArbeidslivet(VurderSelvstendigNæringsdrivendeNyIArbeidslivetDto vurderNyIArbeidslivet) {
        return new no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.VurderSelvstendigNæringsdrivendeNyIArbeidslivetDto(vurderNyIArbeidslivet.erNyIArbeidslivet());
    }

    private static no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.VurderTidsbegrensetArbeidsforholdDto mapTidsbegrensetArbeidsforhold(VurderTidsbegrensetArbeidsforholdDto vurderTidsbegrensetArbeidsforhold) {
        return new no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.VurderTidsbegrensetArbeidsforholdDto(
            mapVurderteArbeidsforhold(vurderTidsbegrensetArbeidsforhold.getFastsatteArbeidsforhold())
        );
    }

    private static List<no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.VurderteArbeidsforholdDto> mapVurderteArbeidsforhold(List<VurderteArbeidsforholdDto> fastsatteArbeidsforhold) {
        return fastsatteArbeidsforhold.stream().map(OppdatererDtoMapper::mapVurdertArbeidsforhold).collect(Collectors.toList());
    }

    private static no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.VurderteArbeidsforholdDto mapVurdertArbeidsforhold(VurderteArbeidsforholdDto vurderteArbeidsforholdDto) {
        return new no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.VurderteArbeidsforholdDto(
            vurderteArbeidsforholdDto.getAndelsnr(),
            vurderteArbeidsforholdDto.isTidsbegrensetArbeidsforhold()
        );
    }

    private static no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.VurderNyoppstartetFLDto mapVurderNyoppstartetFLDto(VurderNyoppstartetFLDto vurderNyoppstartetFL) {
        return new no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.VurderNyoppstartetFLDto(vurderNyoppstartetFL.erErNyoppstartetFL());
    }

    private static no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.BesteberegningFødendeKvinneDto mapBesteberegningFødendeKvinneDto(BesteberegningFødendeKvinneDto besteberegningAndeler) {
        return new no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.BesteberegningFødendeKvinneDto(mapBesteberegningAndeler(besteberegningAndeler.getBesteberegningAndelListe()), mapNyDagpengeAndel(besteberegningAndeler.getNyDagpengeAndel()));
    }

    private static DagpengeAndelLagtTilBesteberegningDto mapNyDagpengeAndel(no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta.DagpengeAndelLagtTilBesteberegningDto nyDagpengeAndel) {
        return nyDagpengeAndel == null ? null : new DagpengeAndelLagtTilBesteberegningDto(nyDagpengeAndel.getFastsatteVerdier().getFastsattBeløp(), nyDagpengeAndel.getFastsatteVerdier().getInntektskategori());
    }

    private static List<no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.BesteberegningFødendeKvinneAndelDto> mapBesteberegningAndeler(List<BesteberegningFødendeKvinneAndelDto> besteberegningAndelListe) {
        return besteberegningAndelListe.stream().map(OppdatererDtoMapper::mapBesteberegningAndel).collect(Collectors.toList());
    }

    private static no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.BesteberegningFødendeKvinneAndelDto mapBesteberegningAndel(BesteberegningFødendeKvinneAndelDto besteberegningFødendeKvinneAndelDto) {
        return new no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.BesteberegningFødendeKvinneAndelDto(
            besteberegningFødendeKvinneAndelDto.getAndelsnr(),
            besteberegningFødendeKvinneAndelDto.getFastsatteVerdier().getFastsattBeløp(),
            besteberegningFødendeKvinneAndelDto.getFastsatteVerdier().getInntektskategori(),
            besteberegningFødendeKvinneAndelDto.getLagtTilAvSaksbehandler());
    }

    private static List<no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.InntektPrAndelDto> mapTilInntektPrAndelListe(List<InntektPrAndelDto> inntektPrAndelList) {
        return inntektPrAndelList.stream().map(OppdatererDtoMapper::mapInntektPrAndel).collect(Collectors.toList());
    }

    private static no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.InntektPrAndelDto mapInntektPrAndel(InntektPrAndelDto inntektPrAndelDto) {
        return new no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.InntektPrAndelDto(inntektPrAndelDto.getInntekt(), inntektPrAndelDto.getAndelsnr());
    }

    private static List<no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FastsattePerioderTidsbegrensetDto> mapTilFastsattTidsbegrensetPerioder(List<FastsattePerioderTidsbegrensetDto> fastsatteTidsbegrensedePerioder) {
        return fastsatteTidsbegrensedePerioder.stream().map(OppdatererDtoMapper::mapTilFastsattTidsbegrensetPeriode).collect(Collectors.toList());
    }

    private static no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FastsattePerioderTidsbegrensetDto mapTilFastsattTidsbegrensetPeriode(FastsattePerioderTidsbegrensetDto fastsattePerioderTidsbegrensetDto) {
        return new no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FastsattePerioderTidsbegrensetDto(fastsattePerioderTidsbegrensetDto.getPeriodeFom(), fastsattePerioderTidsbegrensetDto.getPeriodeTom(), mapTilFastsattTidsbegrensetAndeler(fastsattePerioderTidsbegrensetDto.getFastsatteTidsbegrensedeAndeler()));
    }

    private static List<no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FastsatteAndelerTidsbegrensetDto> mapTilFastsattTidsbegrensetAndeler(List<FastsatteAndelerTidsbegrensetDto> fastsatteTidsbegrensedeAndeler) {
        return fastsatteTidsbegrensedeAndeler.stream().map(OppdatererDtoMapper::mapTilFastsattTidsbegrensetAndel).collect(Collectors.toList());
    }

    private static no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FastsatteAndelerTidsbegrensetDto mapTilFastsattTidsbegrensetAndel(FastsatteAndelerTidsbegrensetDto fastsatteAndelerTidsbegrensetDto) {
        return new no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FastsatteAndelerTidsbegrensetDto(fastsatteAndelerTidsbegrensetDto.getAndelsnr(), fastsatteAndelerTidsbegrensetDto.getBruttoFastsattInntekt());
    }

    private static List<no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.BeregningsaktivitetLagreDto> mapTilBeregningsaktivitetLagreDtoList(List<BeregningsaktivitetLagreDto> beregningsaktivitetLagreDtoList) {
        return beregningsaktivitetLagreDtoList.stream().map(OppdatererDtoMapper::mapTilBeregningsaktivitetLagreDto).collect(Collectors.toList());
    }

    private static no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.BeregningsaktivitetLagreDto mapTilBeregningsaktivitetLagreDto(BeregningsaktivitetLagreDto beregningsaktivitetLagreDto) {
        return no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.BeregningsaktivitetLagreDto.builder()
            .medArbeidsforholdRef(beregningsaktivitetLagreDto.getArbeidsforholdRef() == null ? null : beregningsaktivitetLagreDto.getArbeidsforholdRef().toString())
            .medArbeidsgiverIdentifikator(beregningsaktivitetLagreDto.getArbeidsgiverIdentifikator())
            .medFom(beregningsaktivitetLagreDto.getFom())
            .medOppdragsgiverOrg(beregningsaktivitetLagreDto.getOppdragsgiverOrg())
            .medOpptjeningAktivitetType(beregningsaktivitetLagreDto.getOpptjeningAktivitetType())
            .medSkalBrukes(beregningsaktivitetLagreDto.getSkalBrukes())
            .medTom(beregningsaktivitetLagreDto.getTom())
            .build();
    }

    private static List<no.nav.folketrygdloven.kalkulator.avklaringsbehov.fordeling.FordelBeregningsgrunnlagPeriodeDto> mapTilEndredePerioderList(List<FordelBeregningsgrunnlagPeriodeDto> endretBeregningsgrunnlagPerioder) {
        return endretBeregningsgrunnlagPerioder.stream().map(OppdatererDtoMapper::mapTilFordelBeregningsgrunnlagPeriodeDto).collect(Collectors.toList());
    }

    private static no.nav.folketrygdloven.kalkulator.avklaringsbehov.fordeling.FordelBeregningsgrunnlagPeriodeDto mapTilFordelBeregningsgrunnlagPeriodeDto(FordelBeregningsgrunnlagPeriodeDto fordelBeregningsgrunnlagPeriodeDto) {
        return new no.nav.folketrygdloven.kalkulator.avklaringsbehov.fordeling.FordelBeregningsgrunnlagPeriodeDto(mapFordelBeregningsgrunnlagPeriodeAndeler(fordelBeregningsgrunnlagPeriodeDto.getAndeler()), fordelBeregningsgrunnlagPeriodeDto.getFom(), fordelBeregningsgrunnlagPeriodeDto.getTom());
    }

    private static List<no.nav.folketrygdloven.kalkulator.avklaringsbehov.fordeling.FordelBeregningsgrunnlagAndelDto> mapFordelBeregningsgrunnlagPeriodeAndeler(List<FordelBeregningsgrunnlagAndelDto> andeler) {
        return andeler.stream().map(OppdatererDtoMapper::mapFordelBeregningsgrunnlagPeriodeAndelDto).collect(Collectors.toList());
    }

    private static List<no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FastsettBeregningsgrunnlagAndelDto> mapFastsettBeregningsgrunnlagPeriodeAndeler(List<FastsettBeregningsgrunnlagAndelDto> andeler) {
        return andeler.stream().map(OppdatererDtoMapper::mapFastsettBeregningsgrunnlagPeriodeAndelDto).collect(Collectors.toList());
    }

    private static no.nav.folketrygdloven.kalkulator.avklaringsbehov.fordeling.FordelBeregningsgrunnlagAndelDto mapFordelBeregningsgrunnlagPeriodeAndelDto(FordelBeregningsgrunnlagAndelDto fordelBeregningsgrunnlagAndelDto) {
        return new no.nav.folketrygdloven.kalkulator.avklaringsbehov.fordeling.FordelBeregningsgrunnlagAndelDto(
                mapTilFordelRedigerbarAndelDto(fordelBeregningsgrunnlagAndelDto),
                mapTilFordelFastsatteVerdier(fordelBeregningsgrunnlagAndelDto.getFastsatteVerdier()),
                fordelBeregningsgrunnlagAndelDto.getForrigeInntektskategori(),
                fordelBeregningsgrunnlagAndelDto.getForrigeRefusjonPrÅr(),
                fordelBeregningsgrunnlagAndelDto.getForrigeArbeidsinntektPrÅr());
    }

    private static no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FastsettBeregningsgrunnlagAndelDto mapFastsettBeregningsgrunnlagPeriodeAndelDto(FastsettBeregningsgrunnlagAndelDto fastsettBeregningsgrunnlagAndelDto) {
        return new no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FastsettBeregningsgrunnlagAndelDto(
            mapTilRedigerbarAndelDto(fastsettBeregningsgrunnlagAndelDto),
            mapTilFastsatteVerdier(fastsettBeregningsgrunnlagAndelDto.getFastsatteVerdier()),
            fastsettBeregningsgrunnlagAndelDto.getForrigeInntektskategori(),
            fastsettBeregningsgrunnlagAndelDto.getForrigeRefusjonPrÅr(),
            fastsettBeregningsgrunnlagAndelDto.getForrigeArbeidsinntektPrÅr());
    }

    private static no.nav.folketrygdloven.kalkulator.avklaringsbehov.fordeling.FordelFastsatteVerdierDto mapTilFordelFastsatteVerdier(FordelFastsatteVerdierDto fastsatteVerdier) {
        return no.nav.folketrygdloven.kalkulator.avklaringsbehov.fordeling.FordelFastsatteVerdierDto.Builder.ny()
                .medRefusjonPrÅr(fastsatteVerdier.getRefusjonPrÅr())
                .medFastsattBeløpPrÅr(fastsatteVerdier.getFastsattÅrsbeløp())
                .medFastsattBeløpPrÅrInklNaturalytelse(fastsatteVerdier.getFastsattÅrsbeløpInklNaturalytelse())
                .medInntektskategori(fastsatteVerdier.getInntektskategori())
                .build();
    }

    private static no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FastsatteVerdierDto mapTilFastsatteVerdier(FastsatteVerdierDto fastsatteVerdier) {
        return no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FastsatteVerdierDto.Builder.ny()
                .medFastsattBeløpPrMnd(fastsatteVerdier.getFastsattBeløpPrMnd())
                .medInntektskategori(fastsatteVerdier.getInntektskategori())
                .medSkalHaBesteberegning(fastsatteVerdier.getSkalHaBesteberegning())
                .build();
    }

    private static no.nav.folketrygdloven.kalkulator.avklaringsbehov.fordeling.RedigerbarAndelDto mapTilFordelRedigerbarAndelDto(FordelRedigerbarAndelDto redigerbarAndel) {
        return new no.nav.folketrygdloven.kalkulator.avklaringsbehov.fordeling.RedigerbarAndelDto(
                redigerbarAndel.getAndelsnr(),
                redigerbarAndel.getArbeidsgiverId(),
                redigerbarAndel.getArbeidsforholdId().getAbakusReferanse(),
                redigerbarAndel.getNyAndel(),
                redigerbarAndel.getKilde());
    }

    private static no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.RedigerbarAndelFaktaOmBeregningDto mapTilRedigerbarAndelDto(RedigerbarAndelDto redigerbarAndel) {
        return new no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.RedigerbarAndelFaktaOmBeregningDto(
            false,
            redigerbarAndel.getAndelsnr(),
            redigerbarAndel.getLagtTilAvSaksbehandler());
    }
}
