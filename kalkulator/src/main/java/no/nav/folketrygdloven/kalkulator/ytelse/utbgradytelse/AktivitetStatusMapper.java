package no.nav.folketrygdloven.kalkulator.ytelse.utbgradytelse;


import no.nav.folketrygdloven.kalkulator.modell.uttak.UttakArbeidType;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;

public class AktivitetStatusMapper {

    public static AktivitetStatus mapAktivitetStatus(UttakArbeidType uttakArbeidType) {
        if (UttakArbeidType.ORDINÆRT_ARBEID.equals(uttakArbeidType)) {
            return AktivitetStatus.ARBEIDSTAKER;
        }
        if (UttakArbeidType.SELVSTENDIG_NÆRINGSDRIVENDE.equals(uttakArbeidType)) {
            return AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE;
        }
        if (UttakArbeidType.FRILANS.equals(uttakArbeidType)) {
            return AktivitetStatus.FRILANSER;
        }
        if (UttakArbeidType.INAKTIV.equals(uttakArbeidType)) {
            return AktivitetStatus.BRUKERS_ANDEL;
        }
        if (UttakArbeidType.DAGPENGER.equals(uttakArbeidType)) {
            return AktivitetStatus.DAGPENGER;
        }
        if (UttakArbeidType.BRUKERS_ANDEL.equals(uttakArbeidType)) {
            return AktivitetStatus.BRUKERS_ANDEL;
        }
        if (UttakArbeidType.ANNET.equals(uttakArbeidType)) {
            throw new IllegalArgumentException("Kan ikke gradere " + UttakArbeidType.ANNET);
        }
        throw new IllegalArgumentException("Ukjent UttakArbeidType '" + uttakArbeidType + "' kan ikke mappe til " + AktivitetStatus.class.getName());
    }


}
