lucene-solr-analysis-turkish
============================

Turkish analysis components for Lucene/Solr 

*open source* usage gaining momentum in Turkey. Turkish users on lucene/solr mailing lists are increasing.
This project makes use of publicly available Turkish nlp tools to create lucene/solr plugins from them.
In order to promote and support open source I created this project. Stock lucene/solr has Snowball for Turkish Stemmer.
However this stemmer performs poorly and has funny collisions. I will post some of the collisions here.

Currently we have three TokenFilters.

1. **TRMorphStemFilter(Factory)**
Turkish Stemmer based on [TRmorph](https://github.com/coltekin/TRmorph)
This one is not production ready yet. It requires OS specific [foma](https://code.google.com/p/foma/) executable.
I couldn't find an elegant way to convert foma to java.

2. **ZemberekStemFilter(Factory)**
Turkish Stemmer based on [Zemberek3](https://github.com/ahmetaa/zemberek-nlp)

3. **TurkishDeasciifyFilter(Factory)**
Translation of [Turkish Deasciifier](https://github.com/emres/turkish-deasciifier) from Python to Java.
This filter intended to be used at query time to allow *diacritics-insensitive search* for Turkish.

##Dependencies

* JRE 1.7 or above
* Apache Maven 3.0.3 or above
* Apache Lucene (Solr) 4.x