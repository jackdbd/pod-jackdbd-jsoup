(ns tasks
  (:require
   [babashka.classpath :refer [get-classpath split-classpath]]
   [babashka.process :refer [shell]]
   [clojure.edn :as edn]
   [clojure.string :as str]))

(defn print-classpath []
  (println "=== CLASSPATH BEGIN ===")
  ;; (System/getProperty "java.class.path")
  (doseq [path (set (split-classpath (get-classpath)))]
    (println path))
  (println "=== CLASSPATH END ==="))

(defn snapshot [version-bump]
  (let [project (-> (edn/read-string (slurp "deps.edn")) :aliases :neil :project)
        stable-version (str/replace (:version project) "-SNAPSHOT" "")
        [major minor patch] (str/split stable-version #"\.")]
    (case version-bump
      :major (let [snapshot-version (format "%s.%s.%s-SNAPSHOT" (inc (Integer/parseInt major)) 0 0)]
               (shell (format "neil version set %s --no-tag" snapshot-version))
               (shell "git add deps.edn")
               (shell (format "git commit -m 'set version to %s'" snapshot-version)))
      :minor (let [snapshot-version (format "%s.%s.%s-SNAPSHOT" major (inc (Integer/parseInt minor)) 0)]
               (shell (format "neil version set %s --no-tag" snapshot-version))
               (shell "git add deps.edn")
               (shell (format "git commit -m 'set version to %s'" snapshot-version)))
      :patch (let [snapshot-version (format "%s.%s.%s-SNAPSHOT" major minor (inc (Integer/parseInt patch)))]
               (shell (format "neil version set %s --no-tag" snapshot-version))
               (shell "git add deps.edn")
               (shell (format "git commit -m 'set version to %s'" snapshot-version)))
      (shell "echo 'Usage: bb snapshot [major|minor|patch]'"))))

(defn stable []
  (let [project (-> (edn/read-string (slurp "deps.edn")) :aliases :neil :project)
        version (:version project)]
    (when (not (str/includes? version "-SNAPSHOT"))
      (println "Only a SNAPSHOT version can be promoted to stable")
      (System/exit 1))
    (let [stable-version (str/replace (:version project) "-SNAPSHOT" "")]
      (shell (format "neil version set %s --no-tag" stable-version))
      (shell "git add deps.edn")
      (shell (format "git commit -m v%s" stable-version))
      (shell (format "git tag -a v%s -m v%s" stable-version stable-version)))))

(comment
  (print-classpath)
  )
