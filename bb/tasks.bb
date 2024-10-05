(ns tasks
  (:require
   [babashka.classpath :refer [get-classpath split-classpath]]
   [babashka.process :refer [shell sh]]
   [cheshire.core :as json]
   [clojure.edn :as edn]
   [clojure.string :as str]))

(defn print-classpath []
  (println "=== CLASSPATH BEGIN ===")
  (doseq [path (set (split-classpath (get-classpath)))]
    (println path))
  (println "=== CLASSPATH END ==="))

(def default-prerelease-type "SNAPSHOT")
(def default-prerelease-branches #{"canary" "main" "master"})
(def default-release-branches #{"main" "master"})
(defn default-get-version []
  (-> (edn/read-string (slurp "deps.edn")) :aliases :neil :project :version))

(defn prerelease!
  ([] (prerelease! {}))
  ([{:keys [allowed-branches dry-run force-with-lease get-version prerelease-type] 
     :or {allowed-branches default-prerelease-branches
          dry-run false
          force-with-lease false
          get-version default-get-version
          prerelease-type default-prerelease-type}
     :as _opts}]
   (let [pattern (re-pattern (str "(\\d+\\.\\d+\\.\\d+)-" prerelease-type "\\.(\\d+)"))
         version (get-version)
         matches (re-matches pattern version)
         branch (-> (sh "git branch --show-current") :out str/trim-newline)
         semver (if (empty? matches) 
                  (let [[major minor _patch] (str/split version #"\.")]
                    (format "%s.%s.%s" major (inc (Integer/parseInt minor)) 0))
                  (-> (first matches) (str/split #"-") first))
         next-prerelease (if (empty? matches)
                           1
                           (-> (nth matches 2) Integer/parseInt inc))
         next-version (format "%s-%s.%s" semver prerelease-type next-prerelease)
         commands [(format "neil version set %s --no-tag" next-version)
                   "git add deps.edn"
                   (format "git commit -m 'set version to %s'" next-version)
                   (if force-with-lease
                     (format "git push --atomic --force-with-lease origin %s" branch)
                     (format "git push --atomic origin %s" branch))]]
     (when (not (contains? allowed-branches branch))
       (println (format "[ERROR] Cannot create prerelease: you are on branch: %s" branch))
       (println (format "[TIP] A prerelease is allowed only on these branches: %s" (str allowed-branches)))
       (System/exit 1))
     
     (if dry-run
       (do
         (println (format "[DRY RUN] these commands would have been executed"))
         (doseq [cmd commands]
           (println cmd)))
       (doseq [cmd commands]
         (shell cmd))))))

(defn bump!
  ([] (bump! {}))
  ([{:keys [dry-run get-version kind prerelease-type]
     :or {dry-run false
          get-version default-get-version
          kind :minor
          prerelease-type default-prerelease-type}
     :as _opts}]
   (let [version (get-version)]
     (when (str/includes? version prerelease-type)
       (println (format "[ERROR] Cannot bump: version %s is a prerelease." version))
       (println (format "[TIP] Use `prerelease` to increment the prerelease count."))
       (println (format "[TIP] Use `release` to promote the current prerelease to a release."))
       (System/exit 1))
     
     (let [[major minor patch] (str/split version #"\.")
           semver (case kind
                    :major (format "%s.%s.%s" (inc (Integer/parseInt major)) 0 0)
                    :minor (format "%s.%s.%s" major (inc (Integer/parseInt minor)) 0)
                    :patch (format "%s.%s.%s" major minor (inc (Integer/parseInt patch)))
                    nil)]
       (when (nil? semver)
         (println (format "[ERROR] Cannot bump: version bump must be one of: patch|minor|major"))
         (System/exit 1))

       (let [commands [(format "neil version set %s --no-tag" semver)
                       "git add deps.edn"
                       (format "git commit -m 'set version to %s'" semver)]]
         (if dry-run
           (do
             (println (format "[DRY RUN] these commands would have been executed"))
             (doseq [cmd commands]
               (println cmd)))
           (doseq [cmd commands]
             (shell cmd))))))))

(defn release!
  ([] (release! {}))
  ([{:keys [allowed-branches dry-run force-with-lease get-version prerelease-type]
     :or {allowed-branches default-release-branches
          dry-run false
          force-with-lease false
          get-version default-get-version
          prerelease-type default-prerelease-type}
     :as _opts}]
  (let [pattern (re-pattern (str "(\\d+\\.\\d+\\.\\d+)-" prerelease-type "\\.(\\d+)"))
        version (get-version)
        matches (re-matches pattern version)
        branch (-> (sh "git branch --show-current") :out str/trim-newline)]
    (when (empty? matches)
      (println (format "[ERROR] Cannot release: version %s is not a prerelease of type %s, so it cannot be promoted to a release." prerelease-type version))
      (println (format "[TIP] A prerelease version should match this pattern: %s" pattern))
      (System/exit 1))

    (let [semver (-> (first matches) (str/split #"-") first)
          commands [(format "neil version set %s --no-tag" semver)
                    "git add deps.edn"
                    (format "git commit -m 'set version to %s'" semver)
                    (if force-with-lease
                      (format "git push --atomic --force-with-lease origin %s" branch)
                      (format "git push --atomic origin %s" branch))]]
      (when (not (contains? allowed-branches branch))
        (println (format "[ERROR] Cannot release: you are on branch: %s" branch))
        (println (format "[TIP] A release is allowed only on these branches: %s" (str allowed-branches)))
        (System/exit 1))

      (if dry-run
        (do
          (println (format "[DRY RUN] these commands would have been executed"))
          (doseq [cmd commands]
            (println cmd)))
        (doseq [cmd commands]
          (shell cmd)))))))

(comment
  (print-classpath)

  (def result-branch (sh "git branch --show-current"))
  (def branch (str/trim-newline (:out result-branch)))
  (contains? default-prerelease-branches branch)
  (println "prerelease branches are" (str default-prerelease-branches))

  (def result-prs (sh "gh pr list --state merged --limit 1 --json title,body,number,id,mergedAt,mergeCommit,author,url"))
  (def pr (first (json/parse-string (:out result-prs) true)))
  (def message (str (:title pr) "\n\n" (:body pr) "\n\n" (format "Merged on main with this PR: %s" (:url pr))))

  (bump! {:dry-run true :kind :minor})
  (prerelease! {:dry-run true})
  (release! {:dry-run true})
  )
