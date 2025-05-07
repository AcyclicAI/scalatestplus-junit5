#!/bin/bash

# Script to run sbt ci-release for publishing to Sonatype Central

# Exit on error
set -e

# PGP configuration
export PGP_PASSPHRASE="${PGP_PASSPHRASE:-A9e7k1_gpg}"
export PGP_SECRET="$(base64 -w 0 /home/shared/Synced/Dropbox/.ssh/GPG/tribbloid-private.asc)"

sbt +publishSigned
sbt +publishLocalSigned
#sbt +publishM2Signed

