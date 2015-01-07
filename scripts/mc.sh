#!/bin/sh

# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

QRELS="/Users/iorixxx/Dropbox/diacritic/qrelsMC.txt";
gdeval="/Users/iorixxx/Dropbox/diacritic/gdeval.pl";
trec_eval="/Users/iorixxx/Dropbox/diacritic/trec_eval";

RUNS="/Volumes/data/diacritics/runs";
EVALS="/Volumes/data/diacritics/evals";

readonly QRELS gdeval trec_eval EVALS RUNS

mkdir -p "${EVALS}"

for core in catA catB;
do
    mkdir -p "/Volumes/data/diacritics/evals/$core"
done

for core in catA catB;
do
    for f in ${RUNS}/${core}/*submitted.txt;
    do
    ${trec_eval} -q -c -M1000 ${QRELS} ${f} > "${EVALS}/$core/out_${f##/*/}"
    ${gdeval} -c ${QRELS} ${f} > "${EVALS}/$core/gdeval_${f##/*/}"
    done
done

#<<COMMENT1

# risk-sensitive evaluation
for core in catA catB;
do
for ALPHA in 1 5;
do
for QUERYLENGTH in QS QM; 
do
	for STEMMER in ns f5 snowball zemberek2; 
	do
		for APP in ascii zemberek2_deascii turkish_deascii; 
		do
			BASELINE=$(printf "${RUNS}/%s/tr_%s_%s_submitted.txt" "$core" "$STEMMER" "$QUERYLENGTH");
			TESTRUN=$(printf "${RUNS}/%s/%s_%s_%s_submitted.txt" "$core" "$APP" "$STEMMER" "$QUERYLENGTH");
			
			RESULT=$(printf "${EVALS}/%s/risk_sensitive_%s_%s" "$core" "$ALPHA" "${TESTRUN##/*/}");
			${gdeval} -riskAlpha ${ALPHA} -baseline ${BASELINE} ${QRELS} ${TESTRUN} > ${RESULT};
			echo "gdeval.pl -riskAlpha $ALPHA -baseline $BASELINE $QRELS $TESTRUN > $RESULT";
		done
	done
done
done
done


