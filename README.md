# sigmund

"Sometimes a sigar is just a sigar" - a misquote attributed to Mr Freud

Sigmund implements a friendly clojure wrapper around the Hyperic SIGAR API http://www.hyperic.com/products/sigar. It provides two basic functions:
probe and profile, both of which return system information in the form of 
friendly clojure-style maps.


(use '[sigmund.core :only [probe profile]])

(profile)
(probe :os :fs :cpu :memory :network)

It can provide:

 - os information
 - System memory, swap, cpu, load average, uptime, logins
 - Per-process memory, cpu, credential info, state, arguments, environment, open files
 - File system detection and metrics
 - Network interface detection, configuration information and metrics
 - Network route and connection tables


## Usage

TODO

## License

Copyright © 2012 Chris Zheng

Distributed under the Eclipse Public License, the same as Clojure.
