(ns sigmund.test-core
  (:require [sigmund.core :as sig])
  (:use  midje.sweet))

(sig/net-gateway )
(println (sig/ps-info))
(fact "gateway information"
  (sig/net-gateway) => anything)
