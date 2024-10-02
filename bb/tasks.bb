(ns tasks
  (:require
   [babashka.classpath :refer [get-classpath split-classpath]]))

(defn print-classpath []
  (println "=== CLASSPATH BEGIN ===")
  ;; (System/getProperty "java.class.path")
  (doseq [path (set (split-classpath (get-classpath)))]
    (println path))
  (println "=== CLASSPATH END ==="))

(comment
  (print-classpath)
  )
