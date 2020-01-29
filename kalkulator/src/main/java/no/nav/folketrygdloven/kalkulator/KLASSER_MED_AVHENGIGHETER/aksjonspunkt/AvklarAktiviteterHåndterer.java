package no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt;

import java.util.List;

import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.AvklarteAktiviteterDto;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.BeregningsaktivitetLagreDto;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetOverstyringDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetOverstyringerDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningAktivitetHandlingType;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand;

public class AvklarAktiviteterHåndterer {

    private AvklarAktiviteterHåndterer() {
        // Engler daler ned i skjul
    }

    public static BeregningsgrunnlagGrunnlagDto håndter(AvklarteAktiviteterDto dto, BeregningsgrunnlagInput input) {
        BeregningsgrunnlagGrunnlagDtoBuilder grunnlagBuilder = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());

        List<BeregningsaktivitetLagreDto> handlingListe = dto.getBeregningsaktivitetLagreDtoList();
        BeregningAktivitetAggregatDto registerAktiviteter = input.getBeregningsgrunnlagGrunnlag().getRegisterAktiviteter();
        BeregningAktivitetAggregatDto saksbehandledeAktiviteter = SaksbehandletBeregningsaktivitetTjeneste.lagSaksbehandletVersjon(registerAktiviteter, handlingListe);
        grunnlagBuilder.medSaksbehandletAktiviteter(saksbehandledeAktiviteter);
        return grunnlagBuilder.build(BeregningsgrunnlagTilstand.FASTSATT_BEREGNINGSAKTIVITETER);
    }

    public static BeregningsgrunnlagGrunnlagDto håndterOverstyring(List<BeregningsaktivitetLagreDto> aktiviteter, BeregningsgrunnlagInput input) {
        BeregningsgrunnlagGrunnlagDtoBuilder grunnlagBuilder = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());

        BeregningAktivitetOverstyringerDto.Builder overstyringAggregatBuilder = BeregningAktivitetOverstyringerDto.builder();
        aktiviteter.forEach(overstyrtDto -> {
                BeregningAktivitetOverstyringDto overstyring = lagOverstyring(overstyrtDto, input.getBeregningsgrunnlagGrunnlag().getRegisterAktiviteter()
                    .getBeregningAktiviteter().stream()
                    .filter(a -> a.getNøkkel().equals(overstyrtDto.getNøkkel()))
                    .findFirst()
                    .map(BeregningAktivitetDto::getArbeidsgiver)
                    .orElseThrow(IllegalStateException::new));
                overstyringAggregatBuilder.leggTilOverstyring(overstyring);
            });
        return grunnlagBuilder.medOverstyring(overstyringAggregatBuilder.build()).build(BeregningsgrunnlagTilstand.FASTSATT_BEREGNINGSAKTIVITETER);
    }

    private static BeregningAktivitetOverstyringDto lagOverstyring(BeregningsaktivitetLagreDto overstyrtDto, Arbeidsgiver arbeidsgiver) {
        return BeregningAktivitetOverstyringDto.builder()
            .medHandling(mapTilHandlingType(overstyrtDto))
            .medArbeidsforholdRef(InternArbeidsforholdRefDto.ref(overstyrtDto.getArbeidsforholdRef()))
            .medArbeidsgiver(arbeidsgiver)
            .medPeriode(overstyrtDto.getTom() == null ? Intervall.fraOgMed(overstyrtDto.getFom()) : Intervall.fraOgMedTilOgMed(overstyrtDto.getFom(), overstyrtDto.getTom()))
            .medOpptjeningAktivitetType(overstyrtDto.getOpptjeningAktivitetType())
            .build();
    }

    private static BeregningAktivitetHandlingType mapTilHandlingType(BeregningsaktivitetLagreDto overstyrtDto) {
        if (overstyrtDto.getSkalBrukes()) {
            return BeregningAktivitetHandlingType.BENYTT;
        } else {
            return BeregningAktivitetHandlingType.IKKE_BENYTT;
        }
    }

}
