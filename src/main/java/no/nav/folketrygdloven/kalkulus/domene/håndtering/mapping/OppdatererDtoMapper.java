package no.nav.folketrygdloven.kalkulus.domene.håndtering.mapping;

import java.util.List;

import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.DagpengeAndelLagtTilBesteberegningDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.OverstyrBeregningsgrunnlagDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.håndtering.avklaraktiviteter.AvklarteAktiviteterDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.håndtering.avklaraktiviteter.BeregningsaktivitetLagreDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.håndtering.fakta.ArbeidstakerandelUtenIMMottarYtelseDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.håndtering.fakta.BesteberegningFødendeKvinneAndelDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.håndtering.fakta.BesteberegningFødendeKvinneDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.håndtering.fakta.FaktaBeregningLagreDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.håndtering.fakta.FastsattBrukersAndel;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.håndtering.fakta.FastsatteAndelerTidsbegrensetDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.håndtering.fakta.FastsattePerioderTidsbegrensetDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.håndtering.fakta.FastsatteVerdierDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.håndtering.fakta.FastsettBeregningsgrunnlagAndelDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.håndtering.fakta.FastsettBgKunYtelseDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.håndtering.fakta.FastsettEtterlønnSluttpakkeDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.håndtering.fakta.FastsettMånedsinntektFLDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.håndtering.fakta.FastsettMånedsinntektUtenInntektsmeldingAndelDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.håndtering.fakta.FastsettMånedsinntektUtenInntektsmeldingDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.håndtering.fakta.MottarYtelseDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.håndtering.fakta.RedigerbarAndelDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.håndtering.fakta.RefusjonskravPrArbeidsgiverVurderingDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.håndtering.fakta.VurderATogFLiSammeOrganisasjonAndelDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.håndtering.fakta.VurderATogFLiSammeOrganisasjonDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.håndtering.fakta.VurderEtterlønnSluttpakkeDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.håndtering.fakta.VurderLønnsendringDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.håndtering.fakta.VurderMilitærDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.håndtering.fakta.VurderNyoppstartetFLDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.håndtering.fakta.VurderSelvstendigNæringsdrivendeNyIArbeidslivetDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.håndtering.fakta.VurderTidsbegrensetArbeidsforholdDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.håndtering.fakta.VurderteArbeidsforholdDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.håndtering.fordeling.FordelBeregningsgrunnlagAndelDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.håndtering.fordeling.FordelBeregningsgrunnlagDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.håndtering.fordeling.FordelBeregningsgrunnlagPeriodeDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.håndtering.fordeling.FordelFastsatteVerdierDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.håndtering.fordeling.FordelRedigerbarAndelDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.håndtering.foreslå.FastsettBGTidsbegrensetArbeidsforholdDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.håndtering.foreslå.FastsettBeregningsgrunnlagATFLHåndteringDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.håndtering.foreslå.FastsettBruttoBeregningsgrunnlagSNforNyIArbeidslivetDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.håndtering.foreslå.InntektPrAndelDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.håndtering.overstyring.OverstyrBeregningsgrunnlagHåndteringDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.håndtering.refusjon.VurderRefusjonAndelBeregningsgrunnlagDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.håndtering.refusjon.VurderRefusjonBeregningsgrunnlagDto;


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
                .toList();
    }

    public static List<no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.BeregningsaktivitetLagreDto> mapOverstyrBeregningsaktiviteterDto(List<BeregningsaktivitetLagreDto> beregningsaktivitetLagreDtoList) {
        return beregningsaktivitetLagreDtoList.stream().map(OppdatererDtoMapper::mapTilBeregningsaktivitetLagreDto).toList();
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
        return refusjonskravGyldighet.stream().map(OppdatererDtoMapper::mapRefusjonskravGyldighet).toList();
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
        return arbeidstakerUtenIMMottarYtelse.stream().map(OppdatererDtoMapper::mapArbeidstakterUtenIMMottarYtelse).toList();
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
        return andeler.stream().map(OppdatererDtoMapper::mapFastsattBrukersAndel).toList();
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
        return vurderATogFLiSammeOrganisasjonAndelListe.stream().map(OppdatererDtoMapper::mapVurderATOgFLiSammeOrgAndel).toList();
    }

    private static no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.VurderATogFLiSammeOrganisasjonAndelDto mapVurderATOgFLiSammeOrgAndel(VurderATogFLiSammeOrganisasjonAndelDto vurderATogFLiSammeOrganisasjonAndelDto) {
        return new no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.VurderATogFLiSammeOrganisasjonAndelDto(vurderATogFLiSammeOrganisasjonAndelDto.getAndelsnr(), vurderATogFLiSammeOrganisasjonAndelDto.getArbeidsinntekt());
    }

    private static no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FastsettMånedsinntektUtenInntektsmeldingDto mapFastsattUtenInntektsmeldingDto(FastsettMånedsinntektUtenInntektsmeldingDto fastsattUtenInntektsmelding) {
        return new no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FastsettMånedsinntektUtenInntektsmeldingDto(mapFastsattUtenInntektsmeldingAndelListe(fastsattUtenInntektsmelding.getAndelListe()));
    }

    private static List<no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FastsettMånedsinntektUtenInntektsmeldingAndelDto> mapFastsattUtenInntektsmeldingAndelListe(List<FastsettMånedsinntektUtenInntektsmeldingAndelDto> andelListe) {
        return andelListe.stream().map(OppdatererDtoMapper::mapAndel).toList();
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
        return fastsatteArbeidsforhold.stream().map(OppdatererDtoMapper::mapVurdertArbeidsforhold).toList();
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

    private static DagpengeAndelLagtTilBesteberegningDto mapNyDagpengeAndel(no.nav.foreldrepenger.kalkulus.kontrakt.request.håndtering.fakta.DagpengeAndelLagtTilBesteberegningDto nyDagpengeAndel) {
        return nyDagpengeAndel == null ? null : new DagpengeAndelLagtTilBesteberegningDto(nyDagpengeAndel.getFastsatteVerdier().getFastsattBeløp(), nyDagpengeAndel.getFastsatteVerdier().getInntektskategori());
    }

    private static List<no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.BesteberegningFødendeKvinneAndelDto> mapBesteberegningAndeler(List<BesteberegningFødendeKvinneAndelDto> besteberegningAndelListe) {
        return besteberegningAndelListe.stream().map(OppdatererDtoMapper::mapBesteberegningAndel).toList();
    }

    private static no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.BesteberegningFødendeKvinneAndelDto mapBesteberegningAndel(BesteberegningFødendeKvinneAndelDto besteberegningFødendeKvinneAndelDto) {
        return new no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.BesteberegningFødendeKvinneAndelDto(
            besteberegningFødendeKvinneAndelDto.getAndelsnr(),
            besteberegningFødendeKvinneAndelDto.getFastsatteVerdier().getFastsattBeløp(),
            besteberegningFødendeKvinneAndelDto.getFastsatteVerdier().getInntektskategori(),
            besteberegningFødendeKvinneAndelDto.getLagtTilAvSaksbehandler());
    }

    private static List<no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.InntektPrAndelDto> mapTilInntektPrAndelListe(List<InntektPrAndelDto> inntektPrAndelList) {
        return inntektPrAndelList.stream().map(OppdatererDtoMapper::mapInntektPrAndel).toList();
    }

    private static no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.InntektPrAndelDto mapInntektPrAndel(InntektPrAndelDto inntektPrAndelDto) {
        return new no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.InntektPrAndelDto(inntektPrAndelDto.getInntekt(), inntektPrAndelDto.getAndelsnr());
    }

    private static List<no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FastsattePerioderTidsbegrensetDto> mapTilFastsattTidsbegrensetPerioder(List<FastsattePerioderTidsbegrensetDto> fastsatteTidsbegrensedePerioder) {
        return fastsatteTidsbegrensedePerioder.stream().map(OppdatererDtoMapper::mapTilFastsattTidsbegrensetPeriode).toList();
    }

    private static no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FastsattePerioderTidsbegrensetDto mapTilFastsattTidsbegrensetPeriode(FastsattePerioderTidsbegrensetDto fastsattePerioderTidsbegrensetDto) {
        return new no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FastsattePerioderTidsbegrensetDto(fastsattePerioderTidsbegrensetDto.getPeriodeFom(), fastsattePerioderTidsbegrensetDto.getPeriodeTom(), mapTilFastsattTidsbegrensetAndeler(fastsattePerioderTidsbegrensetDto.getFastsatteTidsbegrensedeAndeler()));
    }

    private static List<no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FastsatteAndelerTidsbegrensetDto> mapTilFastsattTidsbegrensetAndeler(List<FastsatteAndelerTidsbegrensetDto> fastsatteTidsbegrensedeAndeler) {
        return fastsatteTidsbegrensedeAndeler.stream().map(OppdatererDtoMapper::mapTilFastsattTidsbegrensetAndel).toList();
    }

    private static no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FastsatteAndelerTidsbegrensetDto mapTilFastsattTidsbegrensetAndel(FastsatteAndelerTidsbegrensetDto fastsatteAndelerTidsbegrensetDto) {
        return new no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FastsatteAndelerTidsbegrensetDto(fastsatteAndelerTidsbegrensetDto.getAndelsnr(), fastsatteAndelerTidsbegrensetDto.getBruttoFastsattInntekt());
    }

    private static List<no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.BeregningsaktivitetLagreDto> mapTilBeregningsaktivitetLagreDtoList(List<BeregningsaktivitetLagreDto> beregningsaktivitetLagreDtoList) {
        return beregningsaktivitetLagreDtoList.stream().map(OppdatererDtoMapper::mapTilBeregningsaktivitetLagreDto).toList();
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
        return endretBeregningsgrunnlagPerioder.stream().map(OppdatererDtoMapper::mapTilFordelBeregningsgrunnlagPeriodeDto).toList();
    }

    private static no.nav.folketrygdloven.kalkulator.avklaringsbehov.fordeling.FordelBeregningsgrunnlagPeriodeDto mapTilFordelBeregningsgrunnlagPeriodeDto(FordelBeregningsgrunnlagPeriodeDto fordelBeregningsgrunnlagPeriodeDto) {
        return new no.nav.folketrygdloven.kalkulator.avklaringsbehov.fordeling.FordelBeregningsgrunnlagPeriodeDto(mapFordelBeregningsgrunnlagPeriodeAndeler(fordelBeregningsgrunnlagPeriodeDto.getAndeler()), fordelBeregningsgrunnlagPeriodeDto.getFom(), fordelBeregningsgrunnlagPeriodeDto.getTom());
    }

    private static List<no.nav.folketrygdloven.kalkulator.avklaringsbehov.fordeling.FordelBeregningsgrunnlagAndelDto> mapFordelBeregningsgrunnlagPeriodeAndeler(List<FordelBeregningsgrunnlagAndelDto> andeler) {
        return andeler.stream().map(OppdatererDtoMapper::mapFordelBeregningsgrunnlagPeriodeAndelDto).toList();
    }

    private static List<no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FastsettBeregningsgrunnlagAndelDto> mapFastsettBeregningsgrunnlagPeriodeAndeler(List<FastsettBeregningsgrunnlagAndelDto> andeler) {
        return andeler.stream().map(OppdatererDtoMapper::mapFastsettBeregningsgrunnlagPeriodeAndelDto).toList();
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
