(ns ld36.turing-machine
  (:require [ld36
             [protocols :as p]])
  (:import (ld36.protocols TuringMachineException)))

(def busy-beaver {:states ['A 'B 'C]
                  :end-states [:halt :accept :reject]
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

;; (defn- make-turing-machine
;;   ;; this function doesnt work yet
;;   [states symbols accepting-states code]
;;   {:tape {:data {} :pos 0}
;;    :states states
;;    :symbols symbols
;;    :accepting-states accepting-states
;;    :code {}
;;    :current-state (first states)})

(defn- cycle-item
  [col current inc-or-dec]
  (nth col (mod (inc-or-dec (.indexOf col current))
                (count col))))

(defn- gen-states
  [n]
  (map #(symbol (str (char (+ % (int \A)))))
       (range n)))

(defn- gen-symbols
  [n]
  (range n))

;; turing commands

(defn push-state
  [tm]
  (assoc tm :previous-state tm))

(defn pop-state
  [tm]
  (or (:previous-state tm)
      tm))

(defn tape-erase
  [tm]
  (-> tm
      push-state
      (assoc :tape {:pos 0 :data {}})))

(defn tape-read
  [tm]
  (or ((-> tm :tape :data) (-> tm :tape :pos))
      0))

(defn tape-write
  [tm symbol]
  (-> tm
      push-state
      (assoc-in [:tape :data (-> tm :tape :pos)] symbol)))

(defn tape-left [tm]
  (-> tm
      push-state
      (update-in [:tape :pos] dec)))

(defn tape-right [tm]
  (-> tm
      push-state
      (update-in [:tape :pos] inc)))

(defn toggle-head-symbol
  [tm inc-or-dec]
  (let [{:keys [symbols]} tm
        current (tape-read tm)
        next-symbol (cycle-item symbols current inc-or-dec)]
    (tape-write tm next-symbol)))

(defn toggle-code-cell
  [tm state symbol column inc-or-dec]
  (-> tm
      push-state
      (update-in [:code state symbol column]
                 (fn [current-value]
                   (case column
                     :write (cycle-item (:symbols tm) current-value inc-or-dec)
                     :move (cycle-item [:L :R] current-value inc-or-dec)
                     :goto (cycle-item (concat (:states tm)
                                               (get tm :end-states []))
                                       current-value inc-or-dec))))))

(defn add-state
  [tm]
  (-> tm
      push-state
      (update :states #(gen-states (inc (count %))))))

(defn remove-state
  [tm]
  (-> tm
      push-state
      (update :states #(gen-states (dec (count %))))))

(defn add-symbol
  [tm]
  (-> tm
      push-state
      (update :symbols #(gen-symbols (inc (count %))))))

(defn remove-symbol
  [tm]
  (-> tm
      push-state
      (update :symbols #(gen-symbols (dec (count %))))))

(defn current-code-line
  [tm]
  (let [symbol (tape-read tm)
        {:keys [current-state code]} tm
        state-code (code current-state)
        {:keys [write move goto]} ((or state-code {}) symbol)]
    {:state current-state
     :symbol symbol
     :write write
     :move move
     :goto goto}))

(defn terminated?
  [tm]
  (let [{:keys [state symbol write move goto]} (current-code-line tm)]
    (contains? (set (:end-states tm)) goto)))

(defn step
  [tm]
  (let [{:keys [state symbol write move goto]} (current-code-line tm)]
    (when (nil? write)
      (throw (TuringMachineException. "Missing write instruction.")))
    (when (nil? move)
      (throw (TuringMachineException. "Missing move instruction.")))
    (when (nil? goto)
      (throw (TuringMachineException. "Missing goto state instruction.")))

    (if (terminated? tm)
      tm
      (as-> tm tm
        (push-state tm)
        ;; write to tape
        (let []
          (assoc-in tm [:tape :data (-> tm :tape :pos)]
                    write))
        ;; move tape
        (case move
          :L (tape-left tm)
          :R (tape-right tm))
        ;; goto next state
        (assoc tm :current-state goto)))))
