(ns dbunit.core
  (:require [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [clostache.parser :as tmpl]
            [clj-yaml.core :as yaml]
            [dbunit.io]
            [dbunit.resources :as rsrc])
  (:import  [java.io File FileInputStream FileOutputStream]
            [java.nio.file Path Paths]))



(defn read-yaml-file
  "Read a YAML file by name"
  [file-name]
  (yaml/parse-string
    (slurp file-name)))



(defn map-values
  "Apply a function to each value in a map"
  [f m]
  (persistent!
    (reduce-kv (fn [result k v]
                 (assoc! result k (f v)))
      (transient {}) m)))



(defn parse-test-run
  [])

(defn parse-test-with
  [resources define]
  (fn [{:keys [with-resources run assertions]}]
    (let [rsrc-aliases (zipmap (keys with-resources)
                               (repeatedly rsrc/unique-id))
          define (merge rsrc-aliases define)]
      (fn [db-spec]
        (jdbc/with-db-connection [db-conn db-spec]
          (
          
      {:with-resources with-resources
       :run run
       :assertions assertions})))



(defn parse-test-suite
  [{:keys [resources define tests]}]
  (let [resources (map-values rsrc/->resource resources)]
    {:resources resources
     :define    define
     :tests     (map (parse-test resources define) tests)}))


