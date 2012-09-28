(ns sigmund.commands.df
  (:use [sigmund.util :only [up-shift human-readable]])
  (:require [sigmund.sigar :as sig]
            [clojure.pprint :as pp]))

(def ATTRIBUTES [:fs :total :used :free :used-pct :mounted :type])

(defn df-entry [fs-e]
  (let [du (sig/fs-usage (:dir-name fs-e))
        total (up-shift (:total du))
        free  (up-shift (:free du))
        used  (- total free)]
    {:fs       (:dev-name fs-e)
     :total    (human-readable total)
     :used     (human-readable used)
     :free     (human-readable free)
     :used-pct (format "%.0f%%" (* 100. (/ used total)))
     :type     (str (:sys-type-name fs-e) "/" (:type-name fs-e))
     :mounted  (:dir-name fs-e)}))

(defn df []
  (map df-entry (sig/fs-devices)))

(defn print-df []
  (pp/print-table ATTRIBUTES (df)))
