package no.nav.folketrygdloven.kalkulator.avklaringsbehov;

import java.time.LocalDate;

import no.nav.folketrygdloven.kalkulator.input.HåndterBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fordeling.VurderRepresentererStortingetHåndteringDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.PeriodeÅrsak;

public class VurderRepresentererStortingetTjeneste {

    public static BeregningsgrunnlagGrunnlagDto løsAvklaringsbehov(VurderRepresentererStortingetHåndteringDto vurderDto, HåndterBeregningsgrunnlagInput input) {
        BeregningsgrunnlagGrunnlagDtoBuilder grunnlagBuilder = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());
        if (vurderDto.getRepresentererStortinget()) {
            var nyttBg = splittPerioderOgSettPeriodeårsak(input.getBeregningsgrunnlag(), vurderDto.getFom(), vurderDto.getTom());
            grunnlagBuilder.medBeregningsgrunnlag(nyttBg);
        }
        return grunnlagBuilder.build(BeregningsgrunnlagTilstand.FASTSATT_INN);
    }


    public static BeregningsgrunnlagDto splittPerioderOgSettPeriodeårsak(BeregningsgrunnlagDto beregningsgrunnlag,
                                                                         LocalDate stortingsperiodeFom,
                                                                         LocalDate stortingsperiodeTom) {

        BeregningsgrunnlagDto nyttBg = new BeregningsgrunnlagDto(beregningsgrunnlag);
        var eksisterendePerioder = nyttBg.getBeregningsgrunnlagPerioder();
        for (BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode : eksisterendePerioder) {
            var bgPeriode = beregningsgrunnlagPeriode.getPeriode();
            if (!bgPeriode.getFomDato().isBefore(stortingsperiodeFom) && !bgPeriode.getTomDato().isAfter(stortingsperiodeTom)) {
                // Stortingsperiode omfatter hele periode
                oppdaterPeriodeÅrsakForPeriode(beregningsgrunnlagPeriode);
            } else if (bgPeriode.getFomDato().equals(stortingsperiodeFom)) {
                // Stortingsperiode starter likt med perioden og slutter i løpet av perioden
                splittBeregningsgrunnlagPeriodeOgSettPeriodeÅrsakPåFørste(nyttBg, beregningsgrunnlagPeriode, stortingsperiodeTom);
            } else if (bgPeriode.inkluderer(stortingsperiodeFom) && bgPeriode.getTomDato().equals(stortingsperiodeTom)) {
                // Stortingsperiode starter i løpet av perioden og slutter likt med perioden
                splittBeregningsgrunnlagPeriodeOgSettPeriodeÅrsakPåSiste(nyttBg, beregningsgrunnlagPeriode, stortingsperiodeFom);
            } else if (bgPeriode.inkluderer(stortingsperiodeFom) && bgPeriode.inkluderer(stortingsperiodeTom)) {
                // Stortingsperiode starter i løpet av perioden og slutter i løpet av perioden
                splittBeregningsgrunnlagMidtIPeriode(nyttBg, beregningsgrunnlagPeriode, stortingsperiodeFom, stortingsperiodeTom);
            }
        }
        return nyttBg;
    }

    private static void oppdaterPeriodeÅrsakForPeriode(BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode) {
        BeregningsgrunnlagPeriodeDto.oppdater(beregningsgrunnlagPeriode)
                .leggTilPeriodeÅrsak(PeriodeÅrsak.REPRESENTERER_STORTINGET)
                .build();
    }

    private static void splittBeregningsgrunnlagPeriodeOgSettPeriodeÅrsakPåFørste(BeregningsgrunnlagDto beregningsgrunnlag, BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode,
                                                                                  LocalDate nyPeriodeTom) {
        LocalDate eksisterendePeriodeTom = beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriodeTom();
        opprettNyPeriode(beregningsgrunnlag, beregningsgrunnlagPeriode, nyPeriodeTom.plusDays(1), eksisterendePeriodeTom);
        BeregningsgrunnlagPeriodeDto.oppdater(beregningsgrunnlagPeriode)
                .medBeregningsgrunnlagPeriode(beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriodeFom(), nyPeriodeTom)
                .leggTilPeriodeÅrsak(PeriodeÅrsak.REPRESENTERER_STORTINGET)
                .build();
    }

    private static void splittBeregningsgrunnlagPeriodeOgSettPeriodeÅrsakPåSiste(BeregningsgrunnlagDto beregningsgrunnlag, BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode,
                                                                                 LocalDate nyPeriodeFom) {
        LocalDate eksisterendePeriodeFom = beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriodeFom();
        opprettNyPeriode(beregningsgrunnlag, beregningsgrunnlagPeriode, eksisterendePeriodeFom, nyPeriodeFom.minusDays(1));
        BeregningsgrunnlagPeriodeDto.oppdater(beregningsgrunnlagPeriode)
                .medBeregningsgrunnlagPeriode(nyPeriodeFom, beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriodeTom())
                .leggTilPeriodeÅrsak(PeriodeÅrsak.REPRESENTERER_STORTINGET)
                .build();
    }

    private static void opprettNyPeriode(BeregningsgrunnlagDto beregningsgrunnlag,
                                         BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode,
                                         LocalDate fom, LocalDate tom) {
        BeregningsgrunnlagPeriodeDto.kopier(beregningsgrunnlagPeriode)
                .medBeregningsgrunnlagPeriode(fom, tom)
                .build(beregningsgrunnlag);
    }

    private static void splittBeregningsgrunnlagMidtIPeriode(BeregningsgrunnlagDto beregningsgrunnlag,
                                                             BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode,
                                                             LocalDate nyPeriodeFom,
                                                             LocalDate nyPeriodeTom) {
        LocalDate eksisterendePeriodeFom = beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriodeFom();
        LocalDate eksisterendePeriodeTom = beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriodeTom();
        opprettNyPeriode(beregningsgrunnlag, beregningsgrunnlagPeriode, eksisterendePeriodeFom, nyPeriodeFom.minusDays(1));
        opprettNyPeriode(beregningsgrunnlag, beregningsgrunnlagPeriode, nyPeriodeTom.plusDays(1), eksisterendePeriodeTom);
        BeregningsgrunnlagPeriodeDto.oppdater(beregningsgrunnlagPeriode)
                .medBeregningsgrunnlagPeriode(nyPeriodeFom, nyPeriodeTom)
                .leggTilPeriodeÅrsak(PeriodeÅrsak.REPRESENTERER_STORTINGET)
                .build();

    }

}
