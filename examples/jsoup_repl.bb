#!/usr/bin/env bb
(ns jsoup-repl
  (:require
   [babashka.pods :as pods]
   [clojure.edn :as edn]
   [clojure.string :as str]))

(comment
  ;; Run these commands in a Babashka REPL
  (require '[babashka.pods :as pods])

  (def project (-> (edn/read-string (slurp "deps.edn")) :aliases :neil :project))
  (def pod-spec (:name project))
  (def pod-id (name pod-spec))
  (def pod-name (str/replace pod-id #"\." "-"))
  (def pod-version (:version project))

  ;; Example 1: load the pod compiled as an uberjar
  (def uber-file (format "target/%s-%s-standalone.jar" pod-id pod-version))
  (pods/load-pod ["java" "-jar" uber-file])

  ;; Example 2: load the pod compiled as an executable with GraalVM native-image
  (def exe-file (format "target/%s" pod-name))
  (pods/load-pod exe-file)

  ;; Example 3: load a pod that was published to the Babashka pod registry
  ;; https://github.com/babashka/pod-registry
  ;; https://clojars.org/com.github.jackdbd/pod.jackdbd.jsoup
  (pods/load-pod pod-spec "0.1.10")

  (require '[pod.jackdbd.jsoup :as jsoup])
  (require '[babashka.http-client :as http])

  (-> (http/get "https://clojure.org")
      :body
      (jsoup/select "div p")
      first
      :text))
