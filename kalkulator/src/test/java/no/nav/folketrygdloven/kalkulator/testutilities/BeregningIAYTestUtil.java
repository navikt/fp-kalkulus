package no.nav.folketrygdloven.kalkulator.testutilities;

import static java.util.Collections.singletonList;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.behandling.BehandlingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.behandling.Fagsystem;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktørYtelseDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.ArbeidsforholdInformasjonDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.ArbeidsforholdOverstyringDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektspostDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittAnnenAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittFrilansDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittOpptjeningDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.OpptjeningsnøkkelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.VersjonTypeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseAnvistDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseStørrelseDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.kodeverk.ArbeidsforholdHandlingType;
import no.nav.folketrygdloven.kalkulator.modell.iay.kodeverk.Arbeidskategori;
import no.nav.folketrygdloven.kalkulator.modell.iay.kodeverk.InntektsKilde;
import no.nav.folketrygdloven.kalkulator.modell.iay.kodeverk.InntektspostType;
import no.nav.folketrygdloven.kalkulator.modell.iay.kodeverk.RelatertYtelseTilstand;
import no.nav.folketrygdloven.kalkulator.modell.iay.kodeverk.VirksomhetType;
import no.nav.folketrygdloven.kalkulator.modell.typer.AktørId;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.ArbeidType;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.ytelse.RelatertYtelseType;
import no.nav.folketrygdloven.kalkulator.regelmodell.Periode;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;


public class BeregningIAYTestUtil {

    public static void byggArbeidForBehandlingMedVirksomhetPåInntekt(BehandlingReferanse behandlingReferanse,
                                                              LocalDate skjæringstidspunktOpptjening,
                                                              LocalDate fraOgMed,
                                                              LocalDate tilOgMed,
                                                              InternArbeidsforholdRefDto arbId,
                                                              Arbeidsgiver arbeidsgiver, BigDecimal inntektPrMnd,
                                                              InntektArbeidYtelseGrunnlagDtoBuilder inntektArbeidYtelseGrunnlagBuilder) {
        byggArbeidForBehandling(behandlingReferanse.getId(), behandlingReferanse.getAktørId(), skjæringstidspunktOpptjening, fraOgMed, tilOgMed, arbId, arbeidsgiver,
            ArbeidType.ORDINÆRT_ARBEIDSFORHOLD,
            singletonList(inntektPrMnd), true, Optional.empty(), inntektArbeidYtelseGrunnlagBuilder);
    }

    /**
     * Lager oppgitt opptjening for Selvstending næringsdrivende 6 måneder før skjæringstidspunkt med endringsdato en måned før
     * skjæringstidspunkt.
     *
     * Setter virksomhetstype til udefinert som mapper til inntektskategori SELVSTENDING_NÆRINGSDRIVENDE.
     *  @param behandlingReferanse aktuell behandling
     * @param skjæringstidspunktOpptjening skjæringstidpunkt for opptjening
     * @param nyIArbeidslivet spesifiserer om bruker er ny i arbeidslivet
     * @param iayGrunnlagBuilder
     */
    public static void lagOppgittOpptjeningForSN(BehandlingReferanse behandlingReferanse, LocalDate skjæringstidspunktOpptjening, boolean nyIArbeidslivet, InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder) {
        iayGrunnlagBuilder.medOppgittOpptjening(lagOppgittOpptjeningForSN(behandlingReferanse, skjæringstidspunktOpptjening, nyIArbeidslivet, VirksomhetType.UDEFINERT));
    }

    /**
     * Lager oppgitt opptjening for Selvstending næringsdrivende 6 måneder før skjæringstidspunkt med endringsdato en måned før
     * skjæringstidspunkt.
     *  @param behandlingReferanse aktuell behandling
     * @param skjæringstidspunktOpptjening skjæringstidpunkt for opptjening
     * @param nyIArbeidslivet spesifiserer om bruker er ny i arbeidslivet
     * @param virksomhetType spesifiserer virksomhetstype for næringsvirksomheten
     * @return
     */
    private static OppgittOpptjeningDtoBuilder lagOppgittOpptjeningForSN(BehandlingReferanse behandlingReferanse, LocalDate skjæringstidspunktOpptjening, boolean nyIArbeidslivet,
                                                                         VirksomhetType virksomhetType) {
        return lagOppgittOpptjeningForSN(behandlingReferanse, skjæringstidspunktOpptjening, nyIArbeidslivet, virksomhetType,
            singletonList(Periode.of(skjæringstidspunktOpptjening.minusMonths(6), skjæringstidspunktOpptjening)));
    }

