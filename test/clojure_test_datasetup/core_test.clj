(ns clojure-test-datasetup.core-test
  (:import (java.io FileNotFoundException)
           (org.sqlite.javax SQLiteConnectionPoolDataSource)
           (java.sql SQLException))
  (:use midje.sweet)
  (:require [clojure.test :refer :all]
            [clojure.java.jdbc :as jdbc]
            [clojure-test-datasetup.core :refer :all]
            [clojure-test-datasetup.core :as ds]))

(def db-spec {:datasource (doto (new SQLiteConnectionPoolDataSource)
                            (.setUrl "jdbc:sqlite:db/test.db"))})

(defn test-config-mapping
  ([]
    (ds/read-config "resources/sample-setup.json"))
  ([config-file]
    (ds/read-config config-file)))

(facts "Reading Config File"
       (fact "Throws exception if file not found"
             (test-config-mapping "doesn'texist") => (throws FileNotFoundException))

       (fact "If file valid, read and convert to hash-map"
             (test-config-mapping) => [{:data            [{:column_1 2, :column_2 "Something here", :column_3 "Hello"}
                                                          {:column_1 3, :column_2 "Another Thing here", :column_3 "World"}],
                                        :table-name      "table_name",
                                        :table-structure [{:column-datatype "int", :column-name "column_1"}
                                                          {:column-datatype "text", :column-name "column_2"}
                                                          {:column-datatype "varchar(255)", :column-name "column_3"}]}]))

(facts "Transform table structures"
       (fact "Successfull Transformation"
             (ds/transform-table-structures (first (test-config-mapping)))
             =>
             {:data       [{:column_1 2, :column_2 "Something here", :column_3 "Hello"}
                           {:column_1 3, :column_2 "Another Thing here", :column_3 "World"}],
              :table-name "table_name",
              :table-structure
                          [[:column_1 "int"] [:column_2 "text"]
                           [:column_3 "varchar(255)"]]}))

(facts "Transform table structure"
       (fact "Successfull tranformation of int"
             (ds/transform-table-structure {:column-name "column_1", :column-datatype "int"})
             =>
             [:column_1 "int"])
       (fact "Successfull tranformation of text"
             (ds/transform-table-structure {:column-name "column_2", :column-datatype "text"})
             =>
             [:column_2 "text"])
       (fact "Successfull tranformation of varchar"
             (ds/transform-table-structure {:column-name "column_3", :column-datatype "varchar(255)"})
             =>
             [:column_3 "varchar(255)"]))

(facts "End to end integration test for setup"
       (dorun (ds/setup-dataset "resources/sample-setup.json" db-spec))
       (fact "Creation of table"
             (jdbc/query db-spec ["select * from table_name;"])
             =>
             (complement (throws SQLException)))
       (fact "Insertion of rows"
             (< 0 (count (jdbc/query db-spec ["select * from table_name;"])))
             =>
             true)
       (fact "Insertion of data"
             (< 0 (count (jdbc/query db-spec ["select * from table_name where column_1=2;"])))
             =>
             true)
       (fact "Insertion of data"
             (< 0 (count (jdbc/query db-spec ["select * from table_name where column_2=\"Something here\";"])))
             =>
             true)
       (fact "Insertion of data"
             (< 0 (count (jdbc/query db-spec ["select * from table_name where column_2=\"Something not here\";"])))
             =>
             false))

(facts "End to end integration test for tear down"
       (dorun (ds/tear-down-dataset "resources/sample-setup.json" db-spec))
       (fact "Dropping of table"
             (jdbc/query db-spec ["select * from table_name;"])
             =>
             (throws SQLException))
       (fact "Deletion of rows"
             (jdbc/query db-spec ["select * from table_name where column_1=2;"]))
       =>
       (throws SQLException))