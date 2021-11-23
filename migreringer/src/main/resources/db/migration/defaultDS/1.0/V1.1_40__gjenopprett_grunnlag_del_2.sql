/* Saksnummer ACRD8, kobling: 2142652 */
update gr_beregningsgrunnlag
set aktiv = true
where id = 5009832;

/* Saksnummer AEGMS, kobling: 2147782 */
/* Fikser sak som tidligere ble satt til aktiv feilaktig */
update gr_beregningsgrunnlag
set aktiv = false
where id = 2147782;

/* Setter riktig grunnlag til aktiv */
update kalkulator_input
set aktiv = true
where id = 1797257;

update gr_beregningsgrunnlag
set aktiv = true
where id = 5040445;

/* Saksnummer ACNCS, kobling: 2146091 */
/* Fikser sak som tidligere ble satt til aktiv feilaktig */
update gr_beregningsgrunnlag
set aktiv = false
where id = 2146091;

/* Setter riktig grunnlag til aktiv */
update gr_beregningsgrunnlag
set aktiv = true
where id = 5030292;
