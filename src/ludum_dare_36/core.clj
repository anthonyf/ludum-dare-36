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
           [com.badlogic.gdx.scenes.scene2d.ui Image Label Label$LabelStyle ImageButton]
           [com.badlogic.gdx.scenes.scene2d.utils ClickListener]
           [com.badlogic.gdx.graphics.g2d Sprite BitmapFont TextureRegion]
           [com.badlogic.gdx.graphics Color]
           [com.badlogic.gdx.assets AssetManager]
           [com.badlogic.gdx.assets.loaders.resolvers InternalFileHandleResolver]
           [com.badlogic.gdx.graphics.g2d.freetype
            FreeTypeFontGenerator FreeTypeFontGeneratorLoader FreetypeFontLoader
            FreetypeFontLoader$FreeTypeFontLoaderParameter]
           [com.badlogic.gdx.scenes.scene2d.utils TextureRegionDrawable]))

(declare manager)

(defn make-tape-actor
  []
  (let [tape-sprite (Sprite. (.get manager "images/tape.png" Texture))
        head-texture (.get manager "images/head.png")
        label (Label. "011111111111111010111111111111110"
                      (Label$LabelStyle. (.get manager "bitstream30.ttf" BitmapFont)
                                         Color/BLACK))
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
    (.addActor group label)
    (.setPosition label -30 -8)
    (.addActor group head)
    (.setPosition head (- (/ (.getWidth tape-sprite) 2)
                          (/ (.getWidth head-texture) 2)) -9)
    group))

(defn make-tape-buttons
  []
  (let [left-button (ImageButton.
                     (TextureRegionDrawable. (TextureRegion. (.get manager "images/left-arrow-button-up.png" Texture)))
                     (TextureRegionDrawable. (TextureRegion. (.get manager "images/left-arrow-button-down.png" Texture))))
        right-button (ImageButton.
                      (TextureRegionDrawable. (TextureRegion. (.get manager "images/right-arrow-button-up.png" Texture)))
                     (TextureRegionDrawable. (TextureRegion. (.get manager "images/right-arrow-button-down.png" Texture))))]
    (.addListener left-button
                  (proxy [ClickListener] []
                    (clicked [event x y]
                      (println "Hi! clicked!"))))
    [left-button right-button]))

(defn make-stage
  []
  (let [[sw sh] c/screen-size
        stage (proxy [Stage]
                  [(FitViewport. sw sh)])
        tape (make-tape-actor)
        [left-button right-button] (make-tape-buttons)]
    (.addActor stage tape)
    (.addActor stage left-button)
    (.setPosition tape 0 100)
    stage))

(defn setup-asset-manager []
  (let [manager (AssetManager.)
        resolver (InternalFileHandleResolver.)]
    (.setLoader manager FreeTypeFontGenerator (FreeTypeFontGeneratorLoader. resolver))
    (.setLoader manager BitmapFont ".ttf" (FreetypeFontLoader. resolver))
    manager))

(defn make-font-params
  [name & {:keys [size color]}]
  (let [params (FreetypeFontLoader$FreeTypeFontLoaderParameter.)]
    (set! (.fontFileName params) name)
    (and size (set! (.size (.fontParameters params)) size))
    (and color (set! (.color (.fontParameters params)) color))
    params))

(defn make-application
  []
  (let [stage (atom nil)]

    (proxy [ApplicationAdapter]
        []
        (create []
          (proxy-super create)
          (def manager (setup-asset-manager))
          (doseq [[file type & [param]] [["images/tape.png" Texture]
                                         ["images/head.png" Texture]
                                         ["images/button.png" Texture]
                                         ["images/left-arrow-button-down.png" Texture]
                                         ["images/left-arrow-button-up.png" Texture]
                                         ["images/right-arrow-button-down.png" Texture]
                                         ["images/right-arrow-button-up.png" Texture]
                                         ["bitstream30.ttf" BitmapFont (make-font-params "fonts/Bitstream Vera Sans Mono Roman.ttf" :size 100 :color Color/BLACK)]]]
            (.load manager file type param))
          (.finishLoading manager)
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
          (.dispose manager)))))

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
