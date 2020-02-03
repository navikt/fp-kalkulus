package no.nav.folketrygdloven.kalkulus.app.selftest.checks;

import static org.assertj.core.api.Assertions.assertThat;

import javax.naming.NameNotFoundException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
public class DatabaseHealthCheckTest {

    @Test
    public void test_check_healthy() {
        DatabaseHealthCheck dbCheck = new DatabaseHealthCheck();

        ExtHealthCheck.InternalResult result = dbCheck.performCheck();

        assertThat(result.isOk()).isTrue();
        assertThat(result.getResponseTimeMs()).isNotNull();
    }

    @Test
    public void skal_feile_pga_ukjent_jndi_name() {
        DatabaseHealthCheck dbCheck = new DatabaseHealthCheck("jndi/ukjent");

        ExtHealthCheck.InternalResult result = dbCheck.performCheck();

        assertThat(result.isOk()).isFalse();
        assertThat(result.getMessage()).contains("Feil ved JNDI-oppslag for jndi/ukjent");
        assertThat(result.getException()).isInstanceOf(NameNotFoundException.class);
    }

}
