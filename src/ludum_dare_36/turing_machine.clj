(ns ludum-dare-36.turing-machine)

[[[1 :R 1]
  [1 :L 2]]
 [[1 :L 0]
  [1 :R 1]]
 [[1 :L 1]
  [1 :R :halt]]]

(def busy-beaver {:A {0 {:write 1 :move :R :next :B}
                      1 {:write 1 :move :L :next :C}}
                  :B {0 {:write 1 :move :L :next :A}
                      1 {:write 1 :move :R :next :B}}
                  :C {0 {:write 1 :move :L :next :B}
                      1 {:write 1 :move :R :next :halt}}})


(defn make-turing-machine
  [states symbols transitions initial-state accepting-states]
  {;;:tape tape
   :states states
   :initial-state initial-state
   :accepting-states accepting-states
   :symbols symbols})

(defn empty-tape
  []
  {:pos 0 :contents {}})

(defn tape-read
  [{:keys [pos contents]}]
  (contents pos))

(defn tape-write
  [tape symbol])

(defn tape-left
  [tape])


(defn tape-right
  [tape])


(defn step
  [{:keys [tape state program] :as machine}]
  )

(defn run
  ([program tape]
   )
  ([program]
   (run program (empty-tape))))
