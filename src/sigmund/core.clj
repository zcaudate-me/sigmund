(ns sigmund.core
  (:use sigmund.sigar :as sig))

(defn down-kb [bytes] (bit-shift-right bytes 10))
(defn up-kb [bytes] (bit-shift-left bytes 10))

(to-kb 192837390)
