(ns how-to-use
  (:require [babashka.http-client :as http]
            [babashka.pods :as pods]
            [cheshire.core :as json]
            [clojure.edn :as edn]
            [clojure.string :as str]))

(def project (-> (edn/read-string (slurp "deps.edn")) :aliases :neil :project))
(def pod-spec (:name project))
(def pod-id (name pod-spec))
(def pod-name (str/replace pod-id #"\." "-"))
;; (def pod-version "0.1.10")
(def pod-version (:version project))

(def jar-file (format "target/%s-%s.jar" pod-id pod-version))
(def uber-file (format "target/%s-%s-standalone.jar" pod-id pod-version))
(def exe-file (format "target/%s" pod-name))

(def html (str/join "" ["<!DOCTYPE html>"
                        "<html lang='en-US'>"
                        "<head>"
                        "  <meta charset='UTF-8'>"
                        "  <title>Hello world</title>"
                        "</head>"
                        "<body>"
                        "  <h1 data-abc=\"def\">Test world</h1>"
                        "  <div class='foo' id='the-foo'><p>This is foo</p></div>"
                        "  <div class='bar'><p data-abc=\"def\">This is bar</p></div>"
                        "  <div class='foo' id='the-other-foo'><p data-abc=\"xyz\">This is another foo</p></div>"
                        "</body>"
                        "</html>"]))

(pods/load-pod exe-file)
(require '[pod.jackdbd.jsoup :as jsoup])

(def filepath "target/test-html.json")
(def parsed (jsoup/select html "div.foo"))
(spit filepath (json/generate-string {:html html :parsed parsed}))
(println (str "wrote " filepath))

(comment
  ;; Run these commands in a Babashka REPL

  ;; Example 1: load the pod compiled as an uberjar
  (pods/load-pod ["java" "-jar" uber-file])

  ;; Example 2: load the pod compiled as an executable with GraalVM native-image
  (pods/load-pod exe-file)

  ;; Example 3: load a pod that was published to the Babashka pod registry
  ;; https://github.com/babashka/pod-registry
  ;; https://clojars.org/com.github.jackdbd/pod.jackdbd.jsoup
  (pods/load-pod pod-spec "0.1.10")
   
  (jsoup/select html "div.foo") 

  (-> (http/get "https://clojure.org")
      :body
      (jsoup/select "div p")
      first
      :text)
  )
