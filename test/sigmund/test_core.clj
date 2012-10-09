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
(println (sig/th-info))

(seq (:locked-moniters (sig/th-info)))
(seq (:locked-synchronizers (sig/th-info)))
(:thread-state (sig/th-info))
(comment
  (<clj (byte-array 3))
  (-> (th-info) keys sort println)
  ;;(<clj (Thread/currentThread))
  (tid)

  (jvm-threads)

  (.isVerbose (ManagementFactory/getMemoryMXBean))

  (threads)
  (def methods (.getMethods ThreadMXBean))
  (= java.lang.Void/TYPE (.getReturnType (nth methods 12)))

  (seq (.getParameterTypes (first methods)))
  (clojure.pprint/pprint (type (.getReturnType (nth methods 12))))
  (clojure.pprint/pprint (seq (.getMethods  (first methods))))

  (comment
    ;;(defsig native-library [] .getNativeLibrary)
    ;;(defsig proc-modules [^Long pid] .getProcModules)
    ;;(defsig proc-port [^Integer protocol ^Long port] .getProcPort)
    ;;(defsig proc-fd [^Long pid] .getProcFd)
    ;;(defsig multi-process-cpu [^String q] .getMultiProcCpu)
    ;;(defsig multi-process-memory [^String q] .getMultiProcMem)
    ;;(defsig net-service-name [protocol port] .getNetServicesName)
    ))


(sig/ps-env)
(sig/jvm-runtime)
