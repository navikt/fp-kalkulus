package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.inject.Instance;

import no.nav.folketrygdloven.kalkulator.guitjenester.fakta.FaktaOmBeregningTilfelleDtoTjeneste;
import no.nav.folketrygdloven.kalkulator.guitjenester.fakta.KortvarigeArbeidsforholdDtoTjeneste;
import no.nav.folketrygdloven.kalkulator.guitjenester.fakta.KunYtelseDtoTjeneste;
import no.nav.folketrygdloven.kalkulator.guitjenester.fakta.NyOppstartetFLDtoTjeneste;
import no.nav.folketrygdloven.kalkulator.guitjenester.fakta.VurderATFLISammeOrgDtoTjeneste;
import no.nav.folketrygdloven.kalkulator.guitjenester.fakta.VurderBesteberegningTilfelleDtoTjeneste;
import no.nav.folketrygdloven.kalkulator.guitjenester.fakta.VurderLønnsendringDtoTjeneste;
import no.nav.folketrygdloven.kalkulator.guitjenester.fakta.VurderMottarYtelseDtoTjeneste;

public class FaktaOmBeregningTilfelleDtoTjenesteProviderMock {

    public static Instance<FaktaOmBeregningTilfelleDtoTjeneste> getTjenesteInstances() {
        @SuppressWarnings("unchecked")
        Instance<FaktaOmBeregningTilfelleDtoTjeneste> tjenesteInstances = mock(Instance.class);
        List<FaktaOmBeregningTilfelleDtoTjeneste> tjenester = new ArrayList<>();
        leggTilKunYtelseDtoTjeneste(tjenester);
        leggTilKortvarigArbeidsforholdDtoTjeneste(tjenester);
        leggTilVurderBesteberegningDtoTjeneste(tjenester);
        leggTilVurderLønnsendringDtoTjeneste(tjenester);
        leggTilVurderATFLISammeOrgDtoTjeneste(tjenester);
        leggTilNyoppstartetFLDtoTjeneste(tjenester);
        leggTilVurderMottarYtelseDtoTjeneste(tjenester);
        when(tjenesteInstances.iterator()).thenReturn(tjenester.iterator());
        when(tjenesteInstances.stream()).thenReturn(tjenester.stream());
        return tjenesteInstances;
    }

    private static void leggTilVurderMottarYtelseDtoTjeneste(List<FaktaOmBeregningTilfelleDtoTjeneste> tjenester) {
        tjenester.add(new VurderMottarYtelseDtoTjeneste());
    }


    private static void leggTilNyoppstartetFLDtoTjeneste(List<FaktaOmBeregningTilfelleDtoTjeneste> tjenester) {
        tjenester.add(new NyOppstartetFLDtoTjeneste());
    }

    private static void leggTilVurderATFLISammeOrgDtoTjeneste(List<FaktaOmBeregningTilfelleDtoTjeneste> tjenester) {
        tjenester.add(new VurderATFLISammeOrgDtoTjeneste());
    }

    private static void leggTilVurderBesteberegningDtoTjeneste(List<FaktaOmBeregningTilfelleDtoTjeneste> tjenester) {
        tjenester.add(new VurderBesteberegningTilfelleDtoTjeneste());
    }

    private static void leggTilVurderLønnsendringDtoTjeneste(List<FaktaOmBeregningTilfelleDtoTjeneste> tjenester) {
        tjenester.add(new VurderLønnsendringDtoTjeneste());
    }

    private static void leggTilKortvarigArbeidsforholdDtoTjeneste(List<FaktaOmBeregningTilfelleDtoTjeneste> tjenester) {
        tjenester.add(new KortvarigeArbeidsforholdDtoTjeneste());
    }

    private static void leggTilKunYtelseDtoTjeneste(List<FaktaOmBeregningTilfelleDtoTjeneste> tjenester) {
        tjenester.add(new KunYtelseDtoTjeneste());
    }
}
