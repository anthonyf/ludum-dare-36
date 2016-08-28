(ns ld36.turing-machine)

(def tape (atom (let [tape {:data (into {} (for [x (range 10)] [x 1] ))
                            :pos 0}]
                  (-> tape
                      (assoc-in [:data -5] 1)
                      (assoc-in [:data -15] 1)
                      (assoc-in [:data -25] 1)
                      (assoc-in [:data 25] 1)))))

(defn tape-left [tape]
  (update tape :pos dec))

(defn tape-right [tape]
  (update tape :pos inc))



(def busy-beaver {:states ['A 'B 'C]
                  :symbols [0 1]
                  :code {'A {0 {:write 1 :move :R :goto 'B}
                             1 {:write 1 :move :L :goto 'C}}
                         'B {0 {:write 1 :move :L :goto 'A}
                             1 {:write 1 :move :R :goto 'B}}
                         'C {0 {:write 1 :move :L :goto 'B}
                             1 {:write 1 :move :R :goto :halt}}}})

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

(defn step
  [{:keys [tape state program] :as machine}]
  )

(defn run
  ([program tape]
   )
  ([program]
   (run program (empty-tape))))