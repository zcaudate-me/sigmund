(ns sigmund.commands.cpu-load
  (:use [sigmund.util :only [human-readable]])
  (:require [sigmund.core :as sig]
            [clojure.pprint :as pp]))

(def ATTRIBUTES [:name :user :system :wait :nice :irq :total :idle])

(defn avg-fn [& vals]
  (/ (apply + vals) (count vals)))

(defn cpu-entry [cpu-name cpu-e]
  (let [fmt-fn #(format "%.1f%%" (* 100 (cpu-e %)))]
    {:name   cpu-name
     :user   (fmt-fn :user)
     :system (fmt-fn :sys)
     :wait   (fmt-fn :wait)
     :nice   (fmt-fn :nice)
     :irq    (fmt-fn :irq)
     :total  (fmt-fn :combined)
     :idle   (fmt-fn :idle)}))

(defn cpu-load []
  (let [cpu-list (sig/cpu-usage)
        cpu-avg  (apply merge-with avg-fn (map #(dissoc % :class) cpu-list)) ]
    (conj
     (map-indexed (fn [i cpu-e]
                    (cpu-entry (str "CPU " i) cpu-e)) cpu-list)
     (cpu-entry "AVERAGE" cpu-avg))))

(defn print-cpu-load []
  (pp/print-table ATTRIBUTES (cpu-load)))

(comment

  (print-cpu-load)

)
