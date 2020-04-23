package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.YtelsesSpesifiktGrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.frisinn.FrisinnGrunnlag;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittFrilansDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittOpptjeningDto;

@ApplicationScoped
@FagsakYtelseTypeRef("FRISINN")
public class FrisinnGrunnlagMapper implements YtelsesspesifikkRegelMapper {

    @Override
    public YtelsesSpesifiktGrunnlag map(BeregningsgrunnlagDto beregningsgrunnlagDto, BeregningsgrunnlagInput input) {
        Boolean erNyoppstartetFrilans = input.getIayGrunnlag().getOppgittOpptjening().flatMap(OppgittOpptjeningDto::getFrilans).map(OppgittFrilansDto::getErNyoppstartet).orElse(false);
        return new FrisinnGrunnlag(erNyoppstartetFrilans);
    }

}
