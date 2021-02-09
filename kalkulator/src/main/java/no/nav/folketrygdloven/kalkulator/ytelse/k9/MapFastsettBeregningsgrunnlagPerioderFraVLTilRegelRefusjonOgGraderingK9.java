package no.nav.folketrygdloven.kalkulator.ytelse.k9;

import java.time.LocalDate;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.RefusjonskravFrist;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringerDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.ytelse.utbgradytelse.MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelRefusjonOgGraderingUtbgrad;

@FagsakYtelseTypeRef("OMP")
@FagsakYtelseTypeRef("PSB")
@ApplicationScoped
public class MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelRefusjonOgGraderingK9 extends MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelRefusjonOgGraderingUtbgrad {


    /**
     * Skal ikkje vurdere refusjonskravfrist i kalkulus for k9-ytelser. Dette vurderes felles for søknadsfrist.
     *
     */
    @Override
    protected Optional<RefusjonskravFrist> mapRefusjonskravFrist() {
        return Optional.empty();
    }

    /**
     * Returerer ingen dato fordi dette ikke er relevant for k9. Søknadsfrist vurderes utenfor og alle krav skal godkjennes.
     *
     * @param ya Yrkesaktiviet
     * @param refusjonOverstyringer Refusjonsoverstyringer
     * @return Første gyldige dato med refusjon
     */
    @Override
    protected Optional<LocalDate> mapFørsteGyldigeDatoForRefusjon(YrkesaktivitetDto ya, Optional<BeregningRefusjonOverstyringerDto> refusjonOverstyringer) {
        return Optional.empty();
    }


}
