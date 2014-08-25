#!/bin/sh

rm -rf out_*.txt
rm -rf gdeval_*.txt
rm -rf risk_sensitive_*.txt

FILES=*submitted.txt

for f in $FILES; 
do  
	if  ! [[ $f == out_* ]] || [[ $f == gdeval_* ]] || [[ $f == risk_sensitive_gdeval_* ]];
	then
	./trec_eval -q -c -M1000 qrelsMC.txt $f > "out_${f##/*/}"
	fi
done

#<<COMMENT1


# gdeval ERR@20 and NCDG@20
for f in $FILES; 
do  
	if  ! [[ $f == out_* ]] || [[ $f == gdeval_* ]] || [[ $f == risk_sensitive_gdeval_* ]];
	then
    	./gdeval.pl -c qrelsMC.txt $f > "gdeval_${f##/*/}"
	fi
done

# risk sensitive evaluation
for ALPHA in 1 5;
do
for QUERYLENGTH in QS QM; 
do
	for STEMMER in ns f5 snowball zemberek2; 
	do
		for APP in ascii zemberek2_deascii turkish_deascii; 
		do
			BASELINE=$(printf "tr_%s_%s_submitted.txt" "$STEMMER" "$QUERYLENGTH");			
			TESTRUN=$(printf "%s_%s_%s_submitted.txt" "$APP" "$STEMMER" "$QUERYLENGTH");
			
			RESULT=$(printf "risk_sensitive_%s_%s" "$ALPHA" "$TESTRUN");
			./gdeval.pl -riskAlpha $ALPHA -baseline $BASELINE qrelsMC.txt $TESTRUN > $RESULT;
			echo "gdeval.pl -riskAlpha $ALPHA -baseline $BASELINE qrelsMC.txt $TESTRUN > $RESULT"; 			
		done
	done
done
done


