package no.nav.folketrygdloven.kalkulator.ytelse.frisinn;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittFrilansDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittFrilansInntektDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittOpptjeningDtoBuilder;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.FrisinnBehandlingType;

class VilkårTjenesteFRISINNTest {

    public static final LocalDate NOW = LocalDate.now();
    public static final LocalDate SØKNAD_FOM = NOW.plusMonths(1);

    @Test
    void skal_gi_avslag_om_søkt_fl_uten_flandel_og_søkt_avkortet_næring() {
        // Arrange
        BeregningsgrunnlagDto bg = lagBgMedAvkortetNæring();
        KoblingReferanseMock behandlingReferanse = new KoblingReferanseMock(LocalDate.now());
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = lagIayMedNæringOgFrilans();
        BeregningsgrunnlagInput input = new BeregningsgrunnlagInput(
                behandlingReferanse,
                iayGrunnlag, null,
                List.of(),
                new FrisinnGrunnlag(List.of(),
                        Collections.singletonList(
                                new FrisinnPeriode(
                                Intervall.fraOgMedTilOgMed(Intervall.TIDENES_BEGYNNELSE, Intervall.TIDENES_ENDE), true, true)), FrisinnBehandlingType.NY_SØKNADSPERIODE));

        // Act
        var beregningVilkårResultat = new VilkårTjenesteFRISINN().lagVilkårResultatFullføre(input, bg);

        // Assert
        boolean finnesAvslåttVilkår = beregningVilkårResultat.stream().anyMatch(vr -> !vr.getErVilkårOppfylt());
        assertThat(finnesAvslåttVilkår).isTrue();
    }

    private BeregningsgrunnlagDto lagBgMedAvkortetNæring() {
        BeregningsgrunnlagDto bg = BeregningsgrunnlagDto.builder()
                .medSkjæringstidspunkt(NOW)
                .medGrunnbeløp(BigDecimal.TEN)
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE))
                .build();
        BeregningsgrunnlagPeriodeDto periode = BeregningsgrunnlagPeriodeDto.builder()
                .medBeregningsgrunnlagPeriode(SØKNAD_FOM, SØKNAD_FOM.plusMonths(1))
                .build(bg);
        BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
                .medAndelsnr(1L)
                .medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
                .medBeregnetPrÅr(BigDecimal.valueOf(100_000))
                .medRedusertPrÅr(BigDecimal.ZERO)
                .medRedusertBrukersAndelPrÅr(BigDecimal.ZERO)
                .medRedusertRefusjonPrÅr(BigDecimal.ZERO)
                .medAvkortetPrÅr(BigDecimal.ZERO)
                .build(periode);
        return bg;
    }

    private InntektArbeidYtelseGrunnlagDto lagIayMedNæringOgFrilans() {
        Intervall søknadsperiode = Intervall.fraOgMedTilOgMed(SØKNAD_FOM, SØKNAD_FOM.plusMonths(1));
        return InntektArbeidYtelseGrunnlagDtoBuilder.nytt()
                    .medOppgittOpptjening(OppgittOpptjeningDtoBuilder.ny()
                            .leggTilEgneNæring(OppgittOpptjeningDtoBuilder.EgenNæringBuilder.ny()
                                    .medPeriode(søknadsperiode)
                                    .medBruttoInntekt(BigDecimal.valueOf(100_000)))
                            .leggTilFrilansOpplysninger(
                                    new OppgittFrilansDto(false,
                                    List.of(new OppgittFrilansInntektDto(søknadsperiode, BigDecimal.TEN)))))
                            .build();
    }
}
