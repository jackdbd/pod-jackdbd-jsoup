#!/usr/bin/env bb
(ns manifest
  (:require 
   [babashka.cli :as cli]
   [clojure.edn :as edn]
   [clojure.pprint :refer [pprint]]
   [clojure.string :as str]
   [utils :refer [format-edn]]))

(def project (-> (edn/read-string (slurp "deps.edn")) :aliases :neil :project))
(def default-name (:name project))
(def default-version (:version project))
(def default-license "MIT")

(def example-substring "(e.g. URL of a GitHub release asset)")

;; The required fields are :pod/name, :pod/version and :pod/artifacts.
;; https://github.com/babashka/pod-registry

(def spec 
  {:license
   {:desc (format "Project license (e.g. %s)" default-license)
    :default default-license}

   :description
   {:desc "Project description"
    :alias :d
    :default "Babashka pod for parsing HTML with jsoup."}

   :example
   {:desc "Example that shows how to use the pod"
    :alias :e
    :default "examples/jsoup.bb"}

   :name
   {:desc "Namespaced identifier of this project <group-id>/<pod-id> (e.g. com.github.jackdbd/pod.jackdbd.jsoup)"
    :alias :n
    :default default-name
    :require true}

   :language
   {:desc "The programming language the pod is written in"
    :default "clojure"}

   :linux-amd64
   {:desc (format "URL where the amd64 Linux binary is hosted %s" example-substring)}

   :macos-aarch64
   {:desc (format "URL where the AArch64 macOS binary is hosted %s" example-substring)}

   :macos-x86_64
   {:desc (format "URL where the x86_64 macOS binary is hosted %s" example-substring)}

   :windows-amd64
   {:desc (format "URL where the AMD64 Windows binary is hosted %s" example-substring)}

   :uberjar
   {:desc (format "URL where the uberjar is hosted %s" example-substring)}

   :version
   {:desc (format "Project version (e.g. %s)" default-version)
    :default default-version
    :require true}})

(defn help
  [spec]
  (let [stdout (str/trim (format "
Babashka Pod registry manifest.edn generator
  
Generate a manifest.edn to register your pod on the pod registry.
  
Options:
%s

Examples:
  manifest.bb --uberjar <UBERJAR_URL>
  manifest.bb --linux-amd64 <linux-amd64-zip-url> --macos-aarch64 <macos-aarch64-zip-url> --windows-amd64 <windows-amd64-zip-url>
" (cli/format-opts spec)))]
    {:exit-code 0 :stdout stdout}))

(defn error-fn
  [{:keys [spec type cause msg option opts]
    :as _data}]
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
          (System/exit 1)))))

(defn -main
  [& _args]
  ;;  (println "=== args ===" _args)
  ;;  (println "=== *command-line-args* ===" *command-line-args*)
  (let [opts (cli/parse-opts *command-line-args* {:spec spec :error-fn error-fn})
        result (if (or (nil? *command-line-args*) (:help opts) (:h opts))
                 (help {:spec spec})
                 (let [pod-id (:name opts)
                       pod-name (str/replace pod-id #"\." "-")
                       artifacts (cond-> []
                                   (:linux-amd64 opts) (conj {:os/arch "amd64"
                                                              :os/name "Linux.*"
                                                              :artifact/url (:linux-amd64 opts)
                                                              :artifact/executable pod-name})
                                   (:macos-aarch64 opts) (conj {:os/arch "aarch64"
                                                                :os/name "Mac.*"
                                                                :artifact/url (:macos-aarch64 opts)
                                                                :artifact/executable pod-name})
                                   (:macos-x86_64 opts) (conj {:os/arch "x86_64"
                                                               :os/name "Mac.*"
                                                               :artifact/url (:macos-x86_64 opts)
                                                               :artifact/executable pod-name})
                                   (:windows-amd64 opts) (conj {:os/arch "amd64"
                                                                :os/name "Windows.*"
                                                                :artifact/url (:windows-amd64 opts)
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
                   {:exit-code 0 :stdout (format "Wrote %s" filepath)}))]
    (when-let [stdout (:stdout result)]
      (println stdout))))

(when (System/getProperty "babashka.file")
  (-main *command-line-args*))
