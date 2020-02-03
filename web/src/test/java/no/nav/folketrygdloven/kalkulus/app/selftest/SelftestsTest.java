package no.nav.folketrygdloven.kalkulus.app.selftest;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.jboss.weld.junit5.EnableWeld;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;

import no.nav.folketrygdloven.kalkulus.app.selftest.checks.ExtHealthCheck;
import no.nav.vedtak.konfig.KonfigVerdi;

@EnableWeld
public class SelftestsTest {

    @WeldSetup
    WeldInitiator weldInitiator = WeldInitiator.of(WeldInitiator.createWeld()
            .addPackage(true, ExtHealthCheck.class)
            .addPackage(true, KonfigVerdi.class));

    @Inject
    @Any
    Instance<ExtHealthCheck> healthChecks;

    private Selftests selftests;

    @BeforeEach
    public void setup() {
        HealthCheckRegistry registry = Mockito.mock(HealthCheckRegistry.class);

        List<ExtHealthCheck> checks = new ArrayList<>();

        for (ExtHealthCheck ex : healthChecks) {
            ExtHealthCheck newEx = Mockito.spy(ex);
            Mockito.doReturn(false).when(newEx).erKritiskTjeneste();
            checks.add(newEx);
        }

        @SuppressWarnings("unchecked")
        Instance<ExtHealthCheck> testInstance = Mockito.mock(Instance.class);
        Mockito.doReturn(checks.iterator()).when(testInstance).iterator();
        selftests = new Selftests(registry, testInstance, "fpabakus");
    }

    @Test
    public void test_run_skal_utfoere_alle_del_tester() {
        SelftestResultat samletResultat = selftests.run();

        assertThat(samletResultat != null).isTrue();
        assertThat(samletResultat.getApplication()).isNotNull();
        assertThat(samletResultat.getVersion()).isNotNull();
        assertThat(samletResultat.getTimestamp()).isNotNull();
        assertThat(samletResultat.getRevision()).isNotNull();
        assertThat(samletResultat.getBuildTime()).isNotNull();
        assertThat(samletResultat.getAggregateResult()).isNotNull();
        List<HealthCheck.Result> resultList = samletResultat.getAlleResultater();
        assertThat(resultList).isNotNull();
    }
}
