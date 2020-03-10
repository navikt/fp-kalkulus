package no.nav.folketrygdloven.kalkulus.mappers;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.kontrakt.v1.ArbeidsgiverOpplysningerDto;
import no.nav.folketrygdloven.kalkulator.modell.behandling.Fagsystem;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.ArbeidsforholdInformasjonDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.ArbeidsforholdOverstyringDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektspostDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.NaturalYtelseDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittFrilansDto;
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
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.ytelse.TemaUnderkategori;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.ArbeidType;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.ArbeidsforholdHandlingType;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.Arbeidskategori;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.InntektPeriodeType;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.InntektsKilde;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.NaturalYtelseType;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.NæringsinntektType;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.OffentligYtelseType;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.PensjonTrygdType;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.VirksomhetType;
import no.nav.folketrygdloven.kalkulus.felles.v1.AktørIdPersonident;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.iay.arbeid.v1.AktivitetsAvtaleDto;
import no.nav.folketrygdloven.kalkulus.iay.arbeid.v1.ArbeidDto;
import no.nav.folketrygdloven.kalkulus.iay.arbeid.v1.ArbeidsforholdInformasjonDto;
import no.nav.folketrygdloven.kalkulus.iay.inntekt.v1.InntekterDto;
import no.nav.folketrygdloven.kalkulus.iay.inntekt.v1.InntektsmeldingerDto;
import no.nav.folketrygdloven.kalkulus.iay.inntekt.v1.UtbetalingDto;
import no.nav.folketrygdloven.kalkulus.iay.inntekt.v1.UtbetalingsPostDto;
import no.nav.folketrygdloven.kalkulus.iay.ytelse.v1.YtelseDto;
import no.nav.folketrygdloven.kalkulus.iay.ytelse.v1.YtelserDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.UtbetaltNæringsYtelseType;
import no.nav.folketrygdloven.kalkulus.kodeverk.UtbetaltPensjonTrygdType;
import no.nav.folketrygdloven.kalkulus.kodeverk.UtbetaltYtelseFraOffentligeType;
import no.nav.folketrygdloven.kalkulus.kodeverk.UtbetaltYtelseType;
import no.nav.folketrygdloven.kalkulus.opptjening.v1.OppgittEgenNæringDto;
import no.nav.folketrygdloven.kalkulus.opptjening.v1.OppgittOpptjeningDto;

public class MapIAYTilKalulator {

    public static InternArbeidsforholdRefDto mapArbeidsforholdRef(no.nav.folketrygdloven.kalkulus.felles.v1.InternArbeidsforholdRefDto arbeidsforholdRef) {
        if (arbeidsforholdRef == null) {
            return null;
        }
        return InternArbeidsforholdRefDto.ref(arbeidsforholdRef.getAbakusReferanse());
    }

    public static List<ArbeidsgiverOpplysningerDto> mapArbeidsgiverOpplysninger(List<no.nav.folketrygdloven.kalkulus.iay.arbeid.v1.ArbeidsgiverOpplysningerDto> arbeidsgiverOpplysninger) {
        return arbeidsgiverOpplysninger.stream().map(a -> new ArbeidsgiverOpplysningerDto(a.getIdentifikator(), a.getNavn(), a.getFødselsdato()))
                .collect(Collectors.toList());
    }

    public static InntektArbeidYtelseGrunnlagDto mapGrunnlag(no.nav.folketrygdloven.kalkulus.iay.v1.InntektArbeidYtelseGrunnlagDto iayGrunnlag, AktørIdPersonident id) {
        InntektArbeidYtelseGrunnlagDtoBuilder builder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        InntektArbeidYtelseAggregatBuilder data = mapAggregat(iayGrunnlag, id);
        builder.medData(data);

        if (iayGrunnlag.getInntektsmeldingDto() != null) {
            InntektsmeldingAggregatDto inntektsmeldingAggregatDto = mapInntektsmelding(iayGrunnlag.getInntektsmeldingDto());
            builder.setInntektsmeldinger(inntektsmeldingAggregatDto);
        }

        if (iayGrunnlag.getArbeidsforholdInformasjon() != null) {
            no.nav.folketrygdloven.kalkulator.modell.iay.ArbeidsforholdInformasjonDto arbeidsforholdInformasjonDto = mapArbeidsforholdInformasjon(iayGrunnlag.getArbeidsforholdInformasjon());
            builder.medInformasjon(arbeidsforholdInformasjonDto);
        }

        if (iayGrunnlag.getOppgittOpptjening() != null) {
            builder.medOppgittOpptjening(mapOppgittOpptjening(iayGrunnlag.getOppgittOpptjening()));
        }

        builder.medErAktivtGrunnlag(true);
        return builder.build();
    }

