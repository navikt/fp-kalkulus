package no.nav.folketrygdloven.kalkulator.rest.fakta;


import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

import no.nav.folketrygdloven.kalkulator.BehandlingReferanseMock;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.ForeldrepengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.gradering.AktivitetGradering;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagRestInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.BehandlingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.behandling.Skjæringstidspunkt;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagRestDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.SammenligningsgrunnlagPrStatusRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.SammenligningsgrunnlagRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.SammenligningsgrunnlagType;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.ArbeidsgiverMedNavn;
import no.nav.folketrygdloven.kalkulator.opptjening.OpptjeningAktiviteterDto;
import no.nav.folketrygdloven.kalkulator.regelmodell.Periode;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;

public class BeregningsgrunnlagPrStatusOgAndelDtoTjenesteTest {
    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now().minusDays(5);

    private BehandlingReferanse behandlingReferanse = new BehandlingReferanseMock();

    private static final Inntektskategori INNTEKTSKATEGORI = Inntektskategori.ARBEIDSTAKER;
    private static final BigDecimal AVKORTET_PR_AAR = BigDecimal.valueOf(150000);
    private static final BigDecimal BRUTTO_PR_AAR = BigDecimal.valueOf(300000);
    private static final BigDecimal REDUSERT_PR_AAR = BigDecimal.valueOf(500000);
    private static final BigDecimal OVERSTYRT_PR_AAR = BigDecimal.valueOf(500);
    private static final LocalDate ANDEL_FOM = LocalDate.now().minusDays(100);
    private static final LocalDate ANDEL_TOM = LocalDate.now();
    private static final String ORGNR = "973093681";
    private static final Long ANDELSNR = 1L;

    private static final BigDecimal RAPPORTERT_PR_AAR = BigDecimal.valueOf(300000);
    private static final BigDecimal AVVIK_OVER_25_PROSENT = BigDecimal.valueOf(500L);
    private static final BigDecimal AVVIK_UNDER_25_PROSENT = BigDecimal.valueOf(30L);
    private static final LocalDate SAMMENLIGNING_FOM = LocalDate.now().minusDays(100);
    private static final LocalDate SAMMENLIGNING_TOM = LocalDate.now();

    private BeregningsgrunnlagGrunnlagRestDto grunnlag;
    private OpptjeningAktiviteterDto opptjeningAktiviteter;

    @Test
    public void skalFastsetteGrunnlagForSnNårAvvikOver25ProsentOgGammeltSammenligningsgrunnlag(){
        //Arange
        ArbeidsgiverMedNavn arbeidsgiver = ArbeidsgiverMedNavn.virksomhet(ORGNR);
        BeregningsgrunnlagRestDto Beregningsgrunnlag = lagBeregningsgrunnlagMedAvvikOver25Prosent(SammenligningsgrunnlagType.SAMMENLIGNING_ATFL_SN);
        BeregningsgrunnlagPeriodeRestDto bgPeriode = buildBeregningsgrunnlagPeriode(Beregningsgrunnlag);
        byggAndelAt(bgPeriode, arbeidsgiver, 1L);
        byggAndelFl(bgPeriode, arbeidsgiver, 2L);
        byggAndelSn(bgPeriode, arbeidsgiver, null, 3L);
        lagBehandling(Beregningsgrunnlag, arbeidsgiver);
        var ytelsespesifiktGrunnlag = new ForeldrepengerGrunnlag(100, false);
        var iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt().medInntektsmeldinger(Collections.emptyList()).build();
        var input = new BeregningsgrunnlagRestInput(lagReferanseMedStp(behandlingReferanse), iayGrunnlag, AktivitetGradering.INGEN_GRADERING, List.of(), ytelsespesifiktGrunnlag).medBeregningsgrunnlagGrunnlag(grunnlag);
        //Act
        var beregningsgrunnlagPrStatusOgAndelDto = BeregningsgrunnlagPrStatusOgAndelDtoTjeneste.lagBeregningsgrunnlagPrStatusOgAndelDto(input,
            grunnlag.getBeregningsgrunnlag().get().getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList());
        // Assert
        assertThat(beregningsgrunnlagPrStatusOgAndelDto.get(0).getSkalFastsetteGrunnlag()).isEqualTo(false);
        assertThat(beregningsgrunnlagPrStatusOgAndelDto.get(1).getSkalFastsetteGrunnlag()).isEqualTo(false);
        assertThat(beregningsgrunnlagPrStatusOgAndelDto.get(2).getSkalFastsetteGrunnlag()).isEqualTo(true);
    }

