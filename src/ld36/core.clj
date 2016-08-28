(ns ld36.core
  (:require [ld36.common :as c]
            [ld36.protocols :as p]
            [ld36.tape-actor :as ta]
            [ld36.turing-machine :as tm]
            [ld36.code-blocks :as cb])
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

(defn cycle-cell [state symbol column cell]
  )

(def tm (atom tm/busy-beaver))

(defn make-stage
  []
  (let [padding 50
        [sw sh] c/screen-size
        stage (proxy [Stage]
                  [(FitViewport. sw sh)])
        tape-actor (ta/make-tape-actor (:tape @tm))
        program tm/busy-beaver
        [left-button right-button] (ta/make-tape-buttons (fn []
                                                           (swap! tm tm/tape-left)
                                                           (p/move-left tape-actor (:tape @tm)))
                                                         (fn []
                                                           (swap! tm tm/tape-right)
                                                           (p/move-right tape-actor (:tape @tm))))
        code-blocks (cb/make-code-blocks program cycle-cell)]
    (.addActor stage code-blocks)
    (.addActor stage tape-actor)
    (.addActor stage left-button)
    (.addActor stage right-button)
    (.setPosition tape-actor 0 100)
    (.setPosition code-blocks
                  padding
                  (- sh (+ (.getHeight code-blocks)
                           padding)))
    stage))

(def assets [["images/tape.png" Texture]
             ["images/head.png" Texture]
             ["images/button.png" Texture]
             ["images/left-arrow-button-down.png" Texture]
             ["images/left-arrow-button-up.png" Texture]
             ["images/right-arrow-button-down.png" Texture]
             ["images/right-arrow-button-up.png" Texture]
             ["images/move.png" Texture]
             ["images/read.png" Texture]
             ["images/write.png" Texture]
             ["images/goto.png" Texture]
             ["images/table-background.png" Texture]
             ;;["images/table_background.9.png" Texture]
             ["images/small-right-arrow.png" Texture]
             ["images/small-left-arrow.png" Texture]
             ["images/halt.png" Texture]
             ["images/reject.png" Texture]
             ["images/accept.png" Texture]
             ["bitstream100.ttf" BitmapFont (c/make-font-params "fonts/Bitstream Vera Sans Mono Roman.ttf" :size 100 :color Color/BLACK)]
             ["bitstream50.ttf" BitmapFont (c/make-font-params "fonts/Bitstream Vera Sans Mono Roman.ttf" :size 50 :color Color/BLACK)]])

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
