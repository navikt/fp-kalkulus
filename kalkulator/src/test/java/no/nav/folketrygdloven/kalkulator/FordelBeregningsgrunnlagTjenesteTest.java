package no.nav.folketrygdloven.kalkulator;

import static no.nav.folketrygdloven.kalkulator.OpprettRefusjondatoerFraInntektsmeldinger.opprett;
import static no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLTest.GRUNNBELØP;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLNaturalytelse;
import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLRefusjonOgGradering;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.ForeldrepengerGrunnlagMapper;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapBeregningsgrunnlagFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapInntektsgrunnlagVLTilRegel;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapInntektsgrunnlagVLTilRegelFelles;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.YtelsesspesifikkRegelMapper;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelNaturalYtelse;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelRefusjonOgGradering;
import no.nav.folketrygdloven.kalkulator.gradering.AktivitetGradering;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.BehandlingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.VersjonTypeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.testutilities.BeregningInntektsmeldingTestUtil;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.ArbeidType;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.Hjemmel;
import no.nav.folketrygdloven.utils.UnitTestLookupInstanceImpl;
import no.nav.vedtak.konfig.Tid;

public class FordelBeregningsgrunnlagTjenesteTest {

    private static final String ORGNR1 = "995428563";
    private static final String ORGNR2 = "910909088";
    private static final String ORGNR3 = "973861778";
    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.of(2019, Month.JANUARY, 4);

    private FordelBeregningsgrunnlagTjeneste fordelBeregningsgrunnlagTjeneste;
    private List<BeregningAktivitetDto> aktiviteter = new ArrayList<>();
    private BeregningAktivitetAggregatDto beregningAktivitetAggregat;
    private BehandlingReferanse behandlingReferanse = new BehandlingReferanseMock(SKJÆRINGSTIDSPUNKT);
    private InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder;
    private MapInntektsgrunnlagVLTilRegel mapInntektsgrunnlagVLTilRegel = new MapInntektsgrunnlagVLTilRegelFelles();
    private final UnitTestLookupInstanceImpl<YtelsesspesifikkRegelMapper> ytelsesSpesifikkMapper = new UnitTestLookupInstanceImpl<>(new ForeldrepengerGrunnlagMapper());
    private MapBeregningsgrunnlagFraVLTilRegel mapBeregningsgrunnlagFraVLTilRegel = new MapBeregningsgrunnlagFraVLTilRegel(new UnitTestLookupInstanceImpl<>(mapInntektsgrunnlagVLTilRegel), ytelsesSpesifikkMapper);


