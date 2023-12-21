create index idx_kobling_grunnlagskopi_sporing_3
    on kobling_grunnlagskopi_sporing (kopiert_til_kobling_id)
    where aktiv = true;
