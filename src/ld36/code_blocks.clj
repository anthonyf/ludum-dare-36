(ns ld36.code-blocks
  (:require [ld36.common :as c]
            [ld36.turing-machine :as t])
  (:import (com.badlogic.gdx.scenes.scene2d.ui Table Label Label$LabelStyle Image)
           (com.badlogic.gdx.graphics.g2d BitmapFont NinePatch)
           (com.badlogic.gdx.graphics Color)
           (com.badlogic.gdx.utils Scaling Align)))

(defn make-code-block
  [state state-code symbols]
  (let [table (Table.)
        read-image (Image. (c/make-texture-drawable "images/read.png"))
        write-image (Image. (c/make-texture-drawable "images/write.png"))
        move-image (Image. (c/make-texture-drawable "images/move.png"))
        goto-image (Image. (c/make-texture-drawable "images/goto.png"))
        right-arrow (Image. (c/make-texture-drawable "images/small-right-arrow.png"))
        left-arrow (Image. (c/make-texture-drawable "images/small-left-arrow.png"))
        accept (Image. (c/make-texture-drawable "images/accept.png"))
        reject (Image. (c/make-texture-drawable "images/reject.png"))
        halt (Image. (c/make-texture-drawable "images/halt.png"))
        font (.get c/manager "bitstream50.ttf" BitmapFont)
        label (Label. (str state) (Label$LabelStyle. font Color/BLACK))
        blah (Label. "blah" (Label$LabelStyle. font Color/BLACK))]
    (doseq [image [right-arrow left-arrow halt accept reject]]
      (.setScaling image Scaling/none))
    (.setBackground table (c/make-nine-patch-drawable "images/table-background.png" 5 5 5 5))
    ;;(.debug table)
    ;; draw  header
    (-> table
        (.add label)
        (.colspan 4)
        (.align Align/center))
    (.row table)
    ;; draw line header
    (doseq [image [read-image write-image move-image goto-image]]
      (.setScaling image Scaling/none)
      (-> table
          (.add image)
          (.minSize (float 60))
          ))
    (doseq [symbol symbols]
      (let [{:keys [write move goto]} (state-code symbol)]
        (.row table)
        (.add table (Label. (str symbol) (Label$LabelStyle. font Color/BLACK)))
        (.add table (Label. (str write) (Label$LabelStyle. font Color/BLACK)))
        (.add table (case move
                      :L left-arrow
                      :R right-arrow))
        (.add table (case goto
                      :halt halt
                      :accept accept
                      :reject reject
                      (Label. (str goto) (Label$LabelStyle. font Color/BLACK))))))
    table))

(defn make-code-blocks
  [code]
  (let [{:keys [symbols states code]} code
        table (Table.)
        num-columns 3
        states-per-row (partition num-columns num-columns (repeat num-columns nil) states)]
    (doseq [states states-per-row]
      (doseq [state states]
        (if-not (nil? state)
          (-> table
              (.add (make-code-block state (state code) symbols))
              (.pad (float 3)))
          (-> table (.add))))
      (.row table))
    (.pack table)
    table))
