package no.nav.folketrygdloven.kalkulator;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.FrisinnGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittPeriodeInntekt;
import no.nav.folketrygdloven.kalkulator.output.BeregningVilkårResultat;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningAksjonspunktDefinisjon;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.Vilkårsavslagsårsak;

@ApplicationScoped
@FagsakYtelseTypeRef("FRISINN")
public class VilkårTjenesteFRISINN extends VilkårTjeneste {


    @Override
    public Optional<BeregningVilkårResultat> lagVilkårResultatFastsettBeregningsaktiviteter(BeregningsgrunnlagInput input, BeregningsgrunnlagRegelResultat beregningsgrunnlagRegelResultat) {

        FrisinnGrunnlag frisinnGrunnlag = input.getYtelsespesifiktGrunnlag();
        if (beregningsgrunnlagRegelResultat.getAksjonspunkter().stream().anyMatch(bra -> bra.getBeregningAksjonspunktDefinisjon() == BeregningAksjonspunktDefinisjon.INGEN_AKTIVITETER)) {
            if (frisinnGrunnlag.getSøkerYtelseForFrilans() && !frisinnGrunnlag.getSøkerYtelseForNæring()) {
                return Optional.of(new BeregningVilkårResultat(false, Vilkårsavslagsårsak.SØKT_FL_INGEN_FL_INNTEKT));
            } else {
                return Optional.of(new BeregningVilkårResultat(false, Vilkårsavslagsårsak.FOR_LAVT_BG));
            }
        } else {
            if (frisinnGrunnlag.getSøkerYtelseForFrilans() && !frisinnGrunnlag.getSøkerYtelseForNæring()) {
                if (beregningsgrunnlagRegelResultat.getBeregningsgrunnlag() != null) {
                    if (beregningsgrunnlagRegelResultat.getBeregningsgrunnlag().getAktivitetStatuser().stream().noneMatch(as -> as.getAktivitetStatus().erFrilanser())) {
                        return Optional.of(new BeregningVilkårResultat(false, Vilkårsavslagsårsak.SØKT_FL_INGEN_FL_INNTEKT));
                    }
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<BeregningVilkårResultat> lagVilkårResultatFullføre(BeregningsgrunnlagInput input, BeregningsgrunnlagDto beregningsgrunnlagDto) {
        FrisinnGrunnlag frisinnGrunnlag = input.getYtelsespesifiktGrunnlag();
        boolean harSøktFrilans = frisinnGrunnlag.getSøkerYtelseForFrilans();
        boolean harSøktNæring = frisinnGrunnlag.getSøkerYtelseForNæring();
        boolean harAvkortetSøktNæring = harAvkortetSøktNæring(input, beregningsgrunnlagDto, harSøktNæring);
        boolean harAvkortetSøktFrilans = harAvkortetSøktFrilans(input, beregningsgrunnlagDto, harSøktFrilans);

        if ((harSøktFrilans && harAvkortetSøktFrilans) && !harSøktNæring) {
            return Optional.of(new BeregningVilkårResultat(false, Vilkårsavslagsårsak.AVKORTET_GRUNNET_ANNEN_INNTEKT));
        }

        if ((harSøktNæring && harAvkortetSøktNæring) && !harSøktFrilans) {
            return Optional.of(new BeregningVilkårResultat(false, Vilkårsavslagsårsak.AVKORTET_GRUNNET_ANNEN_INNTEKT));
        }

        if (harAvkortetSøktFrilans && harAvkortetSøktNæring) {
            return Optional.of(new BeregningVilkårResultat(false, Vilkårsavslagsårsak.AVKORTET_GRUNNET_ANNEN_INNTEKT));
        }

        return Optional.empty();
    }

    private boolean harAvkortetSøktNæring(BeregningsgrunnlagInput input, BeregningsgrunnlagDto beregningsgrunnlagDto,
                                          boolean harSøktNæring) {
        if (harSøktNæring) {
            List<OppgittPeriodeInntekt> frilansSøktPerioder = input.getIayGrunnlag().getOppgittOpptjening()
                    .stream()
                    .flatMap(oo -> oo.getEgenNæring().stream())
                    .filter(p -> p.getPeriode().getFomDato().isAfter(input.getSkjæringstidspunktOpptjening()))
                    .collect(Collectors.toList());
            return beregningsgrunnlagDto.getBeregningsgrunnlagPerioder()
                    .stream()
                    .filter(p -> overlapperMinstEnPeriode(frilansSøktPerioder, p))
                    .allMatch(this::harNæringUtenUtbetaling);
        }
        return false;
    }

    private boolean harAvkortetSøktFrilans(BeregningsgrunnlagInput input, BeregningsgrunnlagDto beregningsgrunnlagDto,
                                           boolean harSøktFrilans) {
        if (harSøktFrilans) {
            List<OppgittPeriodeInntekt> søktePerioder = input.getIayGrunnlag().getOppgittOpptjening()
                    .stream()
                    .flatMap(oo -> oo.getFrilans().stream())
                    .flatMap(fl -> fl.getOppgittFrilansInntekt().stream())
                    .filter(p -> p.getPeriode().getFomDato().isAfter(input.getSkjæringstidspunktOpptjening()))
                    .collect(Collectors.toList());
            return beregningsgrunnlagDto.getBeregningsgrunnlagPerioder()
                    .stream()
                    .filter(p -> overlapperMinstEnPeriode(søktePerioder, p))
                    .allMatch(this::harFrilansUtenUtbetaling);
        }
        return false;
    }

    private boolean overlapperMinstEnPeriode(List<OppgittPeriodeInntekt> frilansSøktPerioder, BeregningsgrunnlagPeriodeDto p) {
        return frilansSøktPerioder.stream().anyMatch(sp -> p.getPeriode().overlapper(sp.getPeriode()));
    }

    private Boolean harFrilansUtenUtbetaling(BeregningsgrunnlagPeriodeDto p) {
        return p.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(a -> a.getAktivitetStatus().erFrilanser())
                .findFirst()
                .map(a -> a.getDagsats().equals(0L))
                .orElse(false);
    }

    private Boolean harNæringUtenUtbetaling(BeregningsgrunnlagPeriodeDto p) {
        return p.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(a -> a.getAktivitetStatus().erSelvstendigNæringsdrivende())
                .findFirst()
                .map(a -> a.getDagsats().equals(0L))
                .orElse(false);
    }

}
