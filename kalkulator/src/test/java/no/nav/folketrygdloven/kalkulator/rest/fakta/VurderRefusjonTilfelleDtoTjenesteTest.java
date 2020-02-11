package no.nav.folketrygdloven.kalkulator.rest.fakta;

import static no.nav.folketrygdloven.kalkulator.OpprettRefusjondatoerFraInntektsmeldinger.opprett;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.BehandlingReferanseMock;
import no.nav.folketrygdloven.kalkulator.gradering.AktivitetGradering;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagRestInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.BehandlingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagRestDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagRestDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.VersjonTypeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.ArbeidsgiverMedNavn;
import no.nav.folketrygdloven.kalkulator.rest.dto.FaktaOmBeregningDto;
import no.nav.folketrygdloven.kalkulator.testutilities.BeregningInntektsmeldingTestUtil;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.ArbeidType;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.FaktaOmBeregningTilfelle;
import no.nav.vedtak.konfig.Tid;

class VurderRefusjonTilfelleDtoTjenesteTest {
    private static final String ORGNR = "974760673";
    private static final String ORGNR2 = "915933149";

    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now();

    private BehandlingReferanse behandlingReferanse = new BehandlingReferanseMock(SKJÆRINGSTIDSPUNKT);

    private static Map<String, String> arbeidsgiverNavnMap = new HashMap<>();

    public static final String ARBEIDSGIVER_NAVN = "NAV AS";

    public static final String ARBEIDSGIVER_NAVN2 = "Norges beste arbeidsplass";

    static {
        arbeidsgiverNavnMap.put(ORGNR, ARBEIDSGIVER_NAVN);
        arbeidsgiverNavnMap.put(ORGNR2, ARBEIDSGIVER_NAVN2);
    }

    private VurderRefusjonTilfelleDtoTjeneste vurderRefusjonTilfelleDtoTjeneste = new VurderRefusjonTilfelleDtoTjeneste();

