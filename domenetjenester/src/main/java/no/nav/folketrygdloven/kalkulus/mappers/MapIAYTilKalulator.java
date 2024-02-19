package no.nav.folketrygdloven.kalkulus.mappers;


import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.ArbeidsforholdInformasjonDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.ArbeidsforholdOverstyringDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.ArbeidsforholdReferanseDto;
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
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittFrilansInntektDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittOpptjeningDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.RefusjonDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.VersjonTypeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseAnvistDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseAnvistDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.permisjon.PermisjonDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.typer.EksternArbeidsforholdRef;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.felles.v1.BeløpDto;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.iay.arbeid.v1.AktivitetsAvtaleDto;
import no.nav.folketrygdloven.kalkulus.iay.arbeid.v1.ArbeidDto;
import no.nav.folketrygdloven.kalkulus.iay.arbeid.v1.ArbeidsforholdInformasjonDto;
import no.nav.folketrygdloven.kalkulus.iay.arbeid.v1.PermisjonDto;
import no.nav.folketrygdloven.kalkulus.iay.inntekt.v1.InntekterDto;
import no.nav.folketrygdloven.kalkulus.iay.inntekt.v1.InntektsmeldingerDto;
import no.nav.folketrygdloven.kalkulus.iay.inntekt.v1.UtbetalingDto;
import no.nav.folketrygdloven.kalkulus.iay.inntekt.v1.UtbetalingsPostDto;
import no.nav.folketrygdloven.kalkulus.iay.ytelse.v1.YtelseDto;
import no.nav.folketrygdloven.kalkulus.iay.ytelse.v1.YtelseFordelingDto;
import no.nav.folketrygdloven.kalkulus.iay.ytelse.v1.YtelserDto;
import no.nav.folketrygdloven.kalkulus.opptjening.v1.OppgittArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulus.opptjening.v1.OppgittEgenNæringDto;
import no.nav.folketrygdloven.kalkulus.opptjening.v1.OppgittFrilansInntekt;
import no.nav.folketrygdloven.kalkulus.opptjening.v1.OppgittOpptjeningDto;

public class MapIAYTilKalulator {

    private MapIAYTilKalulator() {
    }

    public static InternArbeidsforholdRefDto mapArbeidsforholdRef(no.nav.folketrygdloven.kalkulus.felles.v1.InternArbeidsforholdRefDto arbeidsforholdRef) {
        if (arbeidsforholdRef == null) {
            return InternArbeidsforholdRefDto.nullRef();
        }
        return InternArbeidsforholdRefDto.ref(arbeidsforholdRef.getAbakusReferanse());
    }

    public static Set<ArbeidsforholdReferanseDto> mapArbeidsgiverReferanser(Set<no.nav.folketrygdloven.kalkulus.iay.arbeid.v1.ArbeidsforholdReferanseDto> referanser) {

        return referanser.stream().map(ref -> new ArbeidsforholdReferanseDto(MapFraKalkulator.mapArbeidsgiver(ref.getArbeidsgiver()),
                        mapArbeidsforholdRef(ref.getInternReferanse()),
                        mapEksternReferanse(ref.getEksternReferanse())))
                .collect(Collectors.toSet());
    }

    private static EksternArbeidsforholdRef mapEksternReferanse(no.nav.folketrygdloven.kalkulus.felles.v1.EksternArbeidsforholdRef eksternReferanse) {
        return eksternReferanse == null || eksternReferanse.getReferanse() == null ?
                EksternArbeidsforholdRef.nullRef() : EksternArbeidsforholdRef.ref(eksternReferanse.getReferanse());
    }

    public static InntektArbeidYtelseGrunnlagDto mapGrunnlag(no.nav.folketrygdloven.kalkulus.iay.v1.InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        InntektArbeidYtelseGrunnlagDtoBuilder builder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        InntektArbeidYtelseAggregatBuilder data = mapAggregat(iayGrunnlag);
        builder.medData(data);

        if (iayGrunnlag.getInntektsmeldingDto() != null) {
            InntektsmeldingAggregatDto inntektsmeldingAggregatDto = mapInntektsmelding(iayGrunnlag.getInntektsmeldingDto());
            builder.setInntektsmeldinger(inntektsmeldingAggregatDto);
        }

        if (iayGrunnlag.getArbeidsforholdInformasjon() != null && iayGrunnlag.getArbeidsforholdInformasjon().getOverstyringer() != null) {
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
            ny.medHandling(arbeidsforholdOverstyringDto.getHandling());
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
            if (oppgittFrilans.getOppgittFrilansInntekt() != null) {
                List<OppgittFrilansInntektDto> oppgittInntekt = oppgittFrilans.getOppgittFrilansInntekt().stream().map(MapIAYTilKalulator::mapFrilansInntekt).collect(Collectors.toList());
                builder.leggTilFrilansOpplysninger(new OppgittFrilansDto(oppgittFrilans.getErNyoppstartet(), oppgittInntekt));
            } else {
                builder.leggTilFrilansOpplysninger(new OppgittFrilansDto(oppgittFrilans.getErNyoppstartet()));
            }
        }
        if (oppgittOpptjening.getEgenNæring() != null) {
            oppgittOpptjening.getEgenNæring().forEach(egen -> builder.leggTilEgneNæring(mapEgenNæring(egen)));
        }
        if (oppgittOpptjening.getOppgittArbeidsforhold() != null) {
            oppgittOpptjening.getOppgittArbeidsforhold().forEach(arb -> builder.leggTilOppgittArbeidsforhold(mapArbeidsforhold(arb)));
        }
        return builder;
    }

