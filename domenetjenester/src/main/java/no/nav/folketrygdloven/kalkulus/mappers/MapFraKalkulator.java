package no.nav.folketrygdloven.kalkulus.mappers;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectReader;

import no.nav.folketrygdloven.beregningsgrunnlag.Grunnbeløp;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.kalkulator.BeregningsperiodeTjeneste;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.ForeldrepengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.FrisinnGrunnlag;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.FrisinnPeriode;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.PleiepengerSyktBarnGrunnlag;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.StandardGrunnlag;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.SvangerskapspengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.gradering.AktivitetGradering;
import no.nav.folketrygdloven.kalkulator.gradering.AndelGradering;
import no.nav.folketrygdloven.kalkulator.gradering.AndelGradering.Builder;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.konfig.KonfigTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.behandling.BehandlingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.behandling.Skjæringstidspunkt;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittOpptjeningDto;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulator.modell.typer.AktørId;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.beregning.v1.AktivitetGraderingDto;
import no.nav.folketrygdloven.kalkulus.beregning.v1.GrunnbeløpDto;
import no.nav.folketrygdloven.kalkulus.beregning.v1.OmsorgspengerGrunnlag;
import no.nav.folketrygdloven.kalkulus.beregning.v1.RefusjonskravDatoDto;
import no.nav.folketrygdloven.kalkulus.beregning.v1.YtelsespesifiktGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.KalkulatorInputEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.YtelseTyperKalkulusStøtter;
import no.nav.folketrygdloven.kalkulus.felles.v1.Aktør;
import no.nav.folketrygdloven.kalkulus.felles.v1.AktørIdPersonident;
import no.nav.folketrygdloven.kalkulus.felles.v1.KalkulatorInputDto;
import no.nav.folketrygdloven.kalkulus.iay.v1.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.opptjening.v1.OpptjeningAktiviteterDto;

public class MapFraKalkulator {

    private static final ObjectReader READER = JsonMapper.getMapper().reader();
    private static final String TOGGLE_SPLITTE_SAMMENLIGNINGSGRUNNLAG = "fpsak.splitteSammenligningATFL";

