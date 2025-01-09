(ns hl7v2-query.schema
  (:require
   [clojure.string :as str]
   [clojure.zip :as zip]
   [hl7v2.zipper :as hz :refer [struc-zip struc-tag struc-children]]
   [hl7v2-query.complex :refer [clean]]))

(defn struc-seg-id [node]
  (let [id (name (struc-tag node))]
    (when (and (= 3 (count id))
               (= id (str/upper-case id))
               (seq (struc-children node)))
      id)))

(defn node-schema [loc]
  (let [node (zip/node loc)
        node-path (->> (zip/path loc)
                       (map (comp name hz/struc-tag))
                       (remove nil?))]
    (for [child (hz/struc-children node)]
      [(keyword (str "hl7."
                     (or (struc-seg-id node)
                         (some-> child hz/struc-attrs :type)
                         (when (seq node-path) (str/join "." node-path))
                         (name (struc-tag node))))
                (some-> child hz/struc-tag name))
       (clean
        {:db/cardinality (if (-> child hz/struc-attrs :repeats)
                           :db.cardinality/many
                           :db.cardinality/one)
         :db/valueType (when (seq (hz/struc-children child))
                         :db.type/ref)})])))

(defn gen-schema [struc]
  (->> (struc-zip struc)
       (iterate zip/next)
       (take-while (complement zip/end?))
       (filter (comp seq hz/struc-children zip/node))
       (mapcat node-schema)
       (into (sorted-map))))

(comment

  (require '[clojure.pprint :as pp])

  (spit "tmp/schema.edn"
        (with-out-str
          (-> (slurp "tmp/ORU_R01.edn")
              (read-string)
              (gen-schema)
              (pp/pprint))))

  :.)