    private static OppgittOpptjeningDtoBuilder.OppgittArbeidsforholdBuilder mapArbeidsforhold(OppgittArbeidsforholdDto arb) {
        return OppgittOpptjeningDtoBuilder.OppgittArbeidsforholdBuilder.ny()
                .medPeriode(Intervall.fraOgMedTilOgMed(arb.getPeriode().getFom(), arb.getPeriode().getTom()))
                .medInntekt(arb.getInntekt());
    }

    private static OppgittFrilansInntektDto mapFrilansInntekt(OppgittFrilansInntekt oppgittFrilansInntekt) {
        Periode periode = oppgittFrilansInntekt.getPeriode();
        return new OppgittFrilansInntektDto(Intervall.fraOgMedTilOgMed(periode.getFom(), periode.getTom()), oppgittFrilansInntekt.getInntekt());
    }

    private static Intervall mapDatoIntervall(Periode periode) {
        return Intervall.fraOgMedTilOgMed(periode.getFom(), periode.getTom());
    }

    private static OppgittOpptjeningDtoBuilder.EgenNæringBuilder mapEgenNæring(OppgittEgenNæringDto oppgittEgenNæring) {
        OppgittOpptjeningDtoBuilder.EgenNæringBuilder egenNæringBuilder = OppgittOpptjeningDtoBuilder.EgenNæringBuilder.ny()
                .medPeriode(mapDatoIntervall(oppgittEgenNæring.getPeriode()))
                .medBruttoInntekt(oppgittEgenNæring.getBruttoInntekt())
                .medNyIArbeidslivet(oppgittEgenNæring.getNyIArbeidslivet())
                .medEndringDato(oppgittEgenNæring.getEndringDato())
                .medVirksomhetType(oppgittEgenNæring.getVirksomhetType())
                .medVarigEndring(oppgittEgenNæring.getVarigEndring())
                .medBegrunnelse(oppgittEgenNæring.getBegrunnelse())
                .medNyoppstartet(oppgittEgenNæring.getNyoppstartet());
        if (oppgittEgenNæring.getAktør() != null && oppgittEgenNæring.getAktør().getErOrganisasjon()) {
            egenNæringBuilder.medVirksomhet(oppgittEgenNæring.getAktør().getIdent());
        }
        return egenNæringBuilder;
    }

    public static InntektsmeldingAggregatDto mapInntektsmelding(InntektsmeldingerDto inntektsmeldingDto) {
        InntektsmeldingAggregatDto.InntektsmeldingAggregatDtoBuilder builder = InntektsmeldingAggregatDto.InntektsmeldingAggregatDtoBuilder.ny();
        if (inntektsmeldingDto.getInntektsmeldinger() != null) {
            inntektsmeldingDto.getInntektsmeldinger().forEach(inntektsmelding -> builder.leggTil(mapInntektsmeldingDto(inntektsmelding)));
        }
        return builder.build();
    }

