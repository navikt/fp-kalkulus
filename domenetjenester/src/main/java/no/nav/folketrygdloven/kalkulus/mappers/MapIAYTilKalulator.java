package no.nav.folketrygdloven.kalkulus.mappers;


import java.util.Optional;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.NaturalYtelse;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektspostDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.NaturalYtelseDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittOpptjeningDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.RefusjonDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.VersjonTypeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseAnvistDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseAnvistDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseStørrelseDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseStørrelseDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.typer.AktørId;
import no.nav.folketrygdloven.kalkulator.modell.typer.EksternArbeidsforholdRef;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.ArbeidType;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.InntektsKilde;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.NæringsinntektType;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.OffentligYtelseType;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.PensjonTrygdType;
import no.nav.folketrygdloven.kalkulus.felles.v1.AktørIdPersonident;
import no.nav.folketrygdloven.kalkulus.iay.arbeid.v1.AktivitetsAvtaleDto;
import no.nav.folketrygdloven.kalkulus.iay.arbeid.v1.ArbeidDto;
import no.nav.folketrygdloven.kalkulus.iay.inntekt.v1.InntekterDto;
import no.nav.folketrygdloven.kalkulus.iay.inntekt.v1.UtbetalingDto;
import no.nav.folketrygdloven.kalkulus.iay.inntekt.v1.UtbetalingsPostDto;
import no.nav.folketrygdloven.kalkulus.iay.ytelse.v1.YtelseDto;
import no.nav.folketrygdloven.kalkulus.iay.ytelse.v1.YtelserDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.UtbetaltNæringsYtelseType;
import no.nav.folketrygdloven.kalkulus.kodeverk.UtbetaltPensjonTrygdType;
import no.nav.folketrygdloven.kalkulus.kodeverk.UtbetaltYtelseFraOffentligeType;
import no.nav.folketrygdloven.kalkulus.kodeverk.UtbetaltYtelseType;

public class MapIAYTilKalulator {

    public static InternArbeidsforholdRefDto mapArbeidsforholdRef(no.nav.folketrygdloven.kalkulus.felles.v1.InternArbeidsforholdRefDto arbeidsforholdRef) {
        return InternArbeidsforholdRefDto.ref(arbeidsforholdRef.getAbakusReferanse());
    }

    public static EksternArbeidsforholdRef mapArbeidsforholdEksternRef(EksternArbeidsforholdRef arbeidsforholdRef) {
        return EksternArbeidsforholdRef.ref(arbeidsforholdRef.getReferanse());
    }

    public static InntektArbeidYtelseGrunnlagDto mapGrunnlag(no.nav.folketrygdloven.kalkulus.iay.v1.InntektArbeidYtelseGrunnlagDto iayGrunnlag, AktørIdPersonident id) {
        InntektArbeidYtelseGrunnlagDtoBuilder builder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        InntektArbeidYtelseAggregatBuilder data = mapAggregat(iayGrunnlag, id);
        builder.medData(data);



        //iayGrunnlag.getInntektsmeldinger().ifPresent(aggregat -> builder.setInntektsmeldinger(mapInntektsmelding(aggregat, iayGrunnlag.getArbeidsforholdInformasjon())));
        //iayGrunnlag.getArbeidsforholdInformasjon().ifPresent(arbeidsforholdInformasjon -> builder.medInformasjon(mapArbeidsforholdInformasjon(arbeidsforholdInformasjon)));
        //iayGrunnlag.getOppgittOpptjening().ifPresent(oppgittOpptjening -> builder.medOppgittOpptjening(mapOppgittOpptjening(oppgittOpptjening)));
        //builder.medErAktivtGrunnlag(iayGrunnlag.isAktiv());
        return builder.build();
    }

