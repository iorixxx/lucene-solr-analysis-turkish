## Turkish analysis components for Apache Lucene/Solr

The use of *Open Source Software* is gaining increasing momentum in Turkey.
Turkish users on Apache Lucene/Solr (and other [Apache Projects](https://projects.apache.org/projects.html)) mailing lists are increasing.
This project makes use of publicly available Turkish nlp tools to create [Apache Lucene/Solr plugins](https://cwiki.apache.org/confluence/display/solr/Solr+Plugins) from them.
I created this project in order to promote and support open source.
Stock Lucene/Solr has [SnowballPorterFilter(Factory)](https://cwiki.apache.org/confluence/display/solr/Language+Analysis#LanguageAnalysis-Turkish) for the Turkish language.
However, this stemmer performs poorly and has funny collisions.
For example; *altın*, *alim*, *alın*, *altan*, and *alıntı* are all reduced to a same stem.
In other words, they are treated as if they were the same word even though they have completely different meanings.
I will post some other harmful collisions here.

Currently we have five custom TokenFilters.
To load the plugins, place specified JAR files (along with TurkishAnalysis-5.3.0.jar, which can be created by executing `mvn package` command) in a `lib` directory in the Solr Home directory.
This directory does not exist in the distribution, so you would need to create it for the first time. 
The location for the `lib` directory is near the solr.xml file.

1.
**TRMorphStemFilter(Factory)**
___
Turkish Stemmer based on [TRmorph](https://github.com/coltekin/TRmorph).
This one is not production ready yet.
It requires Operating System specific [foma](https://code.google.com/p/foma/) executable.
I couldn't find an elegant way to convert `foma` to java.
I am using *"executing shell commands in Java to call `flookup`"* workaround advised in [FAQ] (http://code.google.com/p/foma/wiki/FAQ).
If you know something better please let me know.

**Arguments**
  * `lookup`: Absolute path of the OS specific [foma](https://code.google.com/p/foma/) executable.
  * `fst`: Absolute path of the stem.fst file.

**Example**
``` xml
<analyzer>
  <tokenizer class="solr.StandardTokenizerFactory"/>
  <filter class="org.apache.lucene.analysis.tr.TRMorphStemFilterFactory" lookup="/Applications/foma/flookup" fst="/Volumes/datadisk/Desktop/TRmorph-master/stem.fst" />
</analyzer>
```

2.
**Zemberek2StemFilter(Factory)**
___
Turkish Stemmer based on [Zemberek2](https://code.google.com/p/zemberek/).

**JARs**: zemberek-cekirdek-2.1.3.jar zemberek-tr-2.1.3.jar

**Arguments**
  * `strategy`: Strategy to choose one of the multiple stem forms. Valid values are maxLength (the default), minLength, maxMorpheme, minMorpheme, frequency, or first.
  * `dictionary`: Zemberek3's dictionary (*.dict) files, which can be download from [here](https://github.com/ahmetaa/zemberek-nlp/tree/master/morphology/src/main/resources/tr) and could be modified if required.

**Example**
``` xml
<analyzer>
  <tokenizer class="solr.StandardTokenizerFactory"/>
  <filter class="org.apache.lucene.analysis.tr.Zemberek2StemFilterFactory" strategy="minMorpheme"/>
</analyzer>
```

3.
**Zemberek2DeASCIIfyFilter(Factory)**
___
Turkish DeASCIIfier based on [Zemberek2](https://code.google.com/p/zemberek/).

**JARs**: zemberek-cekirdek-2.1.3.jar zemberek-tr-2.1.3.jar

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
___
Turkish Stemmer based on [Zemberek3](https://github.com/ahmetaa/zemberek-nlp)
Download [tr](https://github.com/iorixxx/zemberek-nlp/tree/master/morphology/src/main/resources/tr) folder which contains dictionary files, and put it under solr/collection1/conf.
Please note that zemberek-* jars should be generated from [my fork](https://github.com/iorixxx/zemberek-nlp/).
Here is the [difference](https://github.com/iorixxx/zemberek-nlp/commit/3926bcf3bc719da874e72089d854532cde37d42b) over original repository.

**JARs**: zemberek-morphology-0.9.1.jar zemberek-core-0.9.1.jar

**Arguments**
  * `strategy`: Strategy to choose one of the multiple stem forms by selecting either longest or shortest stem. Valid values are maxLength (the default) or minLength.
  * `dictionary`: Zemberek3's dictionary (*.dict) files, which can be download from [here](https://github.com/ahmetaa/zemberek-nlp/tree/master/morphology/src/main/resources/tr) and could be modified if required.

**Example**
``` xml
<analyzer>
  <tokenizer class="solr.StandardTokenizerFactory"/>
  <filter class="org.apache.lucene.analysis.tr.Zemberek3StemFilterFactory" strategy="maxLength" dictionary="tr/master-dictionary.dict,tr/secondary-dictionary.dict,tr/non-tdk.dict,tr/proper.dict"/>
</analyzer>
```

5.
**TurkishDeASCIIfyFilter(Factory)**
___
Translation of [Emacs Turkish mode](http://www.denizyuret.com/2006/11/emacs-turkish-mode.html) from Lisp into Java.
This filter is intended to be used to allow *diacritics-insensitive search* for Turkish.

**Arguments**
  * `preserveOriginal`: (true/false) If **true**, the original token is preserved. The default is **false**.

**Example**
``` xml
<analyzer>
  <tokenizer class="solr.StandardTokenizerFactory"/>
  <filter class="org.apache.lucene.analysis.tr.TurkishDeASCIIfyFilterFactory" preserveOriginal="false"/>
</analyzer>
 ```

I will post benchmark results of different field types (different stemmers) designed for different use-cases.

## Dependencies
* JRE 1.7 or above
* Apache Maven 3.0.3 or above
* Apache Lucene (Solr) 5.3.0

## Author
Please feel free to contact Ahmet Arslan at `iorixxx at yahoo dot com` if you have any questions, comments or contributions.