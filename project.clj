(defproject gantt "1.0.0-SNAPSHOT"
  :description "FIXME: write description"
  :dependencies [ [org.clojure/clojure "1.3.0"]
                  ;[org.clojure/clojure-contrib "1.2.0"]
                  [org.danlarkin/clojure-json "1.2-SNAPSHOT"]
                  [org.swinglabs/swingx "1.6"]
                ]
  :aot [swing.table]
  ;:aot [swing.table swing.gantt.renderer swing.gantt]
)
