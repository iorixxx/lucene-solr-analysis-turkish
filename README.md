lucene-solr-analysis-turkish
============================

Turkish analysis components for Apache Lucene/Solr 5.3.0

The use of *Open Source Software* is gaining increasing momentum in Turkey. Turkish users on Apache Lucene/Solr (and other Apache projects) mailing lists are increasing.
This project makes use of publicly available Turkish nlp tools to create [Apache Lucene/Solr plugins](https://cwiki.apache.org/confluence/display/solr/Solr+Plugins) from them.
I created this project in order to promote and support open source. Stock Lucene/Solr has
[SnowballPorterFilter(Factory)](https://cwiki.apache.org/confluence/display/solr/Language+Analysis#LanguageAnalysis-Turkish)
for the Turkish language. However, this stemmer performs poorly and has funny collisions.
For example; altın, alim, alın, altan, and alıntı are all reduced to a same stem.
In other words, they are treated as if they were the same word even though they have different meanings.
I will post some other harmful collisions here.

Currently we have five custom TokenFilters. To load the plugins place specified JAR files in a `lib` directory in the Solr Home directory.
This directory does not exist in the distribution, so you would need to create it for the first time. The location is near the solr.xml file.

1.
**TRMorphStemFilter(Factory)**
Turkish Stemmer based on [TRmorph](https://github.com/coltekin/TRmorph)
This one is not production ready yet. It requires Operating System specific [foma](https://code.google.com/p/foma/) executable.
I couldn't find an elegant way to convert `foma` to java. I am using *"executing shell commands in Java to call `flookup`"* workaround advised in [FAQ] (http://code.google.com/p/foma/wiki/FAQ). If you know something better please let me know.

**Arguments**
  * `lookup`: Absolute path of the OS specific [foma](https://code.google.com/p/foma/) executable.
  * `fst`:  Absolute path of the stem.fst file.

**Example**
``` xml
<fieldType name="text_tr_morph" class="solr.TextField" positionIncrementGap="100">
  <analyzer>
    <tokenizer class="solr.StandardTokenizerFactory"/>
    <filter class="solr.ApostropheFilterFactory"/>
    <filter class="solr.TurkishLowerCaseFilterFactory"/>
    <filter class="org.apache.lucene.analysis.tr.TRMorphStemFilterFactory" lookup="/Applications/foma/flookup" fst="/Volumes/datadisk/Desktop/TRmorph-master/stem.fst" />
  </analyzer>
</fieldType>
```

2.
**Zemberek2StemFilter(Factory)**
Turkish Stemmer based on [Zemberek2](https://code.google.com/p/zemberek/)
You need two jars : zemberek-cekirdek-2.1.3.jar zemberek-tr-2.1.3.jar TurkishAnalysis-5.3.0.jar inside solr/collection1/lib directory.

``` xml
<fieldType name="text_tr_zemberek2" class="solr.TextField" positionIncrementGap="100">
  <analyzer>
    <tokenizer class="solr.StandardTokenizerFactory"/>
    <filter class="solr.ApostropheFilterFactory"/>
    <filter class="solr.TurkishLowerCaseFilterFactory"/>
    <filter class="org.apache.lucene.analysis.tr.Zemberek2StemFilterFactory" strategy="minMorpheme"/>
  </analyzer>
</fieldType>
```

3.
**Zemberek2DeASCIIfyFilter(Factory)**
Turkish DeASCIIfier based on [Zemberek2](https://code.google.com/p/zemberek/)
You need two jars : zemberek-cekirdek-2.1.3.jar zemberek-tr-2.1.3.jar TurkishAnalysis-5.3.0.jar inside solr/collection1/lib directory.

**Arguments**: None

**Example**

``` xml
<analyzer>
  <tokenizer class="solr.StandardTokenizerFactory"/>
  <filter class="org.apache.lucene.analysis.tr.Zemberek2DeASCIIfyFilterFactory"/>   
</analyzer>
```

4.
**Zemberek3StemFilter(Factory)**
Turkish Stemmer based on [Zemberek3](https://github.com/ahmetaa/zemberek-nlp)
Download [tr](https://github.com/iorixxx/zemberek-nlp/tree/master/morphology/src/main/resources/tr) folder which contains dictionary files, and put it under solr/collection1/conf.
You need three jars : zemberek-morphology-0.9.1.jar zemberek-core-0.9.1.jar TurkishAnalysis-5.3.0.jar inside solr/collection1/lib directory. Please note that zemberek-* jars need to generated from [my fork](https://github.com/iorixxx/zemberek-nlp/).
Here is the [difference](https://github.com/iorixxx/zemberek-nlp/commit/3926bcf3bc719da874e72089d854532cde37d42b) over original repository.

**Arguments**
  * `strategy`: Strategy to choose one of the multiple stem forms by selecting either longest or shortest stem. Valid values are maxLength (the default) or minLength.
  * `dictionary`: Zemberek3's dictionary (*.dict) files, which can be download from [here](https://github.com/ahmetaa/zemberek-nlp/tree/master/morphology/src/main/resources/tr) and could be modified if required.

**Example**
``` xml
<fieldType name="text_tr_zemberek3" class="solr.TextField" positionIncrementGap="100">
  <analyzer>
    <tokenizer class="solr.StandardTokenizerFactory"/>
    <filter class="solr.ApostropheFilterFactory"/>
    <filter class="solr.TurkishLowerCaseFilterFactory"/>
    <filter class="org.apache.lucene.analysis.tr.Zemberek3StemFilterFactory" strategy="maxLength" dictionary="tr/master-dictionary.dict,tr/secondary-dictionary.dict,tr/non-tdk.dict,tr/proper.dict"/>
  </analyzer>
</fieldType>
```

5.
**TurkishDeASCIIfyFilter(Factory)**
Translation of [Emacs Turkish mode](http://www.denizyuret.com/2006/11/emacs-turkish-mode.html) from Lisp into Java.
This filter is intended to be used to allow *diacritics-insensitive search* for Turkish.

**Arguments**
  * `preserveOriginal`: (true/false) If **true**, the original token is preserved. The default is **false**.

**Example**
``` xml
<fieldType name="text_tr_deascii" class="solr.TextField" positionIncrementGap="100">
   <analyzer>
     <tokenizer class="solr.StandardTokenizerFactory"/>
     <filter class="solr.ApostropheFilterFactory"/>
     <filter class="solr.TurkishLowerCaseFilterFactory"/>
     <filter class="org.apache.lucene.analysis.tr.TurkishDeASCIIfyFilterFactory" preserveOriginal="false"/>
     <filter class="org.apache.lucene.analysis.tr.Zemberek3StemFilterFactory" strategy="maxLength" dictionary="tr/master-dictionary.dict,tr/secondary-dictionary.dict,tr/non-tdk.dict,tr/proper.dict"/>
   </analyzer>
 </fieldType>
 ```

I will post benchmark results of different field types (different stemmers) designed for different use-cases.

##Dependencies
* JRE 1.7 or above
* Apache Maven 3.0.3 or above
* Apache Lucene (Solr) 5.3.0

##Author
Please feel free to contact Ahmet Arslan at `iorixxx at yahoo dot com` if you have any questions, comments or contributions.