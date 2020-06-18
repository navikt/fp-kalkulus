package no.nav.folketrygdloven.kalkulator.kontrollerfakta;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.inject.Instance;

import org.mockito.ArgumentMatchers;

import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.fp.VurderBesteberegningTilfelleUtleder;
import no.nav.folketrygdloven.kalkulator.kontrollerfakta.utledere.ArbeidstakerOgFrilanserISammeOrganisasjonTilfelleUtleder;
import no.nav.folketrygdloven.kalkulator.kontrollerfakta.utledere.EtterlønnSluttpakkeTilfelleUtleder;
import no.nav.folketrygdloven.kalkulator.kontrollerfakta.utledere.KortvarigArbeidsforholdTilfelleUtleder;
import no.nav.folketrygdloven.kalkulator.kontrollerfakta.utledere.KunYtelseTilfelleUtleder;
import no.nav.folketrygdloven.kalkulator.kontrollerfakta.utledere.NyIArbeidslivetTilfelleUtleder;
import no.nav.folketrygdloven.kalkulator.kontrollerfakta.utledere.NyoppstartetFLTilfelleUtleder;
import no.nav.folketrygdloven.kalkulator.kontrollerfakta.utledere.TilfelleUtleder;
import no.nav.folketrygdloven.kalkulator.kontrollerfakta.utledere.VurderLønnsendringTilfelleUtleder;
import no.nav.folketrygdloven.kalkulator.kontrollerfakta.utledere.VurderMottarYtelseTilfelleUtleder;
import no.nav.vedtak.felles.testutilities.cdi.UnitTestLookupInstanceImpl;

public class TilfelleUtlederMockTjeneste {

    public static Instance<TilfelleUtleder> getUtlederInstances() {
        @SuppressWarnings("unchecked")
        Instance<TilfelleUtleder> utlederInstances = mock(Instance.class);
        mockInstance(utlederInstances);
        when(utlederInstances.select(any())).thenReturn(mock(Instance.class));
        when(utlederInstances.select(new FagsakYtelseTypeRef.FagsakYtelseTypeRefLiteral("*"))).thenReturn(utlederInstances);
        return utlederInstances;
    }

    private static void mockInstance(Instance<TilfelleUtleder> utlederInstances) {
        List<TilfelleUtleder> utledere = new ArrayList<>();
        leggTilUtleder(new NyIArbeidslivetTilfelleUtleder(), utledere);
        leggTilUtleder(new KunYtelseTilfelleUtleder(), utledere);
        leggTilUtleder(new NyoppstartetFLTilfelleUtleder(), utledere);
        leggTilUtleder(new VurderLønnsendringTilfelleUtleder(), utledere);
        leggTilUtleder(new KortvarigArbeidsforholdTilfelleUtleder(), utledere);
        leggTilUtleder(new ArbeidstakerOgFrilanserISammeOrganisasjonTilfelleUtleder(), utledere);
        leggTilUtleder(new EtterlønnSluttpakkeTilfelleUtleder(), utledere);
        leggTilUtleder(new VurderMottarYtelseTilfelleUtleder(), utledere);
        leggTilUtleder(new VurderBesteberegningTilfelleUtleder(), utledere);
        when(utlederInstances.iterator()).thenReturn(utledere.iterator());
        when(utlederInstances.stream()).thenReturn(utledere.stream());
    }

    private static void leggTilUtleder(TilfelleUtleder utleder, List<TilfelleUtleder> utledere) {
        utledere.add(utleder);
    }
}
