(ns dbunit.resources.redshift
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.string :as str]
            [clojure.tools.logging :as log]
            [clostache.parser :as tmpl]
            [dbunit.resources :as rsrc]
            ))

(rsrc/derive-database! :redshift :postgres)


(rsrc/when-class-present com.amazon.redshift.jdbc42.Driver
  (extend-type com.amazon.redshift.jdbc42.Driver
    rsrc/DatabaseSpec
    (db-dialect [db-conn] :redshift)
    ))


(rsrc/when-class-present com.amazon.redshift.jdbc41.Driver
  (extend-type com.amazon.redshift.jdbc41.Driver
    rsrc/DatabaseSpec
    (db-dialect [db-conn] :redshift)
    ))


(rsrc/when-class-present com.amazon.redshift.jdbc4.Driver
  (extend-type com.amazon.redshift.jdbc4.Driver
    rsrc/DatabaseSpec
    (db-dialect [db-conn] :redshift)
    ))


(defmethod rsrc/render-create-view
  :redshift
  [db-spec view query]
  (format "CREATE OR REPLACE VIEW `%s` AS %s"
    view query))