    private static no.nav.folketrygdloven.kalkulator.modell.iay.ArbeidsforholdInformasjonDto mapArbeidsforholdInformasjon(ArbeidsforholdInformasjonDto arbeidsforholdInformasjon) {
        ArbeidsforholdInformasjonDtoBuilder builder = ArbeidsforholdInformasjonDtoBuilder.builder(Optional.empty());

        arbeidsforholdInformasjon.getOverstyringer().forEach(arbeidsforholdOverstyringDto -> {
            ArbeidsforholdOverstyringDtoBuilder ny = ArbeidsforholdOverstyringDtoBuilder.oppdatere(Optional.empty());
            ny.medHandling(ArbeidsforholdHandlingType.fraKode(arbeidsforholdOverstyringDto.getHandling().getKode()));
            ny.medArbeidsgiver(MapFraKalkulator.mapArbeidsgiver(arbeidsforholdOverstyringDto.getArbeidsgiver()));
            ny.medArbeidsforholdRef(mapArbeidsforholdRef(arbeidsforholdOverstyringDto.getArbeidsforholdRefDto()));
            builder.leggTil(ny);
        });

        return builder.build();
    }

    private static OppgittOpptjeningDtoBuilder mapOppgittOpptjening(OppgittOpptjeningDto oppgittOpptjening) {
        OppgittOpptjeningDtoBuilder builder = OppgittOpptjeningDtoBuilder.ny();
        if (oppgittOpptjening.getFrilans() != null) {
            no.nav.folketrygdloven.kalkulus.opptjening.v1.OppgittFrilansDto oppgittFrilans = oppgittOpptjening.getFrilans();
            builder.leggTilFrilansOpplysninger(new OppgittFrilansDto(oppgittFrilans.getHarInntektFraFosterhjem(), oppgittFrilans.getErNyoppstartet(), oppgittFrilans.getHarNærRelasjon()));
        }
        if (oppgittOpptjening.getEgenNæring() != null) {
            oppgittOpptjening.getEgenNæring().forEach(egen -> builder.leggTilEgneNæring(mapEgenNæring(egen)));
        }
        return builder;
    }

    private static Intervall mapDatoIntervall(Periode periode) {
        return Intervall.fraOgMedTilOgMed(periode.getFom(), periode.getTom());
    }

    private static OppgittOpptjeningDtoBuilder.EgenNæringBuilder mapEgenNæring(OppgittEgenNæringDto oppgittEgenNæring) {
        OppgittOpptjeningDtoBuilder.EgenNæringBuilder egenNæringBuilder = OppgittOpptjeningDtoBuilder.EgenNæringBuilder.ny()
                .medPeriode(mapDatoIntervall(oppgittEgenNæring.getPeriode()))
                .medBruttoInntekt(oppgittEgenNæring.getBruttoInntekt())
                .medNyIArbeidslivet(oppgittEgenNæring.getNyIArbeidslivet())
                .medVirksomhetType(VirksomhetType.fraKode(oppgittEgenNæring.getVirksomhetType().getKode()))
                .medVarigEndring(oppgittEgenNæring.getVarigEndring())
                .medNærRelasjon(oppgittEgenNæring.getNærRelasjon())
                .medNyoppstartet(oppgittEgenNæring.getNyoppstartet());
        if (oppgittEgenNæring.getAktør() != null && oppgittEgenNæring.getAktør().getErOrganisasjon()) {
            egenNæringBuilder.medVirksomhet(oppgittEgenNæring.getAktør().getIdent());
        }
        return egenNæringBuilder;
    }

