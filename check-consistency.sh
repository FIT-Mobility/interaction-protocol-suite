#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

cd $DIR

RETVAL=0

MAIN_VERSION=`grep "resolved.*interaction-protocol-suite.git\#" main/yarn.lock | grep -o 'interaction-protocol-suite.git#[^"]*' | cut -d'#' -f2`

while IFS= read -r result
do
	first=${result:0:1}
	rest="${result:1}"
	sha=`echo $rest | cut -d' ' -f1`
	path=`echo $rest | cut -d' ' -f2`
	if [ "-" = "$first" ]; then
		echo "submodule $path not initialized"
		RETVAL=-1
	fi
	if [ "+" = "$first" ]; then
		echo "currently checked out commit does not match SHA-1 found in index for $path; try running 'git submodule update'"
		RETVAL=-1
	fi
	if [ "$MAIN_VERSION" != "$sha" ]; then
		echo "main version ($MAIN_VERSION) differs from version $sha in $path"
		RETVAL=-1
	fi
done < <(git submodule status)

API_VERSION=`grep "resolved.*interaction-protocol-suite.git\#" server/api/yarn.lock | grep -o 'interaction-protocol-suite.git#[^"]*' | cut -d'#' -f2`

if [ "$MAIN_VERSION" != "$API_VERSION" ]; then
	echo "main version ($MAIN_VERSION) differs from api version ($API_VERSION)"
	RETVAL=-1
fi

exit $RETVAL
