(ns pod.jackdbd.bencode
  (:refer-clojure :exclude [read])
  (:require
   [bencode.core :as bencode])
  (:import
   (java.io PushbackInputStream)))

(def stdin (PushbackInputStream. System/in))

(defn write [v]
  (bencode/write-bencode System/out v)
  (.flush System/out))

(defn read []
  (bencode/read-bencode stdin))

(comment
  (write "Hello")
  (write 9999)
  (write {:name "Godzilla" :color "black" :size "large"})
  (write [:foo "bar" :baz 123])
  (write {"value" "some value"
          "id" "some-id"
          "status" ["done"]}))
