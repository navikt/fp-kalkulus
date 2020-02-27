package no.nav.folketrygdloven.kalkulus.mappers;

import java.util.List;
import java.util.stream.Collectors;


import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulator.rest.dto.OverstyrBeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.avklaraktiviteter.AvklarteAktiviteterDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.avklaraktiviteter.BeregningsaktivitetLagreDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.overstyring.OverstyrBeregningsgrunnlagHåndteringDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta.ArbeidstakerandelUtenIMMottarYtelseDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta.BesteberegningFødendeKvinneAndelDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta.BesteberegningFødendeKvinneDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta.FaktaBeregningLagreDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta.FastsattBrukersAndel;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta.FastsatteAndelerTidsbegrensetDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta.FastsattePerioderTidsbegrensetDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta.FastsatteVerdierDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.foreslå.FastsettBGTidsbegrensetArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta.FastsettBeregningsgrunnlagATFLDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fordeling.FastsettBeregningsgrunnlagAndelDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fordeling.FastsettBeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta.FastsettBgKunYtelseDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.foreslå.FastsettBruttoBeregningsgrunnlagSNDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.foreslå.FastsettBruttoBeregningsgrunnlagSNforNyIArbeidslivetDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta.FastsettEtterlønnSluttpakkeDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta.FastsettMånedsinntektFLDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta.FastsettMånedsinntektUtenInntektsmeldingAndelDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta.FastsettMånedsinntektUtenInntektsmeldingDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fordeling.FordelBeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta.InntektPrAndelDto;
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
import no.nav.folketrygdloven.kalkulus.håndtering.v1.foreslå.VurderVarigEndringEllerNyoppstartetSNDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta.VurderteArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;


public class OppdatererDtoMapper {
    public static no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FastsettBeregningsgrunnlagATFLDto mapFastsettBeregningsgrunnlagATFLDto(FastsettBeregningsgrunnlagATFLDto tilKalkulus) {
        return new no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FastsettBeregningsgrunnlagATFLDto(mapTilInntektPrAndelListe(tilKalkulus.getInntektPrAndelList()), tilKalkulus.getInntektFrilanser());
    }

    public static no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FastsettBGTidsbegrensetArbeidsforholdDto mapFastsettBGTidsbegrensetArbeidsforholdDto(FastsettBGTidsbegrensetArbeidsforholdDto tidsbegrensetDto) {
        return new no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FastsettBGTidsbegrensetArbeidsforholdDto(
            tidsbegrensetDto.getFastsatteTidsbegrensedePerioder() == null ? null : mapTilFastsattTidsbegrensetPerioder(tidsbegrensetDto.getFastsatteTidsbegrensedePerioder()),
            tidsbegrensetDto.getFrilansInntekt());
    }

    public static no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.AvklarteAktiviteterDto mapAvklarteAktiviteterDto(AvklarteAktiviteterDto dto) {
        return new no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.AvklarteAktiviteterDto(mapTilBeregningsaktivitetLagreDtoList(dto.getBeregningsaktivitetLagreDtoList()));
    }

    public static no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FastsettBruttoBeregningsgrunnlagSNforNyIArbeidslivetDto mapFastsettBruttoBeregningsgrunnlagSNforNyIArbeidslivetDto(FastsettBruttoBeregningsgrunnlagSNforNyIArbeidslivetDto dto) {
        return new no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FastsettBruttoBeregningsgrunnlagSNforNyIArbeidslivetDto(dto.getBruttoBeregningsgrunnlag());
    }

    public static no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FastsettBruttoBeregningsgrunnlagSNDto mapFastsettBruttoBeregningsgrunnlagSNDto(FastsettBruttoBeregningsgrunnlagSNDto dto) {
        return new no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FastsettBruttoBeregningsgrunnlagSNDto(dto.getBruttoBeregningsgrunnlag());
    }

