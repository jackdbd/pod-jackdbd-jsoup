#!/usr/bin/env bb
(ns manifest
  (:require 
   [babashka.cli :as cli]
   [clojure.edn :as edn]
   [clojure.pprint :refer [pprint]]
   [clojure.string :as str]
   [utils :refer [format-edn]]))

(def project (-> (edn/read-string (slurp "deps.edn")) :aliases :neil :project))
(def default-version (:version project))
(def default-name (:name project))

;; Show help with `bb bb/manifest.bb --help` (or with -h, :help, :h)
(defn show-help
  [spec]
  (cli/format-opts (merge spec {:order (vec (keys (:spec spec)))})))

(def cli-spec
  {:spec
   {:license {:desc "Project license (e.g. MIT)"
              :default "MIT"}
    :description {:desc "Project description"
                  :alias :d
                  :default "Babashka pod for parsing HTML with jsoup."}
    :example {:desc "Example that shows how to use the pod"
              :alias :e
              :default "examples/jsoup.bb"}
    :name {:desc "Namespaced identifier of this project <group-id>/<pod-id> (e.g. com.github.jackdbd/pod.jackdbd.jsoup)"
           :alias :n
           :default default-name} 
    :language {:desc "The programming language the pod is written in"
               :default "clojure"}
    :linux-x86_64 {:desc "URL where the x86_64 Linux binary is hosted (e.g. URL of a GitHub release asset)"}
    :macos-aarch64 {:desc "URL where the AArch64 macOS binary is hosted (e.g. URL of a GitHub release asset)"}
    :windows-amd64 {:desc "URL where the AMD64 Windows binary is hosted (e.g. URL of a GitHub release asset)"}
    :windows-x86_64 {:desc "URL where the x86_64 Windows binary is hosted (e.g. URL of a GitHub release asset)"}
    :uberjar {:desc "URL where the uberjar is hosted (e.g. URL of a GitHub release asset)"}
    :version {:desc "Project version (e.g. 1.2.3)"
              :default default-version}}
   :error-fn
   (fn [{:keys [spec type cause msg option opts] :as data}]
     (when (and (= :org.babashka/cli type) (not (or (:help opts) (:h opts))))
       (case cause
         :require (do
                    (println (format "Missing required argument: %s\n" option))
                    (System/exit 1))
         :validate (do (println (format "%s does not exist!\n" msg))
                       (System/exit 1))
         (do (println (str "Unsupported error cause: " cause))
             (println "\nCLI spec:")
             (pprint spec)
             (System/exit 1)))))})

(defn -main
  [args]
  (let [opts (cli/parse-opts args cli-spec)]
    (when (or (:help opts) (:h opts))
      (println (show-help cli-spec))
      (System/exit 0))
    (let [pod-id (:name opts)
          pod-name (str/replace pod-id #"\." "-")
          artifacts (cond-> []
                      (:linux-x86_64 opts) (conj {:os/arch "x86_64"
                                                  :os/name "Linux.*"
                                                  :artifact/url (:linux-x86_64 opts)
                                                  :artifact/executable pod-name})
                      (:macos-aarch64 opts) (conj {:os/arch "aarch64"
                                                   :os/name "Mac.*"
                                                   :artifact/url (:macos-aarch64 opts)
                                                   :artifact/executable pod-name})
                      (:windows-amd64 opts) (conj {:os/arch "amd64"
                                                   :os/name "Windows.*"
                                                   :artifact/url (:windows-amd64 opts)
                                                   :artifact/executable (format "%s.exe" pod-name)})
                      (:windows-x86_64 opts) (conj {:os/arch "x86_64"
                                                    :os/name "Windows.*"
                                                    :artifact/url (:windows-x86_64 opts)
                                                    :artifact/executable (format "%s.exe" pod-name)})
                      (:uberjar opts) (conj {:artifact/url (:uberjar opts)}))
          manifest-data {:pod/name (symbol pod-id)
                         :pod/description (:description opts)
                         :pod/example (:example opts)
                         :pod/language (:language opts)
                         :pod/license (:license opts)
                         :pod/version (:version opts)
                         :pod/artifacts artifacts}
          filepath "manifest.edn"
          edn-content (format-edn manifest-data)]
      (spit filepath edn-content)
      (println (str "\nWrote " filepath))
      (println (str "\nUseful websites for formatting/converting EDN:\n" (str/join "\n" ["https://repo.tiye.me/mvc-works/edn-formatter/" "http://cljson.com/"]))))))

(-main *command-line-args*)
