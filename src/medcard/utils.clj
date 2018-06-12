(ns medcard.utils
  (:require
    [clojure.pprint :refer [pprint]]))


(defn pprn [coll & [name]]
  (locking *out*
    (prn)
    (when name
      (prn name))
    (pprint coll)
    (prn))
  coll)


(defn pstr [coll]
  (with-out-str
    (pprint coll)))


(defn pstrn [coll]
  (let [coll-str (with-out-str (pprint coll))]
    (str "\n" coll-str "\n")))


(defn filter-map [pred m]
  (let [transient-map (transient {})]
    (persistent!
      (reduce-kv
        (fn [filtered-map k v]
          (if (pred k v)
            (assoc! filtered-map k v)
            filtered-map))
        transient-map
        m))))