    public static no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FordelBeregningsgrunnlagDto mapFordelBeregningsgrunnlagDto(FordelBeregningsgrunnlagDto dto) {
        return new no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FordelBeregningsgrunnlagDto(mapTilEndredePerioderList(dto.getEndretBeregningsgrunnlagPerioder()));
    }

    public static no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.VurderVarigEndringEllerNyoppstartetSNDto mapVurderVarigEndringEllerNyoppstartetSNDto(VurderVarigEndringEllerNyoppstartetSNDto dto) {
        return new no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.VurderVarigEndringEllerNyoppstartetSNDto(dto.getErVarigEndretNaering(), dto.getBruttoBeregningsgrunnlag());
    }

    public static List<no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.BeregningsaktivitetLagreDto> mapOverstyrBeregningsaktiviteterDto(List<BeregningsaktivitetLagreDto> beregningsaktivitetLagreDtoList) {
        return beregningsaktivitetLagreDtoList.stream().map(OppdatererDtoMapper::mapTilBeregningsaktivitetLagreDto).collect(Collectors.toList());
    }

    public static no.nav.folketrygdloven.kalkulator.rest.dto.OverstyrBeregningsgrunnlagDto mapOverstyrBeregningsgrunnlagDto(OverstyrBeregningsgrunnlagHåndteringDto dto) {
        return new OverstyrBeregningsgrunnlagDto(mapFastsettBeregningsgrunnlagPeriodeAndeler(dto.getOverstyrteAndeler()), mapTilFaktaOmBeregningLagreDto(dto.getFakta()));
    }

    public static no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FaktaBeregningLagreDto mapTilFaktaOmBeregningLagreDto(FaktaBeregningLagreDto fakta) {
        return new no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FaktaBeregningLagreDto(
            fakta.getVurderNyoppstartetFL() == null ? null : mapVurderNyoppstartetFLDto(fakta.getVurderNyoppstartetFL()),
            fakta.getVurderTidsbegrensetArbeidsforhold() == null ? null : mapTidsbegrensetArbeidsforhold(fakta.getVurderTidsbegrensetArbeidsforhold()),
            fakta.getVurderNyIArbeidslivet() == null ? null : mapVurderNyIArbeidslivet(fakta.getVurderNyIArbeidslivet()),
            fakta.getFastsettMaanedsinntektFL() == null ? null : mapFastsettMånedsinntektFL(fakta.getFastsettMaanedsinntektFL()),
            fakta.getVurdertLonnsendring() == null ? null : mapVurderLønnsendringDto(fakta.getVurdertLonnsendring()),
            fakta.getFastsattUtenInntektsmelding() == null ? null : mapFastsattUtenInntektsmeldingDto(fakta.getFastsattUtenInntektsmelding()),
            fakta.getVurderATogFLiSammeOrganisasjon() == null ? null : mapVurderAtOgFLiSammeOrganisasjonDto(fakta.getVurderATogFLiSammeOrganisasjon()),
            fakta.getBesteberegningAndeler() == null ? null : mapBesteberegningFødendeKvinneDto(fakta.getBesteberegningAndeler()),
            fakta.getFaktaOmBeregningTilfeller() == null ? null : mapFaktaOmBeregningTilfeller(fakta.getFaktaOmBeregningTilfeller()),
            fakta.getKunYtelseFordeling() == null ? null : mapFastsettKunYtelseDto(fakta.getKunYtelseFordeling()),
            fakta.getVurderEtterlønnSluttpakke() == null ? null : mapVurderEtterlønnSluttpakke(fakta.getVurderEtterlønnSluttpakke()),
            fakta.getFastsettEtterlønnSluttpakke() == null ? null : mapFastsettEtterlønnSluttpakker(fakta.getFastsettEtterlønnSluttpakke()),
            fakta.getMottarYtelse() == null ? null : mapMottarYtelse(fakta.getMottarYtelse()),
            fakta.getVurderMilitaer() == null ? null : mapVurderMilitær(fakta.getVurderMilitaer()),
            fakta.getRefusjonskravGyldighet() == null ? null : mapRefusjonskravPrArbeidsgiverVurderingDto(fakta.getRefusjonskravGyldighet())
        );
    }

