(ns hl7v2-query.core
  (:require
   [clojure.java.io :as io]
   [clojure.zip :as zip]
   [datascript.core :as d]
   [hl7v2.core :refer [parse-hl7]]
   [hl7v2.zipper :as hz :refer [struc-zip]]
   [hl7v2-query.complex :refer [clean]]))

(let [struc (read-string (slurp "test/hl7v2-query/data/ORU_R01.edn"))]
  (-> (io/file "test/hl7v2-query/data/oru-r01.hl7")
      (parse-hl7 struc)))


(d/create-conn {:hl7.HD/namespace-id {:db/cardinality :db.cardinality/one}
                :hl7.HD/universal-id {:db/cardinality :db.cardinality/one}
                :hl7.HD/universal-id-type {:db/cardinality :db.cardinality/one}
                :hl7.MSH/sending-application {:db/cardinality :db.cardinality/one
                                              :db/valueType :db.type/ref}})

