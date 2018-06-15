(ns medcard.core
  (:require
    ;; Project:
    [medcard.sql.query :as sql-query]
    ;; Other
    [org.httpkit.server :as server]
    [ring.middleware.resource :refer [wrap-resource]]
    [compojure.core :refer [defroutes GET POST]]
    [compojure.route :as route]
    [compojure.handler :refer [site]]
    [hiccup.core :refer [html]]
    [hiccup.page :refer [html5 include-css include-js]]
    [ring.util.response :refer [redirect]]
    [cheshire.core :refer [generate-string parse-string]]
    [medcard.utils :refer [pstr pprn]]
    )
  (:import
    (org.jsoup Jsoup)
    ))


(def mdc
  (list
    (include-css "https://unpkg.com/material-components-web@latest/dist/material-components-web.min.css")
    (include-css "https://fonts.googleapis.com/css?family=Roboto:300,400,500")
    (include-js "https://unpkg.com/material-components-web@latest/dist/material-components-web.min.js")
    [:style
     ".table-el {
     margin: 15px 0px;
     padding: 10px 10px;
     }
     .menu-el {
     padding: 30px 30px;
     }
     .bbbb {
     margin: 5px 0px;
     border: 1px solid black;
     }
     "]
    ))

(defn html-formating [html-str]
  (.html (.body (Jsoup/parseBodyFragment html-str))))


(defn view-med-cards [med-cards & [family-name]]
  [:html

   [:head
    [:meta {:charset "utf-8"}]
    [:meta {:name "viewport" :content "width=device-width,initial-scale=1"}]
    mdc]

   [:body {:class "mdc-typography"}


    [:div {:class "mdc-layout-grid"}
     [:div {:class "mdc-layout-grid__inner bbbb"}

      [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-8 mdc-typography--headline6"}
       [:a {:href "/card/add" :class "mdc-button menu-el"} "Добавить новую карту"]]

      [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-2 mdc-typography--headline6"}
       [:a {:href "/card/add" :class "mdc-button menu-el"} "О программе"]]

      [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-2 mdc-typography--headline6"}
       [:a {:href "/card/add" :class "mdc-button menu-el"} "О разработчике"]]

      ]]


    [:div {:class "mdc-layout-grid"}




     [:form {:action "/card/search/family-name" :method "GET"}


      [:lable "Найти по фамилии:"]

      [:div {:class "mdc-layout-grid__inner"}

       [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-2 mdc-typography--headline6"}
        [:input {:id "my-text-field" :value family-name :name "family-name" :type "text" :class "mdc-text-field__input"}]]

       [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-2 mdc-typography--headline6"}
        [:input {:value "Найти" :type "submit" :class "mdc-button menu-el"}]]

       ]]

     [:br]
     [:br]

     [:div {:class "mdc-layout-grid__inner"}

      [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-2 mdc-typography--headline6"}
       [:span "Фамилия"]]

      [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-2 mdc-typography--headline6"}
       [:span "Имя"]]

      [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-2 mdc-typography--headline6"}
       [:span "Отчество"]]

      [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-2 mdc-typography--headline6"}
       [:span "Пол"]]

      [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-2 mdc-typography--headline6"}
       [:span "День рождения"]]

      [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-2 mdc-typography--headline6"}
       [:span "Посмотреть записи"]]

      ]

     [:br]

     (reduce
       (fn [view {id :id family_name :family_name first_name :first_name last_name :last_name gender :gender birthdate :birthdate blood_type :blood_type height :height weight :weight}]
         (conj view
               [:div {:class "mdc-layout-grid__inner bbbb"}

                [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-2 table-el"}
                 [:span family_name]]

                [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-2 table-el"}
                 [:span first_name]]

                [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-2 table-el"}
                 [:span last_name]]

                [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-2 table-el"}
                 [:span gender]
                 ]
                [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-2 table-el"}
                 [:span birthdate]]
                [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-2 table-el"}
                 [:span
                  [:a {:href (str "/card/view/" id) :class "mdc-button"} "Посмотреть"]]]

                ]))
       (list)
       med-cards)

     ]]])


