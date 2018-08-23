(ns medcard.sql.query
  (:require
    [hikari-cp.core :refer [make-datasource close-datasource]]
    [clojure.java.jdbc :as jdbc]
    [medcard.utils :refer [pstr pprn]]
    ))


(def datasource-options {:auto-commit        true
                         :read-only          false
                         :connection-timeout 30000
                         :validation-timeout 5000
                         :idle-timeout       600000
                         :max-lifetime       1800000
                         :minimum-idle       10
                         :maximum-pool-size  10
                         :pool-name          "db-pool"
                         :adapter            "postgresql"
                         :username           "medcard"
                         :password           "medcard"
                         :database-name      "medcards"
                         :server-name        "localhost"
                         :port-number        5432
                         :register-mbeans    false})


(def db-spec
  {:datasource (make-datasource datasource-options)})


(def date-formatted
  (java.text.SimpleDateFormat. "yyyy-MM-dd X"))


(defn parse-date [date-str]
  (let [zone (java.time.ZoneOffset/UTC)
        date-str-with-zone (str date-str " " zone)
        date (.parse date-formatted date-str-with-zone)]
    (java.sql.Date. (.getTime date))))

(defn update-card [id card]
  (let [birthdate (:birthdate card)
        birthdate (parse-date birthdate)
        card (assoc card :birthdate birthdate)

        height (:height card)
        height (Long/parseLong height)
        card (assoc card :height height)

        weight (:weight card)
        weight (Long/parseLong weight)
        card (assoc card :weight weight)]

    (:id (first (jdbc/update! db-spec :card
                              card
                              ["id = ?" id]
                              )))))


(defn insert-card [card]
  (let [birthdate (:birthdate card)
        birthdate (parse-date birthdate)
        card (assoc card :birthdate birthdate)

        height (:height card)
        height (Long/parseLong height)
        card (assoc card :height height)

        weight (:weight card)
        weight (Long/parseLong weight)
        card (assoc card :weight weight)]

    (:id (first (jdbc/insert! db-spec :card card)))))



(defn select-card-by-family-name [family-name]
  (jdbc/query
    db-spec
    ["SELECT * from CARD WHERE LOWER(family_name) LIKE LOWER(?);" (str "%" family-name "%")])) ; '%?%'
;; (pprn (select-card-by-family-name "бе"))



(defn select-card-by-id [id]
  (jdbc/query db-spec
              ["SELECT * FROM card WHERE id = ?" id]
              {:result-set-fn first}))


(defn select-card-all []
  (jdbc/query
    db-spec
    ["SELECT * FROM card"]))


(def datetime-formatted-to
  (java.text.SimpleDateFormat. "yyyy-MM-dd HH:mm"))


(defn select-record-by-card-id [card-id]
  (jdbc/query
    db-spec
    ["SELECT * FROM record WHERE card_id = ?" card-id]
    {:row-fn (fn [record]
               (let [datetime (.format datetime-formatted-to (:create_time record))]
                 (assoc record :create_time datetime)))
     }))


(defn insert-record [card-id record]
  (jdbc/insert! db-spec :record
                (assoc record :card_id card-id)))



(defn insert-record [login password]
  (jdbc/insert!
    db-spec :users
    {:login login
     :password password}))


(defn select-user-by-login [login]
  (jdbc/query
    db-spec
    ["SELECT * FROM users WHERE login = ?" login]
    {:result-set-fn first}))

;; (insert-record "a" "b")
;; (select-user-by-login "a")
