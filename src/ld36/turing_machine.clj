(ns ld36.turing-machine)

(def busy-beaver {:states ['A 'B 'C]
                  :symbols [0 1]
                  :code {'A {0 {:write 1 :move :R :goto 'B}
                             1 {:write 1 :move :L :goto 'C}}
                         'B {0 {:write 1 :move :L :goto 'A}
                             1 {:write 1 :move :R :goto 'B}}
                         'C {0 {:write 1 :move :L :goto 'B}
                             1 {:write 1 :move :R :goto :halt}}}
                  :current-state 'A
                  :tape {:data {}
                         :pos 0}})


(def test-program {:states ['A 'B 'C 'D]
                   :symbols [0 1 2]
                   :code {'A {0 {:write 1 :move :R :goto 'B}
                              1 {:write 1 :move :L :goto 'C}}
                          'B {0 {:write 1 :move :L :goto 'A}
                              1 {:write 1 :move :R :goto 'B}}
                          'C {0 {:write 1 :move :L :goto 'B}
                              1 {:write 1 :move :R :goto :halt}}}
                   :current-state 'A
                   :tape {:data {}
                          :pos 0}})


(defn make-turing-machine
  ;; this function doesnt work yet
  [states symbols accepting-states code]
  {:tape {:data {} :pos 0}
   :states states
   :symbols symbols
   :accepting-states accepting-states
   :code {}
   :current-state (first states)})

(defn empty-tape
  [tm]
  (assoc tm [:tape] {:pos 0 :data {}}))

(defn tape-read
  [tm]
  ((-> tm :tape :data) (-> tm :tape :pos)))

(defn tape-write
  [tm symbol]
  (assoc-in [:tape :data (-> tm :tape :pos)] symbol))

(defn tape-left [tm]
  (update-in tm [:tape :pos] dec))

(defn tape-right [tm]
  (update-in tm [:tape :pos] inc))

(defn toggle-head-symbol
  [tm]
  tm)

(defn step
  [tm])

(defn run
  [tm])
