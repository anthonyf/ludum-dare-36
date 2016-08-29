(ns ld36.run-controls
  (:require [ld36
             [common :as c]])
  (:import (com.badlogic.gdx.scenes.scene2d.ui Table)
           (com.badlogic.gdx.scenes.scene2d.utils ClickListener)))

(defn make-run-controls
  [run-clicked-fn step-clicked-fn back-clicked-fn]
  (let [run-button (c/make-button "Run")
        step-button (c/make-button "Step")
        back-button (c/make-button "Back")
        table (Table.)
        padding (float 10)]
    (doseq [[button action-fn] [[run-button run-clicked-fn]
                                [step-button step-clicked-fn]
                                [back-button back-clicked-fn]]]
      (.addListener button (proxy [ClickListener] []
                             (clicked [event x y]
                               (action-fn)))))
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