    @Test
    public void skalFastsetteGrunnlagForBådeFlOgAtSnNårAvvikOver25ProsentOgGammeltSammenligningsgrunnlagOgKunFlOgAtAndel(){
        //Arange
        ArbeidsgiverMedNavn arbeidsgiver = ArbeidsgiverMedNavn.virksomhet(ORGNR);
        BeregningsgrunnlagRestDto Beregningsgrunnlag = lagBeregningsgrunnlagMedAvvikOver25Prosent(SammenligningsgrunnlagType.SAMMENLIGNING_ATFL_SN);
        BeregningsgrunnlagPeriodeRestDto bgPeriode = buildBeregningsgrunnlagPeriode(Beregningsgrunnlag);
        byggAndelAt(bgPeriode, arbeidsgiver, 1L);
        byggAndelFl(bgPeriode, arbeidsgiver, 2L);
        lagBehandling(Beregningsgrunnlag, arbeidsgiver);
        var ytelsespesifiktGrunnlag = new ForeldrepengerGrunnlag(100, false);
        var iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt().medInntektsmeldinger(Collections.emptyList()).build();
        var input = new BeregningsgrunnlagRestInput(lagReferanseMedStp(behandlingReferanse), iayGrunnlag, AktivitetGradering.INGEN_GRADERING, List.of(), ytelsespesifiktGrunnlag).medBeregningsgrunnlagGrunnlag(grunnlag);
        //Act
        var beregningsgrunnlagPrStatusOgAndelDto = BeregningsgrunnlagPrStatusOgAndelDtoTjeneste.lagBeregningsgrunnlagPrStatusOgAndelDto(input,
            grunnlag.getBeregningsgrunnlag().get().getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList());
        // Assert
        assertThat(beregningsgrunnlagPrStatusOgAndelDto.get(0).getSkalFastsetteGrunnlag()).isEqualTo(true);
        assertThat(beregningsgrunnlagPrStatusOgAndelDto.get(1).getSkalFastsetteGrunnlag()).isEqualTo(true);
    }

    @Test
    public void skalFastsetteGrunnlagForSnNårAvvikErUnder25ProsentOgNyIArbeidslivet(){
        //Arange
        ArbeidsgiverMedNavn arbeidsgiver = ArbeidsgiverMedNavn.virksomhet(ORGNR);
        BeregningsgrunnlagRestDto Beregningsgrunnlag = lagBeregningsgrunnlagUtenSammenligningsgrunnlag();
        BeregningsgrunnlagPeriodeRestDto bgPeriode = buildBeregningsgrunnlagPeriode(Beregningsgrunnlag);
        byggAndelAt(bgPeriode, arbeidsgiver, 1L);
        byggAndelFl(bgPeriode, arbeidsgiver, 2L);
        byggAndelSn(bgPeriode, arbeidsgiver, true, 3L);
        lagBehandling(Beregningsgrunnlag, arbeidsgiver);
        var ytelsespesifiktGrunnlag = new ForeldrepengerGrunnlag(100, false);
        var iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt().medInntektsmeldinger(Collections.emptyList()).build();
        var input = new BeregningsgrunnlagRestInput(lagReferanseMedStp(behandlingReferanse), iayGrunnlag, AktivitetGradering.INGEN_GRADERING, List.of(), ytelsespesifiktGrunnlag).medBeregningsgrunnlagGrunnlag(grunnlag);
        //Act
        var beregningsgrunnlagPrStatusOgAndelDto = BeregningsgrunnlagPrStatusOgAndelDtoTjeneste.lagBeregningsgrunnlagPrStatusOgAndelDto(input,
            grunnlag.getBeregningsgrunnlag().get().getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList());
        // Assert
        assertThat(beregningsgrunnlagPrStatusOgAndelDto.get(0).getSkalFastsetteGrunnlag()).isEqualTo(false);
        assertThat(beregningsgrunnlagPrStatusOgAndelDto.get(1).getSkalFastsetteGrunnlag()).isEqualTo(false);
        assertThat(beregningsgrunnlagPrStatusOgAndelDto.get(2).getSkalFastsetteGrunnlag()).isEqualTo(true);
    }

