
update BR_SATS
set TOM = to_timestamp('30/04/2020', 'dd/mm/yyyy')
where FOM = to_timestamp('01/05/2019', 'dd/mm/yyyy') and SATS_TYPE = 'GRUNNBELØP';

INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GRUNNBELØP',to_timestamp('01/05/2020', 'dd/mm/yyyy'),to_timestamp('31/12/2099', 'dd/mm/yyyy'),101351,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GSNITT',to_timestamp('01/01/2020', 'dd/mm/yyyy'),to_timestamp('31/12/2020', 'dd/mm/yyyy'),100853,0);
