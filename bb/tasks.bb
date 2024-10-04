(ns tasks
  (:require
   [babashka.classpath :refer [get-classpath split-classpath]]
   [babashka.process :refer [shell sh]]
   [cheshire.core :as json]
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
        version (:version project)
        result-branch (sh "git branch --show-current")
        result-prs (sh "gh pr list --state merged --limit 1 --json title,body,number,id,mergedAt,mergeCommit,author,url")
        m (first (json/parse-string (:out result-prs) true))
        message (str (:title m) "\n\n" (:body m) "\n\n" (format "Merged on main with this PR: %s" (:url m)))]
    (when (not (str/includes? version "-SNAPSHOT"))
      (println "Only a SNAPSHOT version can be promoted to stable")
      (System/exit 1))
    (when (not= (:out result-branch) "main\n")
      (println "Only when on the `main` branch you can promote a SNAPSHOT version to stable")
      (System/exit 1))
    (let [stable-version (str/replace (:version project) "-SNAPSHOT" "")]
      (shell (format "neil version set %s --no-tag" stable-version))
      (shell "git add deps.edn")
      (shell (format "git commit -m v%s" stable-version))
      (shell ["git" "tag" "-a" (format "v%s" stable-version) "--message" message])
      (shell (format "git push --atomic")))))

(comment
  (print-classpath)
  )
