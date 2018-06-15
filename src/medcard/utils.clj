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


