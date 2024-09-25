(ns pod-jackdbd-jsoup.pod-jackdbd-jsoup
  "FIXME: my new org.corfield.new/scratch project.")

(def ^:dynamic x 123)

(defn exec
  "Invoke me with clojure -X pod-jackdbd-jsoup.pod-jackdbd-jsoup/exec"
  [y]
  (prn "x" x)
  (binding [x 456]
    (println "x rebound" x)))

(defn -main
  "Invoke me with clojure -M -m pod-jackdbd-jsoup.pod-jackdbd-jsoup"
  [& args]
  (println "-main with" args))

(comment
  (exec 999))