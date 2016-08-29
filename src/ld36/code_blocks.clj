(ns ld36.code-blocks
  (:require [ld36
             [common :as c]
             [turing-machine :as tm]])
  (:import (com.badlogic.gdx.scenes.scene2d.ui Stack Table Label Label$LabelStyle Image)
           (com.badlogic.gdx.scenes.scene2d Actor)
           (com.badlogic.gdx.graphics.g2d BitmapFont)
           (com.badlogic.gdx.graphics Color)
           (com.badlogic.gdx.utils Scaling Align)
           [com.badlogic.gdx.scenes.scene2d.utils ClickListener]
           (com.badlogic.gdx Input$Buttons)))

(defn make-empty-cell
  []
  (let [actor (Actor.)]
    (.setBounds actor 0 0 40 40)
    actor))

(defn make-cell-label
  [text]
  (let [font (.get c/manager "bitstream50.ttf" BitmapFont)]
    (Label. (str text) (Label$LabelStyle. font Color/BLACK))))

(defn cycle-cell [tm state symbol column]
  (swap! tm tm/toggle-code-cell state symbol column))

(defn make-goto-cell
  [goto]
  (case goto
    :halt (c/make-non-scaling-image "images/halt.png")
    :accept (c/make-non-scaling-image "images/accept.png")
    :reject (c/make-non-scaling-image "images/reject.png")
    nil (make-empty-cell)
    (make-cell-label goto)))

(defn make-move-cell
  [move]
  (case move
    :L (c/make-non-scaling-image "images/small-left-arrow.png")
    :R (c/make-non-scaling-image "images/small-right-arrow.png")
    (make-empty-cell)))

(defn make-write-cell
  [write]
  (if-not (nil? write)
    (make-cell-label write)
    (make-empty-cell)))

(declare handle-cell-click)

(defn add-click-listeners
  [actor tm state symbol column cell]
  (.addListener actor (proxy [ClickListener] []
                        (clicked [event x y]
                          (handle-cell-click tm state symbol column cell inc))))
  (.addListener actor (proxy [ClickListener] [Input$Buttons/RIGHT]
                        (clicked [event x y]
                          (handle-cell-click tm state symbol column cell dec)))))

(defn handle-cell-click
  [tm state symbol column cell inc-or-dec]
  (swap! tm tm/toggle-code-cell
         state symbol column inc-or-dec)
  (let [{:keys [code]} @tm
        state-code (state code)
        {:keys [write move goto]} ((or state-code {}) symbol)
        new-actor (case column
                    :write (make-write-cell write)
                    :move (make-move-cell move)
                    :goto (make-goto-cell goto))]
    (.setActor cell new-actor)
    (add-click-listeners new-actor tm state symbol column cell)))

(defn make-code-block
  [tm state symbols]
  (let [{:keys [code]} @tm
        state-code (state code)
        table (Table.)
        read-image (Image. (c/make-texture-drawable "images/read.png"))
        write-image (Image. (c/make-texture-drawable "images/write.png"))
        move-image (Image. (c/make-texture-drawable "images/move.png"))
        goto-image (Image. (c/make-texture-drawable "images/goto.png"))
        header-label (make-cell-label state)
        background (Image. (c/make-nine-patch-drawable "images/table-background.png"
                                                       70 7 125 7))
        stack (Stack.)]
    ;;(.debug table)
    ;; draw  header
    (-> table
        (.add header-label)
        (.colspan 4)
        (.align Align/center))
    (.row table)
    ;; draw line header
    (doseq [image [read-image write-image move-image goto-image]]
      (.setScaling image Scaling/none)
      (-> table
          (.add image)
          (.minSize (float 60))))
    (doseq [symbol symbols]
      (let [{:keys [write move goto]} ((or state-code {}) symbol)
            write-cell (make-write-cell write)
            move-cell (make-move-cell move)
            goto-cell (make-goto-cell goto)]

        (.row table)
        (.add table (make-cell-label symbol))
        (doseq [[actor column] (map list
                                    [write-cell move-cell goto-cell]
                                    [:write :move :goto])]
          (let [cell (.add table actor)]
            (add-click-listeners actor tm state symbol column cell)))))
    (.pack table)
    (.add stack background)
    (.add stack table)
    (.pack stack)
    stack))

(defn make-code-blocks
  [tm]
  (let [{:keys [symbols states code]} @tm
        table (Table.)
        num-columns 5
        states-per-row (partition num-columns num-columns (repeat num-columns nil) states)]
    (doseq [states states-per-row]
      (doseq [state states]
        (if-not (nil? state)
          (-> table
              (.add (make-code-block tm state symbols))
              (.pad (float 3)))
          (-> table (.add))))
      (.row table))
    (.pack table)
    table))

(defn stringify
  [col]
  (str "{" (clojure.string/join ", " col) "}"))

(defn make-states-and-symbols-actor
  [tm change-fn]
  (let [table (Table.)
        states-label (make-cell-label (stringify (:states @tm)))
        symbols-label (make-cell-label (stringify (:symbols @tm)))
        [state-minus state-plus
         symbol-minus symbol-plus] (map (fn [[mutator file label tm-accessor]]
                                          (let [button (c/make-non-scaling-image file)]
                                            (.addListener button (proxy [ClickListener] []
                                                                   (clicked [event x y]
                                                                     (swap! tm mutator)
                                                                     (.setText label (stringify (tm-accessor @tm)))
                                                                     (.pack table)
                                                                     (change-fn))))
                                            button))
         [[tm/remove-state "images/minus.png" states-label :states]
          [tm/add-state "images/plus.png" states-label :states]
          [tm/remove-symbol "images/minus.png" symbols-label :symbols]
          [tm/add-symbol "images/plus.png" symbols-label :symbols]])]
    (-> table
        (.add state-minus)
        (.padRight (float 20)))
    (-> table
        (.add state-plus)
        (.padRight (float 20)))
    (-> table
        (.add (make-cell-label "States:"))
        (.align Align/left))
    (-> table
        (.add states-label)
        (.align Align/left))
    (.row table)
    (-> table
        (.add symbol-minus)
        (.padRight (float 20)))
    (-> table
        (.add symbol-plus)
        (.padRight (float 20)))
    (-> table
        (.add (make-cell-label "Symbols:"))
        (.align Align/left))
    (-> table
        (.add symbols-label)
        (.align Align/left))
    (.pack table)
    table))
