UPDATE BG_ANDEL_ARBEIDSFORHOLD
SET refusjonskrav_frist_utfall = 'GODKJENT'
where refusjonskrav_frist_utfall = '-'
  and opprettet_tid <= '2021-11-29 08:16:08.089000';

UPDATE BG_ANDEL_ARBEIDSFORHOLD
SET refusjonskrav_frist_utfall = 'UNDERKJENT'
where refusjonskrav_frist_utfall = '1'
  and opprettet_tid <= '2021-11-25 09:42:49.488000';


