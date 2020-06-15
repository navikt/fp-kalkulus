UPDATE kalkulator_input
SET aktiv = true
FROM kobling
WHERE kalkulator_input.kobling_id = kobling.id AND
      kalkulator_input.id = 1017915 AND kalkulator_input.endret_tid = '2020-05-28 15:31:37.780' AND kobling.saksnummer = '6DQG4';
