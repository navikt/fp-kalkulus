package no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.VurderRefusjonAndelBeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.VurderRefusjonBeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringerDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.typer.AktørId;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand;

public class VurderRefusjonBeregningsgrunnlagHåndterer {

    private VurderRefusjonBeregningsgrunnlagHåndterer() {
        // skjul
    }

    public static BeregningsgrunnlagGrunnlagDto håndter(VurderRefusjonBeregningsgrunnlagDto dto, BeregningsgrunnlagInput input) {
        List<BeregningRefusjonOverstyringDto> eksisterendeOverstyringer = input.getBeregningsgrunnlagGrunnlag().getRefusjonOverstyringer()
                .map(BeregningRefusjonOverstyringerDto::getRefusjonOverstyringer)
                .orElse(Collections.emptyList());
        BeregningRefusjonOverstyringerDto.Builder nyttRefusjonAggregat = BeregningRefusjonOverstyringerDto.builder();
        Map<Arbeidsgiver, List<VurderRefusjonAndelBeregningsgrunnlagDto>> vurderingerSortertPåAG = dto.getAndeler().stream()
                .collect(Collectors.groupingBy(VurderRefusjonBeregningsgrunnlagHåndterer::lagArbeidsgiver));

        lagListeMedRefusjonOverstyringer(vurderingerSortertPåAG, eksisterendeOverstyringer)
                .forEach(nyttRefusjonAggregat::leggTilOverstyring);

        BeregningsgrunnlagGrunnlagDtoBuilder grunnlagBuilder = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());
        grunnlagBuilder.medRefusjonOverstyring(nyttRefusjonAggregat.build());
        return grunnlagBuilder.build(BeregningsgrunnlagTilstand.VURDERT_REFUSJON_UT);
    }

    private static List<BeregningRefusjonOverstyringDto> lagListeMedRefusjonOverstyringer(Map<Arbeidsgiver, List<VurderRefusjonAndelBeregningsgrunnlagDto>> vurderingerSortertPåAG, List<BeregningRefusjonOverstyringDto> eksisterendeOverstyringer) {
        List<BeregningRefusjonOverstyringDto> liste = new ArrayList<>();
        for (Map.Entry<Arbeidsgiver, List<VurderRefusjonAndelBeregningsgrunnlagDto>> entry : vurderingerSortertPåAG.entrySet()) {
            Arbeidsgiver ag = entry.getKey();
            Optional<BeregningRefusjonOverstyringDto> eksisterendeOverstyringForAG = finnKorrektOverstyring(ag, eksisterendeOverstyringer);
            if (eksisterendeOverstyringForAG.isPresent()) {
                BeregningRefusjonOverstyringDto eksisterendeOverstyring = eksisterendeOverstyringForAG.get();

                List<BeregningRefusjonPeriodeDto> nyeRefusjonsperioder = lagListeMedRefusjonsperioder(entry.getValue());
                List<BeregningRefusjonPeriodeDto> eksisterendeOverstyrtePerioder = eksisterendeOverstyring.getRefusjonPerioder()
                        .stream()
                        .map(rp -> new BeregningRefusjonPeriodeDto(rp.getArbeidsforholdRef(), rp.getStartdatoRefusjon()))
                        .collect(Collectors.toList());
                nyeRefusjonsperioder.addAll(eksisterendeOverstyrtePerioder);

                BeregningRefusjonOverstyringDto oppdatertOverstyring = new BeregningRefusjonOverstyringDto(ag,
                        eksisterendeOverstyring.getFørsteMuligeRefusjonFom().orElse(null), nyeRefusjonsperioder);
                liste.add(oppdatertOverstyring);
            } else {
                BeregningRefusjonOverstyringDto nyRefusjonOverstyring = lagNyOverstyring(ag, entry.getValue());
                liste.add(nyRefusjonOverstyring);
            }
        }
        return liste;
    }

    private static BeregningRefusjonOverstyringDto lagNyOverstyring(Arbeidsgiver ag, List<VurderRefusjonAndelBeregningsgrunnlagDto> fastsatteAndeler) {
        List<BeregningRefusjonPeriodeDto> refusjonsperioder = lagListeMedRefusjonsperioder(fastsatteAndeler);
        return new BeregningRefusjonOverstyringDto(ag, null, refusjonsperioder);
    }

    private static List<BeregningRefusjonPeriodeDto> lagListeMedRefusjonsperioder(List<VurderRefusjonAndelBeregningsgrunnlagDto> fastsattAndel) {
        return fastsattAndel.stream()
                .map(VurderRefusjonBeregningsgrunnlagHåndterer::lagRefusjonsperiode)
                .collect(Collectors.toList());
    }

    private static BeregningRefusjonPeriodeDto lagRefusjonsperiode(VurderRefusjonAndelBeregningsgrunnlagDto fastsattAndel) {
        return new BeregningRefusjonPeriodeDto(utledReferanse(fastsattAndel), fastsattAndel.getFastsattRefusjonFom());
    }

    private static InternArbeidsforholdRefDto utledReferanse(VurderRefusjonAndelBeregningsgrunnlagDto fastsattAndel) {
        return fastsattAndel.getInternArbeidsforholdRef() != null ? InternArbeidsforholdRefDto.ref(fastsattAndel.getInternArbeidsforholdRef()) : null;
    }

    private static Optional<BeregningRefusjonOverstyringDto> finnKorrektOverstyring(Arbeidsgiver ag, List<BeregningRefusjonOverstyringDto> refusjonOverstyringer) {
        return refusjonOverstyringer.stream().filter(os -> os.getArbeidsgiver().equals(ag)).findFirst();
    }

    private static Arbeidsgiver lagArbeidsgiver(VurderRefusjonAndelBeregningsgrunnlagDto fastsattAndel) {
        if (fastsattAndel.getArbeidsgiverOrgnr() != null) {
            return Arbeidsgiver.virksomhet(fastsattAndel.getArbeidsgiverOrgnr());
        } else {
            return Arbeidsgiver.person(new AktørId(fastsattAndel.getArbeidsgiverAktørId()));
        }
    }
}
