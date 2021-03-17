#!/usr/bin/env bash
if [ "$TRAVIS_BRANCH" == 'master' ] && [ "$TRAVIS_PULL_REQUEST" == 'false' ]; then
    mkdir cd/deploy
    openssl aes-256-cbc -pass pass:$GPG_ENCPHRASE -in cd/pubring.gpg.enc -out cd/deploy/pubring.gpg -d
    openssl aes-256-cbc -pass pass:$GPG_ENCPHRASE -in cd/secring.gpg.enc -out cd/deploy/secring.gpg -d
    gpg --fast-import cd/deploy/pubring.gpg
    gpg --fast-import cd/deploy/secring.gpg

    mvn deploy -P ossrh --settings cd/mvnsettings.xml
    # delete decrypted keys
    rm -rf cd/deploy
fi