(ns ludum-dare-36.core
  (:require [ludum-dare-36.common :as c])
  (:import [com.badlogic.gdx ApplicationAdapter Gdx]
           [com.badlogic.gdx.graphics GL30 OrthographicCamera]
           [com.badlogic.gdx.backends.lwjgl
            LwjglApplication
            LwjglApplicationConfiguration]
           [org.lwjgl.input Keyboard]
           [com.badlogic.gdx.utils.viewport FitViewport]
           [com.badlogic.gdx.scenes.scene2d Stage Actor]))

(defn make-stage
  []
  (let [[sw sh] c/screen-size]
    (proxy [Stage]
        [(FitViewport. sw sh)]
        (create [])
        )))


(defn make-application
  []
  (let [stage (atom nil)]
    (proxy [ApplicationAdapter]
        []
        (create []
          (reset! stage (make-stage))
          (.setInputProcessor Gdx/input @stage))

        (render []
          (.glClear Gdx/gl GL30/GL_COLOR_BUFFER_BIT)
          (.act @stage)
          (.draw @stage))

        (resize [width height]
          (-> @stage
              .getViewport
              (.update width height true)))

        (dispose []
          (.dispose @stage)))))

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
