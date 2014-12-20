(ns clojure-test-datasetup.core
  (:import (java.sql BatchUpdateException))
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.data.json :as json]))

(defn perform-table-operation [{table-name :table-name, [& table-structure] :table-structure} table-operation db-spec]
  (try (jdbc/db-do-commands db-spec
                            (apply table-operation (keyword table-name)
                                   table-structure))
       (catch BatchUpdateException e))
  )

(defn drop-table [table db-spec]
  (perform-table-operation table jdbc/drop-table-ddl db-spec))

(defn create-table [table db-spec]
  (perform-table-operation table jdbc/create-table-ddl db-spec))

(defn read-config
  "Reads the config file from the file path given"
  [config-file]
  (json/read-str (slurp config-file) :key-fn keyword))

(defn transfrom-table-structure
  [table-structure]
  (let [column-name (get table-structure :column-name)
        column-datatype (get table-structure :column-datatype)]
    (vector (keyword column-name) column-datatype)))

(defn transform-table-structures
  "Transform table structures"
  [table-descriptor]
  (let [table-structures (get table-descriptor :table-structure)]
    (->> table-structures
         (map transfrom-table-structure)
         (vector)
         (assoc table-descriptor :table-structure))
    )
  )

(defn transform-data-element
  [data-element]
  (let [row-data (hash-map)]
  (doseq [[column-name column-value] data-element]
    (assoc row-data (keyword column-name) column-value)))
  )

(defn transform-data
  [table-descriptor]
  (let [data (get table-descriptor :data)]
    (->> data
         (map transform-data-element)
         (assoc table-descriptor :data))))

(defn setup-dataset
  "Takes in the json setup description filename as a string and the database spec
  and sets up the data on the database "
  [setup-file db-spec]
  (->> (read-config setup-file)
       (map transform-table-structures)
       (map transform-data)
       #(map create-table %1 db-spec))
  )