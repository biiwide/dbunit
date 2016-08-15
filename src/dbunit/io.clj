(ns dbunit.io
  (:require [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [clostache.parser :as tmpl]
            [clj-yaml.core :as yaml])
  (:import  [java.io File FileInputStream FileOutputStream]
            [java.nio.file Path Paths]))


(extend-type Path
  io/Coercions
  (io/as-file [^Path p]
    (.toFile p))
  (io/as-url  [^Path p]
    (.toURL (.toUri p)))

  io/IOFactory
  (io/make-reader [path opts]
    (io/make-reader (io/as-file path) opts))
  (io/make-writer [path opts]
    (io/make-writer (io/as-file path) opts))
  (io/make-input-stream [path opts]
    (io/make-input-stream (io/as-file path) opts))
  (io/make-output-stream [path opts]
    (io/make-output-stream (io/as-file path) opts))
  )


(defn ^Path ->path
  [path & more-paths]
  (Paths/get path (into-array String more-paths)))


(defn sibling-file
  [origin-path relative-path]
  (-> (->path origin-path)
      (.resolveSibling (->path relative-path))))