    private static List<no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.RefusjonskravPrArbeidsgiverVurderingDto> mapRefusjonskravPrArbeidsgiverVurderingDto(List<RefusjonskravPrArbeidsgiverVurderingDto> refusjonskravGyldighet) {
        return refusjonskravGyldighet.stream().map(OppdatererDtoMapper::mapRefusjonskravGyldighet).collect(Collectors.toList());
    }

    private static no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.RefusjonskravPrArbeidsgiverVurderingDto mapRefusjonskravGyldighet(RefusjonskravPrArbeidsgiverVurderingDto refusjonskravPrArbeidsgiverVurderingDto) {
        return new no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.RefusjonskravPrArbeidsgiverVurderingDto(
            refusjonskravPrArbeidsgiverVurderingDto.getArbeidsgiverId(),
            refusjonskravPrArbeidsgiverVurderingDto.isSkalUtvideGyldighet()
            );
    }

    private static no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.VurderMilitærDto mapVurderMilitær(VurderMilitærDto vurderMilitaer) {
        return new no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.VurderMilitærDto(vurderMilitaer.getHarMilitaer());
    }

    private static no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.MottarYtelseDto mapMottarYtelse(MottarYtelseDto mottarYtelse) {
        return new no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.MottarYtelseDto(
            mottarYtelse.getFrilansMottarYtelse(),
            mottarYtelse.getArbeidstakerUtenIMMottarYtelse() == null ? null : mapArbeidstakterUtenIMMottarYtelseListe(mottarYtelse.getArbeidstakerUtenIMMottarYtelse()));
    }

    private static List<no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.ArbeidstakerandelUtenIMMottarYtelseDto> mapArbeidstakterUtenIMMottarYtelseListe(List<ArbeidstakerandelUtenIMMottarYtelseDto> arbeidstakerUtenIMMottarYtelse) {
        return arbeidstakerUtenIMMottarYtelse.stream().map(OppdatererDtoMapper::mapArbeidstakterUtenIMMottarYtelse).collect(Collectors.toList());
    }

    private static no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.ArbeidstakerandelUtenIMMottarYtelseDto mapArbeidstakterUtenIMMottarYtelse(ArbeidstakerandelUtenIMMottarYtelseDto arbeidstakerandelUtenIMMottarYtelseDto) {
        return new no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.ArbeidstakerandelUtenIMMottarYtelseDto(
            arbeidstakerandelUtenIMMottarYtelseDto.getAndelsnr(),
            arbeidstakerandelUtenIMMottarYtelseDto.getMottarYtelse()
        );
    }

    private static no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FastsettEtterlønnSluttpakkeDto mapFastsettEtterlønnSluttpakker(FastsettEtterlønnSluttpakkeDto fastsettEtterlønnSluttpakke) {
        return new no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FastsettEtterlønnSluttpakkeDto(fastsettEtterlønnSluttpakke.getFastsattPrMnd());
    }

    private static no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.VurderEtterlønnSluttpakkeDto mapVurderEtterlønnSluttpakke(VurderEtterlønnSluttpakkeDto vurderEtterlønnSluttpakke) {
        return new no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.VurderEtterlønnSluttpakkeDto(
            vurderEtterlønnSluttpakke.getErEtterlønnSluttpakke()
        );
    }

    private static no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FastsettBgKunYtelseDto mapFastsettKunYtelseDto(FastsettBgKunYtelseDto kunYtelseFordeling) {
        return new no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FastsettBgKunYtelseDto(
            kunYtelseFordeling.getAndeler() == null ? null : mapKunYtelseAndeler(kunYtelseFordeling.getAndeler()),
            kunYtelseFordeling.getSkalBrukeBesteberegning()
        );
    }

