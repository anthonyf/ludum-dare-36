(ns ld36.tape-actor
  (:require [ld36.common :as c])
  (:import [com.badlogic.gdx.graphics Texture]
           [com.badlogic.gdx.graphics.g2d Sprite BitmapFont TextureRegion]
           [com.badlogic.gdx.scenes.scene2d.ui Image Label Label$LabelStyle ImageButton]
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
  [left-click-fun right-click-fun]
  (let [left-button (ImageButton.
                     (c/make-texture-drawable "images/left-arrow-button-up.png")
                     (c/make-texture-drawable "images/left-arrow-button-down.png"))
        right-button (ImageButton.
                      (c/make-texture-drawable "images/right-arrow-button-up.png")
                      (c/make-texture-drawable "images/right-arrow-button-down.png"))
        [sw sh] c/screen-size
        spacing 15]
    (.addListener left-button
                  (proxy [ClickListener] []
                    (clicked [event x y]
                      (left-click-fun))))
    (.addListener right-button
                  (proxy [ClickListener] []
                    (clicked [event x y]
                      (right-click-fun))))
    (.setPosition left-button
                  (- (/ sw 2)
                     (.getWidth left-button)
                     spacing)
                  spacing)
    (.setPosition right-button
                  (+ (/ sw 2)
                     spacing)
                  spacing)
    [left-button right-button]))
