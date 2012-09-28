(ns sigmund.sigar
  (:use [sigmund.util :only [<clj]] )
  (:import org.hyperic.sigar.Sigar
           org.hyperic.sigar.NetFlags
           org.hyperic.sigar.OperatingSystem))

(def ^:dynamic *sigar* (Sigar.))
(def TCP NetFlags/CONN_TCP)
(def UDP NetFlags/CONN_UDP)

(defmacro defsig [name doc-string args func]
  `(defn ~name ~doc-string ~args
     (<clj (~func *sigar* ~@args))))

(defmacro defsiglist [name doc-string args func]
  `(defn ~name ~doc-string ~args
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

;; OS

(defn os
  "Returns Operating System profile.
   Map keys:   [:arch :cpu-endian :data-model :description
                :machine :name :patch-level :vendor :vendor-code-name
                :vendor-name :vendor-version :version]"
  []  (<clj (OperatingSystem/getInstance)))

(defsiglist os-pids
  "Returns a list of ids of all processes running on the system"
  [] .getProcList)

(defsig os-processes
  "Returns a summary of the os process pool
   Map keys   [:idle :running :sleeping :stopped
               :threads :total :zombie]"
  [] .getProcStat)

(defsiglist os-load-avg
  "Returns one, five, and fifteen minute averages of scheduled processes
   that are queued to run."
  [] .getLoadAverage)

(defsig os-memory
  "Returns system memory information.
   Map keys:  [:actual-free :actual-used :free :free-percent
               :ram :total :used :used-percent"
  [] .getMem)

(defsig os-swap
  "Returns system swap file information.
   Map keys:   [:free :page-in :page-out :total :used]"
  [] .getSwap)

(defsig os-limits
  "Returns system resource limits.
   Map keys   [:core-cur :core-max :cpu-cur :cpu-max :data-cur :data-max :file-size-cur
               :file-size-max :memory-  cur :memory-max :open-files-cur :open-files-max
               :pipe-size-cur :pipe-size-max :processes-cur :processes-max :stack-cur
               :stack-max :virtual-memory-cur :virtual-memory-max]"
  [] .getResourceLimit)

(defsig os-uptime
  "Returns system uptime
   Map keys: [uptime]"
  [] .getUptime)

(defsiglist os-who
  "Returns list of users."
  [] .getWhoList)


;; CPU

(defsiglist cpus
  "Returns a list containing information about the system CPUs.
   Map keys:   [:cache-size :cores-per-socket :mhz :mhz-max :mhz-min
                :model :total-cores :total-sockets :vendor"
  [] .getCpuInfoList)

(defsig cpu-usage
  "Returns the averaged CPU usage time in cpu ticks.
   Map keys:   [:nice :soft-irq :idle :irq :user
                :sys :wait :stolen :total]"
  [] .getCpu)

(defsig cpu-usage-norm
  "Returns the averaged CPU usage times normalised to total usage.
   Map keys:   [:nice :soft-irq :idle :irq :user
                :sys :wait :stolen :combined]"
  [] .getCpuPerc)

(defsiglist cpu-usages
  "Returns a list of individual CPU usage times for each processor.
   Map keys:   [:nice :soft-irq :idle :irq :user
                :sys :wait :stolen :total]"
  [] .getCpuList)

(defsiglist cpu-usages-norm
  "Returns a list of normalized individual CPU usage times for each processor.
   Map keys:   [:nice :soft-irq :idle :irq :user
                :sys :wait :stolen :combined]"
  [] .getCpuPercList)

(defsig cpu-current-thread
  "Returns the overall cpu usage in ticks for the calling thread
   Map keys:  [:user :total :sys]"
  [] .getThreadCpu)


;; File System

(defn fs-devices
  "Returns a list of the mounted devices
   Map keys    [:dev-name :dir-name :flags :options
                :sys-type-name :type :type-name]"
  [] (map <clj (.values (.getFileSystemMap *sigar*))))

(defsig fs-usage
  "Returns the filesystem usage for a specified path.
   Map keys:   [:avail :class :disk-queue :disk-read-bytes :disk-reads
                :disk-service-time :disk-write-bytes :disk-writes
                :files :free :free-files :total :use-percent :used]"
  [^String f] .getFileSystemUsage)

(defsig fs-mounted-usage
  "Returns the filesystem usage for a specified path or an error if the path is not
   the mount point.
   Map keys:   [:avail :class :disk-queue :disk-read-bytes :disk-reads
                :disk-service-time :disk-write-bytes :disk-writes
                :files :free :free-files :total :use-percent :used]"
  [^String f] .getMountedFileSystemUsage)

;; Directories and Files

(defsig dir-info
  "Returns filesystem information for the specified directory.
   Map keys:   [:blkdevs :chrdevs :disk-usage :files
                :sockets :subdirs :symlinks :total]"
  [^String f] .getDirStat)

(defsig dir-tree-info
  "Returns filesystem information for the specified directory
   as well as all underlying directories.
    Map keys:  [:blkdevs :chrdevs :disk-usage :files
                :sockets :subdirs :symlinks :total]"
  [^String f] .getDirUsage)

(defsig file-info
  "Returns file information for the specified path.
   Map keys:   [:atime :ctime :mtime :device :gid :inode :mode :name
                :nlink :permissions :permissions-string :previous-info
                :size :type :type-char :type-string :uid]"
  [^String f] .getFileInfo)

(defsig link-info
  "Returns link information for the specified path.
   Map keys:   [:atime :ctime :mtime :device :gid :inode :mode :name
                :nlink :permissions :permissions-string :previous-info
                :size :type :type-char :type-string :uid]"
  [^String f] .getLinkInfo)


;; Network

(defsig net-fqdn
  "Returns the Fully Qualified Domain Name for the System"
  [] .getFQDN)


(defsiglist net-connections
  "Returns a list of network connections that have been initiated
   on a particular port. ie. 21, 22.
   Map keys   [:local-address :local-port :receive-queue
               :remote-address :remote-port :send-queue
               :state :state-string :type :type-string]"
  [^Integer port] .getNetConnectionList)


(defsiglist net-routes
  "Returns the list of network routes currently used by the system.
   Map keys   [:destination :flags :gateway :ifname :irtt
               :mask :metric :mtu :refcnt :use :window]"
  [] .getNetRouteList)

(defsig net-gateway
  "Returns the network gateway and the dns information.
   Map keys   [:default-gateway :default-gateway-interface :domain-name
               :host-name :primary-dns :secondary-dns]"
  [] .getNetInfo)

(defsiglist net-if-names
  "Returns the names of the network interfaces on the system."
  [] .getNetInterfaceList)

(defsig net-if-info
  "Returns the configuration information for the specified network interface.
   Map keys   [:address :address6 :broadcast :class :description
               :destination :flags :hwaddr :metric :mtu :name
               :netmask :prefix6-length :scope6 :tx-queue-len :type]"
  [^String name] .getNetInterfaceConfig)

(defsig net-if-usage
  "Returns the data usage information for the network interface
   Map keys   [:rx-bytes :rx-dropped :rx-errors :rx-frame :rx-overruns
               :rx-packets :speed :tx-bytes :tx-carrier :tx-collisions
               :tx-dropped :tx-errors :tx-overruns :tx-packets]"
  [^String name] .getNetInterfaceStat)

(defsig net-listen-addr
  "Returns the network address that is being listened to on the port"
  [^Integer port] .getNetListenAddress)

(defsig net-tcp
  "Returns summarf information for tcp on the system
   Map keys   [:active-opens :attempt-fails :curr-estab
               :estab-resets :in-errs :in-segs :out-rsts
               :out-segs :passive-opens :retrans-segs]"
  [] .getTcp)

(defsig net-tcp-usage
  "Returns the total usage for system tcp.
   Map keys:  [:all-inbound-total :all-outbound-total :tcp-bound
               :tcp-close :tcp-close-wait :tcp-closing :tcp-established
               :tcp-fin-wait1 :tcp-fin-wait2 :tcp-idle :tcp-inbound-total
               :tcp-last-ack :tcp-listen :tcp-outbound-total :tcp-states :
               tcp-syn-recv :tcp-syn-sent :tcp-time-wait]"
  [] .getNetStat)

;; Processes

(defsig pid
  "Returns the process id of the current calling thread."
  [] .getPid)

(defsiglist ps-args
  "Returns the list of arguments given to the process."
  [^Long pid] .getProcArgs)

(defsig ps-cpu
  "Returns the cpu usage by the specified process
   Map keys:  [:last-time :percent :start-time :sys :total :user]"
  [^Long pid] .getProcCpu)

(defsig ps-cred
  "Returns the system credentials of the specified process
   Map keys:  [:egid :euid :gid :uid]"
  [^Long pid] .getProcCred)

(defsig ps-cred-name
  "Returns the system credential names of the specified process.
   Map keys:  [:group :user]"
  [^Long pid] .getProcCredName)

(defsig ps-env
  "Returns the enviroment variables of the specified process."
  [^Long pid] .getProcEnv)

(defsig ps-exe
  "Returns the executable name and current working directory.
   Map keys:   [:cwd :name]"
  [^Long pid] .getProcExe)

(defsig ps-memory
  "Returns the memory states of the specified process.
   Map keys:   [:major-faults :minor-faults :page-faults
                :resident :rss :share :size :vsize]"
  [^Long pid] .getProcMem)

(defsig ps-info
  "Returns running status of the process
   Map keys:   [:name :nice :ppid :priority :processor :state :threads :tty]"
  [^Long pid] .getProcState)

(defn ps-ancestors [pid]
  "Returns running status for all ancestors of the process
   Map keys:   [:name :nice :ppid :priority :processor :state :threads :tty]"
  (loop [acc# () id# pid]
    (cond (> 1 id#) acc#
          :else
          (if-let [state (ps-info id#)]
            (recur (cons id# acc#) (:ppid state))))))

(comment
  ;;(defsig native-library [] .getNativeLibrary)
  ;;(defsig proc-modules [^Long pid] .getProcModules)
  ;;(defsig proc-port [^Integer protocol ^Long port] .getProcPort)
  ;;(defsig proc-fd [^Long pid] .getProcFd)
  ;;(defsig multi-process-cpu [^String q] .getMultiProcCpu)
  ;;(defsig multi-process-memory [^String q] .getMultiProcMem)
  ;;(defsig net-service-name [protocol port] .getNetServicesName)
  (defsig ps-time
    ":start-time :sys :total :user"
    [^Long pid] .getProcTime))