    public static InntektsmeldingAggregatDto mapInntektsmelding(InntektsmeldingerDto inntektsmeldingDto) {
        InntektsmeldingAggregatDto.InntektsmeldingAggregatDtoBuilder builder = InntektsmeldingAggregatDto.InntektsmeldingAggregatDtoBuilder.ny();
        inntektsmeldingDto.getInntektsmeldinger().forEach(inntektsmelding -> builder.leggTil(mapInntektsmeldingDto(inntektsmelding)));
        return builder.build();
    }

    private static InntektsmeldingDto mapInntektsmeldingDto(no.nav.folketrygdloven.kalkulus.iay.inntekt.v1.InntektsmeldingDto inntektsmelding) {
        InntektsmeldingDtoBuilder builder = InntektsmeldingDtoBuilder.builder();
        builder.medArbeidsgiver(MapFraKalkulator.mapArbeidsgiver(inntektsmelding.getArbeidsgiver()));
        builder.medArbeidsforholdId(mapArbeidsforholdRef(inntektsmelding.getArbeidsforholdRef()));
        builder.medRefusjon(inntektsmelding.getRefusjonBeløpPerMnd() == null ? null : inntektsmelding.getRefusjonBeløpPerMnd().getVerdi(), inntektsmelding.getRefusjonOpphører());
        builder.medBeløp(inntektsmelding.getInntektBeløp().getVerdi());
        if (inntektsmelding.getNaturalYtelser() != null) {
            inntektsmelding.getNaturalYtelser().stream().map(MapIAYTilKalulator::mapNaturalYtelse).forEach(builder::leggTil);
        }
        if (inntektsmelding.getStartDatoPermisjon() != null) {
            builder.medStartDatoPermisjon(inntektsmelding.getStartDatoPermisjon());
        }
        if (inntektsmelding.getEndringerRefusjon() != null) {
            inntektsmelding.getEndringerRefusjon().forEach(refusjon -> builder.leggTil(mapRefusjon(refusjon)));
        }
        return builder.build(true);
    }

    private static NaturalYtelseDto mapNaturalYtelse(no.nav.folketrygdloven.kalkulus.iay.inntekt.v1.NaturalYtelseDto naturalYtelse) {
        return new NaturalYtelseDto(naturalYtelse.getPeriode().getFom(),
                naturalYtelse.getPeriode().getTom(),
                naturalYtelse.getBeløp().getVerdi(),
                NaturalYtelseType.fraKode(naturalYtelse.getType().getKode()));
    }

