(ns sigmund.sigar
  (:use [sigmund.util :only [<clj de-java]] )
  (:import java.util.Date
           [java.net InetAddress]
           [ java.lang.management ManagementFactory RuntimeMXBean]
           [org.hyperic.sigar Sigar NetFlags OperatingSystem]))

(def ^:dynamic *sigar* (Sigar.))
(def ^:dynamic *net-usage* (atom nil))

(def TCP NetFlags/CONN_TCP)
(def UDP NetFlags/CONN_UDP)

(defmacro defsig [name doc-string args func]
  `(defn ~name ~doc-string ~args
     (<clj (~func *sigar* ~@args))))

(defmacro defsigps [name doc-string args func]
  `(defn ~name ~doc-string
     (~args
      (assoc (<clj (~func *sigar* ~@args)) :pid (first ~args)))
     ([]
        (~name ~'(pid)))))

(defmacro defsiglist [name doc-string args func]
  `(defn ~name ~doc-string ~args
    (->> (~func *sigar* ~@args) (map <clj))))

;; nfs

(defn nfs-client
  "Returns information for the network file system client.
   Map keys:   [:access :class :commit :create :fsinfo :fsstat :getattr
                :link :lookup :mkdir :mknod :null :pathconf :read :readdir
                :readdirplus :readlink :remove :rename :rmdir :setattr :symlink :write]"
  []
  (or
   (try
     (<clj (.getNfsClientV3 *sigar*))
     (catch Exception e (println e)))
   (try
     (<clj (.getNfsClientV2 *sigar*))
     (catch Exception e (println e)))))

(defn nfs-server []
  "Returns information for the network file system server.
   Map keys:   [:access :class :commit :create :fsinfo :fsstat :getattr
                :link :lookup :mkdir :mknod :null :pathconf :read :readdir
                :readdirplus :readlink :remove :rename :rmdir :setattr :symlink :write]"
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
   Map keys:  [:core-cur :core-max :cpu-cur :cpu-max :data-cur :data-max :file-size-cur
               :file-size-max :memory-  cur :memory-max :open-files-cur :open-files-max
               :pipe-size-cur :pipe-size-max :processes-cur :processes-max :stack-cur
               :stack-max :virtual-memory-cur :virtual-memory-max]"
  [] .getResourceLimit)

(defsig os-uptime
  "Returns system uptime
   Map keys:  [uptime]"
  [] .getUptime)

(defsiglist os-who
  "Returns list of users."
  [] .getWhoList)

;; JVM

(defn jvm
  "Returns the jvm profile for the calling thread.
   Map keys:   [:boot-class-path :boot-class-path-supported? :class-path
                :input-arguments :library-path :management-spec-version
                :name :spec-name :spec-vendor :spec-version :start-time
                :system-properties :uptime :vm-name :vm-vendor :vm-version]"
  []
  (let [methods (.getMethods RuntimeMXBean)
        jvm-impl (ManagementFactory/getRuntimeMXBean)
        names   (map (fn [x] (de-java (.getName x))) methods)
        results (map (fn [x] (.invoke x jvm-impl nil)) methods)]
    (zipmap (map keyword names) results)))

;; CPU

(defsiglist cpu
  "Returns a list containing information about the system CPUs.
   Map keys:   [:cache-size :cores-per-socket :mhz :mhz-max :mhz-min
                :model :total-cores :total-sockets :vendor"
  [] .getCpuInfoList)

(defn cpu-usage [& flags]
  "Returns CPU usage, Additional flags are:
      :absolute, which returns the usage in absolute terms instead of percentages.
      :average, which returns the average usage time of all cpus.
   Map keys:  [:nice :soft-irq :idle :irq :user
                :sys :wait :stolen]
    flag keys:  [:combined (:absolute off) or :total (:absolute on)]"
  (let [flags  (apply hash-set flags)
        abs?   (contains? flags :absolute)
        avg?   (contains? flags :average)]
    (cond (and abs? avg?) (<clj (.getCpu *sigar*))
          avg?            (<clj (.getCpuPerc *sigar*))
          abs?            (->> (.getCpuList *sigar*) (map <clj))
          :else           (->> (.getCpuPercList *sigar*) (map <clj)))))

(defsig cpu-current-thread
  "Returns the overall cpu usage in ticks for the calling thread
   Map keys:  [:user :total :sys]"
  [] .getThreadCpu)

;; File System

(defn fs
  "Returns a list of the mounted devices
   Map keys:   [:dev-name :dir-name :flags :options
                :sys-type-name :type :type-name]"
  [] (map <clj (.values (.getFileSystemMap *sigar*))))

(defn fs-usage
  "Returns the filesystem usage for either a specified path, or for all
   mounted devices the input is empty.
   Map keys:   [:avail :class :disk-queue :disk-read-bytes :disk-reads
                :disk-service-time :disk-write-bytes :disk-writes
                :files :free :free-files :total :use-percent :used]"
  ([^String f] (<clj (.getFileSystemUsage *sigar* f)))
  ([] (map #(fs-usage (:dir-name %)) (fs-devices) )))

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
   Map keys:  [:local-address :local-port :receive-queue
               :remote-address :remote-port :send-queue
               :state :state-string :type :type-string]"
  [^Integer port] .getNetConnectionList)

(defsiglist net-routes
  "Returns the list of network routes currently used by the system.
   Map keys:  [:destination :flags :gateway :ifname :irtt
               :mask :metric :mtu :refcnt :use :window]"
  [] .getNetRouteList)

(defsig net-gateway
  "Returns the network gateway and the dns information.
   Map keys:  [:default-gateway :default-gateway-interface :domain-name
               :host-name :primary-dns :secondary-dns]"
  [] .getNetInfo)

(defsiglist net-if-names
  "Returns the names of the network interfaces on the system."
  [] .getNetInterfaceList)

(defn net-if-info
  "Returns the configuration information for the specified network interface.
   Map keys:  [:address :address6 :broadcast :class :description
               :destination :flags :hwaddr :metric :mtu :name
               :netmask :prefix6-length :scope6 :tx-queue-len :type]"
  ([^String name] (<clj (.getNetInterfaceConfig *sigar* name)))
  ([] (map net-if-info (net-if-names))))

(defn net-if-usage
  "Returns the data usage information for the network interface
   Map keys:  [:rx-bytes :rx-dropped :rx-errors :rx-frame :rx-overruns
               :rx-packets :speed :tx-bytes :tx-carrier :tx-collisions
               :tx-dropped :tx-errors :tx-overruns :tx-packets]"
  ([^String name]
     (-> (<clj (.getNetInterfaceStat *sigar* name))
         (assoc :name name)
         (dissoc :speed)))
  ([] (map net-if-usage (net-if-names))))

(defn net-usage
  "Returns the overall data usage for all network interfaces
   Map keys:  [:tx-bytes :rx-bytes :tx-packets :rx-packets]"
  [] (->> (net-if-usage)
          (map #(select-keys % [:tx-bytes :rx-bytes :tx-packets :rx-packets]))
          (apply merge-with +)))

(defn net-bandwidth
  "Returns the overall data transmission in bytes per second and packets per second
   for all network interfaces
   Map keys:  [:tx-bytes :rx-bytes :tx-packets :rx-packets]"
  [& [stime]]
  (let [usage-fn (constantly [(Date.) (net-usage)])]
    (cond (or (nil? @*net-usage*) stime)
          (do
            (swap! *net-usage* usage-fn)
            (Thread/sleep (or stime 200))
            (net-bandwidth))

          :else
          (let [t-fn       (fn [i] (.getTime (first i)))
                curr-usage (usage-fn)
                t-diff     (- (t-fn curr-usage) (t-fn @*net-usage*))
                merge-fn  #(long ( / (- %1 %2) t-diff 0.001))
                bandwidth  (merge-with merge-fn (second curr-usage) (second @*net-usage*))]
            (swap! *net-usage* (constantly curr-usage))
            (assoc bandwidth :interval t-diff)))))

(defsig net-listen-addr
  "Returns the network address that is being listened to on the port"
  [^Integer port] .getNetListenAddress)

(defsig net-tcp
  "Returns summarf information for tcp on the system
   Map keys:  [:active-opens :attempt-fails :curr-estab
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

(defn net-is-reachable?
  "Returns whether an ip is reachable by the current system"
  [ip]
  (.isReachable (InetAddress/getByName ip) 300))

(defn net-localhost []
  "Returns the localhost by the current system"
  (InetAddress/getLocalHost))

;; Processes

(defsig pid
  "Returns the process id of the current calling thread."
  [] .getPid)

(defn ps-args
  "Returns the list of arguments given to the specified process or the current process if empty."
  ([^Long pid] (->> (.getProcArgs *sigar* pid) (map <clj)))
  ([] (ps-args (pid))))

(defsigps ps-cpu
  "Returns the cpu usage by the specified process or the current process if empty.
   Map keys:  [:last-time :percent :start-time :sys :total :user]"
  [^Long pid] .getProcCpu)

(defsigps ps-cred
  "Returns the system credentials of the specified process or the current process if empty.
   Map keys:  [:egid :euid :gid :uid]"
  [^Long pid] .getProcCred)

(defsigps ps-cred-name
  "Returns the system credential names of the specified process or the current process if empty.
   Map keys:  [:group :user]"
  [^Long pid] .getProcCredName)

(defsigps ps-env
  "Returns the enviroment variables of the specified process or the current process if empty."
  [^Long pid] .getProcEnv)

(defsigps ps-exe
  "Returns the executable name and current working directory of the specified process or the current process if empty.
   Map keys:   [:cwd :name]"
  [^Long pid] .getProcExe)

(defsigps ps-memory
  "Returns the memory states of the specified process or the current process if empty.
   Map keys:   [:major-faults :minor-faults :page-faults
                :resident :rss :share :size :vsize]"
  [^Long pid] .getProcMem)

(defsigps ps-info
  "Returns running status of the specified process or the current process if empty.
   Map keys:   [:name :nice :ppid :priority :processor :state :threads :tty]"
  [^Long pid] .getProcState)

(defn ps-ancestors
  "Returns running status for all ancestors of the specified process or the current process if empty.
   Map keys:   [:name :nice :ppid :priority :processor :state :threads :tty]"
  ([^Long pid]
      (loop [acc# () id# pid]
        (cond (> 1 id#) acc#
              :else
              (if-let [state (ps-info id#)]
                (recur (cons id# acc#) (:ppid state))))))
  ([] (ps-ancestors (pid))))

(comment
  ;;(defsig native-library [] .getNativeLibrary)
  ;;(defsig proc-modules [^Long pid] .getProcModules)
  ;;(defsig proc-port [^Integer protocol ^Long port] .getProcPort)
  ;;(defsig proc-fd [^Long pid] .getProcFd)
  ;;(defsig multi-process-cpu [^String q] .getMultiProcCpu)
  ;;(defsig multi-process-memory [^String q] .getMultiProcMem)
  ;;(defsig net-service-name [protocol port] .getNetServicesName)
)
