package no.nav.folketrygdloven.kalkulator.rest.fakta;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetRestDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.ArbeidsforholdInformasjonDto;
import no.nav.folketrygdloven.kalkulator.rest.dto.AktivitetTomDatoMappingDto;
import no.nav.folketrygdloven.kalkulator.rest.dto.AvklarAktiviteterDto;
import no.nav.folketrygdloven.kalkulator.rest.dto.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.rest.dto.FaktaOmBeregningDto;


@ApplicationScoped
public class AvklarAktiviteterDtoTjeneste {


    private AvklarAktiviteterDtoTjeneste() {
    }

    /**
     * Modifiserer dto for fakta om beregning og setter dto for avklaring av aktiviteter på denne.
     *  @param registerAktivitetAggregat      aggregat for registeraktiviteter
     * @param saksbehandletAktivitetAggregat aggregat for saksbehandlede aktiviteter
     * @param faktaOmBeregningDto            Dto for fakta om beregning som modifiseres
     */
    static void lagAvklarAktiviteterDto(BeregningAktivitetAggregatRestDto registerAktivitetAggregat,
                                        Optional<BeregningAktivitetAggregatRestDto> saksbehandletAktivitetAggregat,
                                        Optional<ArbeidsforholdInformasjonDto> arbeidsforholdInformasjon,
                                        FaktaOmBeregningDto faktaOmBeregningDto) {
        AvklarAktiviteterDto avklarAktiviteterDto = new AvklarAktiviteterDto();
        List<BeregningAktivitetRestDto> beregningAktiviteter = registerAktivitetAggregat.getBeregningAktiviteter();
        List<BeregningAktivitetRestDto> saksbehandletAktiviteter = saksbehandletAktivitetAggregat
                .map(BeregningAktivitetAggregatRestDto::getBeregningAktiviteter)
                .orElse(Collections.emptyList());

        avklarAktiviteterDto.setAktiviteterTomDatoMapping(map(beregningAktiviteter, saksbehandletAktiviteter,
                registerAktivitetAggregat.getSkjæringstidspunktOpptjening(), arbeidsforholdInformasjon));
        faktaOmBeregningDto.setAvklarAktiviteter(avklarAktiviteterDto);
    }

    /**
     * Lager map for fastsettelse av skjæringstidspunkt for beregning.
     * <p>
     * Mapper fra mulige skjæringstidspunkt til aktiviteter som er aktive på dette tidspunktet.
     * Disse aktivitetene vil bli med videre i beregning.
     *
     * @param beregningAktiviteter     registeraktiviteter
     * @param saksbehandletAktiviteter saksbehandlede aktiviteter
     * @param skjæringstidspunkt       Skjæringstidspunkt for beregning
     * @return Liste med mappingobjekter som knytter eit mulig skjæringstidspunkt for beregning til eit sett med aktiviteter
     */
    private static List<AktivitetTomDatoMappingDto> map(List<BeregningAktivitetRestDto> beregningAktiviteter, List<BeregningAktivitetRestDto> saksbehandletAktiviteter,
                                                        LocalDate skjæringstidspunkt, Optional<ArbeidsforholdInformasjonDto> arbeidsforholdInformasjon) {
        Map<LocalDate, List<BeregningAktivitetDto>> collect = beregningAktiviteter.stream()
                .map(aktivitet -> MapBeregningAktivitetDto.mapBeregningAktivitet(aktivitet, saksbehandletAktiviteter, arbeidsforholdInformasjon))
                .collect(Collectors.groupingBy(beregningAktivitetDto -> finnTidligste(beregningAktivitetDto.getTom().plusDays(1), skjæringstidspunkt), Collectors.toList()));
        return collect.entrySet().stream()
                .map(entry -> {
                    AktivitetTomDatoMappingDto dto = new AktivitetTomDatoMappingDto();
                    dto.setTom(entry.getKey());
                    dto.setAktiviteter(entry.getValue());
                    return dto;
                })
                .sorted(Comparator.comparing(AktivitetTomDatoMappingDto::getTom).reversed())
                .collect(Collectors.toList());
    }

    private static LocalDate finnTidligste(LocalDate tom, LocalDate skjæringstidspunkt) {
        if (tom.isAfter(skjæringstidspunkt)) {
            return skjæringstidspunkt;
        }
        return tom;
    }

}
