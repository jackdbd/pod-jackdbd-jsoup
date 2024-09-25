(ns pod.jackdbd.jsoup
  (:gen-class)
  (:refer-clojure :exclude [read read-string])
  (:require [bencode.core :as bencode]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.walk :as walk]
            [pod.jackdbd.jsoup-wrapper :as api]
            [pod.jackdbd.other :as other])
  (:import (java.io PushbackInputStream)))

(def main-ns "pod.jackdbd.jsoup")

(def stdin (PushbackInputStream. System/in))

(defn write [v]
  (bencode/write-bencode System/out v)
  (.flush System/out))

(defn read-string [^"[B" v]
  (String. v))

(defn read []
  (bencode/read-bencode stdin))

(def debug? false)

(defn debug [& strs]
  (when debug?
    (binding [*out* (io/writer System/err)]
      (apply println strs))))

;; Taken from https://github.com/babashka/babashka-sql-pods/blob/92d2eedb5afcb517880d9d4158f7ee5b843b5231/src/pod/babashka/sql.clj#L151
(def lookup
  (let [m {'select api/select
           'multi-arity-func other/multi-arity-func}]
    (zipmap (map (fn [sym]
                   (symbol main-ns (name sym)))
                 (keys m))
            (vals m))))

(comment
  ;; alternative
  (def lookup {'pod.jackdbd.jsoup/select api/select
               'pod.jackdbd.jsoup/multi-arity-func other/multi-arity-func}))

(def describe-map
  (walk/postwalk
   (fn [v]
     (if (ident? v) (name v)
         v))
   `{:format :edn
     :namespaces [{:name pod.jackdbd.jsoup
                   :vars [{:name select}
                          {:name multi-arity-func}
                          {:name public-func-a}
                          {:name public-func-b}]}]
     :opts {:shutdown {}}}))

(debug describe-map)

;; Taken from https://github.com/babashka/pod-babashka-parcera/blob/04a14e783fa463ba6d22aa4386ac34ff5f0db176/src/pod/babashka/parcera.clj#L56
(defn main []
  ;; (prn "Run pod message loop forever")
  (loop []
    ;; (prn "new loop iteration")
    (let [message (try (read)
                       (catch java.io.EOFException _
                         ::EOF))]
      (when-not (identical? ::EOF message)
        (let [op (get message "op")
              op (read-string op)
              op (keyword op)
              id (some-> (get message "id")
                         read-string)
              id (or id "unknown")]
          (case op
            :describe (do (write describe-map)
                          (recur))

            :invoke (do (try
                          (let [var  (-> (get message "var")
                                         read-string
                                         symbol)
                                args (get message "args")
                                args (read-string args)
                                args (edn/read-string args)]
                            (if-let [f (lookup var)]
                              (let [value (binding [*print-meta* true]
                                            (let [result (apply f args)]
                                              (pr-str result)))
                                    reply {"value" value
                                           "id" id
                                           "status" ["done"]}]
                                (write reply))
                              (throw (ex-info (str "Var not found: " var) {}))))
                          (catch Throwable e
                            (debug e)
                            (let [reply {"ex-message" (ex-message e)
                                         "ex-data" (pr-str
                                                    (assoc (ex-data e)
                                                           :type (class e)))
                                         "id" id
                                         "status" ["done" "error"]}]
                              (write reply))))
                        (recur))

            :shutdown (System/exit 0)

            (do
              (let [reply {"ex-message" "Unknown op"
                           "ex-data" (pr-str {:op op})
                           "id" id
                           "status" ["done" "error"]}]
                (write reply))
              (recur))))))))

(def musl?
  "Captured at compile time, to know if we are running inside a
  statically compiled executable with musl."
  (and (= "true" (System/getenv "BABASHKA_STATIC"))
       (= "true" (System/getenv "BABASHKA_MUSL"))))

(defmacro run [expr]
  (if musl?
    ;; When running in musl-compiled static executable we lift execution of bb
    ;; inside a thread, so we have a larger than default stack size, set by an
    ;; argument to the linker. See https://github.com/oracle/graal/issues/3398
    `(let [v# (volatile! nil)
           f# (fn []
                (vreset! v# ~expr))]
       (doto (Thread. nil f# "main")
         (.start)
         (.join))
       @v#)
    `(do ~expr)))

(defn -main [& _args]
  (run (main)))

(comment
  ;; Run these commands in a Clojure REPL
  (def debug? true)
  (debug describe-map)
  musl?

  (write "Hello")
  (write 9999)
  (write {:name "Godzilla" :color "black" :size "large"})
  (write [:foo "bar" :baz 123])
  (write describe-map)
  (write {"value" "some value"
          "id" "some-id"
          "status" ["done"]})

  ;; https://www.cs.cmu.edu/~pattis/15-1XX/common/handouts/ascii.html
  (def hello (byte-array [104 101 108 108 111]))
  (def world (byte-array [119 111 114 108 100]))
  (read-string hello)
  (read-string world))
