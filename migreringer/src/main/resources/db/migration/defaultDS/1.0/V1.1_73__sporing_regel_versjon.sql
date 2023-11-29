ALTER TABLE IF EXISTS REGEL_SPORING_GRUNNLAG ADD regel_versjon varchar(100);
comment on column REGEL_SPORING_GRUNNLAG.regel_versjon is 'Versjon av beregningsreglene som er brukt';

ALTER TABLE IF EXISTS REGEL_SPORING_PERIODE ADD regel_versjon varchar(100);
comment on column REGEL_SPORING_PERIODE.regel_versjon is 'Versjon av beregningsreglene som er brukt';

