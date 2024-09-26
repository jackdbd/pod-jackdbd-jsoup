(ns pod.jackdbd.other-test
  (:require [clojure.test :as t :refer [deftest is testing]]
            [pod.jackdbd.other :refer [multi-arity-func]]))

(deftest multi-arity-func-test
  (testing "7 times 6 is 42"
    (is (= 42 (multi-arity-func 7 6)))))
