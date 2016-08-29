(ns ld36.run-controls
  (:require [ld36
             [common :as c]])
  (:import (com.badlogic.gdx.scenes.scene2d.ui Table)))

(defn make-run-controls
  []
  (let [run-button (c/make-button "Run")
        step-button (c/make-button "Step")
        table (Table.)
        padding 10]
    (-> table (.add run-button)
        (.padBottom (float padding)))
    (.row table)
    (-> table (.add step-button))
    (.pack table)
    table))
