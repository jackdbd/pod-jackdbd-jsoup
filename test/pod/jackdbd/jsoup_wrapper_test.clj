(ns pod.jackdbd.jsoup-wrapper-test
  (:require [clojure.string :as str]
            [clojure.test :as t :refer [deftest is testing]]
            [pod.jackdbd.jsoup-wrapper :refer [element->m]])
  (:import (org.jsoup Jsoup)))

(def html (str/join "" ["<!DOCTYPE html>"
                        "<html lang='en-US'>"
                        "<head>"
                        "  <meta charset='UTF-8'>"
                        "  <title>The title</title>"
                        "</head>"
                        "<body>"
                        "  <h1 data-abc=\"def\">The h1</h1>"
                        "  <div class='foo' id='the-foo'><p>The foo</p></div>"
                        "  <div class='bar'><p data-abc=\"def\">The bar</p></div>"
                        "  <div class='foo' id='the-other-foo'><p data-abc=\"xyz\">The other foo</p></div>"
                        "</body>"
                        "</html>"]))

(def element (Jsoup/parse html))

(deftest element->m-test
  (testing "parsed HTML has the expected text"
    (let [m (element->m element)]
      (is (= "The title The h1 The foo The bar The other foo" (get m :text))))))

