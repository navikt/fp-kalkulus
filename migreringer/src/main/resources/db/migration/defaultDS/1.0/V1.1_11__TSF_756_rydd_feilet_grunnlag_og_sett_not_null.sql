delete from gr_beregningsgrunnlag
where aktiv = false
and opprettet_tid < '2020-06-16 00:00:00.000'
and register_aktiviteter_id is null;

alter table gr_beregningsgrunnlag alter column register_aktiviteter_id SET NOT NULL;
