(ns sigmund.commands.free
  (:use [sigmund.util :only [up-shift human-readable]])
  (:require [sigmund.core :as sig]
            [clojure.pprint :as pp]))

(def ATTRIBUTES [:name :total :used :free :used-pct])

(defn mem-stats [m-e]
  {:name "Memory"
   :total (human-readable (:total m-e) 1)
   :free  (human-readable (:free m-e))
   :used  (human-readable (- (:total m-e) (:free m-e)) 1)
   :used-pct (format "%.1f%%" (:used-percent m-e))})

(defn swap-stats [s-e]
  {:name "Swap"
   :total (human-readable (:total s-e))
   :free  (human-readable (- (:total s-e) (:used s-e)))
   :used  (human-readable (:used s-e))
   :used-pct (format "%.1f%%" (/ (:used s-e) (:total s-e) 1.))})

(defn free []
  (let [mem (sig/os-memory)
        swp (sig/os-swap)]
    (list
      (mem-stats mem)
      (swap-stats swp))))

(defn print-free []
  (pp/print-table ATTRIBUTES (free)))
