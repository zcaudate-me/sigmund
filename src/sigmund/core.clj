(ns sigmund.core
  (:use [sigmund.sigar :as sig]))

(defn profile []
  {:os (sig/os-info)
   :memory (sig/memory-info)})




(defn probe-process [pid]
  {:cred (sig/proc-cred pid)
   :env (sig/proc-env pid)
   :ancestors (sig/proc-ancestors pid)
   :memory (sig/proc-memory)})


(proc-ancestors 2242)
(sig/pid)
(sig/proc-env 1288)
(sig/proc-cred 1288)
(sig/proc-state 2242)
(sig/proc-state (sig/pid))
(clojure.pprint/pprint (sig/proc-memory 2198))
(sig/proc-stats)
(sig/proc-time (sig/pid))
(sig/proc-cpu-info (sig/pid))
(sig/memory-info)
(sig/fs-usage "/")
(sig/proc-modules (sig/pid))
(sig/thread-cpu-info)
(sig/proc-exe 759)
(sig/load-avg)
