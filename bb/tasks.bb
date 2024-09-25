(ns tasks
  (:require [babashka.classpath :refer [get-classpath split-classpath]]
            [babashka.http-client :as http]
            [babashka.pods :as pods]
            [pod.jackdbd.jsoup :as jsoup]
            [clojure.edn :as edn]))

(def project (-> (edn/read-string (slurp "deps.edn")) :aliases :neil :project))
(def pod-name (name (:name project)))
(def pod-version (:version project))
(def uber-file (format "target/%s-%s-standalone.jar" pod-name pod-version))

(defn demo-pod-native []
  (prn (str "Load Babashka pod " pod-name " version " pod-version " (binary)"))
  (pods/load-pod "./target/pod-jackdbd-jsoup")
  (prn "pod loaded (binary)")

  (require '[pod.jackdbd.jsoup :as jsoup])
  (prn "pod required (binary)")

  (-> (http/get "https://clojure.org")
      :body
      (jsoup/select "div p")
      first
      :text)
  )

(defn demo-pod-uberjar []
  (prn (str "Load Babashka pod " pod-name " version " pod-version " (uberjar)"))
  (pods/load-pod ["java" "-jar" uber-file])
  (prn "pod loaded (uberjar)")

  (require '[pod.jackdbd.jsoup :as jsoup])
  (prn "pod required (uberjar)")

  (-> (http/get "https://clojure.org")
      :body
      (jsoup/select "div p")
      first
      :text))

(defn print-classpath
  []
  (println "=== CLASSPATH BEGIN ===")
  ;; (System/getProperty "java.class.path")
  (doseq [path (set (split-classpath (get-classpath)))]
    (println path))
  (println "=== CLASSPATH END ==="))

(comment
  (print-classpath)
  (demo-pod-native)
  (demo-pod-uberjar)
  )