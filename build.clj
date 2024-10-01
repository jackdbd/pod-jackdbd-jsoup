(ns build
  "pod.jackdbd.jsoup's build script.
   
   clojure -T:build jar
   clojure -T:build jar :snapshot true
   clojure -T:build uber
   clojure -T:build uber :snapshot true
   clojure -T:build deploy
   clojure -T:build deploy :snapshot true

   For more information, run:

  clojure -A:deps -T:build help/doc"
  (:require [clojure.tools.build.api :as b]
            [clojure.edn :as edn]
            [deps-deploy.deps-deploy :as dd]))

(def project (-> (edn/read-string (slurp "deps.edn")) :aliases :neil :project))
(def lib (:name project))
(defn- the-version [patch] (format "0.1.%s" patch))
(def version (the-version (b/git-count-revs nil)))
(def snapshot (the-version "999-SNAPSHOT"))

;; https://clojure.github.io/tools.build/clojure.tools.build.api.html#var-write-pom
(defn- pom-template [{:keys [version]}]
  [[:description "Pod for parsing HTML with jsoup."]
   [:url "https://github.com/jackdbd/pod-jackdbd-jsoup"]
   [:licenses
    [:license
     [:name "The MIT License"]
     ;; https://www.tldrlegal.com/license/mit-license
     [:url "https://opensource.org/license/MIT"]]]
   [:developers
    [:developer
     [:name "Giacomo Debidda"]]]
   [:scm
    [:url "https://github.com/jackdbd/pod-jackdbd-jsoup"]
    [:connection "scm:git:https://github.com/jackdbd/pod-jackdbd-jsoup.git"]
    [:developerConnection "scm:git:ssh:git@github.com:jackdbd/pod-jackdbd-jsoup.git"]
    [:tag (str "v" version)]]])

(defn- shared-config [opts]
  (let [version (if (:snapshot opts) snapshot version)]
    (assoc opts
           :basis (b/create-basis {:project "deps.edn"})
           :class-dir "target/classes"
           :jar-file (format "target/%s-%s.jar" (name lib) version)
           :lib lib
           :main 'pod.jackdbd.jsoup
           :pom-data (pom-template {:version version})
           :src-dirs ["src"]
           :target "target"
           :uber-file (format "target/%s-%s-standalone.jar" (name lib) version)
           :version version)))

(defn clean [_]
  (b/delete {:path "target"}))

(defn jar "Build the JAR." [opts]
  (let [config (shared-config opts)
        {:keys [basis class-dir jar-file lib main pom-data src-dirs target version]} config]
    (clean nil)

    (println "\nWriting" (b/pom-path (select-keys config [:lib :class-dir])) "...")
    ;; https://clojure.github.io/tools.build/clojure.tools.build.api.html#var-write-pom
    (b/write-pom {:basis basis
                  :class-dir class-dir
                  :lib lib
                  :pom-data pom-data
                  :src-dirs src-dirs
                  :target target
                  :version version})

    (println "\nCopying src and resources ...")
    (b/copy-dir {:src-dirs ["src" "resources"] :target-dir class-dir})

    (println "\nBuilding" jar-file "...")
    ;; https://clojure.github.io/tools.build/clojure.tools.build.api.html#var-jar
    (b/jar {:class-dir class-dir
            :jar-file jar-file
            :main main})))

(defn uber "Build the uber-JAR." [opts]
  (let [config (shared-config opts)
        {:keys [basis class-dir main uber-file]} config]
    (clean nil)

    (println "\nCopying src and resources ...")
    (b/copy-dir {:src-dirs ["src" "resources"] :target-dir class-dir})

    (println "\nBuilding" uber-file "...")
    ;; https://clojure.github.io/tools.build/clojure.tools.build.api.html#var-compile-clj
    (b/compile-clj {:basis basis :class-dir class-dir :src-dirs ["src"]})
    ;; https://clojure.github.io/tools.build/clojure.tools.build.api.html#var-uber:version version})
    (b/uber {:basis basis
             :class-dir class-dir
             :main main
             :uber-file uber-file})))

(defn deploy "Deploy the JAR to Clojars." [opts]
  (let [config (shared-config opts)
        {:keys [jar-file uber-file]} config
        artifact (b/resolve-path jar-file)
        ;; artifact (b/resolve-path uber-file)
        pom-file (b/pom-path (select-keys config [:lib :class-dir]))]

    ;; https://github.com/slipset/deps-deploy/blob/master/doc/intro.md
    (dd/deploy {:artifact artifact
                :installer :remote
                :pom-file pom-file}))
  opts)

(comment
  (clean nil)
  (uber nil)
  (jar {})
  (uber {})
  (deploy {}))
