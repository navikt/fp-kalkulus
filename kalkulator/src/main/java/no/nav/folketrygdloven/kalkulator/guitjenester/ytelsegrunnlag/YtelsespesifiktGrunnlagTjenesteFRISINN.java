package no.nav.folketrygdloven.kalkulator.guitjenester.ytelsegrunnlag;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.FrisinnGrunnlag;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.FrisinnPeriode;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagRestInput;
import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittEgenNæringDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittFrilansDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittFrilansInntektDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittOpptjeningDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulator.ytelse.frisinn.EffektivÅrsinntektTjenesteFRISINN;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.YtelsespesifiktGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.frisinn.FrisinnGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.frisinn.FrisinnPeriodeDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.frisinn.OpplystPeriodeDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.frisinn.SøknadsopplysningerDto;

@ApplicationScoped
@FagsakYtelseTypeRef("FRISINN")
public class YtelsespesifiktGrunnlagTjenesteFRISINN implements YtelsespesifiktGrunnlagTjeneste {

    public YtelsespesifiktGrunnlagTjenesteFRISINN() {
        // CDI
    }

    @Override
    public Optional<YtelsespesifiktGrunnlagDto> map(BeregningsgrunnlagRestInput input) {
        return Optional.of(mapFrisinngrunnlag(input));
    }

    private FrisinnGrunnlagDto mapFrisinngrunnlag(BeregningsgrunnlagRestInput input) {
        YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag = input.getYtelsespesifiktGrunnlag();

        FrisinnGrunnlag frisinngrunnlag = (FrisinnGrunnlag) ytelsespesifiktGrunnlag;

        FrisinnGrunnlagDto frisinnGrunnlagDto = new FrisinnGrunnlagDto();

        if (frisinngrunnlag.getSøkerYtelseForFrilans()) {
            frisinnGrunnlagDto.setOpplysningerFL(mapFrilansopplysninger(input));
        }

        if (frisinngrunnlag.getSøkerYtelseForNæring()) {
            frisinnGrunnlagDto.setOpplysningerSN(mapNæringsopplysninger(input));
        }

        List<OpplystPeriodeDto> søktePerioder = mapSøktePerider(input);
        frisinnGrunnlagDto.setPerioderSøktFor(søktePerioder);

        FrisinnGrunnlag frisinnGrunnlag = input.getYtelsespesifiktGrunnlag();
        Optional<OppgittOpptjeningDto> oppgitOpptjening = input.getIayGrunnlag().getOppgittOpptjening();
        oppgitOpptjening.ifPresent(oppgittOpptjeningDto -> frisinnGrunnlagDto.setFrisinnPerioder(mapFrisinnPerioder(frisinnGrunnlag.getFrisinnPerioder(), oppgittOpptjeningDto)));

        return frisinnGrunnlagDto;
    }

    private List<FrisinnPeriodeDto> mapFrisinnPerioder(List<FrisinnPeriode> frisinnPerioder, OppgittOpptjeningDto oppgittOpptjening) {
        List<FrisinnPeriodeDto> dtoer = new ArrayList<>();
        frisinnPerioder.forEach(periode -> {
            if (periode.getSøkerFrilans()) {
                lagFrilansperiode(periode.getPeriode(), oppgittOpptjening).ifPresent(dtoer::add);
            }
            if (periode.getSøkerNæring()) {
                lagNæringsperiode(periode.getPeriode(), oppgittOpptjening).ifPresent(dtoer::add);
            }
        });
        return dtoer;
    }

    private Optional<FrisinnPeriodeDto> lagNæringsperiode(Intervall periode, OppgittOpptjeningDto oppgittOpptjening) {
        List<OppgittEgenNæringDto> næringer  = oppgittOpptjening.getEgenNæring();
        if (næringer.isEmpty()) {
            return Optional.empty();
        }
        BigDecimal oppgittNæringsinntektIPerioden = næringer.stream()
                .filter(næring -> !næring.getPeriode().getFomDato().isBefore(periode.getFomDato()) &&
                        !næring.getPeriode().getTomDato().isAfter(periode.getTomDato()))
                .filter(i -> i.getInntekt() != null)
                .map(OppgittEgenNæringDto::getInntekt)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);

