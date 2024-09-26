(ns integration-test
  (:require [babashka.pods :as pods]
            [clojure.edn :as edn]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]))

(def project (-> (edn/read-string (slurp "deps.edn")) :aliases :neil :project))
(def pod-id (name (:name project)))
(def pod-name (str/replace pod-id #"\." "-"))
(def pod-version (:version project))
(def uber-file (format "target/%s-%s-standalone.jar" pod-id pod-version))
(def exe-file (format "target/%s-%s" pod-name pod-version))

(defn symbol-exists-in-m? [m sym]
  (boolean (contains? m sym)))

(defn symbol-exists-in-ns? [ns sym]
  (boolean (ns-resolve ns sym)))

(defn linux? []
  (str/includes? (System/getProperty "os.name") "Linux"))

(deftest pod-packaged-into-uberjar-integration-test
  (pods/load-pod ["java" "-jar" uber-file])
  (testing "the jsoup namespace exists"
    (is (= true (some? (find-ns 'pod.jackdbd.jsoup)))))
  (testing "the jsoup namespace exposes the symbols required for running the pod (-main, describe-map)"
    (let [m (ns-publics 'pod.jackdbd.jsoup)]
      (is (= true (symbol-exists-in-m? m 'main)))
      (is (= true (symbol-exists-in-m? m 'describe-map)))))
  (testing "the jsoup namespace exposes the expected public API (select)"
    (is (= true (symbol-exists-in-ns? 'pod.jackdbd.jsoup 'select)))))

(deftest pod-compiled-to-binary-integration-test
  (when (linux?)
    (pods/load-pod exe-file)
    (testing "the jsoup namespace exists"
      ;; (prn "ns-map" (ns-map 'pod.jackdbd.jsoup))
      (is (= true (some? (find-ns 'pod.jackdbd.jsoup)))))
    (testing "the jsoup namespace exposes the symbols required for running the pod (-main, describe-map)"
      (let [m (ns-publics 'pod.jackdbd.jsoup)]
        (is (= true (symbol-exists-in-m? m 'main)))
        (is (= true (symbol-exists-in-m? m 'describe-map)))))
    (testing "the jsoup namespace exposes the expected public API (select)"
      (is (= true (symbol-exists-in-ns? 'pod.jackdbd.jsoup 'select))))))
