(ns sigmund.sigar
  (:use [sigmund.util :only [<clj]] )
  (:import org.hyperic.sigar.Sigar
           org.hyperic.sigar.NetFlags))

(def ^:dynamic *sigar* (Sigar.))
(def TCP NetFlags/CONN_TCP)
(def UDP NetFlags/CONN_UDP)

(defmacro defsig [name args func]
  `(defn ~name ~args
     (<clj (~func *sigar* ~@args))))

(defmacro defsiglist [name args func]
  `(defn ~name ~args
    (->> (~func *sigar* ~@args) (map <clj))))

(defn nfs-client []
  (or
   (try
     (<clj (.getNfsClientV3 *sigar*))
     (catch Exception e (println e)))
   (try
     (<clj (.getNfsClientV2 *sigar*))
     (catch Exception e (println e)))))

(defn nfs-server []
  (or
   (try
     (<clj (.getNfsServerV3 *sigar*))
     (catch Exception e (println e)))
   (try
     (<clj (.getNfsServerV2 *sigar*))
     (catch Exception e (println e)))))

(defsig cpu-stats [] .getCpu)
(defsig cpu-percentages [] .getCpuPerc)
(defsig dir-stats [^String f] .getDirStat)
(defsig dir-usage [^String f] .getDirUsage)
(defsig file-info [^String f] .getFileInfo)
(defsig link-info [^String f] .getLinkInfo)
(defsig mounted-fs-usage [^String f] .getMountedFileSystemUsage)
(defsig fs-usage [^String f] .getFileSystemUsage)
(defsig fs-map [] .getFileSystemMap)
(defsig fqdn [] .getFQDN)
(defsig memory-info [] .getMem)
(defsig multi-process-cpu [^String q] .getMultiProcCpu)
(defsig multi-process-memory [^String q] .getMultiProcMem)
(defsig native-library [] .getNativeLibrary)
(defsig net-ifconfig [^String name] .getNetInterfaceConfig)
(defsig net-ifstat [^String name] .getNetInterfaceStat)
(defsig net-listen-addr [port] .getNetListenAddress)
(defsig net-service-name [protocol port] .getNetServicesName)
(defsig net-stat [] .getNetStat)
(defsig pid [] .getPid)
(defsig proc-args [^Long pid] .getProcArgs)
(defsig proc-cpu-info [^Long pid] .getProcCpu)
(defsig proc-cred [^Long pid] .getProcCred)
(defsig proc-cred-name [^Long pid] .getProcCredName)
(defsig proc-env [^Long pid] .getProcEnv)
(defsig proc-exe [^Long pid] .getProcExe)
(defsig proc-fd [^Long pid] .getProcFd)
(defsig proc-memory [^Long pid] .getProcMem)
(defsig proc-modules [^Long pid] .getProcModules)
(defsig proc-port [^Integer protocol ^Long port] .getProcPort)
(defsig proc-state [^Long pid] .getProcState)
(defsig proc-stats [] .getProcStat)
(defsig proc-time [^Long pid] .getProcTime)
(defsig resource-limit [] .getResourceLimit)
(defsig swap-info [] .getSwap)
(defsig tcp-info [] .getTcp)
(defsig thread-cpu-info [] .getThreadCpu)
(defsig uptime [] .getUptime)

(defsiglist who [] .getWhoList)
(defsiglist load-avg [] .getLoadAverage)
(defsiglist all-cpu-percentages [] .getCpuPercList)
(defsiglist all-cpu-stats [] .getCpuList)
(defsiglist all-cpu-info [] .getCpuInfoList)
(defsiglist all-fs [] .getFileSystemList)
(defsiglist all-net-connections [^Integer flag] .getNetConnectionList)
(defsiglist all-net-interfaces [] .getNetInterfaceList)
(defsiglist all-net-routes [] .getNetRouteList)
(defsiglist all-procs [] .getProcList)
