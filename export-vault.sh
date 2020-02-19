#!/usr/bin/env bash

if test -f /var/run/secrets/nais.io/serviceuser/username;
then
  export SYSTEMBRUKER_USERNAME=$(cat /var/run/secrets/nais.io/serviceuser/username)
  echo "Setting SYSTEMBRUKER_USERNAME"
fi

if test -f /var/run/secrets/nais.io/serviceuser/password;
then
  export SYSTEMBRUKER_PASSWORD=$(cat /var/run/secrets/nais.io/serviceuser/password)
  echo "Setting SYSTEMBRUKER_PASSWORD"
fi

if test -f /var/run/secrets/nais.io/ldap/username;
then
  export LDAP_USERNAME=$(cat /var/run/secrets/nais.io/ldap/username)
  echo "Setting LDAP_USERNAME"
fi

if test -f /var/run/secrets/nais.io/ldap/password;
then
  export LDAP_PASSWORD=$(cat /var/run/secrets/nais.io/ldap/password)
  echo "Setting LDAP_PASSWORD"
fi

