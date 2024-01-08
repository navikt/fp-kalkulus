package no.nav.folketrygdloven.kalkulus.mappers;

import static no.nav.folketrygdloven.kalkulus.mappers.MapFraKalkulator.mapArbeidsgiver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.input.ForeldrepengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.gradering.AktivitetGradering;
import no.nav.folketrygdloven.kalkulator.modell.gradering.AndelGradering;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.steg.besteberegning.Ytelseandel;
import no.nav.folketrygdloven.kalkulator.steg.besteberegning.Ytelsegrunnlag;
import no.nav.folketrygdloven.kalkulator.steg.besteberegning.Ytelseperiode;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.beregning.v1.AktivitetGraderingDto;
import no.nav.folketrygdloven.kalkulus.beregning.v1.YtelsespesifiktGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;

class ForeldrepengerGrunnlagMapper {

    static ForeldrepengerGrunnlag mapForeldrepengerGrunnlag(YtelsespesifiktGrunnlagDto ytelsespesifiktGrunnlag) {
        ForeldrepengerGrunnlag foreldrepengerGrunnlag = new ForeldrepengerGrunnlag(ytelsespesifiktGrunnlag.getDekningsgrad().intValue(),
                ytelsespesifiktGrunnlag.getKvalifisererTilBesteberegning());
        // TODO(OJR) lag builder?
        no.nav.folketrygdloven.kalkulus.beregning.v1.ForeldrepengerGrunnlag fpKontraktGrunnlag = (no.nav.folketrygdloven.kalkulus.beregning.v1.ForeldrepengerGrunnlag) ytelsespesifiktGrunnlag;
        var aktivitetGradering = fpKontraktGrunnlag.getAktivitetGradering();
        foreldrepengerGrunnlag.setAktivitetGradering(aktivitetGradering == null ? AktivitetGradering.INGEN_GRADERING : mapFraDto(aktivitetGradering));
        foreldrepengerGrunnlag.setSisteSøkteUttaksdag(fpKontraktGrunnlag.getSisteSøkteUttaksdag());
        foreldrepengerGrunnlag.setBesteberegningYtelsegrunnlag(mapYtelsegrunnlag(fpKontraktGrunnlag.getYtelsegrunnlagForBesteberegning()));
        return foreldrepengerGrunnlag;
    }

    public static AktivitetGradering mapFraDto(AktivitetGraderingDto aktivitetGradering) {
        List<AndelGradering> res = new ArrayList<>();
        aktivitetGradering.getAndelGraderingDto().forEach(andel -> {
            AndelGradering.Builder builder = AndelGradering.builder();
            andel.getGraderinger()
                    .forEach(grad -> builder.medGradering(grad.getPeriode().getFom(), grad.getPeriode().getTom(), grad.getArbeidstidProsent().intValue()));
            builder.medStatus(andel.getAktivitetStatus());
            builder.medArbeidsgiver(mapArbeidsgiver(andel.getArbeidsgiver()));
            no.nav.folketrygdloven.kalkulus.felles.v1.InternArbeidsforholdRefDto arbeidsforholdRef = andel.getArbeidsforholdRef();
            if (arbeidsforholdRef != null) {
                builder.medArbeidsforholdRef(InternArbeidsforholdRefDto.ref(arbeidsforholdRef.getAbakusReferanse()));
            }
            res.add(builder.build());
        });

        return new AktivitetGradering(res);
    }


    private static List<Ytelsegrunnlag> mapYtelsegrunnlag(List<no.nav.folketrygdloven.kalkulus.beregning.v1.besteberegning.Ytelsegrunnlag> ytelsegrunnlagForBesteberegning) {
        if (ytelsegrunnlagForBesteberegning == null) {
            return Collections.emptyList();
        }
        return ytelsegrunnlagForBesteberegning.stream()
                .map(yg -> new Ytelsegrunnlag(FagsakYtelseType.fraKode(yg.getYtelse().getKode()), mapYtelseperioder(yg.getPerioder())))
                .collect(Collectors.toList());
    }

    private static List<Ytelseperiode> mapYtelseperioder(List<no.nav.folketrygdloven.kalkulus.beregning.v1.besteberegning.Ytelseperiode> perioder) {
        return perioder.stream()
                .map(p -> new Ytelseperiode(mapPeriode(p.getPeriode()), mapYtelseandeler(p.getAndeler())))
                .collect(Collectors.toList());
    }

    private static Intervall mapPeriode(Periode periode) {
        return Intervall.fraOgMedTilOgMed(periode.getFom(), periode.getTom());
    }

    private static List<Ytelseandel> mapYtelseandeler(List<no.nav.folketrygdloven.kalkulus.beregning.v1.besteberegning.Ytelseandel> andeler) {
        return andeler.stream()
                .map(a -> {
                    if (a.getAktivitetStatus() != null) {
                        return new Ytelseandel(a.getAktivitetStatus(), a.getInntektskategori(), a.getDagsats());
                    } else {
                        return new Ytelseandel(a.getArbeidskategori(), a.getDagsats());
                    }
                })
                .collect(Collectors.toList());
    }


}
