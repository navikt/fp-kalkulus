package no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag;


import org.junit.Rule;
import org.junit.Test;

import no.nav.vedtak.felles.testutilities.db.RepositoryRule;

import no.nav.folketrygdloven.kalkulus.dbstoette.UnittestRepositoryRule;

public class BeregningsgrunnlagRepositoryTest {

    @Rule
    public RepositoryRule repositoryRule = new UnittestRepositoryRule();


    private BeregningsgrunnlagRepository repository = new BeregningsgrunnlagRepository(repositoryRule.getEntityManager());


    @Test
    public void skal_lagre_ned_beregningsgrunnlag() {

        //testet å kjøre inn databasescript i testbase...
        //skal utvide med faktisk lagring
        int foo = 0;
    }
}
