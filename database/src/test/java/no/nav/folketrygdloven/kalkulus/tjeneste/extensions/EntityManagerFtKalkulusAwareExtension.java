package no.nav.folketrygdloven.kalkulus.tjeneste.extensions;

import static no.nav.folketrygdloven.kalkulus.dbstoette.Databaseskjemainitialisering.migrerUnittestSkjemaer;
import static no.nav.folketrygdloven.kalkulus.dbstoette.Databaseskjemainitialisering.settPlaceholdereOgJdniOppslag;

import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.felles.testutilities.db.EntityManagerAwareExtension;
import no.nav.vedtak.felles.testutilities.sikkerhet.DummySubjectHandler;
import no.nav.vedtak.felles.testutilities.sikkerhet.SubjectHandlerUtils;
import no.nav.vedtak.util.env.Environment;

public class EntityManagerFtKalkulusAwareExtension extends EntityManagerAwareExtension {
    private static final Logger LOG = LoggerFactory.getLogger(EntityManagerFtKalkulusAwareExtension.class);
    private static final Environment ENV = Environment.current();

    @Override
    protected void init() {
        LOG.info("Init starter");
        SubjectHandlerUtils.useSubjectHandler(DummySubjectHandler.class);
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Oslo"));
        if (ENV.getProperty("maven.cmd.line.args") == null) {
            LOG.info("Kjører migreringer");
            migrerUnittestSkjemaer();
        }
        settPlaceholdereOgJdniOppslag();
        LOG.info("Init ferdig");
    }

}
