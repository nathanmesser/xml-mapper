(ns xml-mapper.core-test
  (:import [java.io ByteArrayInputStream])
  (:use clojure.test
        xml-mapper.core
        clojure.xml))

(defn- parse-and-convert [xml-text]
  (xml-to-raw (parse (-> xml-text .getBytes ByteArrayInputStream.))))
  
(defn- check [expected xml-text]
  (is (= expected (parse-and-convert xml-text))))

(deftest simple-empty-element  
  (check {:root nil}
         
         "<root/>"))

(deftest simple-element
  (check {:root "text"}
         
         "<root>text</root>")) 

(deftest nested-element
  (check {:root 
          {:elem "text"}}
         
         "<root>
             <elem>text</elem>
          </root>"))

(deftest nested-elements
  (check {:root 
          {:elem "text" 
           :other "text2"}}
         
         "<root>
             <elem>text</elem>
             <other>text2</other>
          </root>"))  

(deftest nested-same-elements
  (check {:root 
          {:elem ["text","text2"]}}
         
         "<root>
             <elem>text</elem>
             <elem>text2</elem>
          </root>"))

(deftest mixed-elements
  (check {:root 
          {:elem "text"
           :text-content "text2"}}
         
          "<root>
             <elem>text</elem>text2</root>"))

(deftest attributes
  (check {:root 
          {:attr1 "val"}}
         
         "<root attr1=\"val\"/>"))

(deftest elements-and-attributes
  (check {:root 
          {:attr1 "val" 
           :elem "val2"}} 
         
         "<root attr1=\"val\">
             <elem>val2</elem>
          </root>"))

(deftest elements-and-attributes-and-text
  (check {:root 
          {:attr1 "val" 
           :elem "val2" 
           :text-content "text"}} 
         
         "<root attr1=\"val\">text<elem>val2</elem></root>"))

(deftest duplicate-attributes-and-elements
  (check {:root 
          {:key ["attrVal","elemVal"]}} 
         
         "<root key=\"attrVal\">
             <key>elemVal</key>
          </root>"))

(deftest deep-nesting
  (check {:root 
          {:key 
              [{:inner ["val1","val2"]},
               {:inner ["val3","val4"]}]
           :key2 
              [{:inner ["val5","val6"]},
               {:inner ["val7","val8"]}]}}
         
         "<root>
              <key>
                 <inner>val1</inner>
                 <inner>val2</inner>
              </key>
              <key>
                 <inner>val3</inner>
                 <inner>val4</inner>
             </key>
             <key2>
                 <inner>val5</inner>
                 <inner>val6</inner>
              </key2>
              <key2>
                 <inner>val7</inner>
                 <inner>val8</inner>
             </key2>
          </root>"))