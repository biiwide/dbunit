(defproject dbunit "0.0.1"

  :description "FIXME: write description"

  :url "http://example.com/FIXME"

  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[clj-yaml "0.4.0"]
                 [org.clojure/clojure "1.8.0"]
                 [org.clojure/java.jdbc "0.6.1"]
                 [org.clojure/test.check "0.9.0"]
                 [org.clojure/tools.logging "0.3.1"]
                 [de.ubercode.clostache/clostache "1.4.0"]
                 ]

  :profiles {:dev {:dependencies [[com.h2database/h2 "1.4.192"]]
                   }
             }

  )