    public static BeregningsgrunnlagInput mapFraKalkulatorInputEntitetTilBeregningsgrunnlagInput(KoblingEntitet kobling,
                                                                                                 KalkulatorInputEntitet kalkulatorInputEntitet,
                                                                                                 Optional<BeregningsgrunnlagGrunnlagEntitet> beregningsgrunnlagGrunnlagEntitet) {
        String json = kalkulatorInputEntitet.getInput();
        KalkulatorInputDto input = null;

        try {
            input = READER.forType(KalkulatorInputDto.class).readValue(json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        if (input != null) {
            return mapFraKalkulatorInputTilBeregningsgrunnlagInput(kobling, input, beregningsgrunnlagGrunnlagEntitet);
        }

        throw new IllegalStateException("Klarte ikke lage input for kobling med id:" + kalkulatorInputEntitet.getKoblingId());
    }


    public static BeregningsgrunnlagInput mapFraKalkulatorInputTilBeregningsgrunnlagInput(KoblingEntitet kobling,
                                                                                          KalkulatorInputDto input,
                                                                                          Optional<BeregningsgrunnlagGrunnlagEntitet> beregningsgrunnlagGrunnlagEntitet) {
        var koblingId = kobling.getId();
        var skjæringstidspunkt = input.getSkjæringstidspunkt();

        FagsakYtelseType ytelseType = FagsakYtelseType.fraKode(kobling.getYtelseTyperKalkulusStøtter().getKode());
        AktørId aktørId = new AktørId(kobling.getAktørId().getId());
        Skjæringstidspunkt build = Skjæringstidspunkt.builder()
                .medFørsteUttaksdato(skjæringstidspunkt)
                .medSkjæringstidspunktOpptjening(skjæringstidspunkt).build();

        var ref = BehandlingReferanse.fra(ytelseType, aktørId, koblingId, kobling.getKoblingReferanse().getReferanse(), Optional.empty(), build);

        AktivitetGraderingDto aktivitetGradering = input.getAktivitetGradering();
        var iayGrunnlag = input.getIayGrunnlag();
        OpptjeningAktiviteterDto opptjeningAktiviteter = input.getOpptjeningAktiviteter();
        List<RefusjonskravDatoDto> refusjonskravDatoer = input.getRefusjonskravDatoer();

        no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto iayGrunnlagMappet = mapFraDto(iayGrunnlag, new AktørIdPersonident(aktørId.getId()));
        BeregningsgrunnlagInput utenGrunnbeløp = new BeregningsgrunnlagInput(ref,
                iayGrunnlagMappet,
                mapFraDto(opptjeningAktiviteter),
                aktivitetGradering != null ? mapFraDto(aktivitetGradering) : null,
                mapFraDto(refusjonskravDatoer),
                mapFraDto(kobling.getYtelseTyperKalkulusStøtter(), input, iayGrunnlagMappet, beregningsgrunnlagGrunnlagEntitet));

        utenGrunnbeløp.leggTilKonfigverdi(BeregningsperiodeTjeneste.INNTEKT_RAPPORTERING_FRIST_DATO, 5);
        utenGrunnbeløp.leggTilToggle(TOGGLE_SPLITTE_SAMMENLIGNINGSGRUNNLAG, false);

        return utenGrunnbeløp.medGrunnbeløpsatser(mapFraDto(input.getGrunnbeløpsatser()));
    }

    private static YtelsespesifiktGrunnlag mapFraDto(YtelseTyperKalkulusStøtter ytelseType,
                                                     KalkulatorInputDto input,
                                                     no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                                     Optional<BeregningsgrunnlagGrunnlagEntitet> beregningsgrunnlagGrunnlagEntitet) {
        YtelseTyperKalkulusStøtter yt = YtelseTyperKalkulusStøtter.fraKode(ytelseType.getKode());
        YtelsespesifiktGrunnlagDto ytelsespesifiktGrunnlag = input.getYtelsespesifiktGrunnlag();

        switch (yt) {
            case FORELDREPENGER:
                ForeldrepengerGrunnlag foreldrepengerGrunnlag = new ForeldrepengerGrunnlag(ytelsespesifiktGrunnlag.getDekningsgrad().intValue(), ytelsespesifiktGrunnlag.getKvalifisererTilBesteberegning());
                //TODO(OJR) lag builder?
                foreldrepengerGrunnlag.setGrunnbeløpMilitærHarKravPå(KonfigTjeneste.forYtelse(FagsakYtelseType.FORELDREPENGER).getAntallGMilitærHarKravPå().intValue());
                return foreldrepengerGrunnlag;
            case SVANGERSKAPSPENGER:
                no.nav.folketrygdloven.kalkulus.beregning.v1.SvangerskapspengerGrunnlag svangerskapspengerGrunnlag = (no.nav.folketrygdloven.kalkulus.beregning.v1.SvangerskapspengerGrunnlag) ytelsespesifiktGrunnlag;
                SvangerskapspengerGrunnlag svpGrunnlag = new SvangerskapspengerGrunnlag(UtbetalingsgradMapper.mapUtbetalingsgrad(svangerskapspengerGrunnlag.getUtbetalingsgradPrAktivitet()));
                svpGrunnlag.setGrunnbeløpMilitærHarKravPå(KonfigTjeneste.forYtelse(FagsakYtelseType.SVANGERSKAPSPENGER).getAntallGMilitærHarKravPå().intValue());
                return svpGrunnlag;
            case PLEIEPENGER_SYKT_BARN:
                no.nav.folketrygdloven.kalkulus.beregning.v1.PleiepengerSyktBarnGrunnlag pleiepengerYtelsesGrunnlag = (no.nav.folketrygdloven.kalkulus.beregning.v1.PleiepengerSyktBarnGrunnlag) ytelsespesifiktGrunnlag;
                PleiepengerSyktBarnGrunnlag pleiepengerSyktBarnGrunnlag = new PleiepengerSyktBarnGrunnlag(UtbetalingsgradMapper.mapUtbetalingsgrad(pleiepengerYtelsesGrunnlag.getUtbetalingsgradPrAktivitet()));
                pleiepengerSyktBarnGrunnlag.setGrunnbeløpMilitærHarKravPå(KonfigTjeneste.forYtelse(FagsakYtelseType.PLEIEPENGER_SYKT_BARN).getAntallGMilitærHarKravPå().intValue());
                return pleiepengerSyktBarnGrunnlag;
            case FRISINN:
                no.nav.folketrygdloven.kalkulus.beregning.v1.FrisinnGrunnlag frisinnGrunnlag = (no.nav.folketrygdloven.kalkulus.beregning.v1.FrisinnGrunnlag) ytelsespesifiktGrunnlag;
                List<FrisinnPeriode> frisinnPerioder = mapFraKontrakt(frisinnGrunnlag, iayGrunnlag.getOppgittOpptjening());
                return new FrisinnGrunnlag(UtbetalingsgradMapperFRISINN.map(iayGrunnlag, beregningsgrunnlagGrunnlagEntitet, frisinnPerioder),
                        frisinnPerioder);
            case OMSORGSPENGER:
                OmsorgspengerGrunnlag omsorgspengerGrunnlag = (OmsorgspengerGrunnlag) ytelsespesifiktGrunnlag;
                no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.OmsorgspengerGrunnlag kalkulatorGrunnlag = new no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.OmsorgspengerGrunnlag(UtbetalingsgradMapper.mapUtbetalingsgrad(omsorgspengerGrunnlag.getUtbetalingsgradPrAktivitet()));
                kalkulatorGrunnlag.setGrunnbeløpMilitærHarKravPå(KonfigTjeneste.forYtelse(FagsakYtelseType.OMSORGSPENGER).getAntallGMilitærHarKravPå().intValue());
                return kalkulatorGrunnlag;
            default:
                return new StandardGrunnlag();
        }
    }

    private static List<FrisinnPeriode> mapFraKontrakt(no.nav.folketrygdloven.kalkulus.beregning.v1.FrisinnGrunnlag frisinnGrunnlag, Optional<OppgittOpptjeningDto> oppgittOpptjening) {
        //FIXME (Er tilfellet ved gamle saker på gamel kontrakt)
        if (frisinnGrunnlag.getPerioderMedSøkerInfo() == null) {
            return MapGammeltFrisinngrunnlag.map(frisinnGrunnlag, oppgittOpptjening);
        }

        return frisinnGrunnlag.getPerioderMedSøkerInfo().stream()
                .map(p -> new FrisinnPeriode(Intervall.fraOgMedTilOgMed(p.getPeriode().getFom(), p.getPeriode().getTom()), p.getSøkerFrilansIPeriode(), p.getSøkerNæringIPeriode()))
                .collect(Collectors.toList());
    }

    private static List<no.nav.folketrygdloven.kalkulator.modell.iay.RefusjonskravDatoDto> mapFraDto(Collection<RefusjonskravDatoDto> refusjonskravDatoer) {
        if (refusjonskravDatoer == null) {
            return Collections.emptyList();
        }
        return refusjonskravDatoer.stream().map(ref ->
                new no.nav.folketrygdloven.kalkulator.modell.iay.RefusjonskravDatoDto(
                        mapArbeidsgiver(ref.getArbeidsgiver()),
                        ref.getFørsteDagMedRefusjonskrav(),
                        ref.getFørsteInnsendingAvRefusjonskrav(),
                        ref.harRefusjonFraStart()))
                .collect(Collectors.toList());
    }

    private static AktivitetGradering mapFraDto(AktivitetGraderingDto aktivitetGradering) {
        List<AndelGradering> res = new ArrayList<>();
        aktivitetGradering.getAndelGraderingDto().forEach(andel -> {
            Builder builder = AndelGradering.builder();
            andel.getGraderinger().forEach(grad -> builder.medGradering(grad.getPeriode().getFom(), grad.getPeriode().getTom(), grad.getArbeidstidProsent().intValue()));
            builder.medStatus(AktivitetStatus.fraKode(andel.getAktivitetStatus().getKode()));
            builder.medArbeidsgiver(mapArbeidsgiver(andel.getArbeidsgiver()));
            no.nav.folketrygdloven.kalkulus.felles.v1.InternArbeidsforholdRefDto arbeidsforholdRef = andel.getArbeidsforholdRef();
            if (arbeidsforholdRef != null) {
                builder.medArbeidsforholdRef(InternArbeidsforholdRefDto.ref(arbeidsforholdRef.getAbakusReferanse()));
            }
            res.add(builder.build());
        });

        return new AktivitetGradering(res);
    }

    public static Arbeidsgiver mapArbeidsgiver(Aktør arbeidsgiver) {
        if (arbeidsgiver == null) {
            return null;
        }
        return arbeidsgiver.getErOrganisasjon() ? Arbeidsgiver.virksomhet(arbeidsgiver.getIdent()) : Arbeidsgiver.person(new AktørId(arbeidsgiver.getIdent()));

    }

    private static no.nav.folketrygdloven.kalkulator.opptjening.OpptjeningAktiviteterDto mapFraDto(OpptjeningAktiviteterDto opptjeningAktiviteter) {
        return new no.nav.folketrygdloven.kalkulator.opptjening.OpptjeningAktiviteterDto(
                opptjeningAktiviteter.getPerioder().stream().map(opptjeningPeriodeDto -> no.nav.folketrygdloven.kalkulator.opptjening.OpptjeningAktiviteterDto.nyPeriode(
                        OpptjeningAktivitetType.fraKode(opptjeningPeriodeDto.getOpptjeningAktivitetType().getKode()),
                        new Periode(opptjeningPeriodeDto.getPeriode().getFom(), opptjeningPeriodeDto.getPeriode().getTom()),
                        opptjeningPeriodeDto.getArbeidsgiver() != null && opptjeningPeriodeDto.getArbeidsgiver().getErOrganisasjon() ? opptjeningPeriodeDto.getArbeidsgiver().getIdent() : null,
                        opptjeningPeriodeDto.getArbeidsgiver() != null && opptjeningPeriodeDto.getArbeidsgiver().getErPerson() ? opptjeningPeriodeDto.getArbeidsgiver().getIdent() : null,
                        opptjeningPeriodeDto.getAbakusReferanse() != null ? InternArbeidsforholdRefDto.ref(opptjeningPeriodeDto.getAbakusReferanse().getAbakusReferanse()) : null
                )).collect(Collectors.toList()));
    }

    private static no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto mapFraDto(InntektArbeidYtelseGrunnlagDto iayGrunnlag, AktørIdPersonident aktørId) {
        return MapIAYTilKalulator.mapGrunnlag(iayGrunnlag, aktørId);
    }

    private static List<Grunnbeløp> mapFraDto(List<GrunnbeløpDto> grunnbeløpsatser) {

        List<Grunnbeløp> collect = grunnbeløpsatser.stream().map(grunnbeløpDto ->
                new Grunnbeløp(
                        grunnbeløpDto.getPeriode().getFom(),
                        grunnbeløpDto.getPeriode().getTom(),
                        grunnbeløpDto.getgVerdi().longValue(),
                        grunnbeløpDto.getgSnitt().longValue()))
                .collect(Collectors.toList());

        return collect;
    }
}
