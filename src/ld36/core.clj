(ns ld36.core
  (:require [ld36.common :as c]
            [ld36.protocols :as p]
            [ld36.tape-actor :as ta]
            [ld36.turing-machine :as tm])
  (:import [com.badlogic.gdx ApplicationAdapter ApplicationListener Gdx]
           [com.badlogic.gdx.graphics Texture GL30 OrthographicCamera]
           [com.badlogic.gdx.backends.lwjgl
            LwjglApplication
            LwjglApplicationConfiguration]
           [org.lwjgl.input Keyboard]
           [com.badlogic.gdx.utils.viewport FitViewport]
           [com.badlogic.gdx.scenes.scene2d.ui Image ImageButton]
           [com.badlogic.gdx.scenes.scene2d Stage]
           [com.badlogic.gdx.graphics.g2d Sprite BitmapFont TextureRegion]
           [com.badlogic.gdx.graphics Color]
           [com.badlogic.gdx.assets.loaders.resolvers InternalFileHandleResolver]))

(defn make-stage
  []
  (let [[sw sh] c/screen-size
        stage (proxy [Stage]
                  [(FitViewport. sw sh)])
        tape-actor (ta/make-tape-actor @tm/tape)
        [left-button right-button] (ta/make-tape-buttons (fn []
                                                           (swap! tm/tape tm/tape-left)
                                                           (p/move-left tape-actor @tm/tape))
                                                         (fn []
                                                           (swap! tm/tape tm/tape-right)
                                                           (p/move-right tape-actor @tm/tape)))]
    (.addActor stage tape-actor)
    (.addActor stage left-button)
    (.addActor stage right-button)
    (.setPosition tape-actor 0 100)
    stage))

(def assets [["images/tape.png" Texture]
             ["images/head.png" Texture]
             ["images/button.png" Texture]
             ["images/left-arrow-button-down.png" Texture]
             ["images/left-arrow-button-up.png" Texture]
             ["images/right-arrow-button-down.png" Texture]
             ["images/right-arrow-button-up.png" Texture]
             ["bitstream30.ttf" BitmapFont (c/make-font-params "fonts/Bitstream Vera Sans Mono Roman.ttf" :size 100 :color Color/BLACK)]])

(defn make-application
  []
  (let [stage (atom nil)]

    (proxy [ApplicationAdapter]
        []
        (create []
          (proxy-super create)

          ;; load all assets
          (doseq [[file type & [param]] assets]
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
