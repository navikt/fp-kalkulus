package no.nav.folketrygdloven.kalkulus.felles.kodeverk;

/**
 *
 * Representerer et kodeverk brukt i applikasjonen. Hvert kodeverk har en eier (default er 'VL' dvs. det er internt
 * kodeverk)
 *
 * Dersom noen av synk flaggene er satt skal dette kodeverket synkroniseres automatisk med kodeverk eier (fungerer bare
 * hvis eier støtter dette og vi har implementert det).
 */
public class Kodeverk extends KodeverkTabell {

    static final String KODEVERK = "KODEVERK";

    private String kodeverkEier;

    private String kodeverkEierReferanse;

    private String kodeverkEierVersjon;

    private String kodeverkEierNavn;

    /** Hvorvidt nye koder skal legges til ved synk. */
    private boolean synkNyeKoderFraKodeverEier;

    /** Hvorvidt endringer i gyldig dato (fom, tom) og navn skal oppdateres ved synk. */
    private boolean synkEksisterendeKoderFraKodeverkEier;

    private Boolean sammensatt;

    public Kodeverk() {
        // proxy for hibernate
    }

    /**
     * For å kunne skape instanser hvor man ikke leser opp fra databasen
     * slik som ved enhetstesting.
     */
    public Kodeverk(String kode, String kodeverkEier, String kodeverkEierVersjon, String kodeverkEierNavn,
                    boolean synkNyeKoderFraKodeverEier, boolean synkEksisterendeKoderFraKodeverkEier, boolean sammensatt){
        super(kode);
        this.kodeverkEier = kodeverkEier;
        this.kodeverkEierVersjon = kodeverkEierVersjon;
        this.kodeverkEierNavn = kodeverkEierNavn;
        this.synkNyeKoderFraKodeverEier = synkNyeKoderFraKodeverEier;
        this.synkEksisterendeKoderFraKodeverkEier = synkEksisterendeKoderFraKodeverkEier;
        this.sammensatt = sammensatt;
    }

    public String getKodeverkEier() {
        return kodeverkEier;
    }

    public String getKodeverkEierNavn() {
        return kodeverkEierNavn;
    }

    @Override
    public String getOffisiellKode() {
        return getKode();
    }

    @Override
    public String getKodeverk() {
        return KODEVERK;
    }

    public String getKodeverkEierReferanse() {
        return kodeverkEierReferanse;
    }

    public String getKodeverkEierVersjon() {
        return kodeverkEierVersjon;
    }

    public boolean getSynkEksisterendeKoderFraKodeverkEier() {
        return synkEksisterendeKoderFraKodeverkEier;
    }

    public boolean getSynkNyeKoderFraKodeverEier() {
        return synkNyeKoderFraKodeverEier;
    }

    public Boolean getSammensatt() {
        return sammensatt;
    }
}
