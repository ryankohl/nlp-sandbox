(ns nlp.pos
  (:require [clojure.string :as string])
  (:import [java.util Arrays])
  (:import [opennlp.tools.namefind TokenNameFinderModel NameFinderME])
  (:import [opennlp.tools.parser ParserModel ParserFactory Parse AbstractBottomUpParser])
  (:import [opennlp.tools.cmdline.parser ParserTool])
  (:import [opennlp.tools.postag POSModel POSTaggerME])
  (:import [opennlp.tools.tokenize SimpleTokenizer])
  (:import [opennlp.tools.util Span]))

(defn get-parser []
  (with-open [fis (clojure.java.io/input-stream "resources/en-parser-chunking.bin")]
    (-> fis (ParserModel.) (ParserFactory/create 20 0.95))))

(defn get-tagger []
  (with-open [fis (clojure.java.io/input-stream "resources/en-pos-maxent.bin")]
    (-> fis (POSModel.) (POSTaggerME.))))

(defn get-extractor [kind]
  (with-open [fis (clojure.java.io/input-stream (str "resources/en-ner-"kind".bin"))]
    (-> fis  (TokenNameFinderModel.) (NameFinderME.))))

(defn parsex [s parser]
  (let [sb (StringBuffer.)
        n (-> s (ParserTool/parseLine parser 1) first (.show sb))]
    (.toString sb)))

(defn parse [s parser]
  (let [root (Parse. s (Span. 0 (count s)) AbstractBottomUpParser/INC_NODE 1.0 0)
        spans (-> (.tokenizePos SimpleTokenizer/INSTANCE s) Arrays/asList)
        n (doseq [span spans] (.insert root (Parse. s span AbstractBottomUpParser/TOK_NODE 0.0 (.indexOf spans span))))
        parse (.parse parser root)
        sb (StringBuffer.)
        nn (.show parse sb)]
    (.toString sb)))
    

(defn tag [s tagger]
  (let [words (-> SimpleTokenizer/INSTANCE (.tokenize s))
        tags (-> tagger (.tag words))]
    {:words (Arrays/asList words)
     :tags (Arrays/asList tags)}))

(defn extract [s extractor]
  (let [words (-> SimpleTokenizer/INSTANCE (.tokenize s) )
        extracts (-> extractor (.find words) )
        n (.clearAdaptiveData extractor)]
    {:words (Arrays/asList words)
     :extracts (Arrays/asList extracts)}))

(defn formalize [s parser]
  (let [x (-> s (ParserTool/parseLine parser 1) first)]
))