    /**
     * Lager oppgitt opptjening for Selvstending næringsdrivende 6 måneder før skjæringstidspunkt med endringsdato en måned før
     * skjæringstidspunkt.
     *  @param behandlingReferanse aktuell behandling
     * @param skjæringstidspunktOpptjening skjæringstidpunkt for opptjening
     * @param nyIArbeidslivet spesifiserer om bruker er ny i arbeidslivet
     * @param virksomhetType spesifiserer virksomhetstype for næringsvirksomheten
     * @param perioder spesifiserer perioder
     * @return
     */
    private static OppgittOpptjeningDtoBuilder lagOppgittOpptjeningForSN(BehandlingReferanse behandlingReferanse, LocalDate skjæringstidspunktOpptjening, boolean nyIArbeidslivet, VirksomhetType virksomhetType,
                                                                         Collection<Periode> perioder) {
        OppgittOpptjeningDtoBuilder oppgittOpptjeningBuilder = OppgittOpptjeningDtoBuilder.ny();
        List<OppgittOpptjeningDtoBuilder.EgenNæringBuilder> næringBuilders = new ArrayList<>();
        perioder.stream().forEach(periode -> {
            næringBuilders.add(OppgittOpptjeningDtoBuilder.EgenNæringBuilder.ny()
                .medBruttoInntekt(BigDecimal.valueOf(10000))
                .medNyIArbeidslivet(nyIArbeidslivet)
                .medPeriode(Intervall.fraOgMedTilOgMed(periode.getFom(), periode.getTom()))
                .medVirksomhetType(virksomhetType)
                .medEndringDato(skjæringstidspunktOpptjening.minusMonths(1)));
        });
        oppgittOpptjeningBuilder.leggTilEgneNæringer(næringBuilders);
        return oppgittOpptjeningBuilder;
    }

    /**
     * Lager oppgitt opptjening for annen aktivitet som f.eks militærtjeneste, vartpenger, ventelønn m.m.
     *  @param arbeidType arbeidType for aktivitet
     * @param fom fra dato
     * @param tom til dato
     * @return
     */
    public static OppgittOpptjeningDtoBuilder lagAnnenAktivitetOppgittOpptjening(ArbeidType arbeidType, LocalDate fom, LocalDate tom) {
        OppgittOpptjeningDtoBuilder oppgittOpptjeningBuilder = OppgittOpptjeningDtoBuilder.ny();
        OppgittAnnenAktivitetDto annenAktivitet = new OppgittAnnenAktivitetDto(
            tom == null ? Intervall.fraOgMed(fom) : Intervall.fraOgMedTilOgMed(fom, tom), arbeidType);
        oppgittOpptjeningBuilder.leggTilAnnenAktivitet(annenAktivitet);
        return oppgittOpptjeningBuilder;
    }

    /**
     * Lager oppgitt opptjening for frilans.
     *
     * @param erNyOppstartet spesifiserer om frilans er nyoppstartet
     */
    public static OppgittOpptjeningDtoBuilder leggTilOppgittOpptjeningForFL(boolean erNyOppstartet, LocalDate fom) {
        OppgittOpptjeningDtoBuilder oppgittOpptjeningBuilder = OppgittOpptjeningDtoBuilder.ny();
        OppgittFrilansDto frilans = new OppgittFrilansDto();
        frilans.setErNyoppstartet(erNyOppstartet);
        OppgittAnnenAktivitetDto annenAktivitet = new OppgittAnnenAktivitetDto(Intervall.fraOgMed(fom), ArbeidType.FRILANSER);
        oppgittOpptjeningBuilder.leggTilAnnenAktivitet(annenAktivitet);
        oppgittOpptjeningBuilder.leggTilFrilansOpplysninger(frilans);
        return oppgittOpptjeningBuilder;
    }

    /**
     * Lager oppgitt opptjening for frilans med periode
     *  @param erNyOppstartet spesifiserer om frilans er nyoppstartet
     * @param perioder perioder med aktiv frilans oppgitt i søknaden
     * @param iayGrunnlagBuilder
     */
    public static void leggTilOppgittOpptjeningForFL(boolean erNyOppstartet, Collection<Periode> perioder, InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder) {
        lagFrilans(erNyOppstartet, perioder, iayGrunnlagBuilder);
    }