    @Test
    public void skalFastsetteGrunnlagForSnNårAvvikErOver25Prosent(){
        //Arange
        ArbeidsgiverMedNavn arbeidsgiver = ArbeidsgiverMedNavn.virksomhet(ORGNR);
        BeregningsgrunnlagRestDto Beregningsgrunnlag = lagBeregningsgrunnlagMedAvvikOver25Prosent(SammenligningsgrunnlagType.SAMMENLIGNING_SN);
        BeregningsgrunnlagPeriodeRestDto bgPeriode = buildBeregningsgrunnlagPeriode(Beregningsgrunnlag);
        byggAndelSn(bgPeriode, arbeidsgiver, false, 1L);
        lagBehandling(Beregningsgrunnlag, arbeidsgiver);
        var ytelsespesifiktGrunnlag = new ForeldrepengerGrunnlag(100, false);
        var iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt().medInntektsmeldinger(Collections.emptyList()).build();
        var input = new BeregningsgrunnlagRestInput(lagReferanseMedStp(behandlingReferanse), iayGrunnlag, AktivitetGradering.INGEN_GRADERING, List.of(), ytelsespesifiktGrunnlag).medBeregningsgrunnlagGrunnlag(grunnlag);
        //Act
        var beregningsgrunnlagPrStatusOgAndelDto = BeregningsgrunnlagPrStatusOgAndelDtoTjeneste.lagBeregningsgrunnlagPrStatusOgAndelDto(input,
            grunnlag.getBeregningsgrunnlag().get().getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList());
        // Assert
        assertThat(beregningsgrunnlagPrStatusOgAndelDto.get(0).getSkalFastsetteGrunnlag()).isEqualTo(true);
    }

    @Test
    public void skalIkkeKasteExceptionNårDetFinnesHverkenAtFlEllerSnAndelOgDetFinnesSammenligningsgrunnlagPrStatus() throws Exception{
        //Arange
        ArbeidsgiverMedNavn arbeidsgiver = ArbeidsgiverMedNavn.virksomhet(ORGNR);
        BeregningsgrunnlagRestDto Beregningsgrunnlag = lagBeregningsgrunnlagMedAvvikOver25Prosent(SammenligningsgrunnlagType.SAMMENLIGNING_ATFL_SN);
        BeregningsgrunnlagPeriodeRestDto bgPeriode = buildBeregningsgrunnlagPeriode(Beregningsgrunnlag);
        byggAndelerAapDpVentelønn(bgPeriode, arbeidsgiver);
        lagBehandling(Beregningsgrunnlag, arbeidsgiver);
        var ytelsespesifiktGrunnlag = new ForeldrepengerGrunnlag(100, false);
        var iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt().medInntektsmeldinger(Collections.emptyList()).build();
        var input = new BeregningsgrunnlagRestInput(lagReferanseMedStp(behandlingReferanse), iayGrunnlag, AktivitetGradering.INGEN_GRADERING, List.of(), ytelsespesifiktGrunnlag).medBeregningsgrunnlagGrunnlag(grunnlag);
        //Act
        BeregningsgrunnlagPrStatusOgAndelDtoTjeneste.lagBeregningsgrunnlagPrStatusOgAndelDto(input,
            grunnlag.getBeregningsgrunnlag().get().getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList());
    }

