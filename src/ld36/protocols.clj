(ns ld36.protocols)

(gen-class :name ld36.protocols.LambdaAction
           :extends com.badlogic.gdx.scenes.scene2d.actions.TemporalAction
           :state state
           :init init
           :prefix lambda-action-
           :methods [[setLambda [Object] void]])

(defn lambda-action-init
  []
  [[] (atom nil)])

(defn lambda-action-setLambda [this fn]
  (reset! (.state this)
          fn))

(defn lambda-action-update
  [this percent])

(defn lambda-action-end
  [this]
  (@(.state this)))

(defprotocol Updatable
  (update-me [this]))

(defprotocol Tape
  (set-tape [this tape])
  (move-right [this tape])
  (move-left [this tape])
  (write-symbol [this tape]))

(gen-class :name ld36.protocols.TuringMachineException
           :extends java.lang.Exception)
