package no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.frisinn;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.folketrygdloven.beregningsgrunnlag.BevegeligeHelligdagerUtil;
import no.nav.folketrygdloven.kalkulator.BeregningsperiodeTjeneste;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Organisasjonstype;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;

@ApplicationScoped
@FagsakYtelseTypeRef("FRISINN")
public class BeregningsperiodeTjenesteFRISINN extends BeregningsperiodeTjeneste {

    public static final LocalDate BEREGNINGSPERIODE_FOM_FRISINN = LocalDate.of(2019, 3, 1);
    public static final LocalDate BEREGNINGSPERIODE_TOM_FRISINN = LocalDate.of(2020, 2, 29);
    public static final LocalDate BEREGNINGSPERIODE_FOM_SN_FRISINN = LocalDate.of(2017, 1, 1);
    public static final LocalDate BEREGNINSPERIODE_TOM_SN_FRISINN = LocalDate.of(2019, 12, 31);

    @Override
    public Intervall fastsettBeregningsperiodeForATFLAndeler(LocalDate skjæringstidspunkt) {
        return Intervall.fraOgMedTilOgMed(BEREGNINGSPERIODE_FOM_FRISINN, BEREGNINGSPERIODE_TOM_FRISINN);
    }

    @Override
    public Intervall fastsettBeregningsperiodeForSNAndeler(LocalDate skjæringstidspunkt) {
        return Intervall.fraOgMedTilOgMed(BEREGNINGSPERIODE_FOM_SN_FRISINN, BEREGNINSPERIODE_TOM_SN_FRISINN);
    }

}
