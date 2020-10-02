delete from REGEL_SPORING_GRUNNLAG where REGEL_INPUT_JSON is null;
alter table REGEL_SPORING_GRUNNLAG drop column REGEL_INPUT;
alter table REGEL_SPORING_GRUNNLAG drop column REGEL_EVALUERING;

insert into REGEL_SPORING_GRUNNLAG (ID, KOBLING_ID, REGEL_EVALUERING_JSON, REGEL_INPUT_JSON, REGEL_TYPE, AKTIV)
(select nextval('SEQ_REGEL_SPORING_GRUNNLAG'), kobling_id, sporing.REGEL_EVALUERING, sporing.REGEL_INPUT, REGEL_TYPE, true
from GR_BEREGNINGSGRUNNLAG gr
inner join BEREGNINGSGRUNNLAG bg on gr.beregningsgrunnlag_id = bg.id
inner join BG_REGEL_SPORING sporing on sporing.bg_id = bg.id
where gr.aktiv = true
and not exists (select 1 from REGEL_SPORING_GRUNNLAG rs where rs.kobling_id = gr.kobling_id
 and rs.REGEL_TYPE = sporing.REGEL_TYPE)
);

delete from REGEL_SPORING_PERIODE where REGEL_INPUT_JSON is null;
alter table REGEL_SPORING_PERIODE drop column REGEL_INPUT;
alter table REGEL_SPORING_PERIODE drop column REGEL_EVALUERING;

insert into REGEL_SPORING_PERIODE (ID, KOBLING_ID, FOM, TOM, REGEL_EVALUERING_JSON, REGEL_INPUT_JSON, REGEL_TYPE, AKTIV)
(
select nextval('SEQ_REGEL_SPORING_PERIODE'), gr.kobling_id,
periode.bg_periode_fom, periode.bg_periode_tom, sporing.REGEL_EVALUERING,
sporing.REGEL_INPUT, sporing.REGEL_TYPE, true
from GR_BEREGNINGSGRUNNLAG gr
inner join BEREGNINGSGRUNNLAG bg on gr.beregningsgrunnlag_id = bg.id
inner join BEREGNINGSGRUNNLAG_PERIODE periode on periode.beregningsgrunnlag_id = bg.id
inner join BG_PERIODE_REGEL_SPORING sporing on sporing.bg_periode_id = periode.id
where gr.aktiv = true
and not exists (select 1 from REGEL_SPORING_PERIODE rs
where rs.kobling_id = gr.kobling_id
and rs.regel_type = sporing.regel_type
and periode.bg_periode_fom = rs.fom)
);