    @Test
    void skal_lage_dto_for_arbeidsgiver_som_har_søkt_refusjon_for_sent() {
        // Arrange
        Map<Arbeidsgiver, LocalDate> førsteInnsendingMap = new HashMap<>();
        InntektArbeidYtelseAggregatBuilder registerBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        BeregningAktivitetAggregatRestDto aktivitetAggregat = leggTilAktivitet(registerBuilder, List.of(ORGNR, ORGNR2));
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(ORGNR);
        Arbeidsgiver arbeidsgiver2 = Arbeidsgiver.virksomhet(ORGNR2);
        InntektsmeldingDto im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORGNR, SKJÆRINGSTIDSPUNKT, BigDecimal.TEN, BigDecimal.TEN);
        førsteInnsendingMap.put(arbeidsgiver, SKJÆRINGSTIDSPUNKT.plusMonths(4));
        InntektsmeldingDto im2 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORGNR2, SKJÆRINGSTIDSPUNKT, BigDecimal.TEN, BigDecimal.TEN);
        førsteInnsendingMap.put(arbeidsgiver2, SKJÆRINGSTIDSPUNKT.plusMonths(2));
        BeregningsgrunnlagGrunnlagRestDtoBuilder grunnlag = byggGrunnlag(aktivitetAggregat, List.of(arbeidsgiver, arbeidsgiver2));
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medData(registerBuilder)
                .medInntektsmeldinger(List.of(im1, im2)).build();
        BeregningsgrunnlagRestInput input = lagInputMedBeregningsgrunnlagOgIAY(behandlingReferanse, grunnlag, BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER, iayGrunnlag, førsteInnsendingMap);

        // Act
        FaktaOmBeregningDto faktaOmBeregningDto = new FaktaOmBeregningDto();
        vurderRefusjonTilfelleDtoTjeneste.lagDto(input, faktaOmBeregningDto);

        // Assert
        assertThat(faktaOmBeregningDto.getRefusjonskravSomKommerForSentListe().size()).isEqualTo(1);
        assertThat(faktaOmBeregningDto.getRefusjonskravSomKommerForSentListe().iterator().next().getArbeidsgiverId()).isEqualTo(arbeidsgiver.getIdentifikator());
        assertThat(faktaOmBeregningDto.getRefusjonskravSomKommerForSentListe().iterator().next().getArbeidsgiverVisningsnavn()).isEqualTo(arbeidsgiverNavnMap.get(ORGNR));

    }

    private BeregningsgrunnlagGrunnlagRestDtoBuilder byggGrunnlag(BeregningAktivitetAggregatRestDto aktivitetAggregat, List<Arbeidsgiver> arbeidsgivere) {
        return BeregningsgrunnlagGrunnlagRestDtoBuilder.oppdatere(Optional.empty())
                .medRegisterAktiviteter(aktivitetAggregat)
                .medBeregningsgrunnlag(lagBeregningsgrunnlag(arbeidsgivere.stream().map(a -> {
                    ArbeidsgiverMedNavn virksomhet = ArbeidsgiverMedNavn.virksomhet(a.getOrgnr());
                    virksomhet.setNavn(arbeidsgiverNavnMap.get(a.getOrgnr()));
                    return virksomhet;
                }).collect(Collectors.toList())));
    }

    private BeregningsgrunnlagRestDto lagBeregningsgrunnlag(List<ArbeidsgiverMedNavn> ags) {

        BeregningsgrunnlagRestDto bg = BeregningsgrunnlagRestDto.builder()
                .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
                .leggTilFaktaOmBeregningTilfeller(List.of(FaktaOmBeregningTilfelle.VURDER_REFUSJONSKRAV_SOM_HAR_KOMMET_FOR_SENT))
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusRestDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)).build();
        BeregningsgrunnlagPeriodeRestDto periode = BeregningsgrunnlagPeriodeRestDto.builder().medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null)
                .build(bg);
        ags.forEach(ag -> BeregningsgrunnlagPrStatusOgAndelRestDto.kopier()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdRestDto.builder().medArbeidsgiver(ag))
                .build(periode)
        );
        return bg;
    }

    private BeregningAktivitetAggregatRestDto leggTilAktivitet(InntektArbeidYtelseAggregatBuilder iayAggregatBuilder, List<String> orgnr) {
        Intervall arbeidsperiode1 = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.minusYears(2), Tid.TIDENES_ENDE);
        var aktørArbeidBuilder = InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder.oppdatere(Optional.empty())
                .medAktørId(behandlingReferanse.getAktørId());
        BeregningAktivitetAggregatRestDto.Builder aktivitetAggregatBuilder = BeregningAktivitetAggregatRestDto.builder()
                .medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT);
        for (String nr : orgnr) {
            leggTilYrkesaktivitet(arbeidsperiode1, aktørArbeidBuilder, nr);
            ArbeidsgiverMedNavn virksomhet = ArbeidsgiverMedNavn.virksomhet(nr);
            virksomhet.setNavn(arbeidsgiverNavnMap.get(nr));
            aktivitetAggregatBuilder.leggTilAktivitet(lagAktivitet(arbeidsperiode1, virksomhet));
        }
        iayAggregatBuilder.leggTilAktørArbeid(aktørArbeidBuilder);
        return aktivitetAggregatBuilder.build();
    }

    private BeregningAktivitetRestDto lagAktivitet(Intervall arbeidsperiode1, ArbeidsgiverMedNavn ag) {
        return BeregningAktivitetRestDto.builder().medArbeidsgiver(ag).medOpptjeningAktivitetType(OpptjeningAktivitetType.ARBEID).medPeriode(Intervall.fraOgMedTilOgMed(arbeidsperiode1.getFomDato(), arbeidsperiode1.getTomDato())).build();
    }

    private void leggTilYrkesaktivitet(Intervall arbeidsperiode, InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder, String orgnr) {
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(orgnr);
        AktivitetsAvtaleDtoBuilder aaBuilder1 = AktivitetsAvtaleDtoBuilder.ny()
                .medPeriode(arbeidsperiode);
        YrkesaktivitetDtoBuilder yaBuilder = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
                .medArbeidsgiver(arbeidsgiver)
                .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
                .leggTilAktivitetsAvtale(aaBuilder1);
        aktørArbeidBuilder.leggTilYrkesaktivitet(yaBuilder);
    }


    public static BeregningsgrunnlagRestInput lagInputMedBeregningsgrunnlagOgIAY(BehandlingReferanse behandlingReferanse,
                                                                             BeregningsgrunnlagGrunnlagRestDtoBuilder beregningsgrunnlagGrunnlagBuilder,
                                                                             BeregningsgrunnlagTilstand tilstand,
                                                                             InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                                                             Map<Arbeidsgiver, LocalDate> førsteInnsendingAvRefusjonMap) {
        BeregningsgrunnlagRestInput input = new BeregningsgrunnlagRestInput(behandlingReferanse, iayGrunnlag,
                AktivitetGradering.INGEN_GRADERING, opprett(behandlingReferanse, iayGrunnlag, førsteInnsendingAvRefusjonMap), null);
        BeregningsgrunnlagGrunnlagRestDto grunnlag = beregningsgrunnlagGrunnlagBuilder.build(tilstand);
        BeregningsgrunnlagRestInput inputMedBeregningsgrunnlag = input.medBeregningsgrunnlagGrunnlag(grunnlag);
        inputMedBeregningsgrunnlag.leggTilBeregningsgrunnlagIHistorikk(grunnlag, tilstand);
        return inputMedBeregningsgrunnlag;
    }

}
