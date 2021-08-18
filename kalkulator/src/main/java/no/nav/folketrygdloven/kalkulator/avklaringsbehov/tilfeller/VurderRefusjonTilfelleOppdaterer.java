package no.nav.folketrygdloven.kalkulator.avklaringsbehov.tilfeller;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.FaktaOmBeregningTilfelleRef;
import no.nav.folketrygdloven.kalkulator.felles.InntektsmeldingMedRefusjonTjeneste;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FaktaBeregningLagreDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.RefusjonskravPrArbeidsgiverVurderingDto;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringerDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.typer.AktørId;
import no.nav.folketrygdloven.kalkulus.typer.OrgNummer;

@ApplicationScoped
@FaktaOmBeregningTilfelleRef("VURDER_REFUSJONSKRAV_SOM_HAR_KOMMET_FOR_SENT")
public class VurderRefusjonTilfelleOppdaterer implements FaktaOmBeregningTilfelleOppdaterer {


    @Override
    public void oppdater(FaktaBeregningLagreDto dto, Optional<BeregningsgrunnlagDto> forrigeBg, BeregningsgrunnlagInput input, BeregningsgrunnlagGrunnlagDtoBuilder grunnlagBuilder) {
        List<RefusjonskravPrArbeidsgiverVurderingDto> gyldighetPrArbeidsgiver = dto.getRefusjonskravGyldighet();
        LocalDate frist = input.getBeregningsgrunnlag().getSkjæringstidspunkt();
        BeregningRefusjonOverstyringerDto beregningRefusjonOverstyringer = map(gyldighetPrArbeidsgiver, frist, input);
        grunnlagBuilder.medRefusjonOverstyring(beregningRefusjonOverstyringer);
    }

    private BeregningRefusjonOverstyringerDto map(List<RefusjonskravPrArbeidsgiverVurderingDto> dto, LocalDate frist, BeregningsgrunnlagInput input) {
        BeregningRefusjonOverstyringerDto.Builder builder = BeregningRefusjonOverstyringerDto.builder();
        for (RefusjonskravPrArbeidsgiverVurderingDto vurderingDto : dto) {
            Arbeidsgiver arbeidsgiver = finnArbeidsgiver(vurderingDto.getArbeidsgiverId());
            if (vurderingDto.isSkalUtvideGyldighet()) {
                builder.leggTilOverstyring(new BeregningRefusjonOverstyringDto(arbeidsgiver, frist));
            } else {
                Optional<LocalDate> førsteLovligeDato = InntektsmeldingMedRefusjonTjeneste.finnFørsteLovligeDatoForRefusjonFørOverstyring(input, arbeidsgiver);
                førsteLovligeDato.ifPresent(dato -> builder.leggTilOverstyring(new BeregningRefusjonOverstyringDto(arbeidsgiver, dato)));
            }
        }
        return builder.build();
    }

    private Arbeidsgiver finnArbeidsgiver(String identifikator) {
        if (OrgNummer.erGyldigOrgnr(identifikator)) {
            return Arbeidsgiver.virksomhet(identifikator);
        }
        return Arbeidsgiver.fra(new AktørId(identifikator));
    }

}
