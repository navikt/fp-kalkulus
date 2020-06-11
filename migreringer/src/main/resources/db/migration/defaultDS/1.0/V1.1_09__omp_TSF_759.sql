UPDATE kalkulator_input
SET aktiv = true
FROM kobling
WHERE kalkulator_input.kobling_id = kobling.id AND
      kalkulator_input.id = 1017915 AND kalkulator_input.endret_tid = '2020-05-26 09:17:46.061' AND kobling.saksnummer = '6DQG4';

UPDATE kalkulator_input
SET aktiv = true
FROM kobling
WHERE kalkulator_input.kobling_id = kobling.id AND
      kalkulator_input.id = 1018769 AND kalkulator_input.endret_tid = '2020-05-27 10:18:16.744' AND kobling.saksnummer = '68FZ6';