    @Test
    public void skalFastsetteGrunnlagForAtNårAvvikStørreEnn25ProsentForAtAndelOgSammenligningsgrunnlagMedTypeSammenliningAt(){
        //Arange
        ArbeidsgiverMedNavn arbeidsgiver = ArbeidsgiverMedNavn.virksomhet(ORGNR);
        BeregningsgrunnlagRestDto Beregningsgrunnlag = lagBeregningsgrunnlagMedAvvikOver25Prosent(SammenligningsgrunnlagType.SAMMENLIGNING_AT);
        BeregningsgrunnlagPeriodeRestDto bgPeriode = buildBeregningsgrunnlagPeriode(Beregningsgrunnlag);
        byggAndelAt(bgPeriode, arbeidsgiver, 1L);
        byggAndelFl(bgPeriode, arbeidsgiver, 2L);
        lagBehandling(Beregningsgrunnlag, arbeidsgiver);
        var ytelsespesifiktGrunnlag = new ForeldrepengerGrunnlag(100, false);
        var iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt().medInntektsmeldinger(Collections.emptyList()).build();
        var input = new BeregningsgrunnlagRestInput(lagReferanseMedStp(behandlingReferanse), iayGrunnlag, AktivitetGradering.INGEN_GRADERING, List.of(), ytelsespesifiktGrunnlag).medBeregningsgrunnlagGrunnlag(grunnlag);
        //Act
        var beregningsgrunnlagPrStatusOgAndelDto = BeregningsgrunnlagPrStatusOgAndelDtoTjeneste.lagBeregningsgrunnlagPrStatusOgAndelDto(input,
            grunnlag.getBeregningsgrunnlag().get().getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList());
        // Assert
        assertThat(beregningsgrunnlagPrStatusOgAndelDto.get(0).getSkalFastsetteGrunnlag()).isEqualTo(true);
        assertThat(beregningsgrunnlagPrStatusOgAndelDto.get(1).getSkalFastsetteGrunnlag()).isEqualTo(false);
    }

    @Test
    public void skalFastsetteGrunnlagForAtOgFlBasertPåSammenligningsgrunnlagNårAvvikStørreEnn25ProsentForAtAndelOgIngenSammenligningsgrunnlagPrStatus(){
        //Arange
        ArbeidsgiverMedNavn arbeidsgiver = ArbeidsgiverMedNavn.virksomhet(ORGNR);
        BeregningsgrunnlagRestDto Beregningsgrunnlag = lagBeregningsgrunnlagMedAvvikOver25ProsentMedKunSammenligningsgrunnlag();
        BeregningsgrunnlagPeriodeRestDto bgPeriode = buildBeregningsgrunnlagPeriode(Beregningsgrunnlag);
        byggAndelAt(bgPeriode, arbeidsgiver, 1L);
        byggAndelFl(bgPeriode, arbeidsgiver, 2L);
        lagBehandling(Beregningsgrunnlag, arbeidsgiver);
        var ytelsespesifiktGrunnlag = new ForeldrepengerGrunnlag(100, false);
        var iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt().medInntektsmeldinger(Collections.emptyList()).build();
        var input = new BeregningsgrunnlagRestInput(lagReferanseMedStp(behandlingReferanse), iayGrunnlag, AktivitetGradering.INGEN_GRADERING, List.of(), ytelsespesifiktGrunnlag).medBeregningsgrunnlagGrunnlag(grunnlag);
        //Act
        var beregningsgrunnlagPrStatusOgAndelDto = BeregningsgrunnlagPrStatusOgAndelDtoTjeneste.lagBeregningsgrunnlagPrStatusOgAndelDto(input,
            grunnlag.getBeregningsgrunnlag().get().getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList());
        // Assert
        assertThat(beregningsgrunnlagPrStatusOgAndelDto.get(0).getSkalFastsetteGrunnlag()).isEqualTo(true);
        assertThat(beregningsgrunnlagPrStatusOgAndelDto.get(1).getSkalFastsetteGrunnlag()).isEqualTo(true);
    }

    @Test
    public void skalFastsetteGrunnlagForKunSnBasertPåSammenligningsgrunnlagNårAvvikStørreEnn25ProsentForAlleAndelerOgIngenSammenligningsgrunnlagPrStatus(){
        //Arange
        ArbeidsgiverMedNavn arbeidsgiver = ArbeidsgiverMedNavn.virksomhet(ORGNR);
        BeregningsgrunnlagRestDto Beregningsgrunnlag = lagBeregningsgrunnlagMedAvvikOver25ProsentMedKunSammenligningsgrunnlag();
        BeregningsgrunnlagPeriodeRestDto bgPeriode = buildBeregningsgrunnlagPeriode(Beregningsgrunnlag);
        byggAndelAt(bgPeriode, arbeidsgiver, 1L);
        byggAndelFl(bgPeriode, arbeidsgiver, 2L);
        byggAndelSn(bgPeriode, arbeidsgiver, false, 3L);
        lagBehandling(Beregningsgrunnlag, arbeidsgiver);
        var ytelsespesifiktGrunnlag = new ForeldrepengerGrunnlag(100, false);
        var iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt().medInntektsmeldinger(Collections.emptyList()).build();
        var input = new BeregningsgrunnlagRestInput(lagReferanseMedStp(behandlingReferanse), iayGrunnlag, AktivitetGradering.INGEN_GRADERING, List.of(), ytelsespesifiktGrunnlag).medBeregningsgrunnlagGrunnlag(grunnlag);
        //Act
        var beregningsgrunnlagPrStatusOgAndelDto = BeregningsgrunnlagPrStatusOgAndelDtoTjeneste.lagBeregningsgrunnlagPrStatusOgAndelDto(input,
            grunnlag.getBeregningsgrunnlag().get().getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList());
        // Assert
        assertThat(beregningsgrunnlagPrStatusOgAndelDto.get(0).getSkalFastsetteGrunnlag()).isEqualTo(false);
        assertThat(beregningsgrunnlagPrStatusOgAndelDto.get(1).getSkalFastsetteGrunnlag()).isEqualTo(false);
        assertThat(beregningsgrunnlagPrStatusOgAndelDto.get(2).getSkalFastsetteGrunnlag()).isEqualTo(true);
    }

