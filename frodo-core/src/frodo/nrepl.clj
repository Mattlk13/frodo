(ns ^{:clojure.tools.namespace.repl/load false} frodo.nrepl
    (:require [clojure.tools.nrepl.server :as nrepl]
              [clojure.java.io :as io]))

(defn resolve-middleware [sym]
  (let [name-space (symbol (namespace sym))]
    (require name-space)
    (ns-resolve (create-ns name-space)
                (symbol (name sym)))))

(defn repl-handler [{:keys [nrepl-middleware]}]
  (apply nrepl/default-handler
         (map resolve-middleware nrepl-middleware)))

(defn start-nrepl! [config & [{:keys [repl-options target-path]} :as project]]
  (when-let [nrepl-port (get-in config [:frodo/config :nrepl :port])]
    (when target-path
      (doto (io/file target-path "repl-port")
        (spit nrepl-port)
        (.deleteOnExit)))

    (nrepl/start-server :port nrepl-port
                        :handler (repl-handler repl-options))
    
    (println "Started nREPL server, port" nrepl-port)))
