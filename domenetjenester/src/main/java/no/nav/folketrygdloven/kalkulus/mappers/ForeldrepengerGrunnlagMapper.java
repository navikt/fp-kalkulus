package no.nav.folketrygdloven.kalkulus.mappers;

import static no.nav.folketrygdloven.kalkulus.mappers.MapFraKalkulator.mapArbeidsgiver;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.input.ForeldrepengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.besteberegning.Ytelseandel;
import no.nav.folketrygdloven.kalkulator.modell.besteberegning.Ytelsegrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.besteberegning.Ytelseperiode;
import no.nav.folketrygdloven.kalkulator.modell.gradering.AktivitetGradering;
import no.nav.folketrygdloven.kalkulator.modell.gradering.AndelGradering;
import no.nav.folketrygdloven.kalkulator.modell.typer.Aktivitetsgrad;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.steg.besteberegning.BesteberegningMånedGrunnlag;
import no.nav.folketrygdloven.kalkulator.steg.besteberegning.BesteberegningVurderingGrunnlag;
import no.nav.folketrygdloven.kalkulator.steg.besteberegning.Inntekt;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.beregning.v1.AktivitetGraderingDto;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BesteberegningMånedsgrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.kodeverk.Dekningsgrad;
import no.nav.folketrygdloven.kalkulus.typer.AktørId;

class ForeldrepengerGrunnlagMapper {

    static ForeldrepengerGrunnlag mapForeldrepengerGrunnlag(no.nav.folketrygdloven.kalkulus.beregning.v1.ForeldrepengerGrunnlag fpKontraktGrunnlag,
                                                            Optional<BeregningsgrunnlagGrunnlagEntitet> beregningsgrunnlagGrunnlagEntitet) {

        ForeldrepengerGrunnlag foreldrepengerGrunnlag = new ForeldrepengerGrunnlag(mapDekningsgrad(fpKontraktGrunnlag.getDekningsgrad().intValue()),
                fpKontraktGrunnlag.getKvalifisererTilBesteberegning());
        var bbGrunnlag = beregningsgrunnlagGrunnlagEntitet.flatMap(BeregningsgrunnlagGrunnlagEntitet::getBeregningsgrunnlag)
            .flatMap(BeregningsgrunnlagEntitet::getBesteberegninggrunnlag);
        var besteberegningvurderingsgrunnlag = bbGrunnlag.map(bbg -> {
            var måneder = bbg.getSeksBesteMåneder().stream().map(ForeldrepengerGrunnlagMapper::mapBBMåned).toList();
            return new BesteberegningVurderingGrunnlag(måneder, mapBeløp(bbg.getAvvik().orElse(null)));
        });
        besteberegningvurderingsgrunnlag.ifPresent(foreldrepengerGrunnlag::setBesteberegningVurderingGrunnlag);
        var aktivitetGradering = fpKontraktGrunnlag.getAktivitetGradering();
        foreldrepengerGrunnlag.setAktivitetGradering(aktivitetGradering == null || aktivitetGradering.getAndelGraderingDto() == null ? AktivitetGradering.INGEN_GRADERING : mapFraDto(aktivitetGradering));
        foreldrepengerGrunnlag.setBesteberegningYtelsegrunnlag(mapYtelsegrunnlag(fpKontraktGrunnlag.getYtelsegrunnlagForBesteberegning()));
        return foreldrepengerGrunnlag;
    }

    private static BesteberegningMånedGrunnlag mapBBMåned(BesteberegningMånedsgrunnlagEntitet måned) {
        var inntekter = måned.getInntekter().stream().map(inntektIMåned -> {
            if (inntektIMåned.getArbeidsgiver() != null) {
                return new Inntekt(mapInternAG(inntektIMåned.getArbeidsgiver()),
                    inntektIMåned.getArbeidsforholdRef() == null ? InternArbeidsforholdRefDto.nullRef() : InternArbeidsforholdRefDto.ref(
                        inntektIMåned.getArbeidsforholdRef().getReferanse()), mapBeløp(inntektIMåned.getInntekt()));
            }
            return new Inntekt(inntektIMåned.getOpptjeningAktivitetType(), mapBeløp(inntektIMåned.getInntekt()));
        }).toList();
        return new BesteberegningMånedGrunnlag(inntekter, YearMonth.from(måned.getPeriode().getFomDato()));
    }

    private static Beløp mapBeløp(no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Beløp inntekt) {
        return inntekt == null ? null : Beløp.fra(inntekt.getVerdi());
    }

    private static no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver mapInternAG(Arbeidsgiver arbeidsgiver) {
        if (arbeidsgiver.getErVirksomhet()) {
            return no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver.virksomhet(arbeidsgiver.getIdentifikator());
        }
        return no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver.person(new AktørId(arbeidsgiver.getIdentifikator()));
    }


    private static Dekningsgrad mapDekningsgrad(int dekningsgradProsent) {
        if (dekningsgradProsent == 80) {
            return Dekningsgrad.DEKNINGSGRAD_80;
        }
        if (dekningsgradProsent == 100) {
            return Dekningsgrad.DEKNINGSGRAD_100;
        }
        throw new IllegalStateException("Ugyldig dekningsgrad på foreldrepenger " + dekningsgradProsent);
    }

    public static AktivitetGradering mapFraDto(AktivitetGraderingDto aktivitetGradering) {
        List<AndelGradering> res = new ArrayList<>();
        aktivitetGradering.getAndelGraderingDto().forEach(andel -> {
            AndelGradering.Builder builder = AndelGradering.builder();
            andel.getGraderinger()
                    .forEach(grad -> builder.medGradering(grad.getPeriode().getFom(), grad.getPeriode().getTom(), Aktivitetsgrad.fra(grad.getArbeidstidProsent().verdi())));
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
                .map(yg -> new Ytelsegrunnlag(yg.getYtelse(), mapYtelseperioder(yg.getPerioder())))
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
