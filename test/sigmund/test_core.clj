(ns sigmund.test-core
  (:require [sigmund.core :as sig])
  (:use  midje.sweet))

(sig/os-pids)
(sig/os-swap)
(sig/os-limits)

(sig/net-gateway)
(println (sig/ps-info))
(fact "gateway information"
  (sig/net-gateway) => anything)
