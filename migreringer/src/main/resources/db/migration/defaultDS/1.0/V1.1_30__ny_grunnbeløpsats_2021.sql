update BR_SATS
set TOM = to_timestamp('30/04/2021', 'dd/mm/yyyy')
where FOM = to_timestamp('01/05/2020', 'dd/mm/yyyy') and SATS_TYPE = 'GRUNNBELØP';

INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GRUNNBELØP',to_timestamp('01/05/2021', 'dd/mm/yyyy'),to_timestamp('31/12/2099', 'dd/mm/yyyy'),106399,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GSNITT',to_timestamp('01/01/2021', 'dd/mm/yyyy'),to_timestamp('31/12/2021', 'dd/mm/yyyy'),104716,0);
