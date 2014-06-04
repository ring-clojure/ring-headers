(defproject ring/ring-headers "0.1.0-SNAPSHOT"
  :description "Ring middleware for common response headers"
  :url "https://github.com/ring-clojure/ring-headers"
  :license {:name "The MIT License"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [ring/ring-core "1.3.0"]]
  :plugins [[codox "0.8.8"]]
  :codox {:project {:name "Ring-Headers"}}
  :profiles
  {:dev {:dependencies [[ring-mock "0.1.5"]]}
   :1.4 {:dependencies [[org.clojure/clojure "1.4.0"]]}
   :1.5 {:dependencies [[org.clojure/clojure "1.5.1"]]}
   :1.6 {:dependencies [[org.clojure/clojure "1.6.0"]]}})