    private static InntektsmeldingDto mapInntektsmeldingDto(no.nav.folketrygdloven.kalkulus.iay.inntekt.v1.InntektsmeldingDto inntektsmelding) {
        InntektsmeldingDtoBuilder builder = InntektsmeldingDtoBuilder.builder();
        builder.medArbeidsgiver(MapFraKalkulator.mapArbeidsgiver(inntektsmelding.getArbeidsgiver()));
        builder.medArbeidsforholdId(mapArbeidsforholdRef(inntektsmelding.getArbeidsforholdRef()));
        builder.medJournalpostId(inntektsmelding.getJournalpostId());
        builder.medKanalreferanse(inntektsmelding.getKanalreferanse());
        builder.medRefusjon(Optional.ofNullable(inntektsmelding.getRefusjonBeløpPerMnd()).map(BeløpDto::getBeløp).orElse(null), inntektsmelding.getRefusjonOpphører());
        builder.medBeløp(inntektsmelding.getInntektBeløp().getBeløp());
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
                naturalYtelse.getBeløp().getBeløp(),
                naturalYtelse.getType());
    }

    private static RefusjonDto mapRefusjon(no.nav.folketrygdloven.kalkulus.iay.inntekt.v1.RefusjonDto refusjon) {
        return new RefusjonDto(refusjon.getRefusjonsbeløpMnd().getBeløp(), refusjon.getFom());
    }

    private static AktivitetsAvtaleDtoBuilder mapAktivitetsAvtale(AktivitetsAvtaleDto aktivitetsAvtale) {
        return AktivitetsAvtaleDtoBuilder.ny()
                .medPeriode(Intervall.fraOgMedTilOgMed(aktivitetsAvtale.getPeriode().getFom(), aktivitetsAvtale.getPeriode().getTom()))
                .medSisteLønnsendringsdato(aktivitetsAvtale.getSisteLønnsendringsdato())
                .medErAnsettelsesPeriode(aktivitetsAvtale.getStillingsprosent() == null);
    }

    private static PermisjonDtoBuilder mapPermisjon(PermisjonDto permisjon) {
        return PermisjonDtoBuilder.ny()
                .medPeriode(Intervall.fraOgMedTilOgMed(permisjon.getPeriode().getFom(), permisjon.getPeriode().getTom()))
                .medProsentsats(permisjon.getProsentsats())
                .medPermisjonsbeskrivelseType(permisjon.getPermisjonsbeskrivelseType());
    }


    private static YrkesaktivitetDto mapYrkesaktivitet(no.nav.folketrygdloven.kalkulus.iay.arbeid.v1.YrkesaktivitetDto yrkesaktivitet) {
        YrkesaktivitetDtoBuilder dtoBuilder = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty());
        yrkesaktivitet.getAktivitetsAvtaler().forEach(aktivitetsAvtale -> dtoBuilder.leggTilAktivitetsAvtale(mapAktivitetsAvtale(aktivitetsAvtale)));
        if (yrkesaktivitet.getPermisjoner() != null) {
            yrkesaktivitet.getPermisjoner().forEach(permisjon -> dtoBuilder.leggTilPermisjon(mapPermisjon(permisjon)));
        }
        dtoBuilder.medArbeidsforholdId(mapArbeidsforholdRef(yrkesaktivitet.getAbakusReferanse()));
        dtoBuilder.medArbeidsgiver(MapFraKalkulator.mapArbeidsgiver(yrkesaktivitet.getArbeidsgiver()));
        dtoBuilder.medArbeidType(yrkesaktivitet.getArbeidType());
        return dtoBuilder.build();
    }

    private static InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder mapInntekt(InntekterDto inntekterDto) {
        InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder builder = InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder.oppdatere(Optional.empty());
        inntekterDto.getUtbetalinger().forEach(inntekt -> builder.leggTilInntekt(mapInntekt(inntekt)));
        return builder;
    }

    private static InntektDtoBuilder mapInntekt(UtbetalingDto inntekt) {
        InntektDtoBuilder builder = InntektDtoBuilder.oppdatere(Optional.empty());
        inntekt.getPoster().forEach(inntektspost -> builder.leggTilInntektspost(mapInntektspost(inntektspost)));
        builder.medArbeidsgiver(MapFraKalkulator.mapArbeidsgiver(inntekt.getUtbetaler()));
        builder.medInntektsKilde(inntekt.getKilde());
        return builder;
    }

    private static InntektspostDtoBuilder mapInntektspost(UtbetalingsPostDto inntektspost) {
        InntektspostDtoBuilder builder = InntektspostDtoBuilder.ny();
        builder.medBeløp(inntektspost.getBeløp());
        builder.medInntektspostType(inntektspost.getInntektspostType());
        builder.medPeriode(inntektspost.getPeriode().getFom(), inntektspost.getPeriode().getTom());
        if (inntektspost.getSkattAvgiftType() != null) {
            builder.medSkatteOgAvgiftsregelType(inntektspost.getSkattAvgiftType());
        }
        if (inntektspost.getLønnsinntektBeskrivelse() != null) {
            builder.medLønnsinntektBeskrivelse(inntektspost.getLønnsinntektBeskrivelse());
        }
        builder.medInntektYtelse(inntektspost.getInntektYtelseType());
        return builder;
    }

    private static InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder mapArbeid(ArbeidDto arbeid) {
        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder builder = InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder.oppdatere(Optional.empty());
        arbeid.getYrkesaktiviteter().forEach(yrkesaktivitet -> builder.leggTilYrkesaktivitet(mapYrkesaktivitet(yrkesaktivitet)));
        return builder;
    }

    private static InntektArbeidYtelseAggregatBuilder mapAggregat(no.nav.folketrygdloven.kalkulus.iay.v1.InntektArbeidYtelseGrunnlagDto grunnlagDto) {
        InntektArbeidYtelseAggregatBuilder builder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        if (grunnlagDto.getArbeidDto() != null) {
            builder.leggTilAktørArbeid(mapArbeid(grunnlagDto.getArbeidDto()));
        }
        if (grunnlagDto.getInntekterDto() != null) {
            builder.leggTilAktørInntekt(mapInntekt(grunnlagDto.getInntekterDto()));
        }
        if (grunnlagDto.getYtelserDto() != null && grunnlagDto.getYtelserDto().getYtelser() != null) {
            builder.leggTilAktørYtelse(mapAktørYtelse(grunnlagDto.getYtelserDto()));
        }
        return builder;
    }

    private static InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder mapAktørYtelse(YtelserDto ytelser) {
        InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder builder = InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder.oppdatere(Optional.empty());
        ytelser.getYtelser().forEach(ytelse -> builder.leggTilYtelse(mapYtelse(ytelse)));
        return builder;

    }

    private static YtelseDtoBuilder mapYtelse(YtelseDto ytelse) {
        YtelseDtoBuilder builder = YtelseDtoBuilder.ny();
        if (ytelse.getYtelseAnvist() != null) {
            ytelse.getYtelseAnvist().forEach(ytelseAnvistDto -> builder.leggTilYtelseAnvist(mapYtelseAnvist(ytelseAnvistDto)));
        }
        if (ytelse.getVedtaksDagsats() != null) {
            builder.medVedtaksDagsats(ytelse.getVedtaksDagsats().getBeløp());
        }
        builder.medPeriode(mapDatoIntervall(ytelse.getPeriode()));
        builder.medYtelseType(ytelse.getRelatertYtelseType());
        builder.medYtelseGrunnlag(mapYtelseGrunnlag(ytelse.getYtelseGrunnlag()));
        return builder;
    }

    private static YtelseGrunnlagDto mapYtelseGrunnlag(no.nav.folketrygdloven.kalkulus.iay.ytelse.v1.YtelseGrunnlagDto ytelseGrunnlag) {
        if (ytelseGrunnlag == null) {
            return null;
        }
        return new YtelseGrunnlagDto(ytelseGrunnlag.getArbeidskategori(), mapFordeling(ytelseGrunnlag.getFordeling()));
    }

    private static List<no.nav.folketrygdloven.kalkulator.modell.iay.YtelseFordelingDto> mapFordeling(List<YtelseFordelingDto> fordeling) {
        if (fordeling == null) {
            return Collections.emptyList();
        }
        return fordeling.stream().map(MapIAYTilKalulator::mapUtbetaling).collect(Collectors.toList());
    }

    private static no.nav.folketrygdloven.kalkulator.modell.iay.YtelseFordelingDto mapUtbetaling(YtelseFordelingDto f) {
        return new no.nav.folketrygdloven.kalkulator.modell.iay.YtelseFordelingDto(
                MapFraKalkulator.mapArbeidsgiver(f.getArbeidsgiver()),
                f.getHyppighet(),
                f.getBeløp(),
                f.getErRefusjon());
    }

    private static YtelseAnvistDto mapYtelseAnvist(no.nav.folketrygdloven.kalkulus.iay.ytelse.v1.YtelseAnvistDto ytelseAnvist) {
        YtelseAnvistDtoBuilder builder = YtelseAnvistDtoBuilder.ny();
        builder.medAnvistPeriode(Intervall.fraOgMedTilOgMed(ytelseAnvist.getAnvistPeriode().getFom(), ytelseAnvist.getAnvistPeriode().getTom()));
        if (ytelseAnvist.getBeløp() != null) {
            builder.medBeløp(ytelseAnvist.getBeløp().getBeløp());
        }
        if (ytelseAnvist.getDagsats() != null) {
            builder.medDagsats(ytelseAnvist.getDagsats().getBeløp());
        }
        if (ytelseAnvist.getUtbetalingsgradProsent() != null) {
            builder.medUtbetalingsgradProsent(ytelseAnvist.getUtbetalingsgradProsent());
        }
        builder.medAnvisteAndeler(AnvistAndelMapper.mapAnvisteAndeler(ytelseAnvist));
        return builder.build();
    }

}