    /**
     * Legger til oppgitt opptjening for FL og SN
     *
     * Legger til eit frilans arbeidsforhold.
     *
     * Legger til ein næringsvirksomhet.
     *  @param skjæringstidspunktOpptjening skjæringstidspunkt for opptjening
     * @param erNyOppstartet spesifiserer om frilans er nyoppstartet
     * @param nyIArbeidslivet spesifiserer om bruker med selvstendig næring er ny i arbeidslivet
     * @param iayGrunnlagBuilder
     */
    public static void leggTilOppgittOpptjeningForFLOgSN(LocalDate skjæringstidspunktOpptjening, boolean erNyOppstartet,
                                                         boolean nyIArbeidslivet, InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder) {
        OppgittOpptjeningDtoBuilder oppgittOpptjeningBuilder = OppgittOpptjeningDtoBuilder.ny();
        OppgittOpptjeningDtoBuilder.EgenNæringBuilder egenNæringBuilder = OppgittOpptjeningDtoBuilder.EgenNæringBuilder.ny()
            .medBruttoInntekt(BigDecimal.valueOf(10000))
            .medNyIArbeidslivet(nyIArbeidslivet)
            .medPeriode(Intervall.fraOgMedTilOgMed(skjæringstidspunktOpptjening.minusMonths(6), skjæringstidspunktOpptjening))
            .medEndringDato(skjæringstidspunktOpptjening.minusMonths(1));
        OppgittFrilansDto frilans = new OppgittFrilansDto();
        frilans.setErNyoppstartet(erNyOppstartet);
        OppgittAnnenAktivitetDto frilansaktivitet = new OppgittAnnenAktivitetDto(
            Intervall.fraOgMedTilOgMed(skjæringstidspunktOpptjening.minusMonths(6), skjæringstidspunktOpptjening.minusDays(1)),
            ArbeidType.FRILANSER);
        oppgittOpptjeningBuilder.leggTilAnnenAktivitet(frilansaktivitet)
            .leggTilFrilansOpplysninger(frilans).leggTilEgneNæringer(singletonList(egenNæringBuilder));
        iayGrunnlagBuilder.medOppgittOpptjening(oppgittOpptjeningBuilder);
    }

    private static void lagFrilans(boolean erNyOppstartet, Collection<Periode> perioder, InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder) {
        OppgittOpptjeningDtoBuilder oppgittOpptjeningBuilder = OppgittOpptjeningDtoBuilder.ny();
        perioder.forEach(periode -> oppgittOpptjeningBuilder.leggTilAnnenAktivitet(mapFrilansPeriode(periode)));
        OppgittFrilansDto frilans = new OppgittFrilansDto();
        frilans.setErNyoppstartet(erNyOppstartet);
        oppgittOpptjeningBuilder.leggTilFrilansOpplysninger(frilans);
        iayGrunnlagBuilder.medOppgittOpptjening(oppgittOpptjeningBuilder);
    }


    private static OppgittAnnenAktivitetDto mapFrilansPeriode(Periode periode) {
        Intervall datoIntervallEntitet = mapPeriode(periode);
        return new OppgittAnnenAktivitetDto(datoIntervallEntitet, ArbeidType.FRILANSER);
    }

    private static Intervall mapPeriode(Periode periode) {
        LocalDate fom = periode.getFom();
        LocalDate tom = periode.getTom();
        if (tom == null) {
            return Intervall.fraOgMed(fom);
        }
        return Intervall.fraOgMedTilOgMed(fom, tom);
    }

