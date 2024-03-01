(ns ring.middleware.authorization
  (:require [clojure.string :as str]
            [ring.util.parsing :as parsing]))

(def ^:private re-token68
  (re-pattern "[A-Za-z0-9._~+/-]+=*"))

(def ^:private re-auth-param
  (re-pattern (str "(" parsing/re-token ")\\s*=\\s*(?:" parsing/re-value ")")))

(defn- parse-auth-params [auth-params]
  (reduce (fn [m kv]
            (if-let [[_ k v1 v2] (re-matches re-auth-param kv)]
              (assoc m (str/lower-case k) (or v1 v2))
              m))
          {}
          (str/split auth-params #"\s*,\s*")))

(defn- parse-authorization [request]
  (when-let [[auth-scheme token-or-params]
             (some-> (get-in request [:headers "authorization"])
                     (str/split #"\s" 2))]
    (cond
      (empty? token-or-params)
      {:scheme (str/lower-case auth-scheme)}

      (re-matches re-token68 token-or-params)
      {:scheme (str/lower-case auth-scheme)
       :token token-or-params}

      :else
      {:scheme (str/lower-case auth-scheme)
       :params (parse-auth-params token-or-params)})))

(defn authorization-request
  "Parses Authorization header in the request map. See: wrap-authorization."
  [request]
  (if (:authorization request)
    request
    (assoc request :authorization (parse-authorization request))))

(defn wrap-authorization
  "Parses the Authorization header in the request map, then assocs the result
  to the :authorization key on the request.

  See RFC 7235 Section 2 and RFC 9110 Section 11:
  * https://datatracker.ietf.org/doc/html/rfc7235#section-2
  * https://datatracker.ietf.org/doc/html/rfc9110#section-11"
  [handler]
  (fn
    ([request]
     (handler (authorization-request request)))
    ([request respond raise]
     (handler (authorization-request request)
              respond
              raise))))
