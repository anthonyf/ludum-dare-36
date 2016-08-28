(ns ld36.common
  (:import [com.badlogic.gdx.graphics Texture]
           [com.badlogic.gdx.graphics.g2d GlyphLayout]
           [com.badlogic.gdx.assets AssetManager]
           [com.badlogic.gdx.assets.loaders.resolvers InternalFileHandleResolver]
           [com.badlogic.gdx.graphics.g2d.freetype
            FreeTypeFontGenerator FreeTypeFontGeneratorLoader FreetypeFontLoader
            FreetypeFontLoader$FreeTypeFontLoaderParameter]
           [com.badlogic.gdx.graphics.g2d BitmapFont TextureRegion NinePatch]
           [com.badlogic.gdx.scenes.scene2d.utils TextureRegionDrawable NinePatchDrawable]))

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
    (.get manager name Texture) l r t b)))
