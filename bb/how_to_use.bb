(ns how-to-use
  (:require [babashka.http-client :as http]
            [babashka.pods :as pods]
            [cheshire.core :as json]
            [clojure.edn :as edn]
            [clojure.string :as str]))

(def project (-> (edn/read-string (slurp "deps.edn")) :aliases :neil :project))
(def pod-id (name (:name project)))
(def pod-name (str/replace pod-id #"\." "-"))
(def pod-version (:version project))
(def uber-file (format "target/%s-%s-standalone.jar" pod-id pod-version))
(def exe-file (format "target/%s-%s" pod-name pod-version))

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
   
  (jsoup/select html "div.foo") 

  (-> (http/get "https://clojure.org")
      :body
      (jsoup/select "div p")
      first
      :text)
  )