    public static AktørYtelseDto leggTilAktørytelse(InntektArbeidYtelseGrunnlagDtoBuilder inntektArbeidYtelseGrunnlagBuilder,
                                             BehandlingReferanse behandlingReferanse,
                                             LocalDate fom,
                                             LocalDate tom, // NOSONAR - brukes bare til test
                                             RelatertYtelseTilstand relatertYtelseTilstand,
                                             String saksnummer,
                                             RelatertYtelseType ytelseType,
                                             List<YtelseStørrelseDto> ytelseStørrelseList,
                                             Arbeidskategori arbeidskategori,
                                             Periode... meldekortPerioder) {
        InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(inntektArbeidYtelseGrunnlagBuilder.getKladd().getRegisterVersjon(), VersjonTypeDto.REGISTER);
        InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder aktørYtelseBuilder = inntektArbeidYtelseAggregatBuilder
            .getAktørYtelseBuilder(behandlingReferanse.getAktørId());
        YtelseDtoBuilder ytelseBuilder = aktørYtelseBuilder.getYtelselseBuilderForType(Fagsystem.INFOTRYGD, ytelseType);
        ytelseBuilder.medPeriode(Intervall.fraOgMedTilOgMed(fom, tom));
        ytelseBuilder.medStatus(relatertYtelseTilstand);
        YtelseGrunnlagDtoBuilder ytelseGrunnlagBuilder = ytelseBuilder.getGrunnlagBuilder()
            .medArbeidskategori(arbeidskategori);
        ytelseStørrelseList.forEach(ytelseGrunnlagBuilder::leggTilYtelseStørrelse);
        ytelseBuilder.medYtelseGrunnlag(ytelseGrunnlagBuilder.build());
        if (meldekortPerioder != null) {
            Arrays.asList(meldekortPerioder).forEach(meldekortPeriode -> {
                YtelseAnvistDto ytelseAnvist = lagYtelseAnvist(meldekortPeriode, ytelseBuilder);
                ytelseBuilder.leggTilYtelseAnvist(ytelseAnvist);
            });
        }
        aktørYtelseBuilder.leggTilYtelse(ytelseBuilder);
        inntektArbeidYtelseGrunnlagBuilder.medData(inntektArbeidYtelseAggregatBuilder);
        return aktørYtelseBuilder.build();
    }

    private static YtelseAnvistDto lagYtelseAnvist(Periode periode, YtelseDtoBuilder ytelseBuilder) {
        return ytelseBuilder.getAnvistBuilder()
            .medAnvistPeriode(Intervall.fraOgMedTilOgMed(periode.getFom(), periode.getTom()))
            .medUtbetalingsgradProsent(BigDecimal.valueOf(100))
            .medDagsats(BigDecimal.valueOf(1000))
            .medBeløp(BigDecimal.valueOf(10000))
            .build();
    }

    public static void byggArbeidForBehandling(BehandlingReferanse behandlingReferanse,
                                               LocalDate skjæringstidspunktOpptjening,
                                               LocalDate fraOgMed,
                                               LocalDate tilOgMed,
                                               InternArbeidsforholdRefDto arbId,
                                               Arbeidsgiver arbeidsgiver,
                                               InntektArbeidYtelseGrunnlagDtoBuilder inntektArbeidYtelseGrunnlagBuilder) {
        byggArbeidForBehandling(behandlingReferanse, skjæringstidspunktOpptjening, fraOgMed, tilOgMed, arbId, arbeidsgiver, BigDecimal.TEN, inntektArbeidYtelseGrunnlagBuilder);
    }

    public static void byggArbeidForBehandling(BehandlingReferanse behandlingReferanse,
                                        LocalDate skjæringstidspunktOpptjening,
                                        DatoIntervallEntitet arbeidsperiode,
                                        InternArbeidsforholdRefDto arbId,
                                        Arbeidsgiver arbeidsgiver,
                                        InntektArbeidYtelseGrunnlagDtoBuilder inntektArbeidYtelseGrunnlagBuilder) {
        byggArbeidForBehandling(behandlingReferanse, skjæringstidspunktOpptjening, arbeidsperiode.getFomDato(), arbeidsperiode.getTomDato(), arbId, arbeidsgiver,
            BigDecimal.TEN, inntektArbeidYtelseGrunnlagBuilder);
    }

    public static void byggArbeidForBehandling(BehandlingReferanse behandlingReferanse,
                                               LocalDate skjæringstidspunktOpptjening,
                                               LocalDate fraOgMed,
                                               LocalDate tilOgMed,
                                               InternArbeidsforholdRefDto arbId,
                                               Arbeidsgiver arbeidsgiver,
                                               BigDecimal inntektPrMnd,
                                               InntektArbeidYtelseGrunnlagDtoBuilder inntektArbeidYtelseGrunnlagBuilder) {
        byggArbeidForBehandling(behandlingReferanse.getId(), behandlingReferanse.getAktørId(), skjæringstidspunktOpptjening, fraOgMed, tilOgMed, arbId, arbeidsgiver,
            ArbeidType.ORDINÆRT_ARBEIDSFORHOLD,
            singletonList(inntektPrMnd),
            arbeidsgiver != null,
            Optional.empty(),
            inntektArbeidYtelseGrunnlagBuilder);
    }


