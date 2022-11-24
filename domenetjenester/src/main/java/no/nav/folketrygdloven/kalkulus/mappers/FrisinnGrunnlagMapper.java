package no.nav.folketrygdloven.kalkulus.mappers;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittOpptjeningDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulator.ytelse.frisinn.FrisinnGrunnlag;
import no.nav.folketrygdloven.kalkulator.ytelse.frisinn.FrisinnPeriode;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.kodeverk.FrisinnBehandlingType;

class FrisinnGrunnlagMapper {


    static FrisinnGrunnlag mapFrisinnGrunnlag(no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto iayGrunnlag, Optional<BeregningsgrunnlagGrunnlagEntitet> beregningsgrunnlagGrunnlagEntitet, no.nav.folketrygdloven.kalkulus.beregning.v1.FrisinnGrunnlag frisinnGrunnlag) {
        List<FrisinnPeriode> frisinnPerioder = mapFraKontrakt(frisinnGrunnlag, iayGrunnlag.getOppgittOpptjening());
        return new FrisinnGrunnlag(UtbetalingsgradMapperFRISINN.map(iayGrunnlag, beregningsgrunnlagGrunnlagEntitet, frisinnPerioder),
                frisinnPerioder, frisinnGrunnlag.getFrisinnBehandlingType() == null ? FrisinnBehandlingType.NY_SØKNADSPERIODE
                : frisinnGrunnlag.getFrisinnBehandlingType());
    }

    private static List<FrisinnPeriode> mapFraKontrakt(no.nav.folketrygdloven.kalkulus.beregning.v1.FrisinnGrunnlag frisinnGrunnlag,
                                                       Optional<OppgittOpptjeningDto> oppgittOpptjening) {
        // FIXME (Er tilfellet ved gamle saker på gamel kontrakt)
        if (frisinnGrunnlag.getPerioderMedSøkerInfo() == null) {
            return MapGammeltFrisinngrunnlag.map(frisinnGrunnlag, oppgittOpptjening);
        }

        return frisinnGrunnlag.getPerioderMedSøkerInfo().stream()
                .map(p -> new FrisinnPeriode(mapPeriode(p.getPeriode()), p.getSøkerFrilansIPeriode(),
                        p.getSøkerNæringIPeriode()))
                .collect(Collectors.toList());
    }


    private static Intervall mapPeriode(Periode periode) {
        return Intervall.fraOgMedTilOgMed(periode.getFom(), periode.getTom());
    }



}
