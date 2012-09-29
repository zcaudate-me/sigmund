# sigmund

"Sometimes a sigar is just a sigar" - a misquote attributed to Mr Freud

Sigmund implements a friendly clojure wrapper around the Hyperic SIGAR API http://www.hyperic.com/products/sigar. It provides:

 - os:          information, processes, memory, swap, resource limits, uptime and logins.
 - cpu:         information, per cpu and average usage.
 - jvm:         runtime information for jvm.
 - filesystem:  mounted devices, disk usage, filesystem properties and usage.
 - network:     usage, bandwidth, gateways, interface, routes and connection status.
 - process:     per process information for cpu, memory, environment, credentials, arguments and other information.


### Installation:

In your project.clj, add to dependencies:

     [sigmund "0.1.0"]

## Usage

The methods are pretty straight forward and comes with documentation

    > (require '[sigmund.core :as sig])

    > (sig/pid)
    ;; => 6210

    > (sig/ps-info)
    ;; => {:pid 6210, :name java, :nice 0, :ppid 6208,
           :priority 24, :processor -1, :state R, :threads 21, :tty -1}

    > (sig/os)
    ;; => {:data-model "64", :machine "i386", :name "MacOSX",
           :cpu-endian "little", :patch-level "unknown", :version "10.6.8",
           :arch "i386", :vendor-version "10.6", :vendor "Apple",
           :vendor-code-name "Snow Leopard", :description "Mac OS X Snow Leopard",
           :vendor-name "Mac OS X"}

    > (sig/os-processes) ;; Have to run as root in order to get correct thread count
    ;; => {:zombie 0, :total 121, :threads 491, :stopped 0,
           :sleeping 0, :running 121, :idle 0}



## License

Copyright © 2012 Chris Zheng

Distributed under the Eclipse Public License, the same as Clojure.
