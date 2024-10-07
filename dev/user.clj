(ns user
  "Tools for interactive development with the REPL.
   This file should not be included in a production build of the application."
  (:require
   [clojure.string :as str]
   [pod.jackdbd.jsoup-wrapper :refer [select]]
   [portal.api :as p]))

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

(comment
  ;; Option 1: launch Portal as standalone PWA
  (def portal (p/open {:window-title "Portal UI"}))
  ;; Option 2: launch Portal as VS Code tab (requires the djblue.portal VS Code extension)
  (def portal (p/open {:launcher :vs-code
                       :theme :portal.colors/nord}))

  (add-tap #'p/submit)

  (tap> {:foo "bar"})
  (p/clear)

  (def elements (select html "div.foo"))
  (tap> elements)

  (tap> (with-meta
          [:portal.viewer/html html]
          {:portal.viewer/default :portal.viewer/hiccup}))

  (tap> (with-meta
          [:portal.viewer/html (-> (first elements) :outer-html)]
          {:portal.viewer/default :portal.viewer/hiccup}))

  (p/close portal))
