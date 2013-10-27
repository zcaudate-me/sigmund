(defproject im.chit/sigmund "0.2.0"
  :description "System analytics for Clojure"
  :url "https://github.com/zcaudate/sigmund"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [sigar/sigar "1.7.0-v20131027"]
                 [sigar/sigar-native-deps "1.7.0-v20131027"]]
  :profiles {:dev {:dependencies [[midje "1.5.0"]]}})
