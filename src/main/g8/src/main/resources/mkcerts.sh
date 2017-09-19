#!/usr/bin/env bash

SRC="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~`
# #make-tutorial-cert
PASSWD=changeit

# It creates/updates a key store with
#
#    - a new public/private key pair, and
#    - a new certificate owned by tutorial.com
#
# #make-tutorial-cert

THIS=$SRC
rm $THIS/tutorial.com.* $THIS/root-ca.*

# #make-tutorial-cert
keytool -genkeypair -v \
  -alias tutorial.com \
  -dname "CN=tutorial.com, OU=Tutorial Org, O=Tutorial Company, L=London, ST=London, C=UK" \
  -keystore $THIS/tutorial.com.jks \
  -keypass $PASSWD \
  -storepass $PASSWD \
  -keyalg RSA \
  -keysize 2048 \
  -validity 365
# #make-tutorial-cert



# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
# #make-ca-cert
# For test purposes, it creates/updates a "fake" root CA
# key store with:
#
#    - a new public/private key pair, and
#    - a new self-signed root CA certificate
#
# This fake root CA will issue/sign the tutorial.com certificate
#
keytool -genkeypair -v \
  -alias root-ca \
  -dname "CN=Root Authority, OU=Root Org, O=Root Company, L=San Francisco, ST=California, C=US" \
  -keystore $THIS/root-ca.jks \
  -keypass $PASSWD \
  -storepass $PASSWD \
  -keyalg RSA \
  -keysize 2048 \
  -ext KeyUsage:critical="keyCertSign" \
  -ext BasicConstraints:critical="ca:true" \
  -validity 9999

# Exports the root CA certificate from the above key store to the
# root-ca.crt file, so that it can easily be imported into client's
# trust stores
#
keytool -export -v \
  -alias root-ca \
  -file $THIS/root-ca.crt \
  -keypass $PASSWD \
  -storepass $PASSWD \
  -keystore $THIS/root-ca.jks \
  -rfc
# #make-ca-cert


# Imports the root CA certificate into the tutorial.com trust store
#
keytool -import -v \
  -alias root-ca \
  -file $THIS/root-ca.crt \
  -keystore $THIS/tutorial.com.jks \
  -storetype JKS \
  -storepass $PASSWD<< EOF
yes
EOF



# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
# #csr
# Creates a CSR - Certificate Signing Request for the
# tutorial.com certificate
#
keytool -certreq -v \
  -alias tutorial.com \
  -keypass $PASSWD \
  -storepass $PASSWD \
  -keystore $THIS/tutorial.com.jks \
  -file $THIS/tutorial.com.csr

# Simulates CSR submission and completion of the
# tutorial.com certificate
#
keytool -gencert -v \
  -alias root-ca \
  -keypass $PASSWD \
  -storepass $PASSWD \
  -keystore $THIS/root-ca.jks \
  -infile $THIS/tutorial.com.csr \
  -outfile $THIS/tutorial.com.crt \
  -ext KeyUsage:critical="digitalSignature,keyEncipherment" \
  -ext EKU="serverAuth" \
  -ext SAN="DNS:tutorial.com" \
  -rfc

# Finally, imports the signed certificate back into
# the tutorial.com key store
#
keytool -import -v \
  -alias tutorial.com \
  -file $THIS/tutorial.com.crt \
  -keystore $THIS/tutorial.com.jks \
  -storetype JKS \
  -storepass $PASSWD
# #csr


# List out the contents of $THIS/tutorial.com.jks just to confirm it.
# If you are using Play as a TLS termination point,
# this is the key store you should present as the server.
keytool -list -v \
  -keystore $THIS/tutorial.com.jks \
  -storepass $PASSWD

echo "Password is $PASSWD"
