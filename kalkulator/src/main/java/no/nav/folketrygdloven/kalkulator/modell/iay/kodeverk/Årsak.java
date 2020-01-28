package no.nav.folketrygdloven.kalkulator.modell.iay.kodeverk;

import no.nav.folketrygdloven.kalkulator.modell.kodeverk.Kodeverdi;

public interface Årsak extends Kodeverdi {

    Årsak UKJENT = new Årsak() {

        @Override
        public String getNavn() {
            return "Ikke definert";
        }

        @Override
        public String getKodeverk() {
            return "AARSAK_TYPE";
        }

        @Override
        public String getKode() {
            return "-";
        }

        @Override
        public String getOffisiellKode() {
            return getKode();
        }
    };
}