        FrisinnPeriodeDto dto = new FrisinnPeriodeDto();
        dto.setFom(periode.getFomDato());
        dto.setTom(periode.getTomDato());
        dto.setStatusSøktFor(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);
        dto.setOppgittInntekt(oppgittNæringsinntektIPerioden);
        return Optional.of(dto);
    }

    private Optional<FrisinnPeriodeDto> lagFrilansperiode(Intervall periode, OppgittOpptjeningDto oppgittOpptjening) {
        Optional<OppgittFrilansDto> oppgittFL = oppgittOpptjening.getFrilans();
        if (oppgittFL.isEmpty()) {
            return Optional.empty();
        }
        BigDecimal oppgittFrilansinntektIPeriode = oppgittFL.get().getOppgittFrilansInntekt().stream()
                .filter(inntekt -> !inntekt.getPeriode().getFomDato().isBefore(periode.getFomDato())
                && !inntekt.getPeriode().getTomDato().isAfter(periode.getTomDato()))
                .filter(i -> i.getInntekt() != null)
                .map(OppgittFrilansInntektDto::getInntekt)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);

        FrisinnPeriodeDto dto = new FrisinnPeriodeDto();
        dto.setFom(periode.getFomDato());
        dto.setTom(periode.getTomDato());
        dto.setStatusSøktFor(AktivitetStatus.FRILANSER);
        dto.setOppgittInntekt(oppgittFrilansinntektIPeriode);
        return Optional.of(dto);
    }

    private List<OpplystPeriodeDto> mapSøktePerider(BeregningsgrunnlagRestInput input) {
        List<OpplystPeriodeDto> søktePerioder = new ArrayList<>();
        YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag = input.getYtelsespesifiktGrunnlag();
        FrisinnGrunnlag frisinnGrunnlag = (FrisinnGrunnlag) ytelsespesifiktGrunnlag;
        LocalDate stpBG = input.getBeregningsgrunnlag().getSkjæringstidspunkt();
        if (frisinnGrunnlag.getSøkerYtelseForNæring()) {
            søktePerioder.addAll(mapSøktePerioderForNæring(input, stpBG));
        }
        if (frisinnGrunnlag.getSøkerYtelseForFrilans()) {
            søktePerioder.addAll(mapSøktePerioderForFrilans(input, stpBG));
        }
        return søktePerioder;
    }

    private List<OpplystPeriodeDto> mapSøktePerioderForFrilans(BeregningsgrunnlagRestInput input, LocalDate stpBG) {
        Optional<OppgittFrilansDto> oppgittFL = input.getIayGrunnlag().getOppgittOpptjening()
                .flatMap(OppgittOpptjeningDto::getFrilans);
        if (oppgittFL.isEmpty()) {
            return Collections.emptyList();
        }
        return oppgittFL.get().getOppgittFrilansInntekt().stream()
                .filter(inntekt -> !inntekt.getPeriode().getFomDato().isBefore(stpBG))
                .map(this::lagSøktPeriodeDtoForFrilans)
                .collect(Collectors.toList());
    }

    private List<OpplystPeriodeDto> mapSøktePerioderForNæring(BeregningsgrunnlagRestInput input, LocalDate stpBG) {
        List<OppgittEgenNæringDto> oppgitteNæringer = input.getIayGrunnlag().getOppgittOpptjening()
                .map(OppgittOpptjeningDto::getEgenNæring)
                .orElse(Collections.emptyList());
        List<OppgittEgenNæringDto> næringerSøktFor = oppgitteNæringer.stream()
                .filter(næring -> !næring.getPeriode().getFomDato().isBefore(stpBG))
                .collect(Collectors.toList());
        return næringerSøktFor.stream()
                .map(this::lagSøktPeriodeDtoForNæring)
                .collect(Collectors.toList());
    }

    private OpplystPeriodeDto lagSøktPeriodeDtoForFrilans(OppgittFrilansInntektDto fl) {
        OpplystPeriodeDto dto = new OpplystPeriodeDto();
        dto.setFom(fl.getPeriode().getFomDato());
        dto.setTom(fl.getPeriode().getTomDato());
        dto.setStatusSøktFor(AktivitetStatus.FRILANSER);
        return dto;
    }

    private OpplystPeriodeDto lagSøktPeriodeDtoForNæring(OppgittEgenNæringDto næring) {
        OpplystPeriodeDto dto = new OpplystPeriodeDto();
        dto.setFom(næring.getPeriode().getFomDato());
        dto.setTom(næring.getPeriode().getTomDato());
        dto.setStatusSøktFor(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);
        return dto;
    }

    private SøknadsopplysningerDto mapNæringsopplysninger(BeregningsgrunnlagRestInput input) {
        LocalDate stpBg = input.getSkjæringstidspunktForBeregning();
        List<OppgittEgenNæringDto> næringer = input.getIayGrunnlag().getOppgittOpptjening()
                .map(OppgittOpptjeningDto::getEgenNæring)
                .orElse(Collections.emptyList());

        boolean erNyoppstartetNæringsdrivende = næringer.stream().anyMatch(OppgittEgenNæringDto::getNyoppstartet);
        BigDecimal oppgittLøpendeÅrsinntekt = næringer.stream()
                .filter(en -> !stpBg.isAfter(en.getTilOgMed()))
                .filter(en -> en.getBruttoInntekt() != null)
                .map(EffektivÅrsinntektTjenesteFRISINN::finnEffektivÅrsinntektForLøpenedeInntekt)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
        BigDecimal oppgittLøpendeInntekt = næringer.stream()
                .filter(en -> !stpBg.isAfter(en.getTilOgMed()))
                .filter(en -> en.getBruttoInntekt() != null)
                .map(OppgittEgenNæringDto::getBruttoInntekt)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);

        SøknadsopplysningerDto dto = new SøknadsopplysningerDto();
        dto.setErNyoppstartet(erNyoppstartetNæringsdrivende);
        dto.setOppgittÅrsinntekt(oppgittLøpendeÅrsinntekt);
        dto.setOppgittInntekt(oppgittLøpendeInntekt);
        return dto;
    }

    private SøknadsopplysningerDto mapFrilansopplysninger(BeregningsgrunnlagRestInput input) {
        LocalDate stpBg = input.getSkjæringstidspunktForBeregning();
        Boolean erNyoppstartetFrilans = input.getIayGrunnlag().getOppgittOpptjening()
                .flatMap(OppgittOpptjeningDto::getFrilans)
                .map(OppgittFrilansDto::getErNyoppstartet)
                .orElse(false);

        List<OppgittFrilansInntektDto> oppgittFLInntekt = input.getIayGrunnlag().getOppgittOpptjening()
                .flatMap(OppgittOpptjeningDto::getFrilans)
                .map(OppgittFrilansDto::getOppgittFrilansInntekt)
                .orElse(Collections.emptyList());

        BigDecimal oppgittLøpendeÅrsinntekt = oppgittFLInntekt.stream()
                .filter(oi -> !oi.getPeriode().getFomDato().isBefore(stpBg))
                .map(EffektivÅrsinntektTjenesteFRISINN::finnEffektivÅrsinntektForLøpenedeInntekt)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
        BigDecimal oppgittLøpendeInntekt = oppgittFLInntekt.stream()
                .filter(oi -> !oi.getPeriode().getFomDato().isBefore(stpBg))
                .map(OppgittFrilansInntektDto::getInntekt)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
        SøknadsopplysningerDto dto = new SøknadsopplysningerDto();
        dto.setOppgittInntekt(oppgittLøpendeInntekt);
        dto.setOppgittÅrsinntekt(oppgittLøpendeÅrsinntekt);
        dto.setErNyoppstartet(erNyoppstartetFrilans);
        return dto;
    }
}
