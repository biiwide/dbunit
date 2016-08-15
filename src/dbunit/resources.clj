(ns dbunit.resources
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.string :as str]
            [clojure.tools.logging :as log]
            [clostache.parser :as tmpl])
  (:import  [java.util UUID]
            ))



(def databases
  (atom (make-hierarchy)))

(defn derive-database! [child parent]
  (swap! databases
    derive child parent))

(derive-database! :mysql :ansi)
(derive-database! :postgres :ansi)


(let [MAX_LONG_BIT (.shiftLeft (BigInteger/valueOf 1) 63)]
  (defn ^BigInteger long-bits->bigint
    [n]
    (if (neg? n)
      (-> (BigInteger/valueOf n) .abs (.or MAX_LONG_BIT))
      (BigInteger/valueOf n))))

(defn left-pad
  [string c min-width]
  (let [width (count string)]
    (if (>= width min-width)
      string
      (loop [i (- min-width width)
             sb (StringBuffer. (int min-width))]
        (if-not (pos? i)
          (str (.append sb string))
          (recur (dec i) (.append sb c)))))))


(defn unique-id []
  (let [random (UUID/randomUUID)
        high-bits (long-bits->bigint (.getMostSignificantBits random))
        low-bits (long-bits->bigint (.getLeastSignificantBits random))
        big-random (.or (.shiftLeft high-bits 64) low-bits)]
    (left-pad (.toString big-random 36)
              \0 25)))


(defn load-class [classname]
  (try (Class/forName (name classname))
    (catch ClassNotFoundException e nil)))

(defmacro when-class-present [class-name & body]
  (when (class? (load-class class-name))
    `(do ~@body)))



(defprotocol DatabaseSpec
  (db-dialect [dp-spec]))



(defn render-create-table
  [db-spec table columns options]
  (jdbc/create-table-ddl db-spec table columns))


(defmulti render-create-view
  (fn [db-spec view query]
    (db-dialect db-spec))
  :default :ansi
  :hierarchy databases)

(defn render-drop-view
  [db-spec view]
  (format "DROP VIEW %s" view))


(defn render-drop-table
  [db-spec table]
  (jdbc/drop-table-ddl table))

(defn- first-key [m]
  (key (first m)))

(defn- first-value [m]
  (val (first m)))

(def resource-type
  "Given a resource definition, determine it's type."
  first-key)

(def resource-def
  "Given a resource definition, return it's body."
  first-value)


(defmulti ->resource-constructor
  "Given a resource definition, return a constructor function."
  resource-type)

(defmulti ->resource-destructor
  "Given a resource definition, return a constructor function."
  resource-type)

(defprotocol Resource
  (create [rsrc db-spec rsrc-name])
  (destroy [rsrc db-spec rsrc-name]))

(defmulti ->resource
  resource-type)

(defmacro defresource [type fields & methods]
  (let [type-name (format "%sResource"
                    (str/upper-case (name type)))]
    `(do (deftype ~type-name ~fields
           Resource ~@methods)
         (defmethod ->resource
           ~(keyword type)
           [{:keys ~fields}]
           (new ~(symbol (format "%s.%s" *ns* type-name)) ~@fields)))))

(defresource table
  [columns options]
  (create [_ db-spec table]
    (jdbc/db-do-commands db-spec
      (render-create-table db-spec
        table columns options)))
  (destroy [_ db-spec table]
    (jdbc/db-do-commands db-spec
      (render-drop-table db-spec table))))


(defresource view
  [query]
  (create [_ db-spec view]
    (jdbc/db-do-commands db-spec
      (render-create-view db-spec view query)))
  (destroy [_ db-spec view]
    (jdbc/db-do-commands db-spec
      (render-drop-view db-spec view))))


(defn with-resources
  [resources ctxt f & args]
  (let [curried-f (if (empty? args)
                    f
                    #(apply f args))]
    (if (empty? resources)
      (apply f args)
      (let [[[rsrc-name rsrc] & more-rsrcs] resources]
        (try
          (create rsrc ctxt rsrc-name)
          (with-resources more-rsrcs ctxt f)
          (finally
            (destroy rsrc ctxt rsrc-name)))))))

