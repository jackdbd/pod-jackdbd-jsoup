#!/usr/bin/env bb
(ns manifest
  (:require 
   [babashka.cli :as cli]
   [utils :refer [format-edn]]
   [clojure.string :as str]))

;; Show help with `bb bb/manifest.bb --help` (or with -h, :help, :h)
(defn show-help
  [spec]
  (cli/format-opts (merge spec {:order (vec (keys (:spec spec)))})))

(def cli-spec
  {:spec
   {:license {:desc "Pod license (e.g. MIT)"
              :require true}
    :pod-id {:desc "Pod ID (e.g. com.github.jackdbd/pod.jackdbd.jsoup)"
             :require true}
    :linux-x86_64 {:desc "URL where the x86_64 Linux binary is hosted (GitHub release)"}
    :macos-aarch64 {:desc "URL where the AArch64 macOS binary is hosted (GitHub release)"}
    :windows-x86_64 {:desc "URL where the x86_64 Windows binary is hosted (GitHub release)"}
    :uberjar {:desc "URL where the uberjar is hosted (GitHub release)"}
    :version {:desc "Pod version (e.g. 1.2.3)"
              :require true}}
   :error-fn
   (fn [{:keys [spec type cause msg option opts] :as data}]
     (when (and (= :org.babashka/cli type) (not (or (:help opts) (:h opts))))
       (case cause
         :require (do
                    (println (format "Missing required argument: %s\n" option))
                    (System/exit 1))
         :validate (do (println (format "%s does not exist!\n" msg))
                       (System/exit 1)))))})

(defn -main
  [args]
  (let [opts (cli/parse-opts args cli-spec)]
    (when (or (:help opts) (:h opts))
      (println (show-help cli-spec))
      (System/exit 0))
    (let [pod-id (:pod-id opts)
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
                      (:windows-x86_64 opts) (conj {:os/arch "x86_64"
                                                    :os/name "Windows.*"
                                                    :artifact/url (:windows-x86_64 opts)
                                                    :artifact/executable (format "%s.exe" pod-name)})
                      (:uberjar opts) (conj {:artifact/url (:uberjar opts)}))
          manifest-data {:pod/name pod-id
                         :pod/description "Babashka pod for parsing HTML with jsoup."
                         :pod/example "examples/jsoup.bb"
                         :pod/language "clojure"
                         :pod/license (:license opts)
                         :pod/version (:version opts)
                         :pod/artifacts artifacts}
          filepath "manifest.edn"
          edn-content (format-edn manifest-data)]
      (spit filepath edn-content)
      (println (str "Wrote " filepath)))))

(-main *command-line-args*)
