## Turkish analysis components for Apache Lucene/Solr

The use of *Open Source Software* is gaining increasing momentum in Turkey.
Turkish users on Apache Lucene/Solr (and other [Apache Projects](https://projects.apache.org/projects.html)) mailing lists are increasing.
This project makes use of publicly available Turkish NLP tools to create [Apache Lucene/Solr plugins](https://cwiki.apache.org/confluence/display/solr/Solr+Plugins) from them.
I created this project in order to promote and support open source.
Stock Lucene/Solr has [SnowballPorterFilter(Factory)](https://cwiki.apache.org/confluence/display/solr/Language+Analysis#LanguageAnalysis-Turkish) for the Turkish language.
However, this stemmer performs poorly and has funny collisions.
For example; *altın*, *alim*, *alın*, *altan*, and *alıntı* are all reduced to a same stem.
In other words, they are treated as if they were the same word even though they have completely different meanings.
I will post some other harmful collisions here.

To make the best out of this library quickly, without going much into details, please do either:

##### TurkishAnalyzer for Solr Users
If you are a Solr user, please use the following field type definition for Turkish.
``` xml
    <!-- Turkish -->
    <dynamicField name="*_txt_tr" type="text_tr"  indexed="true"  stored="true"/>
    <fieldType name="text_tr" class="solr.TextField" positionIncrementGap="100">
      <analyzer> 
        <tokenizer class="solr.StandardTokenizerFactory"/>
        <filter class="solr.ApostropheFilterFactory"/>
        <filter class="solr.TurkishLowerCaseFilterFactory"/>
        <filter class="org.apache.lucene.analysis.tr.Zemberek3StemFilterFactory"/>
      </analyzer>
    </fieldType>
```

##### TurkishAnalyzer for Lucene Users
If you are a Lucene user, please use the following custom analyzer declaration to create an analyzer for Turkish.
``` java
  Analyzer analyzer = CustomAnalyzer.builder()
                .withTokenizer("standard")
                .addTokenFilter("apostrophe")
                .addTokenFilter("turkishlowercase")
                .addTokenFilter(Zemberek3StemFilterFactory.class)
                .build();
```

Currently we have five custom TokenFilters.
To load the plugins, place specified JAR files (along with TurkishAnalysis-6.2.1.jar, which can be created by executing `mvn package` command) in a `lib` directory in the Solr Home directory.
This directory does not exist in the distribution, so you would need to create it for the first time. 
The location for the `lib` directory is near the solr.xml file.
#### TurkishDeASCIIfyFilter(Factory)
___
Translation of [Emacs Turkish mode](http://www.denizyuret.com/2006/11/emacs-turkish-mode.html) from Lisp into Java.
This filter is intended to be used to allow *diacritics-insensitive search* for Turkish.

**Arguments**:
  * `preserveOriginal`: (true/false) If **true**, the original token is preserved. The default is **false**.

**Example**:
``` xml
<analyzer>
  <tokenizer class="solr.StandardTokenizerFactory"/>
  <filter class="org.apache.lucene.analysis.tr.TurkishDeASCIIfyFilterFactory" preserveOriginal="false"/>
</analyzer>
 ```

#### Zemberek3StemFilter(Factory)
___
Turkish Stemmer based on [Zemberek3](https://github.com/ahmetaa/zemberek-nlp).

**JARs**: zemberek-morphology-0.9.2.jar zemberek-core-0.9.2.jar

**Arguments**:
  * `strategy`: Strategy to choose one of the multiple stem forms by selecting either longest or shortest stem. Valid values are maxLength (the default) or minLength.
  * `dictionary`: Zemberek3's dictionary (*.dict) files, which can be download from [here](https://github.com/ahmetaa/zemberek-nlp/tree/master/morphology/src/main/resources/tr) and could be modified if required.
  You may want to add new dictionary items especially for product search. Usually product titles and descriptions are not pure Turkish. 
  When it comes to product search, you may be well familiar with product titles such as `Amigalar için oyun`, `iPadler için çanta`, and so on.
  If you want to handle such non-Turkish product names inflected with Turkish suffixes, the most elegant way is to modify the dictionaries.
  See the [example](https://github.com/ahmetaa/turkish-nlp-examples/blob/master/src/main/java/morphology/AddNewDictionaryItem.java) that adds `tweetlemek` as a verb to the dictionary, so that `tweetledim`, `tweetlemişler`, etc get recognized and stemmed correctly.   

**Example**:
``` xml
<analyzer>
  <tokenizer class="solr.StandardTokenizerFactory"/>
  <filter class="org.apache.lucene.analysis.tr.Zemberek3StemFilterFactory" strategy="maxLength" dictionary="tr/master-dictionary.dict,tr/secondary-dictionary.dict,tr/non-tdk.dict,tr/proper.dict"/>
</analyzer>
```

  If you are happy with the standard dictionaries that shipped with Zemberek3, or you don't intent to alter them, you may prefer to use the no-args directive.
``` xml
    <filter class="org.apache.lucene.analysis.tr.Zemberek3StemFilterFactory"/>
```

#### Zemberek2StemFilter(Factory)
___
Turkish Stemmer based on [Zemberek2](https://code.google.com/p/zemberek/).

**JARs**: zemberek-cekirdek-2.1.3.jar zemberek-tr-2.1.3.jar

**Arguments**:
  * `strategy`: Strategy to choose one of the multiple stem forms. Valid values are maxLength (the default), minLength, maxMorpheme, minMorpheme, frequency, or first.
  
**Example**:
``` xml
<analyzer>
  <tokenizer class="solr.StandardTokenizerFactory"/>
  <filter class="org.apache.lucene.analysis.tr.Zemberek2StemFilterFactory" strategy="minMorpheme"/>
</analyzer>
```

#### Zemberek2DeASCIIfyFilter(Factory)
___
Turkish DeASCIIfier based on [Zemberek2](https://code.google.com/p/zemberek/).

**JARs**: zemberek-cekirdek-2.1.3.jar zemberek-tr-2.1.3.jar

**Arguments**: None

**Example**:
``` xml
<analyzer>
  <tokenizer class="solr.StandardTokenizerFactory"/>
  <filter class="org.apache.lucene.analysis.tr.Zemberek2DeASCIIfyFilterFactory"/>   
</analyzer>
```

#### TRMorphStemFilter(Factory)
___
Turkish Stemmer based on [TRmorph](https://github.com/coltekin/TRmorph).
This one is not production ready yet.
It requires Operating System specific [foma](https://code.google.com/p/foma/) executable.
I couldn't find an elegant way to convert `foma` to java.
I am using *"executing shell commands in Java to call `flookup`"* workaround advised in [FAQ] (http://code.google.com/p/foma/wiki/FAQ).
If you know something better please let me know.

**Arguments**:
  * `lookup`: Absolute path of the OS specific [foma](https://code.google.com/p/foma/) executable.
  * `fst`: Absolute path of the stem.fst file.

**Example**:
``` xml
<analyzer>
  <tokenizer class="solr.StandardTokenizerFactory"/>
  <filter class="org.apache.lucene.analysis.tr.TRMorphStemFilterFactory" lookup="/Applications/foma/flookup" fst="/Volumes/datadisk/Desktop/TRmorph-master/stem.fst" />
</analyzer>
```

I will post benchmark results of different field types (different stemmers) designed for different use-cases.

## Dependencies
* JRE 1.8 or above
* Apache Maven 3.0.3 or above
* Apache Lucene (Solr) 6.2.1 or 

## Author
Please feel free to contact Ahmet Arslan at `iorixxx at yahoo dot com` if you have any questions, comments or contributions.

## Citation Policy
If you use this library for a research purpose, please use the following citation:

``` tex
@article{
  author = "Ahmet Arslan",
  title = "DeASCIIfication approach to handle diacritics in Turkish information retrieval",
  journal = "Information Processing & Management",
  volume = "52",
  number = "2",
  pages = "326 - 339",
  year = "2016",
  doi = "http://dx.doi.org/10.1016/j.ipm.2015.08.004",
  url = "http://www.sciencedirect.com/science/article/pii/S0306457315001053"
}
```