    @Test
    public void skalIkkeKasteExceptionNårDetFinnesHverkenAtFlEllerSnAndelOgDeIkkeFinnesSammenligningsgrunnlagPrStatus() throws Exception{
        //Arange
        ArbeidsgiverMedNavn arbeidsgiver = ArbeidsgiverMedNavn.virksomhet(ORGNR);
        BeregningsgrunnlagRestDto Beregningsgrunnlag = lagBeregningsgrunnlagMedAvvikUnder25ProsentMedKunSammenligningsgrunnlag();
        BeregningsgrunnlagPeriodeRestDto bgPeriode = buildBeregningsgrunnlagPeriode(Beregningsgrunnlag);
        byggAndelerAapDpVentelønn(bgPeriode, arbeidsgiver);
        lagBehandling(Beregningsgrunnlag, arbeidsgiver);
        var ytelsespesifiktGrunnlag = new ForeldrepengerGrunnlag(100, false);
        var iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt().medInntektsmeldinger(Collections.emptyList()).build();
        var input = new BeregningsgrunnlagRestInput(lagReferanseMedStp(behandlingReferanse), iayGrunnlag, AktivitetGradering.INGEN_GRADERING, List.of(), ytelsespesifiktGrunnlag).medBeregningsgrunnlagGrunnlag(grunnlag);
        //Act
        BeregningsgrunnlagPrStatusOgAndelDtoTjeneste.lagBeregningsgrunnlagPrStatusOgAndelDto(input,
            grunnlag.getBeregningsgrunnlag().get().getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList());
    }


    private BeregningsgrunnlagRestDto lagBeregningsgrunnlagMedAvvikOver25Prosent(SammenligningsgrunnlagType sammenligningsgrunnlagType){
        return BeregningsgrunnlagRestDto.builder()
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .leggTilSammenligningsgrunnlag(SammenligningsgrunnlagPrStatusRestDto.builder()
                .medAvvikPromilleNy(AVVIK_OVER_25_PROSENT)
                .medRapportertPrÅr(RAPPORTERT_PR_AAR)
                .medSammenligningsgrunnlagType(sammenligningsgrunnlagType)
                .medSammenligningsperiode(SAMMENLIGNING_FOM, SAMMENLIGNING_TOM))
            .build();
    }

    private BeregningsgrunnlagRestDto lagBeregningsgrunnlagMedAvvikOver25ProsentMedKunSammenligningsgrunnlag(){
        var beregningsgrunnlag =  BeregningsgrunnlagRestDto.builder()
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .build();

        SammenligningsgrunnlagRestDto.builder()
            .medSammenligningsperiode(SAMMENLIGNING_FOM, SAMMENLIGNING_TOM)
            .medRapportertPrÅr(RAPPORTERT_PR_AAR)
            .medAvvikPromilleNy(AVVIK_OVER_25_PROSENT).build(beregningsgrunnlag);

        return beregningsgrunnlag;
    }

    private BeregningsgrunnlagRestDto lagBeregningsgrunnlagUtenSammenligningsgrunnlag(){
        var beregningsgrunnlag =  BeregningsgrunnlagRestDto.builder()
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .build();

        return beregningsgrunnlag;
    }