    @BeforeEach
    public void oppsett() {
        fordelBeregningsgrunnlagTjeneste = new FordelBeregningsgrunnlagTjeneste(lagTjeneste(), mapBeregningsgrunnlagFraVLTilRegel);
        iayGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        leggTilYrkesaktiviteterOgBeregningAktiviteter(iayGrunnlagBuilder, List.of(ORGNR1, ORGNR2, ORGNR3));
        BeregningAktivitetAggregatDto.Builder builder = BeregningAktivitetAggregatDto.builder().medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT);
        aktiviteter.forEach(builder::leggTilAktivitet);
        beregningAktivitetAggregat = builder.build();
    }

    @Test
    public void skal_omfordele_når_refusjon_overstiger_beregningsgrunnlag_for_ein_andel() {
        // Arrange
        // Beregningsgrunnlag fra Foreslå
        BigDecimal beregnetPrÅr1 = BigDecimal.valueOf(120_000);
        Map<String, BigDecimal> orgnrsBeregnetMap = new HashMap<>();
        orgnrsBeregnetMap.put(ORGNR1, beregnetPrÅr1);
        BigDecimal beregnetPrÅr2 = BigDecimal.valueOf(180_000);
        orgnrsBeregnetMap.put(ORGNR2, beregnetPrÅr2);
        BigDecimal beregnetPrÅr3 = BigDecimal.valueOf(240_000);
        orgnrsBeregnetMap.put(ORGNR3, beregnetPrÅr3);
        BeregningsgrunnlagGrunnlagDto grunnlag = lagBeregningsgrunnlag(orgnrsBeregnetMap, behandlingReferanse, beregningAktivitetAggregat);
        BeregningsgrunnlagDto beregningsgrunnlag = grunnlag.getBeregningsgrunnlag().get();

        // Inntektsmelding
        BigDecimal inntektPrMnd1 = BigDecimal.valueOf(10_000);
        BigDecimal refusjonPrMnd1 = BigDecimal.valueOf(20_000);
        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORGNR1, SKJÆRINGSTIDSPUNKT, refusjonPrMnd1, inntektPrMnd1);
        BigDecimal inntektPrMnd2 = BigDecimal.valueOf(15_000);
        BigDecimal refusjonPrMnd2 = BigDecimal.valueOf(15_000);
        var im2 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORGNR2, SKJÆRINGSTIDSPUNKT, refusjonPrMnd2, inntektPrMnd2);
        BigDecimal inntektPrMnd3 = BigDecimal.valueOf(20_000);
        BigDecimal refusjonPrMnd3 = BigDecimal.ZERO;
        var im3 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORGNR3, SKJÆRINGSTIDSPUNKT, refusjonPrMnd3, inntektPrMnd3);
        var inntektsmeldinger = List.of(im1, im2, im3);

        var iayGrunnlag = iayGrunnlagBuilder.medInntektsmeldinger(inntektsmeldinger).build();
        var input = new BeregningsgrunnlagInput(behandlingReferanse, iayGrunnlag, null, AktivitetGradering.INGEN_GRADERING, opprett(behandlingReferanse, iayGrunnlag), null)
                .medGrunnbeløpsatser(GrunnbeløpMock.GRUNNBELØPSATSER)
                .medBeregningsgrunnlagGrunnlag(grunnlag);

        // Act
        BeregningsgrunnlagDto nyttBeregningsgrunnlag = fordelBeregningsgrunnlagTjeneste.fordelBeregningsgrunnlag(input, beregningsgrunnlag);

        // Assert
        assertThat(nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().size()).isEqualTo(1);
        BeregningsgrunnlagPeriodeDto periode = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        List<BeregningsgrunnlagPrStatusOgAndelDto> andeler = periode.getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler.size()).isEqualTo(3);
        BeregningsgrunnlagPrStatusOgAndelDto andel1 = andeler.stream().filter(a -> a.getBgAndelArbeidsforhold().get().getArbeidsgiver().getIdentifikator().equals(ORGNR1)).findFirst().get();
        BeregningsgrunnlagPrStatusOgAndelDto andel2 = andeler.stream().filter(a -> a.getBgAndelArbeidsforhold().get().getArbeidsgiver().getIdentifikator().equals(ORGNR2)).findFirst().get();
        BeregningsgrunnlagPrStatusOgAndelDto andel3 = andeler.stream().filter(a -> a.getBgAndelArbeidsforhold().get().getArbeidsgiver().getIdentifikator().equals(ORGNR3)).findFirst().get();

        // Forventer at ORGNR1 har fått økt sitt brutto bg
        BigDecimal forventetNyBruttoForArbeid1 = BigDecimal.valueOf(240_000);
        assertThat(andel1.getFordeltPrÅr()).isEqualByComparingTo(forventetNyBruttoForArbeid1);
        // Forventer at brutto for arbeid for ORGNR2 er uendret ettersom den ikkje har disponibelt grunnlag å fordele (søker full refusjon)
        assertThat(andel2.getFordeltPrÅr()).isNull();
        // Forventer at ORGNR2 har fått redusert sitt brutto bg
        BigDecimal forventetNyBruttoForArbeid3 = BigDecimal.valueOf(120_000);
        assertThat(andel3.getFordeltPrÅr()).isEqualByComparingTo(forventetNyBruttoForArbeid3);
    }

    private FastsettBeregningsgrunnlagPerioderTjeneste lagTjeneste() {
        var oversetterTilRegelNaturalytelse = new MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelNaturalYtelse();
        var oversetterTilRegelRefusjonOgGradering = new MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelRefusjonOgGradering();
        var oversetterFraRegelTilVLNaturalytelse = new MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLNaturalytelse();
        var oversetterFraRegelTilVLRefusjonOgGradering = new MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLRefusjonOgGradering();
        return new FastsettBeregningsgrunnlagPerioderTjeneste(oversetterTilRegelNaturalytelse,
            new UnitTestLookupInstanceImpl<>(oversetterTilRegelRefusjonOgGradering), oversetterFraRegelTilVLNaturalytelse,
            oversetterFraRegelTilVLRefusjonOgGradering);
    }

    private BeregningsgrunnlagGrunnlagDto lagBeregningsgrunnlag(Map<String, BigDecimal> orgnrs, BehandlingReferanse behandlingReferanse,
                                                                BeregningAktivitetAggregatDto beregningAktivitetAggregat) {
        BeregningsgrunnlagPeriodeDto.Builder beregningsgrunnlagPeriodeBuilder = lagBeregningsgrunnlagPerioderBuilder(SKJÆRINGSTIDSPUNKT, null, orgnrs);
        BeregningsgrunnlagDto.Builder beregningsgrunnlagBuilder = BeregningsgrunnlagDto.builder()
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .medGrunnbeløp(GRUNNBELØP)
            .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER).medHjemmel(Hjemmel.F_14_7));
        beregningsgrunnlagBuilder.leggTilBeregningsgrunnlagPeriode(beregningsgrunnlagPeriodeBuilder);
        BeregningsgrunnlagDto bg = beregningsgrunnlagBuilder.build();
        return BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
            .medBeregningsgrunnlag(bg)
            .medRegisterAktiviteter(beregningAktivitetAggregat)
            .build(BeregningsgrunnlagTilstand.FORESLÅTT);
    }

    private BeregningsgrunnlagPeriodeDto.Builder lagBeregningsgrunnlagPerioderBuilder(LocalDate fom, LocalDate tom, Map<String, BigDecimal> orgnrs) {
        BeregningsgrunnlagPeriodeDto.Builder builder = BeregningsgrunnlagPeriodeDto.builder();
        for (String orgnr : orgnrs.keySet()) {
            Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(orgnr);
            BeregningsgrunnlagPrStatusOgAndelDto.Builder andelBuilder = BeregningsgrunnlagPrStatusOgAndelDto.kopier()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medBeregnetPrÅr(orgnrs.get(orgnr))
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder()
                    .medArbeidsgiver(arbeidsgiver)
                    .medArbeidsperiodeFom(SKJÆRINGSTIDSPUNKT.minusYears(1)));
            builder.leggTilBeregningsgrunnlagPrStatusOgAndel(andelBuilder);
        }
        return builder
            .medBeregningsgrunnlagPeriode(fom, tom);
    }

    private void leggTilYrkesaktiviteterOgBeregningAktiviteter(InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder, List<String> orgnrs) {
        Intervall arbeidsperiode1 = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.minusYears(2), Tid.TIDENES_ENDE);

        var aktørArbeidBuilder = InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder.oppdatere(Optional.empty())
            .medAktørId(behandlingReferanse.getAktørId());
        for (String orgnr : orgnrs) {
            Arbeidsgiver arbeidsgiver = leggTilYrkesaktivitet(arbeidsperiode1, aktørArbeidBuilder, orgnr);
            fjernOgLeggTilNyBeregningAktivitet(arbeidsperiode1.getFomDato(), arbeidsperiode1.getTomDato(), arbeidsgiver, InternArbeidsforholdRefDto.nullRef());
        }
        InntektArbeidYtelseAggregatBuilder registerBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        registerBuilder.leggTilAktørArbeid(aktørArbeidBuilder);
        iayGrunnlagBuilder.medData(registerBuilder);
    }

    private void fjernOgLeggTilNyBeregningAktivitet(LocalDate fom, LocalDate tom, Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto arbeidsforholdRef) {
        if (fom.isAfter(SKJÆRINGSTIDSPUNKT)) {
            throw new IllegalArgumentException("Kan ikke lage BeregningAktivitet som starter etter skjæringstidspunkt");
        }
        aktiviteter.add(lagAktivitet(fom, tom, arbeidsgiver, arbeidsforholdRef));
    }


    private BeregningAktivitetDto lagAktivitet(LocalDate fom, LocalDate tom, Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto arbeidsforholdRef) {
        return BeregningAktivitetDto.builder()
            .medPeriode(Intervall.fraOgMedTilOgMed(fom, tom))
            .medArbeidsgiver(arbeidsgiver)
            .medArbeidsforholdRef(arbeidsforholdRef)
            .medOpptjeningAktivitetType(OpptjeningAktivitetType.ARBEID)
            .build();
    }

    private Arbeidsgiver leggTilYrkesaktivitet(Intervall arbeidsperiode, InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder,
                                               String orgnr) {
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(orgnr);
        AktivitetsAvtaleDtoBuilder aaBuilder1 = AktivitetsAvtaleDtoBuilder.ny()
            .medPeriode(arbeidsperiode);
        YrkesaktivitetDtoBuilder yaBuilder = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
            .medArbeidsgiver(arbeidsgiver)
            .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
            .leggTilAktivitetsAvtale(aaBuilder1);
        aktørArbeidBuilder.leggTilYrkesaktivitet(yaBuilder);
        return arbeidsgiver;
    }


}
