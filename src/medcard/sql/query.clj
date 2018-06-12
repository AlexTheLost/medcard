(ns medcard.sql.query
  (:require
    [hikari-cp.core :refer [make-datasource close-datasource]]
    [clojure.java.jdbc :as jdbc]
    [medcard.utils :refer [pstr pprn]]
    ))

;; https://unpkg.com/material-components-web@latest/dist/material-components-web.min.css
;; https://unpkg.com/material-components-web@latest/dist/material-components-web.min.js


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
                         :username               "medcard"
                         :password           "medcard"
                         :database-name      "medcards"
                         :server-name        "localhost"
                         :port-number        5432
                         :register-mbeans    false})


(def db-spec
  {:datasource (make-datasource datasource-options)})
;; (close-datasource datasource)


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
;; (pprn
;;   (update-card
;;     21
;;     {:family_name "!!!!!!!!!"
;;      :first_name "Александр"
;;      :last_name "Валентинович"
;;      :gender "мужской"
;;      :birthdate "30-07-1989"
;;      :blood_type "A" ;; A B AB O
;;      :height "184"
;;      :weight "82"}))


(defn insert-card [card]
  ;;   (pprn card)
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
;; (pprn
;;   (insert-card
;;     {:family_name "Белькевич"
;;      :first_name "Александр"
;;      :last_name "Валентинович"
;;      :gender "мужской"
;;      :birthdate "30-07-1989"
;;      :blood_type "A" ;; A B AB O
;;      :height "184"
;;      :weight "82"}))


(defn select-card-by-id [id]
  (jdbc/query db-spec
              ["SELECT * FROM card WHERE id = ?" id]
              {:result-set-fn first}))
;; (pprn (select-card-by-id 21))


(defn select-card-all []
  (jdbc/query
    db-spec
    ["SELECT * FROM card"]))
;; (pprn (select-card-all))


(def datetime-formatted-to
  (java.text.SimpleDateFormat. "yyyy-MM-dd HH:mm"))


(defn select-record-by-card-id [card-id]
  (jdbc/query
    db-spec
    ["SELECT * FROM record WHERE card_id = ?" card-id]
    {:row-fn (fn [record]
               (let [datetime (.format datetime-formatted-to (:create_time record))]
                 (assoc record :create_time datetime)))
     ;; :result-set-fn first
     }))
;; (pprn (select-record-by-card-id 12))


(defn insert-record [card-id record]
  (jdbc/insert! db-spec :record
                (assoc record :card_id card-id)))
;; (insert-record 12 {:name "s" :description "d1"})