    /**
    public static Map<Arbeidsgiver, ArbeidsgiverOpplysningerDto> mapArbeidsforholdOpplysninger(Map<Arbeidsgiver, ArbeidsgiverOpplysninger> arbeidsgiverOpplysninger, List<ArbeidsforholdOverstyring> overstyringer) {
        Map<Arbeidsgiver, ArbeidsgiverOpplysningerDto> returnMap = new HashMap<>();
        arbeidsgiverOpplysninger.entrySet().stream()
            .forEach(e -> returnMap.put(mapArbeidsgiver(e.getKey()), mapOpplysning(e.getValue())));
        overstyringer
            .stream()
            .findFirst()
            .ifPresent(arbeidsforhold -> returnMap.put(mapArbeidsgiver(arbeidsforhold.getArbeidsgiver()),
                new ArbeidsgiverOpplysningerDto(arbeidsforhold.getArbeidsgiver().getOrgnr(), arbeidsforhold.getArbeidsgiverNavn())));
        return returnMap;

    }

    public  static ArbeidsgiverOpplysningerDto mapOpplysning(ArbeidsgiverOpplysninger arbeidsgiverOpplysninger) {
        return new ArbeidsgiverOpplysningerDto(arbeidsgiverOpplysninger.getIdentifikator(), arbeidsgiverOpplysninger.getNavn(), arbeidsgiverOpplysninger.getFødselsdato());
    }

    private static OppgittOpptjeningDtoBuilder mapOppgittOpptjening(OppgittOpptjening oppgittOpptjening) {
        OppgittOpptjeningDtoBuilder builder = OppgittOpptjeningDtoBuilder.ny();
        oppgittOpptjening.getFrilans().ifPresent(oppgittFrilans -> builder.leggTilFrilansOpplysninger(new OppgittFrilansDto(oppgittFrilans.getHarInntektFraFosterhjem(), oppgittFrilans.getErNyoppstartet(), oppgittFrilans.getHarNærRelasjon())));

        oppgittOpptjening.getAnnenAktivitet().forEach(oppgittAnnenAktivitet -> builder.leggTilAnnenAktivitet(new OppgittAnnenAktivitetDto(mapDatoIntervall(oppgittAnnenAktivitet.getPeriode()), ArbeidType.fraKode(oppgittAnnenAktivitet.getArbeidType().getKode()))));
        oppgittOpptjening.getEgenNæring().forEach(oppgittEgenNæring -> builder.leggTilEgneNæring(mapEgenNæring(oppgittEgenNæring)));
        oppgittOpptjening.getOppgittArbeidsforhold().forEach(oppgittArbeidsforhold -> builder.leggTilOppgittArbeidsforhold(mapOppgittArbeidsforhold(oppgittArbeidsforhold)));

        return builder;
    }

    private static OppgittOpptjeningDtoBuilder.OppgittArbeidsforholdBuilder mapOppgittArbeidsforhold(OppgittArbeidsforhold oppgittArbeidsforhold) {
        return OppgittOpptjeningDtoBuilder.OppgittArbeidsforholdBuilder.ny()
            .medArbeidType(ArbeidType.fraKode(oppgittArbeidsforhold.getArbeidType().getKode()))
            .medPeriode(mapDatoIntervall(oppgittArbeidsforhold.getPeriode()));
    }

    private static Intervall mapDatoIntervall(no.nav.foreldrepenger.domene.tid.DatoIntervallEntitet periode) {
        return Intervall.fraOgMedTilOgMed(periode.getFomDato(), periode.getTomDato());
    }
     */

    private static OppgittOpptjeningDtoBuilder.EgenNæringBuilder mapEgenNæring() {
        OppgittOpptjeningDtoBuilder.EgenNæringBuilder egenNæringBuilder = OppgittOpptjeningDtoBuilder.EgenNæringBuilder.ny();
            //.medPeriode(mapDatoIntervall(oppgittEgenNæring.getPeriode()))
            //.medBruttoInntekt(oppgittEgenNæring.getBruttoInntekt())
            //.medEndringDato(oppgittEgenNæring.getEndringDato())
            //.medNyIArbeidslivet(oppgittEgenNæring.getNyIArbeidslivet())
            //.medVirksomhetType(VirksomhetType.fraKode(oppgittEgenNæring.getVirksomhetType().getKode()))
            //.medVarigEndring(oppgittEgenNæring.getVarigEndring())
            //.medBegrunnelse(oppgittEgenNæring.getBegrunnelse())
            //.medRegnskapsførerNavn(oppgittEgenNæring.getRegnskapsførerNavn())
            //.medNærRelasjon(oppgittEgenNæring.getNærRelasjon())
            //.medNyoppstartet(oppgittEgenNæring.getNyoppstartet())
            //.medRegnskapsførerTlf(oppgittEgenNæring.getRegnskapsførerTlf());
        //if (oppgittEgenNæring.getOrgnr() != null) {
        //   egenNæringBuilder.medVirksomhet(oppgittEgenNæring.getOrgnr());
        //}
        return egenNæringBuilder;
    }

