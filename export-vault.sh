#!/usr/bin/env bash

if test -f /var/run/secrets/nais.io/serviceuser/password;
then
  export SYSTEMBRUKER_PASSWORD=$(cat /var/run/secrets/nais.io/serviceuser/password)
  echo "Setting SYSTEMBRUKER_PASSWORD"
fi