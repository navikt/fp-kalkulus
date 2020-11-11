package no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagBuilder;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.FaktaAggregatEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.FaktaAktørEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.FaktaArbeidsforholdEntitet;

@ApplicationScoped
public class MigrerFaktaTjeneste {

    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;

    public MigrerFaktaTjeneste() {
        // CDI
    }

    @Inject
    public MigrerFaktaTjeneste(BeregningsgrunnlagRepository beregningsgrunnlagRepository) {
        this.beregningsgrunnlagRepository = beregningsgrunnlagRepository;
    }

    public void migrerFakta() {
        migrerMottarYtelseForFrilans();
        migrerTidsbegrensetArbeid();
    }

    private void migrerMottarYtelseForFrilans() {
        List<BeregningsgrunnlagGrunnlagEntitet> grunnlagUtenMottarYtelseFLFakta = beregningsgrunnlagRepository.finnGrunnlagMedMottarYtelseUtenFakta();

        grunnlagUtenMottarYtelseFLFakta.forEach(gr -> {
            Optional<Boolean> mottarYtelse = gr.getBeregningsgrunnlag().stream().flatMap(bg -> bg.getBeregningsgrunnlagPerioder().stream())
                    .flatMap(p -> p.getBeregningsgrunnlagPrStatusOgAndelList().stream())
                    .filter(a -> a.getAktivitetStatus().erFrilanser() && a.mottarYtelse().isPresent())
                    .findFirst().map(BeregningsgrunnlagPrStatusOgAndel::mottarYtelse)
                    .map(Optional::get);
            if (mottarYtelse.isPresent()) {
                BeregningsgrunnlagGrunnlagBuilder beregningsgrunnlagGrunnlagBuilder = BeregningsgrunnlagGrunnlagBuilder.oppdatere(gr)
                        .medFaktaAggregat(FaktaAggregatEntitet.builder()
                                .medFaktaAktør(FaktaAktørEntitet.builder()
                                        .medHarFLMottattYtelse(mottarYtelse.get())
                                        .build())
                                .build());
                beregningsgrunnlagRepository.lagreForFaktaMigrering(gr.getKoblingId(),
                        beregningsgrunnlagGrunnlagBuilder,
                        gr.getBeregningsgrunnlagTilstand(),
                        gr.erAktivt());
            }

        });
    }

    private void migrerTidsbegrensetArbeid() {
        List<BeregningsgrunnlagGrunnlagEntitet> grunnlagUtenMottarYtelseFLFakta = beregningsgrunnlagRepository.finnGrunnlagTidsbegrensetArbeidUtenFakta();

        grunnlagUtenMottarYtelseFLFakta.forEach(gr -> {
            List<BGAndelArbeidsforhold> tidsbegrensedeArbeidsforhold = gr.getBeregningsgrunnlag().stream().map(bg -> bg.getBeregningsgrunnlagPerioder().get(0))
                    .flatMap(p -> p.getBeregningsgrunnlagPrStatusOgAndelList().stream())
                    .filter(a -> a.getBgAndelArbeidsforhold().isPresent())
                    .map(BeregningsgrunnlagPrStatusOgAndel::getBgAndelArbeidsforhold)
                    .map(Optional::get)
                    .filter(arb -> arb.getErTidsbegrensetArbeidsforhold() != null)
                    .collect(Collectors.toList());
            if (!tidsbegrensedeArbeidsforhold.isEmpty()) {

                FaktaAggregatEntitet.Builder faktaAggregatBuilder = FaktaAggregatEntitet.builder();
                tidsbegrensedeArbeidsforhold.stream().map(arb -> FaktaArbeidsforholdEntitet.builder()
                        .medArbeidsgiver(arb.getArbeidsgiver())
                        .medArbeidsforholdRef(arb.getArbeidsforholdRef())
                        .medErTidsbegrenset(arb.getErTidsbegrensetArbeidsforhold()).build())
                        .forEach(faktaAggregatBuilder::leggTilFaktaArbeidsforholdIgnorerOmEksisterer);

                BeregningsgrunnlagGrunnlagBuilder beregningsgrunnlagGrunnlagBuilder = BeregningsgrunnlagGrunnlagBuilder.oppdatere(gr)
                        .medFaktaAggregat(faktaAggregatBuilder.build());
                beregningsgrunnlagRepository.lagreForFaktaMigrering(
                        gr.getKoblingId(),
                        beregningsgrunnlagGrunnlagBuilder,
                        gr.getBeregningsgrunnlagTilstand(),
                        gr.erAktivt());
            }

        });
    }

}
