package no.nav.folketrygdloven.kalkulus.mappers;

import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.behandling.Skjæringstidspunkt;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.felles.v1.KalkulatorInputDto;
import no.nav.folketrygdloven.kalkulus.typer.AktørId;

public class MapTilGUIInputFraKalkulator {


    public static BeregningsgrunnlagGUIInput mapFraKalkulatorInput(KoblingEntitet kobling,
                                                                   KalkulatorInputDto input,
                                                                   BeregningsgrunnlagGrunnlagEntitet beregningsgrunnlagGrunnlagEntitet) {
        var skjæringstidspunkt = Skjæringstidspunkt.builder()
                .medFørsteUttaksdato(input.getSkjæringstidspunkt())
                .medSkjæringstidspunktOpptjening(input.getSkjæringstidspunkt()).build();
        var ref = KoblingReferanse.fra(
                kobling.getYtelseType(),
                new AktørId(kobling.getAktørId().getId()),
                kobling.getId(),
                kobling.getKoblingReferanse().getReferanse(),
                Optional.empty(),
                skjæringstidspunkt);
        var iayGrunnlagMappet = MapIAYTilKalulator.mapGrunnlag(input.getIayGrunnlag());
        return new BeregningsgrunnlagGUIInput(
                ref,
                iayGrunnlagMappet,
                MapFraKalkulator.mapKravperioder(input.getRefusjonskravPrArbeidsforhold(), input.getRefusjonskravDatoer(), input.getIayGrunnlag(), input.getSkjæringstidspunkt()),
                MapFraKalkulator.mapYtelsespesifiktGrunnlag(kobling.getYtelseType(), input, Optional.ofNullable(beregningsgrunnlagGrunnlagEntitet)));
    }

}