    private static List<no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FastsattBrukersAndel> mapKunYtelseAndeler(List<FastsattBrukersAndel> andeler) {
        return andeler.stream().map(OppdatererDtoMapper::mapFastsattBrukersAndel).collect(Collectors.toList());
    }

    private static no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FastsattBrukersAndel mapFastsattBrukersAndel(FastsattBrukersAndel fastsattBrukersAndel) {
        return new no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FastsattBrukersAndel(
            fastsattBrukersAndel.getNyAndel(),
            fastsattBrukersAndel.getAndelsnr(),
            fastsattBrukersAndel.getLagtTilAvSaksbehandler(),
            fastsattBrukersAndel.getFastsattBeløp(),
            fastsattBrukersAndel.getInntektskategori() == null ? null : Inntektskategori.fraKode(fastsattBrukersAndel.getInntektskategori().getKode())
        );
    }

    private static no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.VurderATogFLiSammeOrganisasjonDto mapVurderAtOgFLiSammeOrganisasjonDto(VurderATogFLiSammeOrganisasjonDto vurderATogFLiSammeOrganisasjon) {
        return new no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.VurderATogFLiSammeOrganisasjonDto(
            mapVurderAtOgFLiSammeOranisasjonAndelListe(vurderATogFLiSammeOrganisasjon.getVurderATogFLiSammeOrganisasjonAndelListe())
        );
    }

    private static List<no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.VurderATogFLiSammeOrganisasjonAndelDto> mapVurderAtOgFLiSammeOranisasjonAndelListe(List<VurderATogFLiSammeOrganisasjonAndelDto> vurderATogFLiSammeOrganisasjonAndelListe) {
        return vurderATogFLiSammeOrganisasjonAndelListe.stream().map(OppdatererDtoMapper::mapVurderATOgFLiSammeOrgAndel).collect(Collectors.toList());
    }

    private static no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.VurderATogFLiSammeOrganisasjonAndelDto mapVurderATOgFLiSammeOrgAndel(VurderATogFLiSammeOrganisasjonAndelDto vurderATogFLiSammeOrganisasjonAndelDto) {
        return new no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.VurderATogFLiSammeOrganisasjonAndelDto(vurderATogFLiSammeOrganisasjonAndelDto.getAndelsnr(), vurderATogFLiSammeOrganisasjonAndelDto.getArbeidsinntekt());
    }

    private static no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FastsettMånedsinntektUtenInntektsmeldingDto mapFastsattUtenInntektsmeldingDto(FastsettMånedsinntektUtenInntektsmeldingDto fastsattUtenInntektsmelding) {
        return new no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FastsettMånedsinntektUtenInntektsmeldingDto(mapFastsattUtenInntektsmeldingAndelListe(fastsattUtenInntektsmelding.getAndelListe()));
    }

    private static List<no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FastsettMånedsinntektUtenInntektsmeldingAndelDto> mapFastsattUtenInntektsmeldingAndelListe(List<FastsettMånedsinntektUtenInntektsmeldingAndelDto> andelListe) {
        return andelListe.stream().map(OppdatererDtoMapper::mapAndel).collect(Collectors.toList());
    }

    private static no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FastsettMånedsinntektUtenInntektsmeldingAndelDto mapAndel(FastsettMånedsinntektUtenInntektsmeldingAndelDto fastsettMånedsinntektUtenInntektsmeldingAndelDto) {
        return new no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FastsettMånedsinntektUtenInntektsmeldingAndelDto(
            fastsettMånedsinntektUtenInntektsmeldingAndelDto.getAndelsnr(),
            new no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FastsatteVerdierDto(
                fastsettMånedsinntektUtenInntektsmeldingAndelDto.getFastsattBeløp(),
                fastsettMånedsinntektUtenInntektsmeldingAndelDto.getInntektskategori() == null ?  null : Inntektskategori.fraKode(fastsettMånedsinntektUtenInntektsmeldingAndelDto.getInntektskategori().getKode())));
    }

