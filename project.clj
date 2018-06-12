(defproject medcard "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [hiccup "1.0.5"]
                 [http-kit "2.3.0"]
                 [compojure "1.6.1"]
                 [ring/ring-core "1.6.3"]
                 [javax.servlet/javax.servlet-api "4.0.1"]
                 [cheshire "5.8.0"]
                 ;; Logger:
                 [org.slf4j/slf4j-simple "1.7.25"]
                 ;; DB:
                 [org.clojure/java.jdbc "0.7.6"]
                 [org.postgresql/postgresql "42.2.2"]
                 [hikari-cp "2.4.0"]
                 ;; DEV:
                 [org.jsoup/jsoup "1.11.3"]
                 ]
  :main medcard.core)