    public static void byggArbeidForBehandling(BehandlingReferanse behandlingReferanse, // NOSONAR - brukes bare til test
                                               LocalDate skjæringstidspunktOpptjening,
                                               LocalDate fraOgMed,
                                               LocalDate tilOgMed,
                                               InternArbeidsforholdRefDto arbId,
                                               Arbeidsgiver arbeidsgiver,
                                               Optional<LocalDate> lønnsendringsdato,
                                               InntektArbeidYtelseGrunnlagDtoBuilder inntektArbeidYtelseGrunnlagBuilder) {
        byggArbeidForBehandling(behandlingReferanse.getId(), behandlingReferanse.getAktørId(), skjæringstidspunktOpptjening, fraOgMed, tilOgMed, arbId, arbeidsgiver,
            ArbeidType.ORDINÆRT_ARBEIDSFORHOLD,
            singletonList(BigDecimal.TEN), arbeidsgiver != null, lønnsendringsdato, inntektArbeidYtelseGrunnlagBuilder);
    }

    public static void byggArbeidForBehandling(BehandlingReferanse behandlingReferanse, // NOSONAR - brukes bare til test
                                               LocalDate skjæringstidspunktOpptjening,
                                               LocalDate fraOgMed,
                                               LocalDate tilOgMed,
                                               InternArbeidsforholdRefDto arbId,
                                               Arbeidsgiver arbeidsgiver, ArbeidType arbeidType,
                                               List<BigDecimal> inntektPrMnd,
                                               boolean virksomhetPåInntekt,
                                               Optional<LocalDate> lønnsendringsdato,
                                               InntektArbeidYtelseGrunnlagDtoBuilder inntektArbeidYtelseGrunnlagBuilder) {
        byggArbeidForBehandling(behandlingReferanse.getId(), behandlingReferanse.getAktørId(), skjæringstidspunktOpptjening, fraOgMed, tilOgMed, arbId, arbeidsgiver,
            arbeidType, inntektPrMnd, virksomhetPåInntekt, lønnsendringsdato, inntektArbeidYtelseGrunnlagBuilder);
    }

    private static void byggArbeidForBehandling(Long behandlingId,
                                                AktørId aktørId,
                                                LocalDate skjæringstidspunktOpptjening,
                                                LocalDate fraOgMed,
                                                LocalDate tilOgMed,
                                                InternArbeidsforholdRefDto arbId,
                                                Arbeidsgiver arbeidsgiver,
                                                ArbeidType arbeidType,
                                                List<BigDecimal> inntektPrMnd,
                                                boolean virksomhetPåInntekt,
                                                Optional<LocalDate> lønnsendringsdato,
                                                InntektArbeidYtelseGrunnlagDtoBuilder inntektArbeidYtelseGrunnlagBuilder) {
        byggArbeidInntekt(aktørId, skjæringstidspunktOpptjening, fraOgMed, tilOgMed, arbId, arbeidsgiver, arbeidType, inntektPrMnd,
            virksomhetPåInntekt, lønnsendringsdato, inntektArbeidYtelseGrunnlagBuilder);
        if (lønnsendringsdato.isPresent()) {
            brukUtenInntektsmelding(aktørId, arbeidType, arbeidsgiver, skjæringstidspunktOpptjening, inntektArbeidYtelseGrunnlagBuilder);
        }
    }

