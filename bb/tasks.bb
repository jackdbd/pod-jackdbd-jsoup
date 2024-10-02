(ns tasks
  (:require
   [babashka.classpath :refer [get-classpath split-classpath]]
   [babashka.http-client :as http]
   [babashka.pods :as pods]
   [clojure.edn :as edn]
   [utils :refer [format-edn]]))

(defn print-classpath []
  (println "=== CLASSPATH BEGIN ===")
  ;; (System/getProperty "java.class.path")
  (doseq [path (set (split-classpath (get-classpath)))]
    (println path))
  (println "=== CLASSPATH END ==="))

(defn manifest.edn
  "Generates a `manifest.edn` for [Babashka pod registry](https://github.com/babashka/pod-registry/)."
  [{:keys [aarch64-linux
           aarch64-macos
           amd64-linux
           amd64-macos
           amd64-windows
           license
           pod-id
           version
           x86_64-linux
           x86_64-macos]}]
  (let [pod-name (name pod-id)
        artifacts (cond-> []
                    aarch64-linux (conj {:os/arch "aarch64"
                                         :os/name "Linux.*"
                                         :artifact/url aarch64-linux
                                         :artifact/executable pod-name})
                    aarch64-macos (conj {:os/arch "aarch64"
                                         :os/name "Mac.*"
                                         :artifact/url aarch64-macos
                                         :artifact/executable pod-name})
                    amd64-linux (conj {:os/arch "amd64"
                                       :os/name "Linux.*"
                                       :artifact/url amd64-linux
                                       :artifact/executable pod-name})
                    amd64-macos (conj {:os/arch "amd64"
                                       :os/name "Mac.*"
                                       :artifact/url amd64-macos
                                       :artifact/executable pod-name})
                    amd64-windows (conj {:os/arch "amd64"
                                         :os/name "Windows.*"
                                         :artifact/url amd64-windows
                                         :artifact/executable (format "%s.exe" pod-name)})
                    x86_64-linux (conj {:os/arch "x86_64"
                                        :os/name "Linux.*"
                                        :artifact/url x86_64-linux
                                        :artifact/executable pod-name})
                    x86_64-macos (conj {:os/arch "x86_64"
                                        :os/name "Mac.*"
                                        :artifact/url x86_64-macos
                                        :artifact/executable pod-name}))]
    {:pod/name pod-id
     :pod/description "Babashka pod for parsing HTML with jsoup."
     :pod/example "examples/jsoup.bb"
     :pod/language "clojure"
     :pod/license license
     :pod/version version
     :pod/artifacts artifacts}))

(defn write-manifest.edn [opts]
  (prn "*command-line-args*" *command-line-args*)
  ;; (when *command-line-args*
  ;;   (prn "got CLI args" *command-line-args*))
  (let [filepath "manifest.edn"
        manifest-data (manifest.edn opts)
        edn-content (format-edn manifest-data)]
    (spit filepath edn-content)
    (println (str "Wrote " filepath))
    ;; (println (edn/read-string (slurp filepath)))
    ))

(comment
  (print-classpath)

  (def project (-> (edn/read-string (slurp "deps.edn")) :aliases :neil :project))
  (def pod-id (:name project))
  (def pod-name (name pod-id))
  (def pod-version (:version project))

  (def uber-file (format "target/%s-%s-standalone.jar" pod-name pod-version))

  ;; It seems these functions cannot be defined outside of a rich comment block,
  ;; otherwise the Babashka task runner cannot run ANY task. I guess it's due to
  ;; the fact that jsoup/select cannot be resolved until the pod is loaded.
  (defn demo-pod-native []
    (prn (str "Load Babashka pod " pod-name " version " pod-version " (binary)"))
    (pods/load-pod "./target/pod-jackdbd-jsoup")
    (prn "pod loaded (binary)")

    (require '[pod.jackdbd.jsoup :as jsoup])
    (prn "pod required (binary)")

    (-> (http/get "https://clojure.org")
        :body
        (jsoup/select "div p")
        first
        :text))

  (defn demo-pod-uberjar []
    (prn (str "Load Babashka pod " pod-name " version " pod-version " (uberjar)"))
    (pods/load-pod ["java" "-jar" uber-file])
    (prn "pod loaded (uberjar)")

    (require '[pod.jackdbd.jsoup :as jsoup])
    (prn "pod required (uberjar)")

    (-> (http/get "https://clojure.org")
        :body
        (jsoup/select "div p")
        first
        :text))

  (demo-pod-native)
  (demo-pod-uberjar)

  (def aarch64-macos "https://github.com/fluree/pod-fluree-crypto/releases/download/v0.1.2/pod-fluree-crypto-linux-arm64.zip")
  (def amd64-windows "some-github-release-url-windows_amd64.zip")
  (def x86_64-linux "some-github-release-url-linux_x86_64.zip")

  (manifest.edn {:aarch64-macos aarch64-macos
                 :amd64-windows amd64-windows
                 :x86_64-linux x86_64-linux
                 :license "MIT"
                 :pod-id pod-id
                 :version pod-version})

  (write-manifest.edn {:aarch64-macos aarch64-macos
                       :amd64-windows amd64-windows
                       :x86_64-linux x86_64-linux
                       :license "MIT"
                       :pod-id pod-id
                       :version pod-version})
  )