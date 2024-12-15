package no.nav.folketrygdloven.kalkulus.app.tjenester;

import static jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static jakarta.ws.rs.core.Response.Status.OK;
import static jakarta.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import no.nav.folketrygdloven.kalkulus.app.healthcheck.HealthCheckRestService;
import no.nav.vedtak.server.LiveAndReadinessAware;

@ExtendWith(MockitoExtension.class)
public class NaisRestTjenesteTest {


    private HealthCheckRestService sjekk;

    @Mock
    private LiveAndReadinessAware db;

    @BeforeEach
    void setup() {
        sjekk = new HealthCheckRestService(List.of(db), List.of(db));
    }

    @Test
    void test_isAlive_skal_returnere_status_200() {
        when(db.isAlive()).thenReturn(true);
        assertThat(sjekk.isAlive().getStatus()).isEqualTo(OK.getStatusCode());
    }

    @Test
    void test_isReady_skal_returnere_service_unavailable_når_kritiske_selftester_feiler() {
        when(db.isAlive()).thenReturn(false);
        assertThat(sjekk.isReady().getStatus()).isEqualTo(SERVICE_UNAVAILABLE.getStatusCode());
        assertThat(sjekk.isAlive().getStatus()).isEqualTo(INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @Test
    void test_isReady_skal_returnere_status_delvis_når_db_feiler() {
        when(db.isAlive()).thenReturn(true);
        when(db.isReady()).thenReturn(false);
        assertThat(sjekk.isReady().getStatus()).isEqualTo(SERVICE_UNAVAILABLE.getStatusCode());
        assertThat(sjekk.isAlive().getStatus()).isEqualTo(OK.getStatusCode());
    }

    @Test
    void test_isReady_skal_returnere_status_ok_når_selftester_er_ok() {
        when(db.isAlive()).thenReturn(true);
        when(db.isReady()).thenReturn(true);
        assertThat(sjekk.isReady().getStatus()).isEqualTo(OK.getStatusCode());
        assertThat(sjekk.isAlive().getStatus()).isEqualTo(OK.getStatusCode());
    }
}
