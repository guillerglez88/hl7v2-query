(ns hl7v2-query.schema
  (:require
   [clojure.zip :as zip]
   [hl7v2.zipper :as hz :refer [struc-zip]]
   [hl7v2-query.complex :refer [clean]]))

(defn node-schema [node]
  (for [child (hz/struc-children node)
        :let [type (some-> child hz/struc-attrs :type)
              attr (some-> child hz/struc-tag name)]]
    [(keyword (str "hl7." type) attr)
     (clean
      {:db/cardinality (if (-> child hz/struc-attrs :repeats)
                         :db.cardinality/many
                         :db.cardinality/one)
       :db/valueType (when (seq (hz/struc-children child))
                       :db.type/ref)})]))

(defn gen-schema [struc]
  (->> (struc-zip struc)
       (iterate zip/next)
       (take-while (complement zip/end?))
       (filter (comp seq hz/struc-children zip/node))
       (drop 1)
       (mapcat (comp node-schema zip/node))
       (into {})))

(comment

  (require '[clojure.pprint :as pp])

  (spit "tmp/schema.edn"
        (with-out-str
          (-> (slurp "tmp/ORU_R01.edn")
              (read-string)
              (gen-schema)
              (pp/pprint))))

  :.)