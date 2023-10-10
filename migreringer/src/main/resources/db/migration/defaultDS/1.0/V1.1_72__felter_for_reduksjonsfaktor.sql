ALTER TABLE IF EXISTS BEREGNINGSGRUNNLAG_PERIODE ADD reduksjonsfaktor_inaktiv_type_a NUMERIC (19,4);
comment on column BEREGNINGSGRUNNLAG_PERIODE.reduksjonsfaktor_inaktiv_type_a is 'Reduksjonsfaktor benyttet ved midlertidig inaktiv type A (§8-47a)';
