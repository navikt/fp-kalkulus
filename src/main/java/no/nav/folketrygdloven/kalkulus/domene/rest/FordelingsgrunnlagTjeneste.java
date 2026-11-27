package no.nav.folketrygdloven.kalkulus.domene.rest;

import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.folketrygdloven.kalkulator.guitjenester.BeregningsgrunnlagGuiTjeneste;
import no.nav.folketrygdloven.kalkulator.guitjenester.KalkulatorGuiInterface;
import no.nav.folketrygdloven.kalkulator.modell.refusjon.FordelingGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.refusjon.Refusjonsandel;
import no.nav.folketrygdloven.kalkulator.modell.refusjon.Refusjonsperiode;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.fordeling.FordelingGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.fordeling.RefusjonsandelEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.fordeling.RefusjonsperiodeEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.mapFraEntitet.BehandlingslagerTilKalkulusMapper;
import no.nav.folketrygdloven.kalkulus.domene.mapFraEntitet.IAYMapperTilKalkulus;
import no.nav.folketrygdloven.kalkulus.domene.mappers.VerdityperMapper;
import no.nav.folketrygdloven.kalkulus.domene.tjeneste.fordeling.FordelingsgrunnlagRepository;
import no.nav.folketrygdloven.kalkulus.domene.tjeneste.beregningsgrunnlag.BeregningsgrunnlagRepository;
import no.nav.folketrygdloven.kalkulus.felles.JournalpostId;
import no.nav.folketrygdloven.kalkulus.response.fordelingsgrunnlag.gui.FordelingGrunnlagDto;

@ApplicationScoped
public class FordelingsgrunnlagTjeneste {
    private FordelingsgrunnlagRepository fordelingsgrunnlagRepository;
    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    private final KalkulatorGuiInterface dtoTjeneste = new BeregningsgrunnlagGuiTjeneste();

    FordelingsgrunnlagTjeneste() {
        // CDI
    }

    @Inject
    public FordelingsgrunnlagTjeneste(FordelingsgrunnlagRepository fordelingsgrunnlagRepository,
                                     BeregningsgrunnlagRepository beregningsgrunnlagRepository) {
        this.fordelingsgrunnlagRepository = fordelingsgrunnlagRepository;
        this.beregningsgrunnlagRepository = beregningsgrunnlagRepository;
    }

    public Optional<FordelingGrunnlagDto> lagDto(KoblingEntitet kobling) {
        return fordelingsgrunnlagRepository.hentAktivtGrunnlag(kobling.getId())
            .map(FordelingsgrunnlagTjeneste::mapEntitetTilDomenemodell)
            .map(fordelingsgrunnlag -> {
                var beregningsgrunnlag = beregningsgrunnlagRepository.hentBeregningsgrunnlagGrunnlagEntitet(kobling.getId())
                    .flatMap(BeregningsgrunnlagGrunnlagEntitet::getBeregningsgrunnlag)
                    .map(BehandlingslagerTilKalkulusMapper::mapBeregningsgrunnlag);
                return dtoTjeneste.lagFordelingsgrunnlagDto(fordelingsgrunnlag, beregningsgrunnlag);
            });
    }

    private static FordelingGrunnlag mapEntitetTilDomenemodell(FordelingGrunnlagEntitet entitet) {
        return new FordelingGrunnlag(entitet.getRefusjonsandeler().stream()
            .map(FordelingsgrunnlagTjeneste::mapAndel)
            .toList());
    }

    private static Refusjonsandel mapAndel(RefusjonsandelEntitet andel) {
        return new Refusjonsandel(
            IAYMapperTilKalkulus.mapArbeidsgiver(andel.getArbeidsgiver()),
            IAYMapperTilKalkulus.mapArbeidsforholdRef(andel.getArbeidsforholdRef()),
            andel.getKilde(),
            andel.getJournalpostId().map(JournalpostId::new).orElse(null),
            andel.getRefusjonsperioder().stream().map(FordelingsgrunnlagTjeneste::mapPeriode).toList());
    }

    private static Refusjonsperiode mapPeriode(RefusjonsperiodeEntitet periode) {
        return new Refusjonsperiode(
            Intervall.fraOgMedTilOgMed(periode.getPeriode().getFomDato(), periode.getPeriode().getTomDato()),
            VerdityperMapper.beløpFraDao(periode.getRefusjonPrMnd()),
            periode.getUtfall(),
            periode.getUtfallÅrsak());
    }
}