    private BeregningsgrunnlagRestDto lagBeregningsgrunnlagMedAvvikUnder25ProsentMedKunSammenligningsgrunnlag(){
        var beregningsgrunnlag =  BeregningsgrunnlagRestDto.builder()
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .build();

        SammenligningsgrunnlagRestDto.builder()
            .medSammenligningsperiode(SAMMENLIGNING_FOM, SAMMENLIGNING_TOM)
            .medRapportertPrÅr(RAPPORTERT_PR_AAR)
            .medAvvikPromilleNy(AVVIK_UNDER_25_PROSENT).build(beregningsgrunnlag);

        return beregningsgrunnlag;
    }

    private void byggAndelAt(BeregningsgrunnlagPeriodeRestDto beregningsgrunnlagPeriode, ArbeidsgiverMedNavn arbeidsgiver, Long andelsNr) {
        BGAndelArbeidsforholdRestDto.Builder bga = BGAndelArbeidsforholdRestDto
            .builder()
            .medArbeidsperiodeFom(LocalDate.now().minusYears(1))
            .medArbeidsperiodeTom(LocalDate.now().plusYears(2))
            .medArbeidsgiver(arbeidsgiver);

        BeregningsgrunnlagPrStatusOgAndelRestDto.kopier()
            .medBGAndelArbeidsforhold(bga)
            .medInntektskategori(INNTEKTSKATEGORI)
            .medAndelsnr(andelsNr)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medBeregningsperiode(ANDEL_FOM, ANDEL_TOM)
            .medBeregnetPrÅr(BRUTTO_PR_AAR)
            .medAvkortetPrÅr(AVKORTET_PR_AAR)
            .medRedusertPrÅr(REDUSERT_PR_AAR)
            .medOverstyrtPrÅr(OVERSTYRT_PR_AAR)
            .build(beregningsgrunnlagPeriode);
    }

    private void byggAndelFl(BeregningsgrunnlagPeriodeRestDto beregningsgrunnlagPeriode, ArbeidsgiverMedNavn arbeidsgiver, Long andelsNr) {
        BGAndelArbeidsforholdRestDto.Builder bga = BGAndelArbeidsforholdRestDto
            .builder()
            .medArbeidsperiodeFom(LocalDate.now().minusYears(1))
            .medArbeidsperiodeTom(LocalDate.now().plusYears(2))
            .medArbeidsgiver(arbeidsgiver);

        BeregningsgrunnlagPrStatusOgAndelRestDto.kopier()
            .medBGAndelArbeidsforhold(bga)
            .medInntektskategori(INNTEKTSKATEGORI)
            .medAndelsnr(andelsNr)
            .medAktivitetStatus(AktivitetStatus.FRILANSER)
            .medBeregningsperiode(ANDEL_FOM, ANDEL_TOM)
            .medBeregnetPrÅr(BRUTTO_PR_AAR)
            .build(beregningsgrunnlagPeriode);
    }

    private void byggAndelSn(BeregningsgrunnlagPeriodeRestDto beregningsgrunnlagPeriode, ArbeidsgiverMedNavn arbeidsgiver, Boolean erNyIArbeidslivet, Long andelsNr) {
        BGAndelArbeidsforholdRestDto.Builder bga = BGAndelArbeidsforholdRestDto
            .builder()
            .medArbeidsperiodeFom(LocalDate.now().minusYears(1))
            .medArbeidsperiodeTom(LocalDate.now().plusYears(2))
            .medArbeidsgiver(arbeidsgiver);

        BeregningsgrunnlagPrStatusOgAndelRestDto.kopier()
            .medBGAndelArbeidsforhold(bga)
            .medInntektskategori(INNTEKTSKATEGORI)
            .medAndelsnr(andelsNr)
            .medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
            .medBeregningsperiode(ANDEL_FOM, ANDEL_TOM)
            .medBeregnetPrÅr(BRUTTO_PR_AAR)
            .medAvkortetPrÅr(AVKORTET_PR_AAR)
            .medRedusertPrÅr(REDUSERT_PR_AAR)
            .medOverstyrtPrÅr(OVERSTYRT_PR_AAR)
            .medNyIArbeidslivet(erNyIArbeidslivet)
            .build(beregningsgrunnlagPeriode);
    }

