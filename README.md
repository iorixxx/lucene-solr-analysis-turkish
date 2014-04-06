lucene-solr-analysis-turkish
============================

Turkish analysis components for Lucene/Solr 

Currently we have three TokenFilters.

1. TRMorphStemFilter(Factory)
Turkish Stemmer based on [TRmorph](https://github.com/coltekin/TRmorph)
This one is not production ready yet. It requires [foma](https://code.google.com/p/foma/)
I couldn't find an elegant way to convert foma to java.

2. ZemberekStemFilter
Turkish Stemmer based on [Zemberek3](https://github.com/ahmetaa/zemberek-nlp)

3. TurkishDeasciifyFilter
Translation of [Turkish Deasciifier](https://github.com/emres/turkish-deasciifier) from Python to Java