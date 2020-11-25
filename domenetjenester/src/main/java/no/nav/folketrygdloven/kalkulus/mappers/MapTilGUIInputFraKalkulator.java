package no.nav.folketrygdloven.kalkulus.mappers;

import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectReader;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.behandling.Skjæringstidspunkt;
import no.nav.folketrygdloven.kalkulator.modell.typer.AktørId;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.KalkulatorInputEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.felles.v1.KalkulatorInputDto;

public class MapTilGUIInputFraKalkulator {

    private static final ObjectReader READER = JsonMapper.getMapper().reader();


    public static BeregningsgrunnlagGUIInput map(KoblingEntitet kobling,
                                                 KalkulatorInputEntitet kalkulatorInputEntitet,
                                                 Optional<BeregningsgrunnlagGrunnlagEntitet> beregningsgrunnlagGrunnlagEntitet) {
        String json = kalkulatorInputEntitet.getInput();
        KalkulatorInputDto input = null;

        try {
            input = READER.forType(KalkulatorInputDto.class).readValue(json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        if (input != null) {
            return mapFraKalkulatorInput(kobling, input, beregningsgrunnlagGrunnlagEntitet);
        }

        throw new IllegalStateException("Klarte ikke lage input for kobling med id:" + kalkulatorInputEntitet.getKoblingId());
    }


    private static BeregningsgrunnlagGUIInput mapFraKalkulatorInput(KoblingEntitet kobling, KalkulatorInputDto input, Optional<BeregningsgrunnlagGrunnlagEntitet> beregningsgrunnlagGrunnlagEntitet) {
        var skjæringstidspunkt = Skjæringstidspunkt.builder()
                .medFørsteUttaksdato(input.getSkjæringstidspunkt())
                .medSkjæringstidspunktOpptjening(input.getSkjæringstidspunkt()).build();
        var ref = KoblingReferanse.fra(
                FagsakYtelseType.fraKode(kobling.getYtelseTyperKalkulusStøtter().getKode()),
                new AktørId(kobling.getAktørId().getId()),
                kobling.getId(),
                kobling.getKoblingReferanse().getReferanse(),
                Optional.empty(),
                skjæringstidspunkt);
        var aktivitetGradering = input.getAktivitetGradering();
        var refusjonskravDatoer = input.getRefusjonskravDatoer();
        var iayGrunnlagMappet = MapIAYTilKalulator.mapGrunnlag(input.getIayGrunnlag());
        return new BeregningsgrunnlagGUIInput(
                ref,
                iayGrunnlagMappet,
                aktivitetGradering != null ? MapFraKalkulator.mapFraDto(aktivitetGradering) : null,
                MapFraKalkulator.mapFraDto(refusjonskravDatoer),
                MapFraKalkulator.mapFraDto(kobling.getYtelseTyperKalkulusStøtter(), input, iayGrunnlagMappet, beregningsgrunnlagGrunnlagEntitet));
    }

}
