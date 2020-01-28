package no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.kodeverk;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulator.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModell;

public class MapAktivitetStatusVedSkjæringstidspunktFraRegelTilVL {
    private static final Map<no.nav.folketrygdloven.kalkulator.regelmodell.AktivitetStatus, AktivitetStatus> AKTIVITET_STATUS_MAP = new EnumMap<>(
        no.nav.folketrygdloven.kalkulator.regelmodell.AktivitetStatus.class);

    static {
        AKTIVITET_STATUS_MAP.put(no.nav.folketrygdloven.kalkulator.regelmodell.AktivitetStatus.AAP, AktivitetStatus.ARBEIDSAVKLARINGSPENGER);
        AKTIVITET_STATUS_MAP.put(no.nav.folketrygdloven.kalkulator.regelmodell.AktivitetStatus.BA, AktivitetStatus.BRUKERS_ANDEL);
        AKTIVITET_STATUS_MAP.put(no.nav.folketrygdloven.kalkulator.regelmodell.AktivitetStatus.DP, AktivitetStatus.DAGPENGER);
        AKTIVITET_STATUS_MAP.put(no.nav.folketrygdloven.kalkulator.regelmodell.AktivitetStatus.KUN_YTELSE, AktivitetStatus.KUN_YTELSE);
        AKTIVITET_STATUS_MAP.put(no.nav.folketrygdloven.kalkulator.regelmodell.AktivitetStatus.MS, AktivitetStatus.MILITÆR_ELLER_SIVIL);
        AKTIVITET_STATUS_MAP.put(no.nav.folketrygdloven.kalkulator.regelmodell.AktivitetStatus.SN, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);
        AKTIVITET_STATUS_MAP.put(no.nav.folketrygdloven.kalkulator.regelmodell.AktivitetStatus.UDEFINERT, AktivitetStatus.UDEFINERT);
    }

    private MapAktivitetStatusVedSkjæringstidspunktFraRegelTilVL() {
        // skjul private constructor
    }

    static boolean contains(no.nav.folketrygdloven.kalkulator.regelmodell.AktivitetStatus aktivitetStatus) {
        return AKTIVITET_STATUS_MAP.containsKey(aktivitetStatus);
    }

    static AktivitetStatus map(no.nav.folketrygdloven.kalkulator.regelmodell.AktivitetStatus aktivitetStatus) {
        if (aktivitetStatus == null) {
            return null;
        }
        var mappedStatus = AKTIVITET_STATUS_MAP.get(aktivitetStatus);
        if (mappedStatus == null) {
            throw new IllegalStateException(
                "Har ikke mapping til " + AktivitetStatus.class.getName() + " fra " + aktivitetStatus.getClass().getName() + "." + aktivitetStatus);
        }
        return mappedStatus;
    }

    public static AktivitetStatus mapAktivitetStatusfraRegelmodell(AktivitetStatusModell regelmodell,
                                                                   no.nav.folketrygdloven.kalkulator.regelmodell.AktivitetStatus regelVerdi) {
        if (erATFL(regelVerdi)) {
            return kombinertStatus(regelmodell, false);
        } else if (erATFL_SN(regelVerdi)) {
            return kombinertStatus(regelmodell, true);
        }
        return MapAktivitetStatusVedSkjæringstidspunktFraRegelTilVL.map(regelVerdi);
    }

    private static boolean erATFL(no.nav.folketrygdloven.kalkulator.regelmodell.AktivitetStatus aktivitetStatus) {
        return no.nav.folketrygdloven.kalkulator.regelmodell.AktivitetStatus.ATFL.equals(aktivitetStatus);
    }

    private static boolean erATFL_SN(no.nav.folketrygdloven.kalkulator.regelmodell.AktivitetStatus aktivitetStatus) {
        return no.nav.folketrygdloven.kalkulator.regelmodell.AktivitetStatus.ATFL_SN.equals(aktivitetStatus);
    }

    private static AktivitetStatus kombinertStatus(AktivitetStatusModell regelmodell, boolean medSN) {
        List<Arbeidsforhold> alleArbeidsforhold = regelmodell.getBeregningsgrunnlagPrStatusListe().stream()
            .flatMap(bgps -> bgps.getArbeidsforholdList().stream())
            .collect(Collectors.toList());
        Optional<Arbeidsforhold> frilanser = alleArbeidsforhold.stream().filter(Arbeidsforhold::erFrilanser).findAny();
        Optional<Arbeidsforhold> arbeidstaker = alleArbeidsforhold.stream().filter(af -> !(af.erFrilanser())).findAny();
        if (frilanser.isPresent()) {
            if (arbeidstaker.isPresent()) {
                return medSN ? AktivitetStatus.KOMBINERT_AT_FL_SN : AktivitetStatus.KOMBINERT_AT_FL;
            } else {
                return medSN ? AktivitetStatus.KOMBINERT_FL_SN : AktivitetStatus.FRILANSER;
            }
        } else {
            return medSN ? AktivitetStatus.KOMBINERT_AT_SN : AktivitetStatus.ARBEIDSTAKER;
        }
    }
}
