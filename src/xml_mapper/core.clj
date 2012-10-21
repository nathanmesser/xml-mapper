(ns ^{:author "Nathan Messer",
     :doc "Provides the xml-to-raw function.
 
Using clojure.data.xml, takes an InputStream or Reader for an XML source, and turns it into raw, friendly clojure data structures with no xml artifacts.

Inspired by Approach #3 here http://www.w3.org/2011/10/integration-workshop/s/ExperienceswithJSONandXMLTransformations.v08.pdf
   Converting from XML to JSON focusing on 'friendliness'.

This is NOT intended to be able to round trip.

Currently the only namespace handling is data.xml's stripping of prefixes."}     
     xml-mapper.core
     (:require [clojure.data.xml :as xml]))

(def ^{:private true} 
  into-vector (comp vec flatten vector))

(defn- contains-one-string 
  "check if item is a vector containing a single string"
  [item]  
  (and (seq? item) (= (count item) 1) (string? (first item))))

(defn- get-element-contents
  "given the attributes and children of an element, return it's content
   Either a text string
   Or map of tag and attribute names to contents (with text refered to as :text-content"
  [attrs children]  
  (cond
      (empty? attrs)        children
      (string? children) (merge-with into-vector attrs {:text-content children})
      :else              (merge-with into-vector attrs children)))

(declare parsed-to-friendly)

(defn- convert-element
  "converts a single element to a map of it's tag name to it's contents"
  [elem]    
  (let [attrs    (:attrs elem)
        children (parsed-to-friendly (:content elem))
        content  (get-element-contents attrs children)]
    (hash-map (keyword (:tag elem)) content)))

(defn- convert-collection
  "maps elements in a collection to their contents, with the tag name being the key"
  [coll]
  (apply merge-with into-vector (map parsed-to-friendly coll)))

(defn- parsed-to-friendly
  [item]
  (cond
    (map? item) (convert-element item)
    (contains-one-string item) (first item)
    (seq? item) (convert-collection item)
    (string? item) {:text-content item}))

(defn xml-to-raw
   "Converts either a reader or an input stream for a source of raw XML into to Clojure data structures.

   Element names become keys.  

   <root/> => {:root nil} 

   Element contents become values. 

   <root>hello</root> => {:root \"hello\"}

   Multiple elements become a single key to a vector of values. 

   <elem>val1</elem><elem>val2</elem> => {:elem [\"val1\",\"val2\"]}

   Text content is usually just the value associated with the element.
   However an element has mixed text and element content, the keyword :text-content identifies the text

   <root>text<elem>value</elem></root> => {:root {:text-content \"text\" :elem \"value\"}}
    
   Attributes become part of the map an element refers to

   <root class=\"bold\"/> => {:root {:class \"bold\"}}

   <root class=\"bold\"><elem>value</elem></root> => {:root {:class \"bold\" :elem \"value\"}}

   This does mean that if an element has attributes, it's text child will be in the map under the key :text-content as with mixed text/element children.

   Inspired by Approach #3 here http://www.w3.org/2011/10/integration-workshop/s/ExperienceswithJSONandXMLTransformations.v08.pdf
   Converting from XML to JSON focusing on 'friendliness'

   clojure.data.xml is used for parsing."
  [input]
  (parsed-to-friendly (xml/parse input)))
