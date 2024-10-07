(ns pod.jackdbd.jsoup-wrapper
  "Clojure wrapper for jsoup (Java HTML Parser)."
  (:require
   [clojure.string :as str])
  (:import
   (org.jsoup Jsoup)
   (org.jsoup.nodes Element Attribute)))

(set! *warn-on-reflection* true)

(defn element->m
  [^Element element]
  {:id (.id element)
   :class-names (.classNames element)
   :tag-name (.normalName element)
   :attrs (->> (.attributes element)
               .iterator
               iterator-seq
               (map (juxt (memfn ^Attribute getKey) (memfn ^Attribute getValue)))
               (into {}))
   :own-text (.ownText element)
   :text (.text element)
   :whole-text (.wholeText element)
   :inner-html (.html element)
   :outer-html (.outerHtml element)})

(defn select
  "Selects all HTML elements matching a CSS query.
   
   [org.jsoup.select.Selector docs](https://jsoup.org/apidocs/org/jsoup/select/Selector.html)"
  [html css-query]
  (let [elements (-> (Jsoup/parse html)
                     (.select ^String css-query))]
    (map element->m elements)))

(comment
  (def html (str/join "" ["<!DOCTYPE html>"
                          "<html lang='en-US'>"
                          "<head>"
                          "  <meta charset='UTF-8'>"
                          "  <title>Hello world</title>"
                          "</head>"
                          "<body>"
                          "  <h1 data-abc=\"def\">Test world</h1>"
                          "  <div class='foo' id='the-foo'><p>This is foo</p></div>"
                          "  <div class='bar'><p data-abc=\"def\">This is bar</p></div>"
                          "  <div class='foo' id='the-other-foo'><p data-abc=\"xyz\">This is another foo</p></div>"
                          "</body>"
                          "</html>"]))

  (def element (Jsoup/parse html))
  (element->m element)

  (select html "*")
  (select html "div.foo")
  (select html "div#the-foo")
  (select html "div#the-other-foo")
  (select html "div.bar")
  (select html "div#the-bar")
  (select html "[data-abc=\"def\"]")
  (select html "[data-abc=\"xyz\"]"))
