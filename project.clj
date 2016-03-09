(defproject ring/ring-headers "0.2.0"
  :description "Ring middleware for common response headers"
  :url "https://github.com/ring-clojure/ring-headers"
  :license {:name "The MIT License"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [ring/ring-core "1.4.0"]]
  :plugins [[lein-codox "0.9.4"]]
  :codox {:project {:name "Ring-Headers"}
          :output-path "codox"}
  :aliases {"test-all" ["with-profile" "default:+1.6:+1.7:+1.8" "test"]}
  :profiles
  {:dev {:dependencies [[ring/ring-mock "0.3.0"]]}
   :1.6 {:dependencies [[org.clojure/clojure "1.6.0"]]}
   :1.7 {:dependencies [[org.clojure/clojure "1.7.0"]]}
   :1.8 {:dependencies [[org.clojure/clojure "1.8.0"]]}})
