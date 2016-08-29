(ns ld36.common
  (:import [com.badlogic.gdx.graphics Texture]
           [com.badlogic.gdx.graphics.g2d GlyphLayout]
           [com.badlogic.gdx.assets AssetManager]
           [com.badlogic.gdx.assets.loaders.resolvers InternalFileHandleResolver]
           [com.badlogic.gdx.graphics.g2d.freetype
            FreeTypeFontGenerator FreeTypeFontGeneratorLoader FreetypeFontLoader
            FreetypeFontLoader$FreeTypeFontLoaderParameter]
           (com.badlogic.gdx.graphics.g2d BitmapFont TextureRegion NinePatch)
           (com.badlogic.gdx.scenes.scene2d.ui Image ImageTextButton
                                               ImageTextButton$ImageTextButtonStyle)
           (com.badlogic.gdx.utils Scaling Align)
           (com.badlogic.gdx.scenes.scene2d.utils TextureRegionDrawable NinePatchDrawable)))

(def title "Ludum Dare 36")
(def screen-size [1920 1080])

(defn make-font-params
  [name & {:keys [size color]}]
  (let [params (FreetypeFontLoader$FreeTypeFontLoaderParameter.)]
    (set! (.fontFileName params) name)
    (and size (set! (.size (.fontParameters params)) size))
    (and color (set! (.color (.fontParameters params)) color))
    params))

(defn setup-asset-manager []
  (let [manager (AssetManager.)
        resolver (InternalFileHandleResolver.)]
    (.setLoader manager FreeTypeFontGenerator (FreeTypeFontGeneratorLoader. resolver))
    (.setLoader manager BitmapFont ".ttf" (FreetypeFontLoader. resolver))
    manager))

(def manager (setup-asset-manager))

(def glyph-layout (GlyphLayout.))

(defn char-width [font char]
   (doto glyph-layout
     (.setText font char)))

(defn make-texture-drawable
  [name]
  (TextureRegionDrawable. (TextureRegion. (.get manager name Texture))))

(defn make-nine-patch-drawable
  [name l r t b]
  (NinePatchDrawable.
   (NinePatch.
    (TextureRegion. (.get manager name Texture))
    l r t b)))

(defn make-non-scaling-image
  [name]
  (let [image (Image. (make-texture-drawable name))]
    (.setScaling image Scaling/none)
    image))

(defn make-button [text]
  (let [font (.get manager "bitstream50.ttf" BitmapFont)
        up-image (make-texture-drawable "images/blank-button-up.png")
        down-image (make-texture-drawable "images/blank-button-down.png")]
    (ImageTextButton. text (ImageTextButton$ImageTextButtonStyle.
                            up-image
                            down-image
                            nil
                            font))))
