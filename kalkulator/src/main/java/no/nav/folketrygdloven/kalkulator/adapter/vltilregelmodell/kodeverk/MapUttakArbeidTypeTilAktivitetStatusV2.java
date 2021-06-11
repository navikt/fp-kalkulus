package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.kodeverk;


import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.AktivitetStatusV2;
import no.nav.folketrygdloven.kalkulator.modell.uttak.UttakArbeidType;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;

public class MapUttakArbeidTypeTilAktivitetStatusV2 {

    public static AktivitetStatusV2 mapAktivitetStatus(UttakArbeidType uttakArbeidType) {
        if (UttakArbeidType.ORDINÆRT_ARBEID.equals(uttakArbeidType)) {
            return AktivitetStatusV2.AT;
        }
        if (UttakArbeidType.SELVSTENDIG_NÆRINGSDRIVENDE.equals(uttakArbeidType)) {
            return AktivitetStatusV2.SN;
        }
        if (UttakArbeidType.FRILANS.equals(uttakArbeidType)) {
            return AktivitetStatusV2.FL;
        }
        if (UttakArbeidType.INAKTIV.equals(uttakArbeidType)) {
            return AktivitetStatusV2.IN;
        }
        if (UttakArbeidType.ANNET.equals(uttakArbeidType)) {
            throw new IllegalArgumentException("Kan ikke gradere " + UttakArbeidType.ANNET);
        }
        throw new IllegalArgumentException("Ukjent UttakArbeidType '" + uttakArbeidType + "' kan ikke mappe til " + AktivitetStatus.class.getName());
    }


}
