(ns clojure-test-datasetup.core
  (:import (java.sql BatchUpdateException))
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.data.json :as json]))

(defn perform-table-operation [table-descriptor table-operation db-spec]
  (let [{table-name :table-name, [& table-structure] :table-structure} table-descriptor]
    (try (jdbc/db-do-commands db-spec
                              (apply table-operation (keyword table-name)
                                     table-structure))
         (catch BatchUpdateException e
           (prn (.getMessage e))))
    table-descriptor))

(defn insert-in-table-name-map
  [db-spec table-name data-to-insert]
  (try (jdbc/insert! db-spec (keyword table-name) data-to-insert)
       (catch Exception e
         (prn (.getMessage e))))
  )

(defn insert-data
  [db-spec table-descriptor]
  (let [{data-to-insert :data table-name :table-name} table-descriptor]
    (mapv (partial insert-in-table-name-map db-spec table-name) data-to-insert))
  table-descriptor)

(defn drop-table [db-spec table]
  (perform-table-operation (assoc table :table-structure []) jdbc/drop-table-ddl db-spec))

(defn create-table [db-spec table]
  (perform-table-operation table jdbc/create-table-ddl db-spec))

(defn read-config
  "Reads the config file from the file path given"
  [config-file]
  (json/read-str (slurp config-file) :key-fn keyword))

(defn transform-table-structure
  [table-structure]
  (let [column-name (get table-structure :column-name)
        column-datatype (get table-structure :column-datatype)]
    (vector (keyword column-name) column-datatype)))

(defn transform-table-structures
  "Transform table structures"
  [table-descriptor]
  (let [table-structures (get table-descriptor :table-structure)]
    (->> table-structures
         (mapv transform-table-structure)
         (assoc table-descriptor :table-structure))
    )
  )

(defn setup-dataset
  "Takes in the json setup description filename as a string and the database spec
  and sets up the data on the database "
  [setup-file db-spec]
  (->> (read-config setup-file)
       (mapv transform-table-structures)
       (map (partial create-table db-spec))
       (map (partial insert-data db-spec))
       )
  )

(defn tear-down-dataset
  "Takes in the json setup description filename as a string and the database spec and
  tears down the data if setup already"
  [setup-file db-spec]
  (->> (read-config setup-file)
       (mapv transform-table-structures)
       (mapv (partial drop-table db-spec))))