    private static no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.VurderLønnsendringDto mapVurderLønnsendringDto(VurderLønnsendringDto vurdertLonnsendring) {
        return new no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.VurderLønnsendringDto(vurdertLonnsendring.erLønnsendringIBeregningsperioden());
    }

    private static no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FastsettMånedsinntektFLDto mapFastsettMånedsinntektFL(FastsettMånedsinntektFLDto fastsettMaanedsinntektFL) {
        return new no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FastsettMånedsinntektFLDto(fastsettMaanedsinntektFL.getMaanedsinntekt());
    }

    private static no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.VurderSelvstendigNæringsdrivendeNyIArbeidslivetDto mapVurderNyIArbeidslivet(VurderSelvstendigNæringsdrivendeNyIArbeidslivetDto vurderNyIArbeidslivet) {
        return new no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.VurderSelvstendigNæringsdrivendeNyIArbeidslivetDto(vurderNyIArbeidslivet.erNyIArbeidslivet());
    }

    private static no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.VurderTidsbegrensetArbeidsforholdDto mapTidsbegrensetArbeidsforhold(VurderTidsbegrensetArbeidsforholdDto vurderTidsbegrensetArbeidsforhold) {
        return new no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.VurderTidsbegrensetArbeidsforholdDto(
            mapVurderteArbeidsforhold(vurderTidsbegrensetArbeidsforhold.getFastsatteArbeidsforhold())
        );
    }

    private static List<no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.VurderteArbeidsforholdDto> mapVurderteArbeidsforhold(List<VurderteArbeidsforholdDto> fastsatteArbeidsforhold) {
        return fastsatteArbeidsforhold.stream().map(OppdatererDtoMapper::mapVurdertArbeidsforhold).collect(Collectors.toList());
    }

    private static no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.VurderteArbeidsforholdDto mapVurdertArbeidsforhold(VurderteArbeidsforholdDto vurderteArbeidsforholdDto) {
        return new no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.VurderteArbeidsforholdDto(
            vurderteArbeidsforholdDto.getAndelsnr(),
            vurderteArbeidsforholdDto.isTidsbegrensetArbeidsforhold(),
            vurderteArbeidsforholdDto.isOpprinneligVerdi()
        );
    }

    private static no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.VurderNyoppstartetFLDto mapVurderNyoppstartetFLDto(VurderNyoppstartetFLDto vurderNyoppstartetFL) {
        return new no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.VurderNyoppstartetFLDto(vurderNyoppstartetFL.erErNyoppstartetFL());
    }

    private static no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.BesteberegningFødendeKvinneDto mapBesteberegningFødendeKvinneDto(BesteberegningFødendeKvinneDto besteberegningAndeler) {
        return new no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.BesteberegningFødendeKvinneDto(mapBesteberegningAndeler(besteberegningAndeler.getBesteberegningAndelListe()));
    }

    private static List<no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.BesteberegningFødendeKvinneAndelDto> mapBesteberegningAndeler(List<BesteberegningFødendeKvinneAndelDto> besteberegningAndelListe) {
        return besteberegningAndelListe.stream().map(OppdatererDtoMapper::mapBesteberegningAndel).collect(Collectors.toList());
    }

    private static no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.BesteberegningFødendeKvinneAndelDto mapBesteberegningAndel(BesteberegningFødendeKvinneAndelDto besteberegningFødendeKvinneAndelDto) {
        return new no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.BesteberegningFødendeKvinneAndelDto(
            besteberegningFødendeKvinneAndelDto.getAndelsnr(),
            besteberegningFødendeKvinneAndelDto.getFastsatteVerdier().getFastsattBeløp(),
            Inntektskategori.fraKode(besteberegningFødendeKvinneAndelDto.getFastsatteVerdier().getInntektskategori().getKode()),
            besteberegningFødendeKvinneAndelDto.getLagtTilAvSaksbehandler());
    }

