package no.nav.folketrygdloven.kalkulator.endringsresultat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;


public class LagEnAndelTjeneste implements LagAndelTjeneste {

    private static final String ORGNR = "987123987";
    private static final List<InternArbeidsforholdRefDto> ARBEIDSFORHOLDLISTE = List.of(InternArbeidsforholdRefDto.nyRef(), InternArbeidsforholdRefDto.nyRef(),
            InternArbeidsforholdRefDto.nyRef(), InternArbeidsforholdRefDto.nyRef());

    @Override
    public void lagAndeler(BeregningsgrunnlagPeriodeDto periode, boolean medOppjustertDagsat, boolean skalDeleAndelMellomArbeidsgiverOgBruker) {
        Dagsatser ds = new Dagsatser(medOppjustertDagsat, skalDeleAndelMellomArbeidsgiverOgBruker);
        BGAndelArbeidsforholdDto.Builder bga = BGAndelArbeidsforholdDto
            .builder()
            .medArbeidsgiver(Arbeidsgiver.virksomhet(ORGNR))
            .medArbeidsforholdRef(ARBEIDSFORHOLDLISTE.get(0))
            .medArbeidsperiodeFom(LocalDate.now().minusYears(1))
            .medArbeidsperiodeTom(LocalDate.now().plusYears(2));
        BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
            .medBGAndelArbeidsforhold(bga)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medBeregnetPrÅr(BigDecimal.valueOf(240000))
            .medRedusertBrukersAndelPrÅr(ds.getDagsatsBruker())
            .medRedusertRefusjonPrÅr(ds.getDagsatsArbeidstaker())
            .build(periode);
    }
}
