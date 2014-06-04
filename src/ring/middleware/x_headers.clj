(ns ring.middleware.x-headers
  (:require [clojure.string :as str]
            [ring.util.response :as resp]))

(defn- allow-from? [frame-options]
  (and (map? frame-options)
       (= (keys frame-options) [:allow-from])
       (string? (:allow-from frame-options))))

(defn- format-frame-options [frame-options]
  (if (map? frame-options)
    (str "ALLOW-FROM " (:allow-from frame-options))
    (str/upper-case (name frame-options))))

(defn wrap-frame-options
  [handler frame-options]
  {:pre [(or (= frame-options :deny)
             (= frame-options :sameorigin)
             (allow-from? frame-options))]}
  (let [header-value (format-frame-options frame-options)]
    (fn [request]
      (-> (handler request)
          (resp/header "X-Frame-Options" header-value)))))
