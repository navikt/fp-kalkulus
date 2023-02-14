ALTER TABLE IF EXISTS BEREGNINGSGRUNNLAG_PERIODE
    ADD INNTEKT_GRADERINGSPROSENT_BRUTTO NUMERIC (19,2);

comment on column BEREGNINGSGRUNNLAG_PERIODE.INNTEKT_GRADERINGSPROSENT_BRUTTO is 'Graderingsprosent ved gradering mot inntekt. Angir totalt bortfalt inntekt av totalt brutto beregningsgrunnlag.';