    private void byggAndelerAapDpVentelønn(BeregningsgrunnlagPeriodeRestDto beregningsgrunnlagPeriode, ArbeidsgiverMedNavn arbeidsgiver) {
        BGAndelArbeidsforholdRestDto.Builder bga = BGAndelArbeidsforholdRestDto
            .builder()
            .medArbeidsperiodeFom(LocalDate.now().minusYears(1))
            .medArbeidsperiodeTom(LocalDate.now().plusYears(2))
            .medArbeidsgiver(arbeidsgiver);

        BeregningsgrunnlagPrStatusOgAndelRestDto.kopier()
            .medBGAndelArbeidsforhold(bga)
            .medInntektskategori(INNTEKTSKATEGORI)
            .medAndelsnr(ANDELSNR)
            .medAktivitetStatus(AktivitetStatus.DAGPENGER)
            .build(beregningsgrunnlagPeriode);

        BeregningsgrunnlagPrStatusOgAndelRestDto.kopier()
            .medBGAndelArbeidsforhold(bga)
            .medInntektskategori(INNTEKTSKATEGORI)
            .medAndelsnr(ANDELSNR+1)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSAVKLARINGSPENGER)
            .build(beregningsgrunnlagPeriode);

        BeregningsgrunnlagPrStatusOgAndelRestDto.kopier()
            .medBGAndelArbeidsforhold(bga)
            .medInntektskategori(INNTEKTSKATEGORI)
            .medAndelsnr(ANDELSNR+2)
            .medAktivitetStatus(AktivitetStatus.VENTELØNN_VARTPENGER)
            .build(beregningsgrunnlagPeriode);
    }

    private BeregningsgrunnlagRestDto lagBehandling(BeregningsgrunnlagRestDto beregningsgrunnlag, ArbeidsgiverMedNavn arbeidsgiver) {
        BeregningAktivitetAggregatRestDto beregningAktiviteter = lagBeregningAktiviteter(arbeidsgiver);
        BeregningsgrunnlagGrunnlagRestDto beregningsgrunnlagGrunnlag = BeregningsgrunnlagGrunnlagRestDtoBuilder.oppdatere(Optional.empty())
            .medRegisterAktiviteter(beregningAktiviteter)
            .medBeregningsgrunnlag(beregningsgrunnlag).build(BeregningsgrunnlagTilstand.OPPRETTET);

        this.grunnlag = beregningsgrunnlagGrunnlag;
        this.opptjeningAktiviteter = OpptjeningAktiviteterDto.fraOrgnr(OpptjeningAktivitetType.ARBEID, Periode.of( SKJÆRINGSTIDSPUNKT.minusMonths(10), SKJÆRINGSTIDSPUNKT.minusDays(1)), arbeidsgiver.getOrgnr());

        return beregningsgrunnlag;
    }

    private BeregningAktivitetAggregatRestDto lagBeregningAktiviteter(ArbeidsgiverMedNavn arbeidsgiver) {
        return lagBeregningAktiviteter(BeregningAktivitetAggregatRestDto.builder(), arbeidsgiver);
    }

    private BeregningAktivitetAggregatRestDto lagBeregningAktiviteter(BeregningAktivitetAggregatRestDto.Builder builder, ArbeidsgiverMedNavn arbeidsgiver) {
        return builder.medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT)
            .leggTilAktivitet(BeregningAktivitetRestDto.builder()
                .medArbeidsgiver(arbeidsgiver)
                .medOpptjeningAktivitetType(OpptjeningAktivitetType.ARBEID)
                .medPeriode(Intervall.fraOgMedTilOgMed(ANDEL_FOM, ANDEL_TOM))
                .build())
            .build();
    }

    private BehandlingReferanse lagReferanseMedStp(BehandlingReferanse behandlingReferanse) {
        Skjæringstidspunkt skjæringstidspunkt = Skjæringstidspunkt.builder().medUtledetSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT).build();
        return behandlingReferanse.medSkjæringstidspunkt(skjæringstidspunkt);
    }

    private BeregningsgrunnlagPeriodeRestDto buildBeregningsgrunnlagPeriode(BeregningsgrunnlagRestDto beregningsgrunnlag) {
        return BeregningsgrunnlagPeriodeRestDto.builder()
            .medBeregningsgrunnlagPeriode(ANDEL_FOM, null)
            .build(beregningsgrunnlag);
    }
}
