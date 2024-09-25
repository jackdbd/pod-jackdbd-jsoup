(ns user
  "Tools for interactive development with the REPL.
   This file should not be included in a production build of the application."
  (:require [portal.api :as p]))

(comment
  (def portal (p/open {:window-title "Portal UI"}))
  (add-tap #'p/submit)

  (tap> {:foo "bar"})
  (p/clear)
  (p/close portal))
