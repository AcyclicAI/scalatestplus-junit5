#!/bin/bash

# Script to run sbt ci-release for publishing to Sonatype Central

# Exit on error
set -e

# Set required environment variables
export CI=true  # Required by sbt-ci-release
export CI_COMMIT_TAG="." # should be set but value doesn't matter, see https://github.com/sbt/sbt-ci-release/issues/392

# Sonatype credentials
# For new Sonatype Central Portal, you need to use a bearer token
# Get your token from: https://central.sonatype.com/account
export SONATYPE_USERNAME="${SONATYPE_USERNAME:-ro41jk}"
export SONATYPE_PASSWORD="${SONATYPE_PASSWORD:-9KAiRPHEKIHGy3GfbeTq5IcEqTkrshXbe}"

# The new Sonatype Central uses different authentication
# You may need to set these instead:
# export SONATYPE_CREDENTIAL_HOST="central.sonatype.com"
# export SONATYPE_BEARER_TOKEN="${SONATYPE_BEARER_TOKEN:-your-bearer-token-here}"

# PGP configuration
export PGP_PASSPHRASE="${PGP_PASSPHRASE:-A9e7k1_gpg}"
export PGP_SECRET="$(base64 -w 0 /home/shared/Synced/Dropbox/.ssh/GPG/tribbloid-private.asc)"

sbt ci-release
