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
    [ring.middleware.basic-authentication :refer [wrap-basic-authentication]]
    [medcard.utils :refer [pstr pprn]]
    )
  (:import
    (org.jsoup Jsoup)
    ))


(def mdc
  (list
    (include-css "/min.css")
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
     .ggray {
     background-color: #b3e0ff;
     }
     .bgray {
     background-color: #E6E6FA;
     }
     "]
    ))


(def footer
  [:footer
   [:hr]
   [:div
    {:style "text-align: center;"}
    "&#169 Ксения Белькевич"]])


(defn html-formating [html-str]
  (.html (.body (Jsoup/parseBodyFragment html-str))))


(defn getName [authorization]
  (when authorization
    (let [token (subs authorization 6)
          lp (String. (.decode (java.util.Base64/getDecoder) token))]
      (first (clojure.string/split lp #":")))))


(defn view-med-cards [med-cards & [family-name authorization]]
  [:html

   [:head
    [:meta {:charset "utf-8"}]
    [:meta {:name "viewport" :content "width=device-width,initial-scale=1"}]
    mdc]

   [:body {:class "mdc-typography"}

    [:div {:class "mdc-layout-grid ggray bbbb"}
     [:div {:class "mdc-layout-grid__inner"}

      [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-2 mdc-typography--headline6"}
       [:a {:href "/card/add" :class "mdc-button menu-el"} "Добавить новую карту"]]

      [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-4 mdc-typography--headline6"}
       (when-let [user-name (getName authorization)]
         [:div {:class "mdc-button menu-el"} (str "Вы вошли как '" user-name "'")]
         )]

      [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-2 mdc-typography--headline6"}
       [:a {:href "/about/programm" :class "mdc-button menu-el"} "О программе"]]

      [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-2 mdc-typography--headline6"}
       [:a {:href "/about/developer" :class "mdc-button menu-el"} "О разработчике"]]

      [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-2 mdc-typography--headline6"}
       [:a {:href "/user/create" :class "mdc-button menu-el"} "Создать аккаунт"]]

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
       [:span "Дата рождения"]]

      [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-2 mdc-typography--headline6"}]

      ]

     [:br]

     (reduce
       (fn [view {id :id family_name :family_name first_name :first_name last_name :last_name gender :gender birthdate :birthdate blood_type :blood_type height :height weight :weight}]
         (conj view
               [:div {:class "mdc-layout-grid__inner bgray bbbb"}

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

     footer

     (include-js "/min.js")

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
      [:input {:type "text" :minlength 1 :pattern "[А-Я][а-я]{0,10}" :title "Первая буква заглавная и далее до 10 строчных" :name "family_name" :value (:family_name card) :class "inputs" :required true}]]

     [:br]
     [:br]

     [:label "Имя:"
      [:br]
      [:input {:type "text" :minlength 1 :pattern "[А-Я][а-я]{0,10}" :title "Первая буква заглавная и далее до 10 строчных" :name "first_name" :value (:first_name card) :class "inputs" :required true}]]

     [:br]
     [:br]

     [:label "Отчество:"
      [:br]
      [:input {:type "text" :minlength 1 :pattern "[А-Я][а-я]{0,10}" :title "Первая буква заглавная и далее до 10 строчных" :name "last_name" :value (:last_name card) :class "inputs" :required true}]]

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

     [:label "Дата рождения:"
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
      [:input {:type "number" :min 1 :max 250 :name "height" :value (:height card) :class "inputs" :required true}]]

     [:br]
     [:br]

     [:label "Вес:"
      [:br]
      [:input {:type "number" :min 1 :max 250 :name "weight" :value (:weight card) :class "inputs" :required true}]]

     [:br]
     [:br]

     [:input {:type "submit" :name "" :value "Сохранить"}]]

    footer

    (include-js "/min.js")

    ]])


(defn view-card [card records]

  [:body {:class "mdc-typography"}

   [:div

    mdc

    [:style
     "table, th, td {
     border: 1px solid black;
     }"]


    [:div {:class "mdc-layout-grid ggray bbbb"}
     [:div {:class "mdc-layout-grid__inner"}

      [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-10 mdc-typography--headline6"}
       [:a {:href "/" :class "mdc-button menu-el"} "Список пациентов"]]

      [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-2 mdc-typography--headline6"}
       [:a {:href (str "/card/change/" (:id card)) :class "mdc-button menu-el"} "Редактировать"]]

      ]]

    [:div {:class "mdc-layout-grid"}

     [:h3 "Параметры выбранного пациента:"]

     [:div {:class "mdc-layout-grid__inner"}

      [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-4 mdc-typography--headline6"}
       [:span "Параметр"]]

      [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-4 mdc-typography--headline6"}
       [:span "Значение"]]]

     [:div {:class "mdc-layout-grid__inner bgray bbbb"}

      [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-4 table-el"}
       [:span "Фамилия"]]

      [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-4 table-el"}
       [:span (:family_name card)]]]

     [:div {:class "mdc-layout-grid__inner bgray bbbb"}

      [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-4 table-el"}
       [:span "Имя"]]

      [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-4 table-el"}
       [:span (:first_name card)]]]

     [:div {:class "mdc-layout-grid__inner bgray bbbb"}

      [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-4 table-el"}
       [:span "Отчество"]]

      [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-4 table-el"}
       [:span (:last_name card)]]]

     [:div {:class "mdc-layout-grid__inner bgray bbbb"}

      [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-4 table-el"}
       [:span "Пол"]]

      [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-4 table-el"}
       [:span (:gender card)]]]

     [:div {:class "mdc-layout-grid__inner bgray bbbb"}

      [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-4 table-el"}
       [:span "Дата рождения"]]

      [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-4 table-el"}
       [:span (:birthdate card)]]]

     [:div {:class "mdc-layout-grid__inner bgray bbbb"}

      [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-4 table-el"}
       [:span "Тип крови"]]

      [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-4 table-el"}
       [:span (:blood_type card)]]]

     [:div {:class "mdc-layout-grid__inner bgray bbbb"}

      [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-4 table-el"}
       [:span "Рост"]]

      [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-4 table-el"}
       [:span (:height card)]]]

     [:div {:class "mdc-layout-grid__inner bgray bbbb"}

      [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-4 table-el"}
       [:span "Вес"]]

      [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-4 table-el"}
       [:span (:weight card)]]]
     ]

    [:div {:class "mdc-layout-grid"}


     [:h3 "Список осмотров:"]

     [:a {:href (str "/card/record/add/" (:id card))  :class "mdc-button menu-el"} "Добавить осмотр"]
     [:br]



     (when-not (empty? records)

       [:div {:class "mdc-layout-grid__inner"}

        [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-3 mdc-typography--headline6 "}
         [:span "Название осмотра"]]

        [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-2 mdc-typography--headline6"}
         [:span "Дата"]]

        [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-7 mdc-typography--headline6"}
         [:span "Описание"]]])

     (reduce
       (fn [view record]
         (conj view
               [:div {:class "mdc-layout-grid__inner bbbb bgray"}

                [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-3 table-el"}
                 [:span (:name record)]]

                [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-2 table-el"}
                 [:span (:create_time record)]]

                [:div {:class "mdc-layout-grid__cell mdc-layout-grid__cell--span-7 table-el"}
                 [:span (:description record)]]]))
       (list)
       records)

     footer

     (include-js "/min.js")

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
     [:input {:type "text" :minlength 1 :pattern "[А-Я][а-я]{0,45}" :title "Первая буква заглавная и далее до 45 строчных" :name "name" :class "inputs" :required true}]]

    [:br]

    [:label "Описание:"
     [:br]
     [:textarea {:minlength 1 :name "description" :class "inputs" :required true :rows 30}]]

    [:br]
    [:br]

    [:input {:type "submit" :value "Сохранить"}]

    footer

    (include-js "/min.js")
    ]])


(defn view-user-create []
  [:div {:style "width:40%; margin: auto;"}
   [:style
    ".inputs {
    width: 100%;
    }"]

   [:h3 "Добавить нового пользователя:"]

   [:form {:action "/user/create" :method "POST"}

    [:label "Логин:"
     [:br]
     [:input {:type "text" :name "login" :class "inputs" :required true}]]

    [:br]

    [:label "Пароль:"
     [:br]
     [:input {:type "text" :name "password" :class "inputs" :required true}]]

    [:br]
    [:br]

    [:input {:type "submit" :value "Создать"}]

    footer

    (include-js "/min.js")
    ]])


(defroutes handler
  (GET "/" r
       (let [headers (:headers r)
             authorization (get headers "authorization")]

         (-> (sql-query/select-card-all)
             ;; pprn
             (view-med-cards nil authorization)
             html
             ;; html-formating
             (str "<!DOCTYPE html> \n")
             )))


  (GET "/card/search/family-name" r
       (let [headers (:headers r)
             authorization (get headers "authorization")
             family-name (:family-name (:params r))]
         (str
           "<!DOCTYPE html> \n"
           (html
             (view-med-cards
               (sql-query/select-card-by-family-name family-name)
               family-name
               authorization)))))


  (GET "/card/add" []
       (->> (html (view-add-card "/card/add"))
            ;; html-formating
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
                ;; html-formating
                (str "<!DOCTYPE html> \n")
                ))))

  (POST "/card/change/:id" {params :params}
        (let [id (java.lang.Long/parseLong (:id params))]
          (sql-query/update-card id (dissoc params :id))
          (redirect (str "/card/view/" id))))


  (POST "/card/add" {params :params}
        (let [id (sql-query/insert-card params)]
          ;; (pstr params)
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


  (GET "/about/programm" []
       (html5
         [:html
          {:lang "ru"}
          [:head
           [:meta {:charset "utf-8"}]
           [:title "О программе"]
           [:style
            "\n     .block1 { \n      width: 1000px; /* Ширина элемента в пикселах */\n      padding: 20px; /* Поля вокруг текста */\n      margin-top: 40px; \n      margin: auto; /* Выравниваем по центру */\n      background: #66b3ff; /* Цвет фона */  \n      background: linear-gradient(to right, #66b3ff, #99ccff, #cce6ff, #ffffff);\n      }\n      .center {text-align: center;}\n      .left {text-align: left;}\n   "]]
          [:body
           " \n   "
           [:div.block1
            [:div.center [:h1 "О программе"]]
            [:div.left
             [:p
              [:strong
               "Основными целями разработки программного средства являются:"]]
             [:ul
              {:type "square"}
              [:li
               "повысить продуктивность и качество работы сотрудников медицинского учреждения;"]
              [:li
               "повысить скорость и качество ведения консультаций пациентов;"]
              [:li
               "повысить удовлетворяемость пациентов от предоставленных услуг медицинского учреждения."]]
             [:p [:strong "Функции приложения:"]]
             [:ul
              {:type "square"}
              [:li
               [:i "Информационная:"]
               [:ul
                [:li
                 "Обеспечение доступа к медицинской картотеке, содержащей в себе задокументированную и сохраненную соответствующую медицинскую информацию о состоянии здоровья пациента: данные о пациенте, диагнозы, осмотры, заключения, результаты лабораторных анализов и прочие медицинские исследования."]]]
              [:li
               [:i "Маркетинговая:"]
               [:ul
                [:li
                 "Возможность врачу без дополнительных затрат времени и сил работать с информацией о состоянии здоровья пациента с максимальным удобством для себя и максимальной эффективностью с точки зрения лечебно-диагностического процесса."]
                [:li
                 "Возможность вторичному пациенту обращаться в медицинское учреждение без носителя информации, содержащий историю болезни, что повысит комфорт и скорость обслуживания клиента."]
                [:li
                 "Предоставление пациентам печатных или электронных копий хранимой информации."]]]
              [:li
               [:i "Коммуникационная:"]
               [:ul
                [:li
                 "Предоставление, уполномоченному медицинскому персоналу, полной информации о данных и здоровье конкретного пациента, хранящейся в медицинской картотеке."]]]]
             "  \n   "]]]]))


  (GET "/about/developer" []
       (html5
         [:html
          {:lang "ru"}
          [:head
           [:meta {:charset "utf-8"}]
           [:title "О разработчике"]
           [:style
            "\n     .block1 { \n      width: 1000px; /* Ширина элемента в пикселах */\n      padding: 20px; /* Поля вокруг текста */\n      margin-top: 40px; \n      margin: auto; /* Выравниваем по центру */\n      background: #66b3ff; /* Цвет фона */\nbackground: linear-gradient(to bottom, #66b3ff, #99ccff, #cce6ff, #ffffff);\ndisplay: flex;\n    justify-content: center; /*Центрирование по горизонтали*/\n    align-items: center;     /*Центрирование по вертикали */\n      }\n      /*.center {text-align: center;}*/\n   "]]
          [:body
           " \n   "
           [:div.block1
            [:div.center
             [:center [:h1 "О разработчике"]]
             [:h3
              "ФИО слушателя:  "
              [:font {:color "#000d1a"} "Белькевич Ксения Сергеевна"]]
             [:h3 "Группа:  " [:font {:color "#000d1a"} "ПВ2-16ПО"]]
             [:h3
              "Год разработки программы: "
              [:font {:color "#000d1a"} "2018 год"]]]]]]))


  (GET "/user/create" []
       (html5 (view-user-create)))


  (POST "/user/create" [login password]
        (sql-query/insert-record login password)
        (redirect "/"))


  (GET "/logout" []
       {"WWW-Authenticate" "Basic realm=\"Login required\""
        :status 401})


  (route/resources "/")


  (route/not-found "<h1>Page not found</h1>"))


(defn authenticated? [user-name user-pass]
  (or
    (and (= user-name "admin")
         (= user-pass "admin"))
    (let [password (:password (sql-query/select-user-by-login user-name))]
      (= password user-pass))))


(def app
  (-> handler
      (site)
      (wrap-resource "")
      (wrap-basic-authentication authenticated?)))


(defn -main [& args]
  (server/run-server #'app {:port 8081}))


;; (-main)

