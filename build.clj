(ns build
  "pod.jackdbd.jsoup's build script.
   
   clojure -T:build jar
   clojure -T:build uber
   clojure -T:build deploy

   For more information, run:
   
   clojure -A:deps -T:build help/doc"
  (:require
   [clojure.tools.build.api :as b]
   [clojure.edn :as edn]
   [clojure.pprint :refer [pprint]]
   [deps-deploy.deps-deploy :as dd]))

;; The clojure.tools.build.api library works only in Clojure, not Babashka.
;; Consider replacing it with this fork https://github.com/babashka/tools.bbuild

(def project (-> (edn/read-string (slurp "deps.edn")) :aliases :neil :project))
(def lib (:name project))

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
  (let [vers (:version project)]
    (assoc opts
           :basis (b/create-basis {:project "deps.edn"})
           :class-dir "target/classes"
           :jar-file (format "target/%s-%s.jar" (name lib) vers)
           :lib lib
           :main 'pod.jackdbd.jsoup
           :pom-data (pom-template {:version vers})
           :src-dirs ["src"]
           :target "target"
           :uber-file (format "target/%s-%s-standalone.jar" (name lib) vers)
           :version vers)))

(defn clean "Remove all compilation artifacts." [_]
  (b/delete {:path "target"}))

(defn jar "Build the JAR." [opts]
  (let [config (shared-config opts)
        {:keys [basis class-dir jar-file lib main pom-data src-dirs target version]} config]

    (clean nil)

    ;; https://clojure.github.io/tools.build/clojure.tools.build.api.html#var-write-pom
    (println "\nWriting" (b/pom-path (select-keys config [:lib :class-dir])) "...")
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
        {:keys [basis class-dir main pom-data src-dirs target uber-file version]} config]

    (clean nil)

    (println "\nWriting" (b/pom-path (select-keys config [:lib :class-dir])) "...")
    (b/write-pom {:basis basis
                  :class-dir class-dir
                  :lib lib
                  :pom-data pom-data
                  :src-dirs src-dirs
                  :target target
                  :version version})

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

(defn deploy "Deploy the uberjar to Clojars." [opts]
  (println "\nOptions")
  (pprint opts)

  (let [config (shared-config opts)
        {:keys [uber-file]} config
        artifact (b/resolve-path uber-file)
        pom-file (b/pom-path (select-keys config [:lib :class-dir]))]

    (println "\nConfig")
    (pprint config)

    ;; https://github.com/slipset/deps-deploy/blob/master/doc/intro.md
    (dd/deploy {:artifact artifact
                :installer :remote
                :pom-file pom-file}))
  opts)

(comment
  (prn (pom-template {:version (:version project)}))

  (def snapshot-version (format "%s-SNAPSHOT" (:version project)))
  (prn (pom-template {:version snapshot-version}))

  (jar {})
  (uber {})
  (deploy {}))
