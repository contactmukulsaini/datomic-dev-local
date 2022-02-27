(ns ddl.core
  (:require [datomic.client.api :as d]))

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))


(comment

  ;; Refer the the Datomic docs for configuring/setting it up
  ;; https://docs.datomic.com/cloud/dev-local.html#getting-started
  ;; https://docs.datomic.com/cloud/tutorial/client.html#create-database 

  ;;Creating datamic client
  (def client (d/client {:server-type :dev-local
                         :system "datomic-samples"}))

  ;;Reading databases
  (d/list-databases client {})

  ;;Create a database
  (d/create-database client {:db-name "movies-new"})

  ;;Establish connection with your database
  (def conn (d/connect client {:db-name "movies-new"}))

  ;;Define schema of your DB
  (def movie-schema [{:db/ident :movie/title
                      :db/valueType :db.type/string
                      :db/cardinality :db.cardinality/one
                      :db/doc "The title of the movie"}

                     {:db/ident :movie/genre
                      :db/valueType :db.type/string
                      :db/cardinality :db.cardinality/one
                      :db/doc "The genre of the movie"}

                     {:db/ident :movie/release-year
                      :db/valueType :db.type/long
                      :db/cardinality :db.cardinality/one
                      :db/doc "The year the movie was released in theaters"}])

  ;;Transacting schema in our db.
  (d/transact conn {:tx-data movie-schema})

  ;;Defining a set of movies to add to DB
  (def first-movies [{:movie/title "The Goonies"
                      :movie/genre "action/adventure"
                      :movie/release-year 1986}
                     {:movie/title "Commando3"
                      :movie/genre "thriller/action"
                      :movie/release-year 1985}
                     {:movie/title "Commando"
                      :movie/genre "thriller/action"
                      :movie/release-year 1985}
                     {:movie/title "Repo Man"
                      :movie/genre "punk dystopia"
                      :movie/release-year 1984}])

  ;;Transct the movies data to add it to DB
  (d/transact conn {:tx-data first-movies})    ;; Now you DB contains the data of movies as per the schema you defined

  ;;Define reference to db
  (def db (d/db conn))


  ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
  ;;Write query to fech movie titles data from DB
  (def all-titles-q '[:find ?movie-title
                      :where [_ :movie/title ?movie-title]])

  ;;Quering DB for the movie title's data
  (d/q all-titles-q db)

  ;;Find movie genre
  (d/q '[:find ?movie-genre
         :where [_ :movie/genre ?movie-genre]] db)

  ;;Find movie release year
  (d/q '[:find ?movie-year
         :where [_ :movie/release-year ?movie-year]] db)

  ;;Find movie title's that were relesae in year 1984
  (d/q '[:find ?movie-title
         :where
         [?m :movie/release-year 1985]
         [?m :movie/title ?movie-title]] db)

  ;;Find entity-id where movie-genre "thriller/action"
  (d/q '[:find ?e
         :where
         [?e :movie/genre "thriller/action"]
         [?e :movie/release-year 1985]] db)

  ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
  (d/q '[:find ?e
         :where
         [?e :movie/title "Commando"]] db)

  (d/q '[:find ?name
         :where
         [?e :movie/genre "thriller/action"]
         [?e :movie/release-year 1985]
         [?e :movie/title ?name]] db)

  (d/q '[:find ?e
         :where
         [?e :movie/release-year 1986]] db)

  (d/q '[:find ?title ?year ?genre
         :where
         [?e :movie/release-year ?year]
         [?e :movie/genre ?genre]
         [?e :movie/title ?title]] db)


  ;;Queries with arguments 
  (d/q {:query '[:find ?e
                 :in $ ?year
                 :where
                 [?e :movie/release-year ?year]]
        :args [db 1986]})

  (d/q {:query '[:find ?title ?year ?genre
                 :in $ ?year
                 :where
                 [?e :movie/release-year ?year]
                 [?e :movie/genre ?genre]
                 [?e :movie/title ?title]]
        :args [db 1986]})

  (d/q {:query '[:find ?title ?year ?genre
                 :where
                 [?e :movie/release-year ?year]
                 [?e :movie/genre ?genre]
                 [?e :movie/title ?title]]
        :args [db]})


  (d/entity db 87960930222179)


  ;;
  ;; (d/pull db '[:movie/release-year :movie/title] "The Goonies")


  (d/q '[:find ?movie-year
         :where [_ :movie/release-year ?movie-year]] db)

  (d/q '[:find ?e
         :where [?e :movie/release-year 1984]] db)

  (defn entity-exists? [db eid]
    (try
      (some->> eid
               (d/datoms db :eavt)
               (first)
               (:e))
      (catch Throwable _ nil)))

  (d/datoms db)

  (entity-exists? db :movie/genre)
  ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


  ;;Drop your databse
  (d/delete-database client {:db-name "movies-new"})
  )