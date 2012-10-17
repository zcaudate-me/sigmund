# sigmund

<pre>
"Sometimes a sigar is just a sigar" - a misquote attributed to Mr Freud
</pre>

Sigmund is friendly clojure wrapper around the Hyperic SIGAR API http://www.hyperic.com/products/sigar. It can tell you all sorts of information about your currently executing process as well as the system that you are working on. It provides quite a bit more information than JMX:

 - os:          information, processes, memory, swap, resource limits, uptime and logins.
 - cpu:         information, per cpu and average usage.
 - jvm:         runtime information for jvm.
 - filesystem:  mounted devices, disk usage, filesystem properties and usage.
 - network:     usage, bandwidth, gateways, interface, routes and connection status.
 - process:     per process information for cpu, memory, environment, credentials, arguments and other information.


### Installation:

In your project.clj, add to dependencies:

     [sigmund "0.1.1"]

Sigmund comes with included with native dependencies for MacOSX and Windows. Linux users will have to compile the native binaries and follow the intructions here: https://github.com/zcaudate/sigar-native-deps

## Usage

The methods are pretty straight forward and comes with documentated code. Sigar itself has a few incomplete functionalities depending upon the platform that it is running. However, it adds alot more functionality than what the JMX provides. Below is a pretty comphrehensive summary of methods run on my machine, it give a good indication of the usefulness of the tool for system diagnosis.

    > (require '[sigmund.core :as sig])

### Processes:

    > (sig/pid)
    ;; => 6606

    > (sig/ps-info)
    ;; => {:pid 6606, :name java, :nice 0, :ppid 6208,
           :priority 24, :processor -1, :state R, :threads 21, :tty -1}

    > (sig/os-processes) ;; Have to run as root in order to get correct thread count
    ;; => {:zombie 0, :total 121, :threads 491, :stopped 0,
           :sleeping 0, :running 121, :idle 0}

    > (sig/os-pids) ;; Listing all the pids that are running
    ;; => (6606 6604 6600 6598 6570 6520 6452......
           100 68 66 65 61 55 53 52 50 49 48 46 42 41 38
           37 36 35 32 19 18 17 16 15 14 13 12 11 10 1)

    > (sig/ps-info 49)  ;; Lets take a look at another process
    ;; => {:pid 49, :name "loginwindow", :nice 0, :ppid 1, :priority 0,
           :processor -1, :state \S, :threads 2, :tty -1}

    > (sig/ps-ancestors 6606)
    ;; => (1 759 1027 1028 1029 6598 6604 6606)

    > (sig/ps-memory)
    ;; => {:pid 6606, :major-faults -1, :minor-faults -1,
           :page-faults 93415, :resident 140861440, :rss 140861440,
           :share -1, :size 2966564864, :vsize 2966564864}

    > (sig/ps-env)
    ;; => {"LEIN_HOME" "/Users/Chris/.lein", "VISUAL" "mate -w",
           "DISPLAY" "/tmp/launch-S5i5ZA/org.x:0",
           "SSH_AUTH_SOCK" "/tmp/launch-fWZyt0/Listeners",
           "PATH" ..................}

    > (sig/ps-cpu)
    ;; => {:pid 6606, :last-time 1348898330133, :percent 0.0,
           :start-time 1348897489628, :sys 1358, :total 30400, :user 29042}

    > (sig/ps-cpu 1)
    ;; => {:pid 1, :last-time 1348898639103, :percent 0.0,
           :start-time 1348864986380, :sys 20737, :total 20915, :user 178}

### OS:

    > (sig/os)
    ;; => {:data-model "64", :machine "i386", :name "MacOSX",
           :cpu-endian "little", :patch-level "unknown", :version "10.6.8",
           :arch "i386", :vendor-version "10.6", :vendor "Apple",
           :vendor-code-name "Snow Leopard", :description "Mac OS X Snow Leopard",
           :vendor-name "Mac OS X"}

    > (sig/os-load-avg)
    ;; => (1.07958984375 1.14404296875 1.16357421875)

    > (sig/os-swap)
    ;; => {:used 0, :total 67108864, :page-out 0, :page-in 210204, :free 67108864}

    > (sig/os-memory)
    ;; => {:used-percent 53.873634338378906, :used 2827116544, :total 4294967296,
           :ram 4096, :free-percent 46.126365661621094, :free 1467850752,
           :actual-used 2313854976, :actual-free 1981112320}

### CPU:

    > (sig/cpu)
    ({:model "MacBook5,1", :cores-per-socket 2, :mhz-min 2000, :mhz 2000,
      :cache-size 3072, :total-cores 2, :mhz-max 2000, :total-sockets 1,
      :vendor "Intel"} ...)

    > (sig/cpu-usage)
    ;; => ({:nice 0.0, :soft-irq 0.0, :idle 0.8666077738515902, :irq 0.0,
            :combined 0.1333922261484099, :user 0.06581272084805653,
            :sys 0.06757950530035335, :wait 0.0, :stolen 0.0}
           {:nice 0.0, :soft-irq 0.0, :idle 0.8573216520650814, :irq 0.0,
            :combined 0.14267834793491865, :user 0.07310608849296915,
            :sys 0.0695722594419495, :wait 0.0, :stolen 0.0})

    > (sig/cpu-usage :average)
    ;; => {:nice 0.0, :soft-irq 0.0, :idle 0.29, :irq 0.0, :combined 0.71,
           :user 0.55, :sys 0.16, :wait 0.0, :stolen 0.0}

    > (sig/cpu-usage :average :absolute)
    ;; => {:nice 0, :total 68122500, :soft-irq 0, :idle 59986850, :irq 0,
           :user 4057610, :sys 4078040, :wait 0, :stolen 0}

    > (sig/cpu-current-thread)
    ;; => {:user 7377000, :total 7924000, :sys 547000}

### File System:

    > (sig/fs-devices)
    ;; => ({:type-name "local", :type 2, :sys-type-name "msdos",
            :options "rw,async,local", :flags 0, :dir-name "/Volumes/EFI",
            :dev-name "/dev/disk0s1"}
           {:type-name "local", :type 2, :sys-type-name "hfs",
            :options "rw,local,rootfs", :flags 0, :dir-name "/",
            :dev-name "/dev/disk0s2"})

    > (sig/fs-usage "/")
    ;; => {:use-percent 0.92, :disk-service-time -1.0, :total 234095152,
           :disk-queue -1.0, :used 214006288, :free-files 4958216,
           :free 20088864, :disk-reads 37598, :avail 19832864,
           :disk-write-bytes 1749602816, :files 58523786, :disk-writes 64763,
           :disk-read-bytes 1245852672}

    > (sig/dir-info "/app")
    ;; => {:total 3, :symlinks 0, :subdirs 2, :sockets 0, :files 1,
           :disk-usage 12632, :chrdevs 0, :blkdevs 0}

    > (sig/dir-tree-info "/app")
    ;; => {:total 16129, :symlinks 2, :subdirs 148, :sockets 0,
           :files 15979, :disk-usage 1038007915, :chrdevs 0, :blkdevs 0}

    > (sig/file-info "/app")
    ;; => {:atime 1348899585000, :gid 20, :permissions-string "rwxr-xr-x",
           :previous-info nil, :name "/app", :type-char \d, :ctime 1348043675000,
           :device 234881026, :inode 41052666, :size 170, :nlink 5, :uid 501,
           :type 2, :mtime 1348043675000, :permissions 1877,
           :type-string "directory", :mode 755}

### Network:

    > sigmund.test-core=> (sig/net-usage)
    ;; => {:rx-packets 92554, :tx-packets 93789, :rx-bytes 26899792, :tx-bytes 18938536}

    > (sig/net-bandwidth)
    ;; => {:interval 218, :rx-packets 0, :tx-packets 0, :rx-bytes 0, :tx-bytes 0}

    > (sig/net-bandwidth)
    ;; => {:interval 4212, :rx-packets 1, :tx-packets 1, :rx-bytes 183, :tx-bytes 183}

    > (sig/net-bandwidth)
    ;; => {:interval 3285, :rx-packets 3, :tx-packets 3, :rx-bytes 841, :tx-bytes 841}

    > (sig/net-if-info "en7")
    ;; => {:broadcast "172.20.10.15", :mtu 1500, :address6 "fe80::c74:c2ff:fedd:18f",
           :name "en7", :netmask "255.255.255.240", :destination "0.0.0.0",
           :scope6 32, :metric 0, :hwaddr "0E:74:C2:DD:01:8F", :prefix6-length 64,
           :type "Ethernet", :tx-queue-len 0, :flags 34915, :address "172.20.10.2",
           :description "en7"}

    > (sig/net-if-names)
    ;; => ("lo0" "en0" "en1" "vmnet1" "vmnet8" "en7")

    > (sig/net-gateway)
    ;; => {:secondary-dns "61.88.88.88", :primary-dns "198.142.0.51",
           :host-name "macbook.local", :domain-name "",
           :default-gateway-interface "en7", :default-gateway "172.20.10.1"}

    > (sig/net-is-reachable? "www.google.com")
    ;; => true


### JVM
It has wrappers for all the JMX Beans:

    > (sig/jvm-runtime)
    ;; => ..... All the stuff provided by the java.lang.management.RuntimeMXBean....

    > (sig/jvm-memory)
    ;; => {:non-heap-memory-usage {:used 40746560, :max 186646528, :init 24317952, :committed 60465152},
           :heap-memory-usage {:used 39103256, :max 129957888, :init 0, :committed 85000192},
           :object-pending-finalization-count 0, :verbose? false}

    > (sig/jvm-threads)
    ;; => {:total-started-thread-count 147, :thread-contention-monitoring-supported? true, :find-deadlocked-threads nil,
           :object-monitor-usage-supported? true, :current-thread-cpu-time 286337000, :thread-cpu-time-enabled? true,
           :thread-count 11, :current-thread-user-time 282691000, :daemon-thread-count 3, :find-monitor-deadlocked-threads nil,
           :current-thread-cpu-time-supported? true, :thread-cpu-time-supported? true, :peak-thread-count 13,
           :all-thread-ids (95 20 19 18 17 16 15 6 3 2 1), :thread-contention-monitoring-enabled? false, :synchronizer-usage-supported? true}

    > (sig/jvm-compilation)
    ;; => {:total-compilation-time 23854, :compilation-time-monitoring-supported? true, :name "HotSpot 64-Bit Tiered Compilers"}

    > (sig/thid)
    ;; => 101

    > (sig/th-info)
    ;; => {:stack-trace [], :lock-info nil, :lock-owner-name nil, :blocked-time -1, :locked-synchronizers [],
           :thread-state #<State RUNNABLE>, :locked-monitors [], :suspended false, :thread-id 130, :in-native false,
           :waited-count 0, :lock-name nil, :lock-owner-id -1, :blocked-count 0, :thread-name "Swank Worker Thread", :waited-time -1}


## More Examples

Examples of how easy it is to build system diagnostic tools can be seen in the sigmund.commands directory. These commands are ported over from the `org.hyperic.sigar.cmd` package. See http://www.hyperic.com/support/docs/sigar/. When the code needed to generate these commands are compared, sigmund wins hands down.

    (use 'sigmund.commands.df)
    (print-df)
    ;; =>
    ; ==============================================================================
    ; :fs          | :total | :used | :free | :used-pct | :mounted     | :type      
    ; ==============================================================================
    ; /dev/disk0s1 | 196M   | 15M   | 181M  | 8%        | /Volumes/EFI | msdos/local
    ; devfs        | 110K   | 110K  | 0     | 100%      | /dev         | devfs/none 
    ; /dev/disk0s2 | 223G   | 198G  | 25G   | 89%       | /            | hfs/local  
    ; ==============================================================================


    (require '[sigmund.commands.free :as f])
    (f/print-free)
    ;; =>
    ; ===========================================
    ; :name  | :total | :used | :free | :used-pct
    ; ===========================================
    ; Memory | 4096M  | 4053M | 42M   | 72.4%    
    ; Swap   | 2G     | 1G    | 967M  | 0.5%     
    ; ===========================================


    (require '[sigmund.commands.cpu-load :as cl])
    (cl/print-cpu-load)
    ;; =>
    ; =================================================================
    ; :name   | :user | :system | :wait | :nice | :irq | :total | :idle
    ; =================================================================
    ; AVERAGE | 59.7% | 9.1%    | 0.0%  | 0.0%  | 0.0% | 68.8%  | 31.2%
    ; CPU 0   | 69.4% | 6.1%    | 0.0%  | 0.0%  | 0.0% | 75.5%  | 24.5%
    ; CPU 1   | 50.0% | 12.0%   | 0.0%  | 0.0%  | 0.0% | 62.0%  | 38.0%
    ; =================================================================

## License

Copyright © 2012 Chris Zheng

Distributed under the Eclipse Public License, the same as Clojure.
