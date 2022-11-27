alter table if exists TILKOMMET_INNTEKT
ADD arbeidsforhold_intern_id UUID;

comment on column TILKOMMET_INNTEKT.arbeidsforhold_intern_id is 'Arbeidsforhold intern id';