    private static List<no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.FaktaOmBeregningTilfelle> mapFaktaOmBeregningTilfeller(List<FaktaOmBeregningTilfelle> tilfeller) {
        return tilfeller.stream().map(FaktaOmBeregningTilfelle::getKode).map(no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.FaktaOmBeregningTilfelle::fraKode).collect(Collectors.toList());
    }


    private static List<no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.InntektPrAndelDto> mapTilInntektPrAndelListe(List<InntektPrAndelDto> inntektPrAndelList) {
        return inntektPrAndelList.stream().map(OppdatererDtoMapper::mapInntektPrAndel).collect(Collectors.toList());
    }

    private static no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.InntektPrAndelDto mapInntektPrAndel(InntektPrAndelDto inntektPrAndelDto) {
        return new no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.InntektPrAndelDto(inntektPrAndelDto.getInntekt(), inntektPrAndelDto.getAndelsnr());
    }

    private static List<no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FastsattePerioderTidsbegrensetDto> mapTilFastsattTidsbegrensetPerioder(List<FastsattePerioderTidsbegrensetDto> fastsatteTidsbegrensedePerioder) {
        return fastsatteTidsbegrensedePerioder.stream().map(OppdatererDtoMapper::mapTilFastsattTidsbegrensetPeriode).collect(Collectors.toList());
    }

    private static no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FastsattePerioderTidsbegrensetDto mapTilFastsattTidsbegrensetPeriode(FastsattePerioderTidsbegrensetDto fastsattePerioderTidsbegrensetDto) {
        return new no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FastsattePerioderTidsbegrensetDto(fastsattePerioderTidsbegrensetDto.getPeriodeFom(), fastsattePerioderTidsbegrensetDto.getPeriodeTom(), mapTilFastsattTidsbegrensetAndeler(fastsattePerioderTidsbegrensetDto.getFastsatteTidsbegrensedeAndeler()));
    }

    private static List<no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FastsatteAndelerTidsbegrensetDto> mapTilFastsattTidsbegrensetAndeler(List<FastsatteAndelerTidsbegrensetDto> fastsatteTidsbegrensedeAndeler) {
        return fastsatteTidsbegrensedeAndeler.stream().map(OppdatererDtoMapper::mapTilFastsattTidsbegrensetAndel).collect(Collectors.toList());
    }

    private static no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FastsatteAndelerTidsbegrensetDto mapTilFastsattTidsbegrensetAndel(FastsatteAndelerTidsbegrensetDto fastsatteAndelerTidsbegrensetDto) {
        return new no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FastsatteAndelerTidsbegrensetDto(fastsatteAndelerTidsbegrensetDto.getAndelsnr(), fastsatteAndelerTidsbegrensetDto.getBruttoFastsattInntekt());
    }

    private static List<no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.BeregningsaktivitetLagreDto> mapTilBeregningsaktivitetLagreDtoList(List<BeregningsaktivitetLagreDto> beregningsaktivitetLagreDtoList) {
        return beregningsaktivitetLagreDtoList.stream().map(OppdatererDtoMapper::mapTilBeregningsaktivitetLagreDto).collect(Collectors.toList());
    }

    private static no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.BeregningsaktivitetLagreDto mapTilBeregningsaktivitetLagreDto(BeregningsaktivitetLagreDto beregningsaktivitetLagreDto) {
        return no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.BeregningsaktivitetLagreDto.builder()
            .medArbeidsforholdRef(beregningsaktivitetLagreDto.getArbeidsforholdRef())
            .medArbeidsgiverIdentifikator(beregningsaktivitetLagreDto.getArbeidsgiverIdentifikator())
            .medFom(beregningsaktivitetLagreDto.getFom())
            .medOppdragsgiverOrg(beregningsaktivitetLagreDto.getOppdragsgiverOrg())
            .medOpptjeningAktivitetType(OpptjeningAktivitetType.fraKode(beregningsaktivitetLagreDto.getOpptjeningAktivitetType().getKode()))
            .medSkalBrukes(beregningsaktivitetLagreDto.getSkalBrukes())
            .medTom(beregningsaktivitetLagreDto.getTom())
            .build();
    }