    /**
    private static ArbeidsforholdInformasjonDto mapArbeidsforholdInformasjon(ArbeidsforholdInformasjon arbeidsforholdInformasjon) {
        ArbeidsforholdInformasjonDtoBuilder builder = ArbeidsforholdInformasjonDtoBuilder.builder(Optional.empty());
        arbeidsforholdInformasjon.getArbeidsforholdReferanser().forEach(arbeidsforholdReferanse -> builder.leggTilNyReferanse(mapRefDto(arbeidsforholdReferanse)));
        arbeidsforholdInformasjon.getOverstyringer().forEach(arbeidsforholdOverstyring -> builder.leggTil(mapOverstyringerDto(arbeidsforholdOverstyring)));
        return builder.build();
    }

    private static BekreftetPermisjonDto mapBekreftetPermisjonDto(BekreftetPermisjon bekreftetPermisjon) {
        return new BekreftetPermisjonDto(bekreftetPermisjon.getPeriode().getFomDato(), bekreftetPermisjon.getPeriode().getTomDato(), BekreftetPermisjonStatus.fraKode(bekreftetPermisjon.getStatus().getKode()));
    }

    private static ArbeidsforholdReferanseDto mapRefDto(ArbeidsforholdReferanse arbeidsforholdReferanse) {
        return new ArbeidsforholdReferanseDto(mapArbeidsgiver(arbeidsforholdReferanse.getArbeidsgiver()),
            mapArbeidsforholdRef(arbeidsforholdReferanse.getInternReferanse()),
            mapArbeidsforholdEksternRef(arbeidsforholdReferanse.getEksternReferanse()));
    }
     */

    public static InntektsmeldingAggregatDto mapInntektsmelding() {
        //InntektsmeldingAggregatDto.InntektsmeldingAggregatDtoBuilder builder = InntektsmeldingAggregatDto.InntektsmeldingAggregatDtoBuilder.ny();
        //aggregat.getAlleInntektsmeldinger().forEach(inntektsmelding -> builder.leggTil(mapInntektsmeldingDto(inntektsmelding)));
        //arbeidsforholdInformasjon.ifPresent(info -> builder.medArbeidsforholdInformasjonDto(mapArbeidsforholdInformasjon(info)));
        //return builder.build();
        return null;
    }

    private static InntektsmeldingDto mapInntektsmeldingDto() {
        InntektsmeldingDtoBuilder builder = InntektsmeldingDtoBuilder.builder();
        //builder.medArbeidsgiver(mapArbeidsgiver(inntektsmelding.getArbeidsgiver()));
        //builder.medArbeidsforholdId(mapArbeidsforholdRef(inntektsmelding.getArbeidsforholdRef()));
        //builder.medRefusjon(inntektsmelding.getRefusjonBeløpPerMnd() == null ? null : inntektsmelding.getRefusjonBeløpPerMnd().getVerdi(), inntektsmelding.getRefusjonOpphører());
        //builder.medBeløp(inntektsmelding.getInntektBeløp().getVerdi());
        //inntektsmelding.getNaturalYtelser().stream().map(IAYMapperTilKalkulus::mapNaturalYtelse).forEach(builder::leggTil);
        //inntektsmelding.getStartDatoPermisjon().ifPresent(builder::medStartDatoPermisjon);
        //inntektsmelding.getEndringerRefusjon().forEach(refusjon -> builder.leggTil(mapRefusjon(refusjon)));
        return builder.build(true);
    }

    private static NaturalYtelseDto mapNaturalYtelse(NaturalYtelse naturalYtelse) {
        //return new NaturalYtelseDto(naturalYtelse.getPeriode().getFomDato(), naturalYtelse.getPeriode().getTomDato(), naturalYtelse.getBeloepPerMnd().getVerdi(), NaturalYtelseType.fraKode(naturalYtelse.getType().getKode()));
        return null;
    }

    private static RefusjonDto mapRefusjon() {
       // return new RefusjonDto(refusjon.getRefusjonsbeløp().getVerdi(), refusjon.getFom());
        return null;
    }

    private static AktivitetsAvtaleDtoBuilder mapAktivitetsAvtale(AktivitetsAvtaleDto aktivitetsAvtale) {
        return AktivitetsAvtaleDtoBuilder.ny()
            .medPeriode(Intervall.fraOgMedTilOgMed(aktivitetsAvtale.getPeriode().getFom(), aktivitetsAvtale.getPeriode().getTom()))
            .medProsentsats(aktivitetsAvtale.getStillingsprosent())
            .medErAnsettelsesPeriode(aktivitetsAvtale.getStillingsprosent() == null);
    }


