(defproject org.bahmni/clojure-test-datasetup "1.0"
            :description "Setup test data from JSON for Integration tests in Clojure. Can be used with all test frameworks."
            :url "https://github.com/Bhamni/clojure_test_datasetup"
            :license {:name "Affero General Public License,"
                      :url  "http://www.gnu.org/licenses/agpl-3.0.html"}
            :dependencies [[org.clojure/clojure "1.6.0"]
                           [org.clojure/data.json "0.2.5"]
                           [org.clojure/java.jdbc "0.3.6"]
                           [midje "1.6.3"]
                           [org.xerial/sqlite-jdbc "3.8.7"]]
            :resource-paths ["resource"]
            :group-id "org.ict4h"
            :artifact-id "clojure-test-datasetup"
            :packaging "jar"
            :aliases {"test" ["midje"]}
            :name "clojure-test-datasetup"
            :repositories [["snapshots" {:url       "https://clojars.org/repo/"
                                         :releases  true
                                         :snapshots true
                                         :username "mihirkh"
                                         :password :env}]]
            :scm {:name                 "git"
                  :tag                  "HEAD"
                  :url                  "https://github.com/Bhamni/clojure_test_datasetup.git"
                  :connection           "scm:git:git@github.com:Bhamni/clojure_test_datasetup.git"
                  :developer-connection "scm:git:git@github.com:Bhamni/clojure_test_datasetup.git"}
            :plugins [[lein-midje "3.1.3"]])