    private static List<no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FastsettBeregningsgrunnlagPeriodeDto> mapTilEndredePerioderList(List<FastsettBeregningsgrunnlagPeriodeDto> endretBeregningsgrunnlagPerioder) {
        return endretBeregningsgrunnlagPerioder.stream().map(OppdatererDtoMapper::mapTilFastsettBeregningsgrunnlagPeriodeDto).collect(Collectors.toList());
    }

    private static no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FastsettBeregningsgrunnlagPeriodeDto mapTilFastsettBeregningsgrunnlagPeriodeDto(FastsettBeregningsgrunnlagPeriodeDto fastsettBeregningsgrunnlagPeriodeDto) {
        return new no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FastsettBeregningsgrunnlagPeriodeDto(mapFastsettBeregningsgrunnlagPeriodeAndeler(fastsettBeregningsgrunnlagPeriodeDto.getAndeler()), fastsettBeregningsgrunnlagPeriodeDto.getFom(), fastsettBeregningsgrunnlagPeriodeDto.getTom());
    }

    private static List<no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FastsettBeregningsgrunnlagAndelDto> mapFastsettBeregningsgrunnlagPeriodeAndeler(List<FastsettBeregningsgrunnlagAndelDto> andeler) {
        return andeler.stream().map(OppdatererDtoMapper::mapFastsettBeregningsgrunnlagPeriodeAndelDto).collect(Collectors.toList());
    }

    private static no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FastsettBeregningsgrunnlagAndelDto mapFastsettBeregningsgrunnlagPeriodeAndelDto(FastsettBeregningsgrunnlagAndelDto fastsettBeregningsgrunnlagAndelDto) {
        return new no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FastsettBeregningsgrunnlagAndelDto(
            mapTilRedigerbarAndelDto(fastsettBeregningsgrunnlagAndelDto),
            mapTilFastsatteVerdier(fastsettBeregningsgrunnlagAndelDto.getFastsatteVerdier()),
            fastsettBeregningsgrunnlagAndelDto.getForrigeInntektskategori() == null ? null : Inntektskategori.fraKode(fastsettBeregningsgrunnlagAndelDto.getForrigeInntektskategori().getKode()),
            fastsettBeregningsgrunnlagAndelDto.getForrigeRefusjonPrÅr(),
            fastsettBeregningsgrunnlagAndelDto.getForrigeArbeidsinntektPrÅr());
    }

    private static no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FastsatteVerdierDto mapTilFastsatteVerdier(FastsatteVerdierDto fastsatteVerdier) {
        return new no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FastsatteVerdierDto(
            fastsatteVerdier.getRefusjonPrÅr(),
            fastsatteVerdier.getFastsattÅrsbeløp(),
            Inntektskategori.fraKode(fastsatteVerdier.getInntektskategori().getKode()),
            fastsatteVerdier.getSkalHaBesteberegning());
    }

    private static no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.RedigerbarAndelDto mapTilRedigerbarAndelDto(RedigerbarAndelDto redigerbarAndel) {
        return new no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.RedigerbarAndelDto(
            redigerbarAndel.getAndelsnr(),
            redigerbarAndel.getArbeidsgiverId(),
            redigerbarAndel.getArbeidsforholdId().getReferanse(),
            redigerbarAndel.getNyAndel(),
            redigerbarAndel.getAktivitetStatus() == null ? null : AktivitetStatus.fraKode(redigerbarAndel.getAktivitetStatus().getKode()),
            redigerbarAndel.getArbeidsforholdType() == null ? null : OpptjeningAktivitetType.fraKode(redigerbarAndel.getArbeidsforholdType().getKode()),
            redigerbarAndel.getLagtTilAvSaksbehandler(),
            redigerbarAndel.getBeregningsperiodeFom(),
            redigerbarAndel.getBeregningsperiodeTom());
    }
}