    private static YrkesaktivitetDto mapYrkesaktivitet(no.nav.folketrygdloven.kalkulus.iay.arbeid.v1.YrkesaktivitetDto yrkesaktivitet) {
        YrkesaktivitetDtoBuilder dtoBuilder = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty());
        yrkesaktivitet.getAktivitetsAvtaler().forEach(aktivitetsAvtale -> dtoBuilder.leggTilAktivitetsAvtale(mapAktivitetsAvtale(aktivitetsAvtale)));
        dtoBuilder.medArbeidsforholdId(mapArbeidsforholdRef(yrkesaktivitet.getAbakusReferanse()));
        dtoBuilder.medArbeidsgiver(MapFraKalkulator.mapArbeidsgiver(yrkesaktivitet.getArbeidsgiver()));
        dtoBuilder.medArbeidsgiverNavn(yrkesaktivitet.getNavnArbeidsgiverUtland());
        dtoBuilder.medArbeidType(ArbeidType.fraKode(yrkesaktivitet.getArbeidType().getKode()));
        return dtoBuilder.build();
    }


    private static InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder mapInntekt(InntekterDto inntekterDto, AktørIdPersonident id){
        InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder builder = InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder.oppdatere(Optional.empty());
        inntekterDto.getUtbetalinger().forEach(inntekt -> builder.leggTilInntekt(mapInntekt(inntekt)));
        builder.medAktørId(new AktørId(id.getIdent()));

        return builder;
    }

    private static InntektDtoBuilder mapInntekt(UtbetalingDto inntekt) {
        InntektDtoBuilder builder = InntektDtoBuilder.oppdatere(Optional.empty());
        inntekt.getPoster().forEach(inntektspost -> builder.leggTilInntektspost(mapInntektspost(inntektspost)));
        builder.medArbeidsgiver(MapFraKalkulator.mapArbeidsgiver(inntekt.getUtbetaler()));
        builder.medInntektsKilde(InntektsKilde.fraKode(inntekt.getKilde().getKode()));
        return builder;
    }

    private static InntektspostDtoBuilder mapInntektspost(UtbetalingsPostDto inntektspost) {
        InntektspostDtoBuilder builder = InntektspostDtoBuilder.ny();
        builder.medBeløp(inntektspost.getBeløp());
        builder.medInntektspostType(inntektspost.getInntektspostType().getKode());
        builder.medPeriode(inntektspost.getPeriode().getFom(), inntektspost.getPeriode().getTom());
        builder.medSkatteOgAvgiftsregelType(inntektspost.getSkattAvgiftType().getKode());
        builder.medYtelse(mapUtbetaltYtelseTypeTilGrunnlag(inntektspost.getYtelseType()));

        return builder;
    }

    private static InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder mapArbeid(ArbeidDto arbeid, AktørIdPersonident id) {
        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder builder = InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder.oppdatere(Optional.empty());
        arbeid.getYrkesaktiviteter().forEach(yrkesaktivitet -> builder.leggTilYrkesaktivitet(mapYrkesaktivitet(yrkesaktivitet)));
        builder.medAktørId(new AktørId(id.getIdent()));
        return builder;
    }

    private static InntektArbeidYtelseAggregatBuilder mapAggregat(no.nav.folketrygdloven.kalkulus.iay.v1.InntektArbeidYtelseGrunnlagDto grunnlagDto, AktørIdPersonident id) {
        InntektArbeidYtelseAggregatBuilder builder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        builder.leggTilAktørArbeid(mapArbeid(grunnlagDto.getArbeidDto(), id));
        builder.leggTilAktørInntekt(mapInntekt(grunnlagDto.getInntekterDto(), id));
        builder.leggTilAktørYtelse(mapAktørYtelse(grunnlagDto.getYtelserDto(), id));
        return builder;

    }

    private static InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder mapAktørYtelse(YtelserDto ytelser, AktørIdPersonident id) {
        InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder builder = InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder.oppdatere(Optional.empty());
        ytelser.getYtelser().forEach(ytelse -> builder.leggTilYtelse(mapYtelse(ytelse)));
        builder.medAktørId(new AktørId(id.getIdent()));
        return builder;

    }

    private static YtelseDtoBuilder mapYtelse(YtelseDto ytelse) {
        YtelseDtoBuilder builder = YtelseDtoBuilder.oppdatere(Optional.empty());
        //ytelse.getYtelseAnvist().forEach(ytelseAnvist -> builder.leggTilYtelseAnvist(mapYtelseAnvist(ytelseAnvist)));
        //ytelse.getYtelseGrunnlag().ifPresent(ytelseGrunnlag -> builder.medYtelseGrunnlag(mapYtelseGrunnlag(ytelseGrunnlag)));
        //builder.medBehandlingsTema(TemaUnderkategori.fraKode(ytelse.getBehandlingsTema().getKode()));
        //builder.medKilde(Fagsystem.fraKode(ytelse.getKilde().getKode()));
        //builder.medPeriode(mapDatoIntervall(ytelse.getPeriode()));
        //builder.medYtelseType(RelatertYtelseType.fraKode(ytelse.getRelatertYtelseType().getKode()));
        return builder;
    }

    private static YtelseGrunnlagDto mapYtelseGrunnlag() {
        YtelseGrunnlagDtoBuilder builder = YtelseGrunnlagDtoBuilder.ny();
       // ytelseGrunnlag.getYtelseStørrelse().forEach(ytelseStørrelse -> builder.leggTilYtelseStørrelse(mapYtelseStørrelse(ytelseStørrelse)));
        //ytelseGrunnlag.getArbeidskategori().ifPresent(arbeidskategori -> builder.medArbeidskategori(Arbeidskategori.fraKode(arbeidskategori.getKode())));
        //ytelseGrunnlag.getDekningsgradProsent().ifPresent(stillingsprosent -> builder.medDekningsgradProsent(stillingsprosent.getVerdi()));
        //ytelseGrunnlag.getGraderingProsent().ifPresent(stillingsprosent -> builder.medGraderingProsent(stillingsprosent.getVerdi()));
       // ytelseGrunnlag.getOpprinneligIdentdato().ifPresent(builder::medOpprinneligIdentdato);
        //ytelseGrunnlag.getInntektsgrunnlagProsent().ifPresent(stillingsprosent -> builder.medInntektsgrunnlagProsent(stillingsprosent.getVerdi()));
        //ytelseGrunnlag.getVedtaksDagsats().ifPresent(beløp -> builder.medVedtaksDagsats(beløp.getVerdi()));
        return builder.build();
    }

    private static YtelseStørrelseDto mapYtelseStørrelse() {
        YtelseStørrelseDtoBuilder builder = YtelseStørrelseDtoBuilder.ny();
        //builder.medBeløp(ytelseStørrelse.getBeløp().getVerdi());
        //builder.medHyppighet(InntektPeriodeType.fraKode(ytelseStørrelse.getHyppighet().getKode()));
        //ytelseStørrelse.getOrgnr().ifPresent(builder::medVirksomhet);
        return builder.build();
    }

    private static YtelseAnvistDto mapYtelseAnvist() {
        YtelseAnvistDtoBuilder builder = YtelseAnvistDtoBuilder.ny();
        //builder.medAnvistPeriode(Intervall.fraOgMedTilOgMed(ytelseAnvist.getAnvistFOM(), ytelseAnvist.getAnvistFOM()));
        //ytelseAnvist.getBeløp().ifPresent(beløp -> builder.medBeløp(beløp.getVerdi()));
        //ytelseAnvist.getDagsats().ifPresent(dagsats -> builder.medDagsats(dagsats.getVerdi()));
        //ytelseAnvist.getUtbetalingsgradProsent().ifPresent(stillingsprosent -> builder.medUtbetalingsgradProsent(stillingsprosent.getVerdi()));
        return builder.build();
    }

    static no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.YtelseType mapUtbetaltYtelseTypeTilGrunnlag(UtbetaltYtelseType type) {

        if (type == null)
            return OffentligYtelseType.UDEFINERT;

        if (type instanceof UtbetaltNæringsYtelseType) {
            return NæringsinntektType.fraKode(((UtbetaltNæringsYtelseType) type).getKode());

        } else if (type instanceof UtbetaltPensjonTrygdType) {
            return PensjonTrygdType.fraKode(((UtbetaltPensjonTrygdType) type).getKode());
        } else if (type instanceof UtbetaltYtelseFraOffentligeType) {
            return OffentligYtelseType.fraKode(((UtbetaltYtelseFraOffentligeType) type).getKode());
        }
        throw new IllegalStateException("Kunne ikke mappe" + type);
    }

}
