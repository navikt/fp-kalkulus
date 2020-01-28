package no.nav.folketrygdloven.kalkulator.testutilities;

import static no.nav.folketrygdloven.kalkulator.modell.virksomhet.Organisasjonstype.KUNSTIG;
import static no.nav.folketrygdloven.kalkulator.modell.virksomhet.Organisasjonstype.VIRKSOMHET;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.folketrygdloven.kalkulator.modell.typer.AktørId;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.OrgNummer;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Virksomhet;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.VirksomhetEntitet;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.VirksomhetRepository;

@ApplicationScoped
public class BeregningArbeidsgiverTestUtil {

    private VirksomhetRepository virksomhetRepository;
    private static final String BEREGNINGVIRKSOMHET = "Beregningvirksomhet";

    BeregningArbeidsgiverTestUtil() {
        // for CDI
    }

    @Inject
    public BeregningArbeidsgiverTestUtil(VirksomhetRepository virksomhetRepository) {
        this.virksomhetRepository = virksomhetRepository;
    }

    public Arbeidsgiver forArbeidsgiverVirksomhet(String orgnr) {
        lagVirksomhet(orgnr, false);
        return Arbeidsgiver.virksomhet(orgnr);
    }

    //For å opprette et arbeidsforhold som er lagt til av saksbehandler
    public Arbeidsgiver forKunstigArbeidsforhold() {
        return Arbeidsgiver.virksomhet(OrgNummer.KUNSTIG_ORG);
    }

    public Arbeidsgiver forArbeidsgiverpPrivatperson(AktørId aktørId) {
        return Arbeidsgiver.person(aktørId);
    }

    public VirksomhetEntitet lagVirksomhet(String orgnr, boolean kunstig) {
        Optional<Virksomhet> virksomhetOpt = virksomhetRepository.hent(orgnr);
        if (!virksomhetOpt.isPresent()) {
            VirksomhetEntitet virksomhet = new VirksomhetEntitet.Builder()
                .medOrgnr(orgnr)
                .medNavn(BEREGNINGVIRKSOMHET)
                .oppdatertOpplysningerNå()
                .medOrganisasjonstype(kunstig ? KUNSTIG : VIRKSOMHET)
                .build();
            virksomhetRepository.lagre(virksomhet);
            return virksomhet;
        } else {
            return (VirksomhetEntitet) virksomhetOpt.get();
        }
    }
}