    private static void byggArbeidInntekt(AktørId aktørId,
                                          LocalDate skjæringstidspunktOpptjening,
                                          LocalDate fraOgMed,
                                          LocalDate tilOgMed,
                                          InternArbeidsforholdRefDto arbId,
                                          Arbeidsgiver arbeidsgiver,
                                          ArbeidType arbeidType,
                                          List<BigDecimal> inntektPrMnd,
                                          boolean virksomhetPåInntekt,
                                          Optional<LocalDate> lønnsendringsdato,
                                          InntektArbeidYtelseGrunnlagDtoBuilder inntektArbeidYtelseGrunnlagBuilder) {
        InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(inntektArbeidYtelseGrunnlagBuilder.getKladd().getRegisterVersjon(), VersjonTypeDto.REGISTER);
        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder = inntektArbeidYtelseAggregatBuilder.getAktørArbeidBuilder(aktørId);
        YrkesaktivitetDtoBuilder yrkesaktivitetBuilder = hentYABuilder(aktørArbeidBuilder, arbeidType, arbeidsgiver, arbId);

        AktivitetsAvtaleDtoBuilder aktivitetsAvtale = yrkesaktivitetBuilder.getAktivitetsAvtaleBuilder()
            .medPeriode(tilOgMed == null ? DatoIntervallEntitet.fraOgMed(fraOgMed) : DatoIntervallEntitet.fraOgMedTilOgMed(fraOgMed, tilOgMed))
            .medErAnsettelsesPeriode(false)
            .medSisteLønnsendringsdato(lønnsendringsdato.orElse(null));
        AktivitetsAvtaleDtoBuilder arbeidsperiode = yrkesaktivitetBuilder.getAktivitetsAvtaleBuilder()
            .medPeriode(tilOgMed == null ? DatoIntervallEntitet.fraOgMed(fraOgMed) : DatoIntervallEntitet.fraOgMedTilOgMed(fraOgMed, tilOgMed));

        yrkesaktivitetBuilder
            .medArbeidType(arbeidType)
            .medArbeidsgiver(arbeidsgiver)
            .leggTilAktivitetsAvtale(aktivitetsAvtale)
            .leggTilAktivitetsAvtale(arbeidsperiode);
        if (arbId != null) {
            yrkesaktivitetBuilder.medArbeidsforholdId(arbId);
        }

        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeid = aktørArbeidBuilder
            .leggTilYrkesaktivitet(yrkesaktivitetBuilder);
        inntektArbeidYtelseAggregatBuilder.leggTilAktørArbeid(aktørArbeid);
        byggInntektForBehandling(aktørId, skjæringstidspunktOpptjening, inntektArbeidYtelseAggregatBuilder, inntektPrMnd,
            virksomhetPåInntekt, arbeidsgiver);

        inntektArbeidYtelseGrunnlagBuilder.medData(inntektArbeidYtelseAggregatBuilder);
    }

    private static void brukUtenInntektsmelding(AktørId aktørId,
                                                ArbeidType arbeidType,
                                                Arbeidsgiver arbeidsgiver,
                                                LocalDate skjæringstidspunktOpptjening,
                                                InntektArbeidYtelseGrunnlagDtoBuilder inntektArbeidYtelseGrunnlagBuilder) {
        var filter = new YrkesaktivitetFilterDto(inntektArbeidYtelseGrunnlagBuilder.getArbeidsforholdInformasjon(), inntektArbeidYtelseGrunnlagBuilder.getKladd().getAktørArbeidFraRegister(aktørId))
            .før(skjæringstidspunktOpptjening);

        if (!filter.getYrkesaktiviteter().isEmpty()) {
            YrkesaktivitetDto yrkesaktivitet = finnKorresponderendeYrkesaktivitet(filter, arbeidType, arbeidsgiver);
            final ArbeidsforholdInformasjonDtoBuilder informasjonBuilder =
                ArbeidsforholdInformasjonDtoBuilder
                .oppdatere(inntektArbeidYtelseGrunnlagBuilder.getInformasjon());

            final ArbeidsforholdOverstyringDtoBuilder overstyringBuilderFor = informasjonBuilder.getOverstyringBuilderFor(yrkesaktivitet.getArbeidsgiver(),
                yrkesaktivitet.getArbeidsforholdRef());
            overstyringBuilderFor.medHandling(ArbeidsforholdHandlingType.BRUK_UTEN_INNTEKTSMELDING);
            informasjonBuilder.leggTil(overstyringBuilderFor);
            inntektArbeidYtelseGrunnlagBuilder.medInformasjon(informasjonBuilder.build());
        }

    }

    public static YrkesaktivitetDto finnKorresponderendeYrkesaktivitet(YrkesaktivitetFilterDto filter, ArbeidType arbeidType, Arbeidsgiver arbeidsgiver) {
        Collection<YrkesaktivitetDto> yrkesaktiviteter = finnKorresponderendeYrkesaktiviteter(filter, arbeidType);
        return yrkesaktiviteter
            .stream()
            .filter(ya -> ya.getArbeidsgiver().equals(arbeidsgiver))
            .findFirst().get();
    }

