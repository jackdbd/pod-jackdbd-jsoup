(ns pod.jackdbd.jsoup-test
  (:refer-clojure :exclude [read-string])
  (:require [clojure.test :as t :refer [deftest is testing]]
            [pod.jackdbd.jsoup :refer [read-string]]))

(deftest read-string-hello-test
  (testing "byte-array is hello"
    (let [hello (byte-array [104 101 108 108 111])]
      (is (= "hello" (read-string hello))))))
