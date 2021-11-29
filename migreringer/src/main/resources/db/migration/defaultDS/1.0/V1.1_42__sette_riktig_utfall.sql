UPDATE BG_ANDEL_ARBEIDSFORHOLD
SET refusjonskrav_frist_utfall = '-'
where refusjonskrav_frist_utfall = '0';
