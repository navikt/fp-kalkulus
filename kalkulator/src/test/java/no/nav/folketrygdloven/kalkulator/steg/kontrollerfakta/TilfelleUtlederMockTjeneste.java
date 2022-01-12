package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.inject.Instance;

import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.utledere.ArbeidstakerOgFrilanserISammeOrganisasjonTilfelleUtleder;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.utledere.EtterlønnSluttpakkeTilfelleUtleder;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.utledere.KortvarigArbeidsforholdTilfelleUtleder;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.utledere.KunYtelseTilfelleUtleder;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.utledere.NyIArbeidslivetTilfelleUtleder;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.utledere.NyoppstartetFLTilfelleUtleder;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.utledere.TilfelleUtleder;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.utledere.VurderLønnsendringTilfelleUtleder;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.utledere.VurderMottarYtelseTilfelleUtleder;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.ytelse.fp.VurderBesteberegningTilfelleUtleder;

public class TilfelleUtlederMockTjeneste {

    @SuppressWarnings("unchecked")
    public static Instance<TilfelleUtleder> getUtlederInstances() {
        Instance<TilfelleUtleder> utlederInstances = mock(Instance.class);
        Instance<TilfelleUtleder> emptyMockInstances = mock(Instance.class);
        mockInstance(utlederInstances);
        when(utlederInstances.select(any())).thenReturn(emptyMockInstances);
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
