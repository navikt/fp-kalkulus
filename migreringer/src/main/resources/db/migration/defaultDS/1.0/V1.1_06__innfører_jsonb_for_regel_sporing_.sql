alter table BEREGNINGSGRUNNLAG alter column regelinput_periodisering type jsonb using regelinput_periodisering::JSON;
alter table BEREGNINGSGRUNNLAG alter column regelinput_skjaringstidspunkt type jsonb using regelinput_skjaringstidspunkt::JSON;
alter table BEREGNINGSGRUNNLAG alter column regellogg_brukers_status type jsonb using regellogg_brukers_status::JSON;
alter table BEREGNINGSGRUNNLAG alter column regellogg_skjaringstidspunkt type jsonb using regellogg_skjaringstidspunkt::JSON;

alter table BEREGNINGSGRUNNLAG_PERIODE alter column regel_evaluering type jsonb using regel_evaluering::JSON;
alter table BEREGNINGSGRUNNLAG_PERIODE alter column regel_evaluering_fastsett type jsonb using regel_evaluering_fastsett::JSON;
alter table BEREGNINGSGRUNNLAG_PERIODE alter column regel_input type jsonb using regel_input::JSON;
alter table BEREGNINGSGRUNNLAG_PERIODE alter column regel_input_fastsett type jsonb using regel_input_fastsett::JSON;
alter table BEREGNINGSGRUNNLAG_PERIODE alter column regel_input_fastsett_2 type jsonb using regel_input_fastsett_2::JSON;
alter table BEREGNINGSGRUNNLAG_PERIODE alter column regel_evaluering_fastsett_2 type jsonb using regel_evaluering_fastsett_2::JSON;
alter table BEREGNINGSGRUNNLAG_PERIODE alter column regel_input_vilkar type jsonb using regel_input_vilkar::JSON;
alter table BEREGNINGSGRUNNLAG_PERIODE alter column regel_evaluering_vilkar type jsonb using regel_evaluering_vilkar::JSON;
alter table BEREGNINGSGRUNNLAG_PERIODE alter column regel_input_oppdater_svp type jsonb using regel_input_oppdater_svp::JSON;
alter table BEREGNINGSGRUNNLAG_PERIODE alter column regel_evaluering_oppdater_svp type jsonb using regel_evaluering_oppdater_svp::JSON;

alter table BG_PERIODE_REGEL_SPORING alter column regel_evaluering type jsonb using regel_evaluering::JSON;
alter table BG_PERIODE_REGEL_SPORING alter column regel_input type jsonb using regel_input::JSON;

alter table BG_REGEL_SPORING alter column regel_input type jsonb using regel_input::JSON;
alter table BG_REGEL_SPORING alter column regel_evaluering type jsonb using regel_evaluering::JSON;
