(ns integration-test
  (:require
   [babashka.pods :as pods]
   [clojure.edn :as edn]
   [clojure.string :as str]
   [clojure.test :refer [deftest is testing]]))

(def project (-> (edn/read-string (slurp "deps.edn")) :aliases :neil :project))
(def pod-id (name (:name project)))
(def pod-name (str/replace pod-id #"\." "-"))
(def pod-version (:version project))

(defn symbol-exists-in-m? [m sym]
  (boolean (contains? m sym)))

(defn symbol-exists-in-ns? [ns sym]
  (boolean (ns-resolve ns sym)))

(defn linux? []
  (str/includes? (System/getProperty "os.name") "Linux"))

(defn macos? []
  (str/includes? (System/getProperty "os.name") "Mac"))

(defn windows? []
  (str/includes? (System/getProperty "os.name") "Windows"))

(deftest pod-packaged-into-uberjar-integration-test
  (if (windows?)
    (pods/load-pod ["java" "-jar" (format "target\\%s-%s-standalone.jar" pod-id pod-version)])
    (pods/load-pod ["java" "-jar" (format "target/%s-%s-standalone.jar" pod-id pod-version)]))
  (testing "the jsoup namespace exists"
    (is (= true (some? (find-ns 'pod.jackdbd.jsoup)))))
  (testing "the jsoup namespace exposes the symbols required for running the pod (-main, describe-map)"
    (let [m (ns-publics 'pod.jackdbd.jsoup)]
      (is (= true (symbol-exists-in-m? m 'main)))
      (is (= true (symbol-exists-in-m? m 'describe-map)))))
  (testing "the jsoup namespace exposes the expected public API (select)"
    (is (= true (symbol-exists-in-ns? 'pod.jackdbd.jsoup 'select)))))

(deftest pod-compiled-to-binary-integration-test
  (if (windows?)
    (pods/load-pod (format "target\\%s.exe" pod-name))
    (pods/load-pod (format "target/%s" pod-name)))
  (testing "the jsoup namespace exists"
      ;; (prn "ns-map" (ns-map 'pod.jackdbd.jsoup))
    (is (= true (some? (find-ns 'pod.jackdbd.jsoup)))))
  (testing "the jsoup namespace exposes the symbols required for running the pod (-main, describe-map)"
    (let [m (ns-publics 'pod.jackdbd.jsoup)]
      (is (= true (symbol-exists-in-m? m 'main)))
      (is (= true (symbol-exists-in-m? m 'describe-map)))))
  (testing "the jsoup namespace exposes the expected public API (select)"
    (is (= true (symbol-exists-in-ns? 'pod.jackdbd.jsoup 'select)))))
