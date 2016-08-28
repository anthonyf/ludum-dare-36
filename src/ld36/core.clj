(ns ld36.core
  (:require [ld36.common :as c]
            [ld36.protocols :as p]
            [ld36.tape-actor :as ta])
  (:import [com.badlogic.gdx ApplicationAdapter ApplicationListener Gdx]
           [com.badlogic.gdx.graphics Texture GL30 OrthographicCamera]
           [com.badlogic.gdx.backends.lwjgl
            LwjglApplication
            LwjglApplicationConfiguration]
           [org.lwjgl.input Keyboard]
           [com.badlogic.gdx.utils.viewport FitViewport]
           [com.badlogic.gdx.scenes.scene2d.ui Image ImageButton]
           [com.badlogic.gdx.scenes.scene2d.utils ClickListener]
           [com.badlogic.gdx.scenes.scene2d Stage]
           [com.badlogic.gdx.graphics.g2d Sprite BitmapFont TextureRegion]
           [com.badlogic.gdx.graphics Color]
           [com.badlogic.gdx.assets.loaders.resolvers InternalFileHandleResolver]
           [com.badlogic.gdx.scenes.scene2d.utils TextureRegionDrawable]))


(defprotocol Tape
  (set-tape [this tape])
  (move-right [this tape])
  (move-left [this tape])
  (write-symbol [this tape]))

(def tape (atom (let [tape {:data (into {} (for [x (range 10)] [x 1] ))
                            :pos 0}]
                  (-> tape
                      (assoc-in [:data -5] 1)
                      (assoc-in [:data -15] 1)
                      (assoc-in [:data -25] 1)
                      (assoc-in [:data 25] 1)))))

(defn tape-left [tape]
  (update tape :pos dec))

(defn tape-right [tape]
  (update tape :pos inc))

(defn make-tape-buttons
  [left-click-fun right-click-fun]
  (let [left-button (ImageButton.
                     (TextureRegionDrawable. (TextureRegion. (.get c/manager "images/left-arrow-button-up.png" Texture)))
                     (TextureRegionDrawable. (TextureRegion. (.get c/manager "images/left-arrow-button-down.png" Texture))))
        right-button (ImageButton.
                      (TextureRegionDrawable. (TextureRegion. (.get c/manager "images/right-arrow-button-up.png" Texture)))
                      (TextureRegionDrawable. (TextureRegion. (.get c/manager "images/right-arrow-button-down.png" Texture))))
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

(defn make-stage
  []
  (let [[sw sh] c/screen-size
        stage (proxy [Stage]
                  [(FitViewport. sw sh)])
        tape-actor (ta/make-tape-actor @tape)
        [left-button right-button] (make-tape-buttons (fn []
                                                        (swap! tape tape-left)
                                                        (move-left tape-actor @tape))
                                                      (fn []
                                                        (swap! tape tape-right)
                                                        (move-right tape-actor @tape)))]
    (.addActor stage tape-actor)
    (.addActor stage left-button)
    (.addActor stage right-button)
    (.setPosition tape-actor 0 100)
    stage))

(defn make-application
  []
  (let [stage (atom nil)]

    (proxy [ApplicationAdapter]
        []
        (create []
          (proxy-super create)
          (doseq [[file type & [param]] [["images/tape.png" Texture]
                                         ["images/head.png" Texture]
                                         ["images/button.png" Texture]
                                         ["images/left-arrow-button-down.png" Texture]
                                         ["images/left-arrow-button-up.png" Texture]
                                         ["images/right-arrow-button-down.png" Texture]
                                         ["images/right-arrow-button-up.png" Texture]
                                         ["bitstream30.ttf" BitmapFont (c/make-font-params "fonts/Bitstream Vera Sans Mono Roman.ttf" :size 100 :color Color/BLACK)]]]
            (.load c/manager file type param))
          (.finishLoading c/manager)
          (reset! stage (make-stage))
          (.setInputProcessor Gdx/input @stage))

        (render []
          (proxy-super render)
          (apply (fn [r g b a] (.glClearColor Gdx/gl  r g b a))
                 (map #(float (/ % 255)) [164 168 128 255]))
          (.glClear Gdx/gl GL30/GL_COLOR_BUFFER_BIT)
          (.act @stage)
          (.draw @stage))

        (resize [width height]
          (proxy-super resize width height)
          (-> @stage
              .getViewport
              (.update width height true)))

        (dispose []
          (proxy-super dispose)
          (.dispose @stage)
          (.dispose c/manager)))))

(defn -main
  []
  (let [[width height] c/screen-size
        config (doto (LwjglApplicationConfiguration.)
                 (-> .title (set! c/title))
                 (-> .width (set! width))
                 (-> .height (set! height))
                 (-> .resizable (set! true)))]
    (LwjglApplication. (make-application) config)
    (Keyboard/enableRepeatEvents true)))
