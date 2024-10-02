(ns utils
  (:require
    [clojure.string :as str]))

(defn format-edn [data]
  (cond
    ;; If the data is a map, format each key-value pair manually
    (map? data) (str "{" (str/join " " (for [[k v] data]
                                         (str (pr-str k) " " (format-edn v))))
                     "}")

    ;; If the data is a vector, format each element manually
    (vector? data) (str "[" (str/join " " (map format-edn data)) "]")

    ;; If the data is a regular value (string, number, keyword, etc.), just return it as a string
    :else (pr-str data)))
