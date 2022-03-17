package no.nav.folketrygdloven.kalkulus.app.tjenester;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.core.Response;
import no.nav.folketrygdloven.kalkulus.app.selftest.NaisRestTjeneste;

@SuppressWarnings("resource")
public class NaisRestTjenesteTest {

    private NaisRestTjeneste restTjeneste;

    @BeforeEach
    public void setup() {
        restTjeneste = new NaisRestTjeneste();
    }

    @Test
    public void test_isAlive_skal_returnere_status_200() {
        Response response = restTjeneste.isAlive();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }


    @Test
    public void test_isReady_skal_returnere_status_ok_når_selftester_er_ok() {

        Response response = restTjeneste.isReady();

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void test_preStop_skal_kalle_stopServices_og_returnere_status_ok() {
        Response response = restTjeneste.preStop();

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }
}