    private static RefusjonDto mapRefusjon(no.nav.folketrygdloven.kalkulus.iay.inntekt.v1.RefusjonDto refusjon) {
        return new RefusjonDto(refusjon.getRefusjonsbeløpMnd().getVerdi(), refusjon.getFom());
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

    private static InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder mapInntekt(InntekterDto inntekterDto, AktørIdPersonident id) {
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
        if (inntektspost.getSkattAvgiftType() != null) {
            builder.medSkatteOgAvgiftsregelType(inntektspost.getSkattAvgiftType().getKode());
        }
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
        if (grunnlagDto.getArbeidDto() != null) {
            builder.leggTilAktørArbeid(mapArbeid(grunnlagDto.getArbeidDto(), id));
        }
        if (grunnlagDto.getInntekterDto() != null) {
            builder.leggTilAktørInntekt(mapInntekt(grunnlagDto.getInntekterDto(), id));
        }
        if (grunnlagDto.getYtelserDto() != null) {
            builder.leggTilAktørYtelse(mapAktørYtelse(grunnlagDto.getYtelserDto(), id));
        }
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
        if (ytelse.getYtelseAnvist() != null) {
            ytelse.getYtelseAnvist().forEach(ytelseAnvistDto -> builder.leggTilYtelseAnvist(mapYtelseAnvist(ytelseAnvistDto)));
        }

        if (ytelse.getYtelseGrunnlag() != null) {
            builder.medYtelseGrunnlag(mapYtelseGrunnlag(ytelse.getYtelseGrunnlag()));
        }
        builder.medBehandlingsTema(TemaUnderkategori.fraKode(ytelse.getTemaUnderkategori().getKode()));
        builder.medKilde(Fagsystem.fraKode(ytelse.getKilde().getKode()));
        builder.medPeriode(mapDatoIntervall(ytelse.getPeriode()));
        builder.medYtelseType(FagsakYtelseType.fraKode(ytelse.getRelatertYtelseType().getKode()));
        return builder;
    }

    private static YtelseGrunnlagDto mapYtelseGrunnlag(no.nav.folketrygdloven.kalkulus.iay.ytelse.v1.YtelseGrunnlagDto ytelseGrunnlag) {
        YtelseGrunnlagDtoBuilder builder = YtelseGrunnlagDtoBuilder.ny();
        List<no.nav.folketrygdloven.kalkulus.iay.ytelse.v1.YtelseStørrelseDto> ytelser = ytelseGrunnlag.getYtelseStørrelse();
        if (ytelser != null) {
            ytelser.forEach(ytelseStørrelse -> builder.leggTilYtelseStørrelse(mapYtelseStørrelse(ytelseStørrelse)));
        }
        if (ytelseGrunnlag.getArbeidskategori() != null) {
            builder.medArbeidskategori(Arbeidskategori.fraKode(ytelseGrunnlag.getArbeidskategori().getKode()));
        }
        if (ytelseGrunnlag.getDekningsgradProsent() != null) {
            builder.medDekningsgradProsent(ytelseGrunnlag.getDekningsgradProsent());
        }

        if (ytelseGrunnlag.getGraderingProsent() != null) {
            builder.medGraderingProsent(ytelseGrunnlag.getGraderingProsent());
        }

        if (ytelseGrunnlag.getOpprinneligIdentdato() != null) {
            builder.medOpprinneligIdentdato(ytelseGrunnlag.getOpprinneligIdentdato());
        }

        if (ytelseGrunnlag.getInntektProsent() != null) {
            builder.medInntektsgrunnlagProsent(ytelseGrunnlag.getInntektProsent());
        }

        if (ytelseGrunnlag.getVedtaksDagsats() != null) {
            builder.medVedtaksDagsats(ytelseGrunnlag.getVedtaksDagsats().getVerdi());
        }

        return builder.build();
    }

    private static YtelseStørrelseDto mapYtelseStørrelse(no.nav.folketrygdloven.kalkulus.iay.ytelse.v1.YtelseStørrelseDto ytelseStørrelse) {
        YtelseStørrelseDtoBuilder builder = YtelseStørrelseDtoBuilder.ny();
        builder.medBeløp(ytelseStørrelse.getBeløp().getVerdi());
        builder.medHyppighet(InntektPeriodeType.fraKode(ytelseStørrelse.getHyppighet().getKode()));
        if (ytelseStørrelse.getAktør().getErOrganisasjon()) {
            builder.medVirksomhet(ytelseStørrelse.getAktør().getIdent());
        }
        return builder.build();
    }

    private static YtelseAnvistDto mapYtelseAnvist(no.nav.folketrygdloven.kalkulus.iay.ytelse.v1.YtelseAnvistDto ytelseAnvist) {
        YtelseAnvistDtoBuilder builder = YtelseAnvistDtoBuilder.ny();
        builder.medAnvistPeriode(Intervall.fraOgMedTilOgMed(ytelseAnvist.getAnvistPeriode().getFom(), ytelseAnvist.getAnvistPeriode().getTom()));
        if (ytelseAnvist.getBeløp() != null) {
            builder.medBeløp(ytelseAnvist.getBeløp().getVerdi());
        }
        if (ytelseAnvist.getDagsats() != null) {
            builder.medDagsats(ytelseAnvist.getDagsats().getVerdi());
        }
        if (ytelseAnvist.getUtbetalingsgradProsent() != null) {
            builder.medUtbetalingsgradProsent(ytelseAnvist.getUtbetalingsgradProsent());
        }
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