    private static Collection<YrkesaktivitetDto> finnKorresponderendeYrkesaktiviteter(YrkesaktivitetFilterDto filter, ArbeidType arbeidType) {
        if (ArbeidType.FRILANSER_OPPDRAGSTAKER_MED_MER.equals(arbeidType)) {
            return filter.getFrilansOppdrag();
        } else {
            return filter.getYrkesaktiviteter();
        }
    }

    private static YrkesaktivitetDtoBuilder hentYABuilder(InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder, ArbeidType arbeidType,
                                                          Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto arbId) {
        if (arbId == null) {
            return aktørArbeidBuilder.getYrkesaktivitetBuilderForType(arbeidType);
        } else {
            return aktørArbeidBuilder.getYrkesaktivitetBuilderForNøkkelAvType(new OpptjeningsnøkkelDto(arbId, arbeidsgiver), arbeidType);
        }

    }

    public static void byggInntektForBehandling(AktørId aktørId,
                                                LocalDate skjæringstidspunktOpptjening,
                                                InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder, List<BigDecimal> inntektPrMnd,
                                                boolean virksomhetPåInntekt, Arbeidsgiver arbeidsgiver) {

        InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder aktørInntekt = inntektArbeidYtelseAggregatBuilder.getAktørInntektBuilder(aktørId);

        InntektDtoBuilder inntektBeregningBuilder = aktørInntekt
            .getInntektBuilder(InntektsKilde.INNTEKT_BEREGNING, OpptjeningsnøkkelDto.forArbeidsforholdIdMedArbeidgiver(null, arbeidsgiver));

        // Lager et år (12 mnd) med inntekt for beregning
        byggInntekt(inntektBeregningBuilder, skjæringstidspunktOpptjening, inntektPrMnd, virksomhetPåInntekt, arbeidsgiver);
        aktørInntekt.leggTilInntekt(inntektBeregningBuilder);

        InntektDtoBuilder inntektSammenligningBuilder = aktørInntekt
            .getInntektBuilder(InntektsKilde.INNTEKT_SAMMENLIGNING, OpptjeningsnøkkelDto.forArbeidsforholdIdMedArbeidgiver(null, arbeidsgiver));

        // Lager et år (12 mnd) med inntekt for sammenligningsgrunnlag
        byggInntekt(inntektSammenligningBuilder, skjæringstidspunktOpptjening, inntektPrMnd, virksomhetPåInntekt, arbeidsgiver);
        aktørInntekt.leggTilInntekt(inntektSammenligningBuilder);

        inntektArbeidYtelseAggregatBuilder.leggTilAktørInntekt(aktørInntekt);
    }

    private static void byggInntekt(InntektDtoBuilder builder, LocalDate skjæringstidspunktOpptjening, List<BigDecimal> inntektPrMnd, boolean virksomhetPåInntekt,
                                    Arbeidsgiver arbeidsgiver) {
        if (virksomhetPåInntekt) {
            for (int i = 0; i <= 12; i++) {
                BigDecimal inntekt = getInntekt(inntektPrMnd, i);
                builder
                    .leggTilInntektspost(
                        lagInntektspost(skjæringstidspunktOpptjening.minusMonths(i + 1L).plusDays(1), skjæringstidspunktOpptjening.minusMonths(i), inntekt))
                    .medArbeidsgiver(arbeidsgiver);
            }
        } else {
            for (int i = 0; i <= 12; i++) {
                BigDecimal inntekt = getInntekt(inntektPrMnd, i);
                builder.leggTilInntektspost(
                    lagInntektspost(skjæringstidspunktOpptjening.minusMonths(i + 1L).plusDays(1), skjæringstidspunktOpptjening.minusMonths(i), inntekt));
            }
        }
    }

    private static BigDecimal getInntekt(List<BigDecimal> inntektPrMnd, int i) {
        BigDecimal inntekt;
        if (inntektPrMnd.size() >= i + 1) {
            inntekt = inntektPrMnd.get(i);
        } else {
            inntekt = inntektPrMnd.get(inntektPrMnd.size() - 1);
        }
        return inntekt;
    }

    private static InntektspostDtoBuilder lagInntektspost(LocalDate fom, LocalDate tom, BigDecimal lønn) {
        return InntektspostDtoBuilder.ny()
            .medBeløp(lønn)
            .medPeriode(fom, tom)
            .medInntektspostType(InntektspostType.LØNN);
    }
}
