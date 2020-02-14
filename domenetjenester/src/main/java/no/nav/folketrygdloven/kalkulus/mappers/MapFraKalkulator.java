package no.nav.folketrygdloven.kalkulus.mappers;


import static no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.YtelseTyperKalkulusStøtter.FORELDREPENGER;
import static no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.YtelseTyperKalkulusStøtter.SVANGERSKAPSPENGER;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectReader;

import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.ForeldrepengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.gradering.AktivitetGradering;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.behandling.BehandlingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.behandling.BehandlingStatus;
import no.nav.folketrygdloven.kalkulator.modell.behandling.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulator.modell.behandling.Skjæringstidspunkt;
import no.nav.folketrygdloven.kalkulator.modell.typer.AktørId;
import no.nav.folketrygdloven.kalkulus.beregning.v1.AktivitetGraderingDto;
import no.nav.folketrygdloven.kalkulus.beregning.v1.RefusjonskravDatoDto;
import no.nav.folketrygdloven.kalkulus.beregning.v1.YtelsespesifiktGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.KalkulatorInputEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.YtelseTyperKalkulusStøtter;
import no.nav.folketrygdloven.kalkulus.felles.v1.KalkulatorInputDto;
import no.nav.folketrygdloven.kalkulus.iay.v1.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.opptjening.v1.OpptjeningAktiviteterDto;

public class MapFraKalkulator {

    private static final ObjectReader READER = JsonMapper.getMapper().reader();


    public BeregningsgrunnlagInput mapFraKalkulatorInputEntitetTilBeregningsgrunnlagInput(KoblingEntitet kobling, KalkulatorInputEntitet kalkulatorInputEntitet) {
        String json = kalkulatorInputEntitet.getInput();
        KalkulatorInputDto input = null;

        try {
            input = READER.forType(KalkulatorInputDto.class).readValue(json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        if (input != null) {
            return mapFraKalkulatorInputTilBeregningsgrunnlagInput(kobling, input);
        }

        throw new IllegalStateException("Klarte ikke lage input for kobling med id:" + kalkulatorInputEntitet.getKoblingId());
    }


    public BeregningsgrunnlagInput mapFraKalkulatorInputTilBeregningsgrunnlagInput(KoblingEntitet kobling, KalkulatorInputDto input) {
        var koblingId = kobling.getId();
        var skjæringstidspunkt = input.getSkjæringstidspunkt();

        FagsakYtelseType foreldrepenger = FagsakYtelseType.FORELDREPENGER;
        AktørId aktørId = new AktørId(kobling.getAktørId().getId());
        BehandlingStatus avsluttet = BehandlingStatus.AVSLUTTET;
        Skjæringstidspunkt build = Skjæringstidspunkt.builder().medSkjæringstidspunktOpptjening(skjæringstidspunkt).build();

        var ref = BehandlingReferanse.fra(foreldrepenger, aktørId, koblingId, kobling.getKoblingReferanse().getReferanse(), Optional.empty(), avsluttet, build);

        AktivitetGraderingDto aktivitetGradering = input.getAktivitetGradering();
        var iayGrunnlag = input.getIayGrunnlag();
        OpptjeningAktiviteterDto opptjeningAktiviteter = input.getOpptjeningAktiviteter();
        List<RefusjonskravDatoDto> refusjonskravDatoer = input.getRefusjonskravDatoer();

        return new BeregningsgrunnlagInput(ref,
                mapFraDto(iayGrunnlag, aktørId),
                mapFraDto(opptjeningAktiviteter),
                mapFraDto(aktivitetGradering),
                mapFraDto(refusjonskravDatoer),
                mapFraDto(kobling.getYtelseTyperKalkulusStøtter(), input.getYtelsespesifiktGrunnlag()));
    }

    private YtelsespesifiktGrunnlag mapFraDto(YtelseTyperKalkulusStøtter ytelseType, YtelsespesifiktGrunnlagDto ytelsespesifiktGrunnlag) {
        YtelseTyperKalkulusStøtter yt = YtelseTyperKalkulusStøtter.fraKode(ytelseType.getKode());
        if (FORELDREPENGER == yt) {
            return new ForeldrepengerGrunnlag(ytelsespesifiktGrunnlag.getDekningsgrad().intValue(), ytelsespesifiktGrunnlag.getKvalifisererTilBesteberegning());
        } else if (SVANGERSKAPSPENGER == yt) {
            throw new IllegalStateException("Støtter ikke denne ennå");
        }
        return null;
    }

    private List<no.nav.folketrygdloven.kalkulator.modell.iay.RefusjonskravDatoDto> mapFraDto(List<RefusjonskravDatoDto> refusjonskravDatoer) {
        return null;
    }

    private AktivitetGradering mapFraDto(AktivitetGraderingDto aktivitetGradering) {
        return null;
    }

    private no.nav.folketrygdloven.kalkulator.opptjening.OpptjeningAktiviteterDto mapFraDto(OpptjeningAktiviteterDto opptjeningAktiviteter) {
        return null;
    }

    private no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto mapFraDto(InntektArbeidYtelseGrunnlagDto iayGrunnlag, AktørId aktørId) {
        return null;
    }
}
