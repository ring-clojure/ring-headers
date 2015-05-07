(ns ring.middleware.default-charset
  "Middleware for automatically adding a charset to the content-type header in
  response maps."
  (:require [ring.util.response :as response]))

(defn- text-based-content-type? [content-type]
  (or (re-find #"text/" content-type)
      (re-find #"application/xml" content-type)))

(defn- contains-charset? [content-type]
  (re-find #";\s*charset=[^;]*" content-type))

(defn- add-charset [resp charset]
  (if-let [content-type (response/get-header resp "Content-Type")]
    (if (and (text-based-content-type? content-type)
             (not (contains-charset? content-type)))
      (response/charset resp charset)
      resp)
    resp))

(defn wrap-default-charset
  "Middleware that adds a charset to the content-type header of the response if
  one was not set by the handler."
  [handler charset]
  (fn [req]
    (if-let [resp (handler req)]
      (add-charset resp charset))))
