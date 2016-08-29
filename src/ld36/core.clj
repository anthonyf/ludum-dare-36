(ns ld36.core
  (:require [ld36
             [code-blocks :as cb]
             [common :as c]
             [protocols :as p]
             [tape-actor :as ta]
             [turing-machine :as tm]
             [run-controls :as rc]])
  (:import [com.badlogic.gdx ApplicationAdapter Gdx]
           [com.badlogic.gdx.backends.lwjgl LwjglApplication LwjglApplicationConfiguration]
           [com.badlogic.gdx.graphics Color GL30 Texture]
           com.badlogic.gdx.graphics.g2d.BitmapFont
           com.badlogic.gdx.scenes.scene2d.Stage
           com.badlogic.gdx.utils.viewport.FitViewport
           org.lwjgl.input.Keyboard))

(def tm (atom tm/busy-beaver))

(defn head-clicked [tape-actor code-selection code-blocks]
  (swap! tm tm/toggle-head-symbol inc)
  (p/write-symbol tape-actor (:tape @tm))
  (cb/update-code-selection-position code-selection @tm code-blocks))

(defn make-stage
  []
  (let [padding 50
        [sw sh] c/screen-size
        stage (proxy [Stage]
                  [(FitViewport. sw sh)])
        code-blocks (let [cb (cb/make-code-blocks tm (fn [state]
                                                       ;; TODO state change
                                                       ))]
                      (.setName cb "code-blocks")
                      cb)
        code-selection (cb/make-code-selection)
        tape-actor (ta/make-tape-actor (:tape @tm)
                                       (fn [tape-actor]
                                         (head-clicked tape-actor code-selection code-blocks)))
        set-code-blocks-pos (fn [code-blocks]
                              (.setPosition code-blocks
                                            padding
                                            (- sh (+ (.getHeight code-blocks)
                                                     padding
                                                     100))))
        update-code-blocks (fn []
                             (doseq [actor (.getActors stage)]
                               (when (= (.getName actor) "code-blocks")
                                 (.remove actor)))
                             (let [cb (cb/make-code-blocks tm (fn [state]
                                                                ;; TODO state change
                                                                ))]
                               (.setName cb "code-blocks")
                               (set-code-blocks-pos cb)
                               (.addActor stage cb)
                               (.toBack cb)
                               (cb/update-code-selection-position code-selection @tm code-blocks)))

        tape-buttons (ta/make-tape-buttons (fn []
                                             (swap! tm tm/tape-left)
                                             (p/move-left tape-actor (:tape @tm))
                                             (update-code-blocks))
                                           (fn []
                                             (swap! tm tm/tape-right)
                                             (p/move-right tape-actor (:tape @tm))
                                             (update-code-blocks))
                                           (fn []
                                             (swap! tm tm/tape-erase)
                                             (p/set-tape tape-actor (:tape @tm))
                                             (update-code-blocks)))
        states-and-symbols (cb/make-states-and-symbols
                            tm
                            update-code-blocks)
        run-controls (rc/make-run-controls (fn []
                                             ;; run
                                             )
                                           (fn []
                                             ;; step
                                             (swap! tm tm/step)
                                             (update-code-blocks)
                                             (p/set-tape tape-actor (:tape @tm))
                                             (cb/update-state-and-symbols states-and-symbols tm))
                                           (fn []
                                             ;;back
                                             (swap! tm tm/pop-state)
                                             (update-code-blocks)
                                             (p/set-tape tape-actor (:tape @tm))
                                             (cb/update-state-and-symbols states-and-symbols tm)))]
    (.setPosition run-controls
                  (- sw (+ (.getWidth run-controls)
                           padding))
                  (- (/ sh 2)
                     (/ (.getHeight run-controls) 2)))
    (.addActor stage run-controls)
    (.setPosition (:actor states-and-symbols) padding (- sh 140))
    (.addActor stage code-blocks)
    (.addActor stage (:actor states-and-symbols))
    (.addActor stage code-selection)
    (.addActor stage tape-actor)
    (.addActor stage tape-buttons)
    (.setPosition tape-actor 0 100)
    (set-code-blocks-pos code-blocks)
    (cb/update-code-selection-position code-selection @tm code-blocks)
    stage))

(def assets [["images/tape.png" Texture]
             ["images/head.png" Texture]
             ["images/left-arrow-button-down.png" Texture]
             ["images/left-arrow-button-up.png" Texture]
             ["images/right-arrow-button-down.png" Texture]
             ["images/right-arrow-button-up.png" Texture]
             ["images/move.png" Texture]
             ["images/read.png" Texture]
             ["images/write.png" Texture]
             ["images/goto.png" Texture]
             ["images/table-background.png" Texture]
             ["images/small-right-arrow.png" Texture]
             ["images/small-left-arrow.png" Texture]
             ["images/halt.png" Texture]
             ["images/reject.png" Texture]
             ["images/accept.png" Texture]
             ["images/plus.png" Texture]
             ["images/minus.png" Texture]
             ["images/code-selection.png" Texture]
             ["images/blank-button-up.png" Texture]
             ["images/blank-button-down.png" Texture]
             ["images/code-selection.png" Texture]
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
