#!/usr/bin/env bb

(require '[babashka.http-client :as http])
(require '[babashka.pods :as pods])

;; Test me by running bb -f examples/jsoup.bb when registering this pod, then delete me.
(pods/load-pod "target/pod-jackdbd-jsoup")

;; Uncomment me when registering this pod.
;; (pods/load-pod 'com.github.jackdbd/jsoup "0.4.0")

(require '[pod.jackdbd.jsoup :as jsoup])

(def text (-> (http/get "https://clojure.org")
              :body
              (jsoup/select "div p")
              first
              :text))

(println text)
