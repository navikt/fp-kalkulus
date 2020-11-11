package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAktørDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.SammenligningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.SammenligningsgrunnlagPrStatusDto;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.SammenligningsgrunnlagType;
@ApplicationScoped
@FagsakYtelseTypeRef()
public class FastsettGrunnlagGenerell {
    private static final Map<SammenligningsgrunnlagType, AktivitetStatus> SAMMENLIGNINGSGRUNNLAGTYPE_AKTIVITETSTATUS_MAP;
    static {
        SAMMENLIGNINGSGRUNNLAGTYPE_AKTIVITETSTATUS_MAP = Map.of(
                SammenligningsgrunnlagType.SAMMENLIGNING_AT, no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus.ARBEIDSTAKER,
                SammenligningsgrunnlagType.SAMMENLIGNING_FL, no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus.FRILANSER,
                SammenligningsgrunnlagType.SAMMENLIGNING_SN, no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);
    }

    public boolean skalGrunnlagFastsettes(BeregningsgrunnlagGUIInput input, no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto andel){
        if(finnesIngenSammenligningsgrunnlagPrStatus(input)){
            return skalGrunnlagFastsettesForGammeltSammenligningsgrunnlag(input, andel, input.getBeregningsgrunnlag().getSammenligningsgrunnlag());
        }

        Optional<SammenligningsgrunnlagPrStatusDto> sammenligningsgrunnlagIkkeSplittet = input.getBeregningsgrunnlag().getSammenligningsgrunnlagPrStatusListe().stream()
                .filter(s -> s.getSammenligningsgrunnlagType().equals(SammenligningsgrunnlagType.SAMMENLIGNING_ATFL_SN))
                .findAny();

        if(sammenligningsgrunnlagIkkeSplittet.isPresent()) {
            return skalGrunnlagFastsettesForGammeltSammenligningsgrunnlag(input, andel, sammenligningsgrunnlagIkkeSplittet.get());
        }

        if(no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE.equals(andel.getAktivitetStatus())){
            return skalGrunnlagFastsettesForSN(input);
        }

        return input.getBeregningsgrunnlag().getSammenligningsgrunnlagPrStatusListe().stream()
                .filter(s -> SAMMENLIGNINGSGRUNNLAGTYPE_AKTIVITETSTATUS_MAP.get(s.getSammenligningsgrunnlagType()).equals(andel.getAktivitetStatus()))
                .anyMatch(s -> erAvvikStørreEnn25Prosent(s.getAvvikPromilleNy()));
    }

    private static boolean finnesIngenSammenligningsgrunnlagPrStatus(BeregningsgrunnlagGUIInput input){
        return input.getBeregningsgrunnlag().getSammenligningsgrunnlagPrStatusListe().isEmpty();
    }

    private static boolean skalGrunnlagFastsettesForSN(BeregningsgrunnlagGUIInput input) {
        Optional<SammenligningsgrunnlagPrStatusDto> sammenligningsgrunnlagPrStatus =  input.getBeregningsgrunnlag().getSammenligningsgrunnlagPrStatusListe().stream()
                .filter(s -> no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE.equals(SAMMENLIGNINGSGRUNNLAGTYPE_AKTIVITETSTATUS_MAP.get(s.getSammenligningsgrunnlagType())))
                .findAny();
        if(sammenligningsgrunnlagPrStatus.isPresent()){
            return erAvvikStørreEnn25Prosent(sammenligningsgrunnlagPrStatus.get().getAvvikPromilleNy());
        }
        Boolean erNyIArbeidslivet = finnErNyIArbeidslivet(input);
        return Boolean.TRUE.equals(erNyIArbeidslivet);
    }

    private static boolean skalGrunnlagFastsettesForGammeltSammenligningsgrunnlag(BeregningsgrunnlagGUIInput input,
                                                                                  no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto andel,
                                                                                  SammenligningsgrunnlagPrStatusDto sammenligningsgrunnlagPrStatus){
        if(finnesSelvstendigNæringsdrivendeAndel(input)) {
            if(andel.getAktivitetStatus().erSelvstendigNæringsdrivende()){
                return erAvvikStørreEnn25Prosent(sammenligningsgrunnlagPrStatus.getAvvikPromilleNy()) || Boolean.TRUE.equals(finnErNyIArbeidslivet(input));
            } else {
                return false;
            }
        }
        return erAvvikStørreEnn25Prosent(sammenligningsgrunnlagPrStatus.getAvvikPromilleNy());
    }

    private static boolean skalGrunnlagFastsettesForGammeltSammenligningsgrunnlag(BeregningsgrunnlagGUIInput input,
                                                                                  no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto andel,
                                                                                  SammenligningsgrunnlagDto sammenligningsgrunnlag){
        if(finnesSelvstendigNæringsdrivendeAndel(input)) {
            if (andel.getAktivitetStatus().erSelvstendigNæringsdrivende()) {
                return finnesSammenligningsgrunnlagOgErAvvikStørreEnn25Prosent(sammenligningsgrunnlag) || Boolean.TRUE.equals(finnErNyIArbeidslivet(input));
            } else {
                return false;
            }
        }
        return finnesSammenligningsgrunnlagOgErAvvikStørreEnn25Prosent(sammenligningsgrunnlag);
    }

    private static Boolean finnErNyIArbeidslivet(BeregningsgrunnlagGUIInput input) {
        Optional<FaktaAggregatDto> faktaAggregat = input.getBeregningsgrunnlagGrunnlag().getFaktaAggregat();
        return faktaAggregat.flatMap(FaktaAggregatDto::getFaktaAktør).map(FaktaAktørDto::getErNyIArbeidslivetSN).orElse(null);
    }

    private static boolean finnesSammenligningsgrunnlagOgErAvvikStørreEnn25Prosent(SammenligningsgrunnlagDto sammenligningsgrunnlag){
        if(sammenligningsgrunnlag != null){
            return erAvvikStørreEnn25Prosent(sammenligningsgrunnlag.getAvvikPromilleNy());
        }
        return false;
    }

    private static boolean erAvvikStørreEnn25Prosent(BigDecimal avvikPromille){
        return avvikPromille.compareTo(BigDecimal.valueOf(250)) > 0;
    }

    private static boolean finnesSelvstendigNæringsdrivendeAndel(BeregningsgrunnlagGUIInput input){
        List<no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto> beregningsgrunnlagPrStatusOgAndel = input.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList();
        return beregningsgrunnlagPrStatusOgAndel.stream().anyMatch(a -> a.getAktivitetStatus().erSelvstendigNæringsdrivende());
    }
}
