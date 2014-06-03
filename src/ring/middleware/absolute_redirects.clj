(ns ring.middleware.absolute-redirects
  (:require [ring.util.request :as req])
  (:import  [java.net URL MalformedURLException]))

(defn- redirect? [response]
  (#{301 302 303 307} (:status response)))

(defn- get-header-key [response ^String header-name]
  (->> response :headers keys
       (filter #(.equalsIgnoreCase header-name %))
       first))

(defn- update-header [response header f & args]
  (if-let [header (get-header-key response header)]
    (apply update-in response [:headers header] f args)
    response))

(defn- url? [^String s]
  (try (URL. s) true
       (catch MalformedURLException _ false)))

(defn- absolute-url [location request]
  (if (url? location)
    location
    (let [url (URL. (req/request-url request))]
      (str (URL. url location)))))

(defn wrap-absolute-redirects
  [handler]
  (fn [request]
    (let [response (handler request)]
      (if (redirect? response)
        (update-header response "location" absolute-url request)
        response))))
