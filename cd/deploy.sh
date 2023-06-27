#!/usr/bin/env bash
if [ "$TRAVIS_BRANCH" == 'master' ] && [ "$TRAVIS_PULL_REQUEST" == 'false' ]; then
    mkdir cd/deploy

    openssl aes-256-cbc -pass pass:$GPG_ENCPHRASE -in cd/pubkeys.asc.enc -out cd/deploy/pubkeys.asc -pbkdf2 -d
    openssl aes-256-cbc -pass pass:$GPG_ENCPHRASE -in cd/prikeys.asc.enc -out cd/deploy/prikeys.asc -pbkdf2 -d
    gpg --batch --fast-import cd/deploy/pubkeys.asc
    gpg --batch --fast-import cd/deploy/prikeys.asc

    mvn deploy -P ossrh --settings cd/mvnsettings.xml
    # delete decrypted keys
    rm -rf cd/deploy
fi