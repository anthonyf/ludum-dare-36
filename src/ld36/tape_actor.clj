(ns ld36.tape-actor
  (:require [ld36.common :as c])
  (:import [com.badlogic.gdx.graphics Texture]
           [com.badlogic.gdx.graphics.g2d Sprite BitmapFont TextureRegion]
           [com.badlogic.gdx.scenes.scene2d.ui Image Label Label$LabelStyle ImageButton Table]
           [com.badlogic.gdx.graphics Color]
           [com.badlogic.gdx.scenes.scene2d Actor Group]
           [com.badlogic.gdx.scenes.scene2d.actions Actions MoveByAction TemporalAction]
           [ld36.protocols LambdaAction]
           [com.badlogic.gdx.scenes.scene2d.utils ClickListener]))

(defn tape-to-string
  [{:keys [data pos]}]
  (clojure.string/join
   ""
   (for [x (range (- pos 17) (+ pos 17 1))]
     (or (data x) " "))))

(defn make-tape-actor
  [tape head-clicked-fn]
  (let [tape-sprite (Sprite. (.get c/manager "images/tape.png" Texture))
        head-texture (.get c/manager "images/head.png")
        font (.get c/manager "bitstream100.ttf" BitmapFont)
        char-width (.width (c/char-width font "1"))
        speed 0.25
        label (Label. (tape-to-string tape)
                      (Label$LabelStyle. font
                                         Color/BLACK))
        head (Image. head-texture)
        tape (proxy [Actor]
                 []
                 (draw [batch parent-alpha]
                   (proxy-super draw batch parent-alpha)
                   (.draw tape-sprite batch))
                 (positionChanged []
                   (proxy-super positionChanged)
                   (.setPosition tape-sprite (.getX this) (.getY this))))
        group (proxy [Group ld36.protocols.Tape] []
                (set_tape [tape] )
                (move_right [tape]
                  (.addAction label (Actions/sequence
                                     (Actions/moveBy (- char-width) 0 speed)
                                     (let [action (Actions/action ld36.protocols.LambdaAction)]
                                       (.setLambda action (fn []
                                                            (.moveBy label char-width 0)
                                                            (.setText label (tape-to-string tape))))
                                       action))))
                (move_left [tape]
                  (.addAction label (Actions/sequence
                                     (Actions/moveBy char-width 0 speed)
                                     (let [action (Actions/action ld36.protocols.LambdaAction)]
                                       (.setLambda action (fn []
                                                            (.moveBy label (- char-width) 0)
                                                            (.setText label (tape-to-string tape))))
                                       action))))
                (write_symbol [tape]
                  (.setText label (tape-to-string tape))))]
    (.addListener head
                  (proxy [ClickListener] []
                    (clicked [event x y]
                      (head-clicked-fn group))))
    (.setBounds tape
                (.getX tape-sprite) (.getY tape-sprite)
                (.getWidth tape-sprite) (.getHeight tape-sprite))
    (.addActor group tape)
    (.addActor group label)
    (.setPosition label (- (+ 30 char-width)) -8)
    (.addActor group head)
    (.setPosition head (- (/ (.getWidth tape-sprite) 2)
                          (/ (.getWidth head-texture) 2)) -9)
    group))

(defn make-tape-buttons
  [left-click-fn right-click-fn clear-fn]
  (let [left-button (ImageButton.
                     (c/make-texture-drawable "images/left-arrow-button-up.png")
                     (c/make-texture-drawable "images/left-arrow-button-down.png"))
        right-button (ImageButton.
                      (c/make-texture-drawable "images/right-arrow-button-up.png")
                      (c/make-texture-drawable "images/right-arrow-button-down.png"))
        clear-button (c/make-button "Clear")
        table (Table.)
        [sw sh] c/screen-size
        padding (float 10)
        spacing 15]
    (doseq [[button action-fn] [[left-button left-click-fn]
                                [right-button right-click-fn]
                                [clear-button clear-fn]]]
      (.addListener button
                    (proxy [ClickListener] []
                      (clicked [event x y]
                        (action-fn)))))
    (-> table (.add left-button) (.padRight padding))
    (-> table (.add clear-button) (.padRight padding))
    (-> table (.add right-button))
    (.pack table)
    (.setPosition table
                  (- (/ sw 2)
                     (/ (.getWidth table) 2))
                  spacing)
    table))
