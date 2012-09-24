(ns sigmund.commands.netusage
  (:import java.util.Date)
  (:use [sigmund.util :only [human-readable]])
  (:require [sigmund.sigar :as sig]
            [clojure.pprint :as pp]))

(def *network-status* (atom nil))

(defn current-network-status []
  (let [status     (->> (sig/all-net-interfaces)
                        (map (fn [x] (assoc (sig/net-ifstat x) :name x)))
                        (map #(select-keys % [:name :tx-bytes :rx-bytes])))
        all-status (apply merge-with +
                          (map #(select-keys % [:tx-bytes :rx-bytes]) status))]
    [(Date.) all-status]))

(defn bandwidth-fn [inst0 inst1]
  (let [t-fn (fn [i] (.getTime (first i)))
        tx-fn #(:tx-bytes (second %))
        rx-fn #(:rx-bytes (second %))
        t-diff (/ (- (t-fn inst1) (t-fn inst0)) 1000.)]
    {:tx-rate (str (human-readable (long (/ (- (tx-fn inst1) (tx-fn inst0)) t-diff)) 1) "bps")
     :rx-rate (str (human-readable (long (/ (- (rx-fn inst1) (rx-fn inst0)) t-diff)) 1) "bps")}))

(defn rough-bandwidth []
  (cond (nil? @*network-status*)
        (do
          (swap! *network-status* (fn [_] (current-network-status)))
          (Thread/sleep 300)
          (rough-bandwidth))

        :else
        (let [nnetwork-status (current-network-status)
              bandwidth
              (bandwidth-fn @*network-status* nnetwork-status)]
          (swap! *network-status* (fn [_] nnetwork-status))
          bandwidth)))

(rough-bandwidth)