(defn view-add-card [action & [card]]

  [:html

   [:head
    [:meta {:charset "utf-8"}]
    [:meta {:name "viewport" :content "width=device-width,initial-scale=1"}]

    mdc

    [:style
     ".add_view {
     margin-left: 35%;
     margin-right: 35%;
     width: 30%;
     }
     .inputs {
     width: 100%;
     }
     "]]

   [:body {:class "mdc-typography"}

    [:form {:action action :method "POST" :class "add_view"}

     [:div
      [:h3 "Создание карты нового пациента:"]
      [:br]]

     [:label "Фамилия:"
      [:br]
      [:input {:type "text" :minlength 1 :name "family_name" :value (:family_name card) :class "inputs" :required true}]]

     [:br]
     [:br]

     [:label "Имя:"
      [:br]
      [:input {:type "text" :minlength 1 :name "first_name" :value (:first_name card) :class "inputs" :required true}]]

     [:br]
     [:br]

     [:label "Отчество:"
      [:br]
      [:input {:type "text" :minlength 1 :name "last_name" :value (:last_name card) :class "inputs" :required true}]]

     [:br]
     [:br]

     [:label "Пол:"
      [:br]
      (let [gender (:gender card)
            man? (= "Мужской" gender)]

        (if man?
          [:div
           [:label "Мужской"
            [:input {:type "radio" :name "gender" :value "Мужской" :checked true}]]
           [:label "Женский"
            [:input {:type "radio" :name "gender" :value "Женский"}]]]
          [:div
           [:label "Мужской"
            [:input {:type "radio" :name "gender" :value "Мужской"}]]
           [:label "Женский"
            [:input {:type "radio" :name "gender" :value "Женский" :checked true}]]])

        )]

     [:br]
     [:br]

     [:label "День рождения:"
      [:br]
      ;; :value "30/11/2015" :placeholder "DD/MM/YYYY" :pattern "[0-9]{2}/[0-9]{2}/[0-9]{4}"
      [:input {:type "date" :name "birthdate" :value (:birthdate card) :class "inputs" :required true}]]

     [:br]
     [:br]

     [:label "Тип крови:"
      [:br]

      ;; A B AB O
      (let [blood_type (:blood_type card)]
        [:select {:name "blood_type" :class "inputs"}
         [:option {:disabled true} "Выберете тип крови:"]
         (if (= "A" blood_type)
           [:option {:value "A" :checked true} "A"]
           [:option {:value "A"} "A"])
         (if (= "B" blood_type)
           [:option {:value "B" :checked true} "B"]
           [:option {:value "B"} "B"])
         (if (= "AB" blood_type)
           [:option {:value "AB" :checked true} "AB"]
           [:option {:value "AB"} "AB"])
         (if (= "O" blood_type)
           [:option {:value "O" :checked true} "O"]
           [:option {:value "O"} "O"])])]

     [:br]
     [:br]
     [:label "Рост:"
      [:br]
      [:input {:type "text" :min 1 :name "height" :value (:height card) :class "inputs" :required true}]]

     [:br]
     [:br]

     [:label "Вес:"
      [:br]
      [:input {:type "number" :min 1 :name "weight" :value (:weight card) :class "inputs" :required true}]]

     [:br]
     [:br]

     [:input {:type "submit" :name "" :value "Сохранить"}]]

    ]])


(defn view-card [card records]

  [:body {:class "mdc-typography"}

   [:div {:style "width:40%; margin: auto;"}

    mdc

    [:style
     "table, th, td {
     border: 1px solid black;
     }"]



    [:div {:class "mdc-layout-grid"}
     [:div {:class "mdc-layout-grid__inner bbbb"}

      [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-8 mdc-typography--headline6"}
       [:a {:href "/" :class "mdc-button menu-el"} "Посмотреть всех"]]

      [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-2 mdc-typography--headline6"}
       [:a {:href (str "/card/change/" (:id card)) :class "mdc-button menu-el"} "Редактировать"]]

      ]]


    [:h3 "Параметры выбранного пользователя:"]


    [:div {:class "mdc-layout-grid"}
     [:div {:class "mdc-layout-grid__inner"}

      [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-4 mdc-typography--headline6"}
       [:span "Параметр"]]

      [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-4 mdc-typography--headline6"}
       [:span "Значение"]]]

     [:div {:class "mdc-layout-grid__inner bbbb"}

      [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-4 table-el"}
       [:span "Фамилия"]]

      [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-4 table-el"}
       [:span (:family_name card)]]]

     [:div {:class "mdc-layout-grid__inner bbbb"}

      [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-4 table-el"}
       [:span "Имя"]]

      [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-4 table-el"}
       [:span (:first_name card)]]]

     [:div {:class "mdc-layout-grid__inner bbbb"}

      [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-4 table-el"}
       [:span "Отчество"]]

      [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-4 table-el"}
       [:span (:last_name card)]]]

     [:div {:class "mdc-layout-grid__inner bbbb"}

      [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-4 table-el"}
       [:span "Пол"]]

      [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-4 table-el"}
       [:span (:gender card)]]]

     [:div {:class "mdc-layout-grid__inner bbbb"}

      [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-4 table-el"}
       [:span "День рождения"]]

      [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-4 table-el"}
       [:span (:birthdate card)]]]

     [:div {:class "mdc-layout-grid__inner bbbb"}

      [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-4 table-el"}
       [:span "Тип крови"]]

      [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-4 table-el"}
       [:span (:blood_type card)]]]

     [:div {:class "mdc-layout-grid__inner bbbb"}

      [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-4 table-el"}
       [:span "Рост"]]

      [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-4 table-el"}
       [:span (:height card)]]]

     [:div {:class "mdc-layout-grid__inner bbbb"}

      [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-4 table-el"}
       [:span "Вес"]]

      [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-4 table-el"}
       [:span (:weight card)]]]
     ]


    [:h3 "Список осмотров:"]

    [:a {:href (str "/card/record/add/" (:id card))  :class "mdc-button menu-el"} "Добавить осмотр"]
    [:br]


    [:div {:class "mdc-layout-grid"}
     [:div {:class "mdc-layout-grid__inner"}

      [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-4 mdc-typography--headline6"}
       [:span "Название осмотра"]]

      [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-4 mdc-typography--headline6"}
       [:span "Дата"]]

      [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-4 mdc-typography--headline6"}
       [:span "Описание"]]]


     (reduce
       (fn [view record]
         (conj view
               [:div {:class "mdc-layout-grid__inner bbbb"}

                [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-4 table-el"}
                 [:span (:name record)]]

                [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-4 table-el"}
                 [:span (:create_time record)]]

                [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-4 table-el"}
                 [:span (:description record)]]]))
       (list)
       records)

     ]]])


(defn view-record-add [id]
  [:div {:style "width:40%; margin: auto;"}
   [:style
    ".inputs {
    width: 100%;
    }"]

   [:h3 "Добавить запись осмотра:"]

   [:form {:action (str "/card/record/add/" id) :method "POST"}

    [:label "Название:"
     [:br]
     [:input {:type "text" :minlength 1 :name "name" :class "inputs" :required true}]]

    [:br]

    [:label "Описание:"
     [:br]
     [:textarea {:minlength 1 :name "description" :class "inputs" :required true :rows 30}]]

    [:br]
    [:br]

    [:input {:type "submit" :value "Сохранить"}]
    ]])


(defroutes handler
  (GET "/" []
       (->> (sql-query/select-card-all)
            ;; pprn
            view-med-cards
            html
            ;;             html-formating
            (str "<!DOCTYPE html> \n")
            ))


  (GET "/card/search/family-name" [family-name]
       (str
         "<!DOCTYPE html> \n"
         (html
           (view-med-cards
             (sql-query/select-card-by-family-name family-name)
             family-name))))


  (GET "/card/add" []
       (->> (html (view-add-card "/card/add"))
            html-formating
            (str "<!DOCTYPE html> \n")
            ))


  (POST "/card/add" {params :params}
        (let [id (sql-query/insert-card params)]
          (redirect (str "/card/view/" id))))


  (GET "/card/change/:id" [id]
       (let [id (java.lang.Long/parseLong id)]
         (let [card (sql-query/select-card-by-id id)]
           (->> (view-add-card (str "/card/change/" id) card)
                (html)
                html-formating
                (str "<!DOCTYPE html> \n")
                ))))

  (POST "/card/change/:id" {params :params}
        (let [id (java.lang.Long/parseLong (:id params))]
          (sql-query/update-card id (dissoc params :id))
          (redirect (str "/card/view/" id))))


  (POST "/card/add" {params :params}
        (let [id (sql-query/insert-card params)]
          (pstr params)
          (redirect (str "/card/view/" id))))


  (GET "/card/view/:id" [id]
       (let [id (java.lang.Long/parseLong id)
             card (sql-query/select-card-by-id id)
             card-records (sql-query/select-record-by-card-id id)]
         (html (view-card card card-records))))


  (GET "/card/record/add/:id" [id]
       (html-formating (html (view-record-add id))))


  (POST "/card/record/add/:id" {params :params}
        (let [id (java.lang.Long/parseLong (:id params))]
          (sql-query/insert-record id (dissoc params :id))
          (redirect (str "/card/view/" id))))

  (route/resources "/")

  (route/not-found "<h1>Page not found</h1>"))
;; (sql-query/select-card-by-id 21)

(def app
  (-> handler
      (site)
      (wrap-resource "")))


(defn -main [& args]
  (server/run-server #'app {:port 8081}))


;; (-main)

