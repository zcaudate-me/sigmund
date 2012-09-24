(ns sigmund.commands.sysinfo
  (:use [sigmund.util :only [human-readable]])
  (:require [sigmund.sigar :as sig]
            [clojure.pprint :as pp]))

(pp/pprint (sig/os-info))
(pp/pprint (sig/proc-stats))
