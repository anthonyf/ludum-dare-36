(ns ld36.run-controls
  (:require [ld36
             [common :as c]])
  (:import (com.badlogic.gdx.scenes.scene2d.ui Table)))

(defn make-run-controls
  []
  (let [run-button (c/make-button "Run")
        step-button (c/make-button "Step")
        back-button (c/make-button "Back")
        table (Table.)
        padding (float 10)]
    (-> table
        (.add run-button)
        (.padBottom padding))
    (.row table)
    (-> table
        (.add step-button)
        (.padBottom padding))
    (.row table)
    (-> table
        (.add back-button))
    (.pack table)
    table))
