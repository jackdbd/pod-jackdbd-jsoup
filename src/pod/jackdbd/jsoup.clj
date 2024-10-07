(ns pod.jackdbd.jsoup
  (:gen-class)
  (:refer-clojure :exclude [read-string])
  (:require
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [clojure.walk :as walk]
   [pod.jackdbd.bencode :as bencode]
   [pod.jackdbd.jsoup-wrapper :as api]))

(def main-ns "pod.jackdbd.jsoup")

(defn read-string [^"[B" v]
  (String. v))

(def debug? false)

(defn debug [& strs]
  (when debug?
    (binding [*out* (io/writer System/err)]
      (apply println strs))))

(def lookup {'pod.jackdbd.jsoup/select api/select})
;; Taken from https://github.com/babashka/babashka-sql-pods/blob/92d2eedb5afcb517880d9d4158f7ee5b843b5231/src/pod/babashka/sql.clj#L151

(comment
  ;; alternative, seen here: https://github.com/babashka/babashka-sql-pods/blob/92d2eedb5afcb517880d9d4158f7ee5b843b5231/src/pod/babashka/sql.clj#L151
  (def lookup
    (let [m {'select api/select}]
      (zipmap (map (fn [sym]
                     (symbol main-ns (name sym)))
                   (keys m))
              (vals m)))))

(def describe-map
  (walk/postwalk
   (fn [v]
     (if (ident? v) (name v)
         v))
   `{:format :edn
     :namespaces [{:name pod.jackdbd.jsoup
                   :vars [{:name select}]}]
     :opts {:shutdown {}}}))

(debug describe-map)

;; Taken from https://github.com/babashka/pod-babashka-parcera/blob/04a14e783fa463ba6d22aa4386ac34ff5f0db176/src/pod/babashka/parcera.clj#L56
(defn main []
  (loop []
    (let [message (try (bencode/read)
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
            :describe (do (bencode/write describe-map)
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
                                (bencode/write reply))
                              (throw (ex-info (str "Var not found: " var) {}))))
                          (catch Throwable e
                            (debug e)
                            (let [reply {"ex-message" (ex-message e)
                                         "ex-data" (pr-str
                                                    (assoc (ex-data e)
                                                           :type (class e)))
                                         "id" id
                                         "status" ["done" "error"]}]
                              (bencode/write reply))))
                        (recur))

            :shutdown (System/exit 0)

            (do
              (let [reply {"ex-message" "Unknown op"
                           "ex-data" (pr-str {:op op})
                           "id" id
                           "status" ["done" "error"]}]
                (bencode/write reply))
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

  (bencode/write describe-map)

  ;; https://www.cs.cmu.edu/~pattis/15-1XX/common/handouts/ascii.html
  (def hello (byte-array [104 101 108 108 111]))
  (def world (byte-array [119 111 114 108 100]))
  (read-string hello)
  (read-string world))
