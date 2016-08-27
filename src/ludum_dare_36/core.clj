(ns ludum-dare-36.core
  (:require [ludum-dare-36.common :as c])
  (:import [com.badlogic.gdx ApplicationAdapter Gdx]
           [com.badlogic.gdx.graphics Texture GL30 OrthographicCamera]
           [com.badlogic.gdx.backends.lwjgl
            LwjglApplication
            LwjglApplicationConfiguration]
           [org.lwjgl.input Keyboard]
           [com.badlogic.gdx.utils.viewport FitViewport]
           [com.badlogic.gdx.scenes.scene2d Stage Actor Group]
           [com.badlogic.gdx.scenes.scene2d.ui Image]
           [com.badlogic.gdx.graphics.g2d Sprite]
           [com.badlogic.gdx.assets AssetManager]))

(declare manager)

(defn make-tape-actor
  []
  (let [tape-sprite (Sprite. (.get manager "images/tape.png" Texture))
        head-texture (.get manager "images/head.png")
        group (proxy [Group] [])
        head (proxy [Image]
                 [head-texture])
        tape (proxy [Actor]
                 []
                 (draw [batch parent-alpha]
                   (proxy-super draw batch parent-alpha)
                   (.draw tape-sprite batch))
                 (positionChanged []
                   (proxy-super positionChanged)
                   (.setPosition tape-sprite (.getX this) (.getY this))))]
    (.setBounds tape
                (.getX tape-sprite) (.getY tape-sprite)
                (.getWidth tape-sprite) (.getHeight tape-sprite))
    (.addActor group tape)
    (.addActor group head)
    (.setPosition head (- (/ (.getWidth tape-sprite) 2)
                          (/ (.getWidth head-texture) 2)) -9)
    group))

(defn make-stage
  []
  (let [[sw sh] c/screen-size
        stage (proxy [Stage]
                  [(FitViewport. sw sh)])
        tape (make-tape-actor)]
    (.addActor stage tape)
    (.setPosition tape 0 100)
    stage))

(defn make-application
  []
  (let [stage (atom nil)]

    (proxy [ApplicationAdapter]
        []
        (create []
          (def manager (AssetManager.))
          (doseq [file ["images/tape.png" "images/head.png"]]
            (.load manager file Texture))
          (.finishLoading manager)
          (reset! stage (make-stage))
          (.setInputProcessor Gdx/input @stage))

        (render []
          (apply (fn [r g b a] (.glClearColor Gdx/gl  r g b a))
                 (map #(float (/ % 255)) [164 168 128 255]))
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
