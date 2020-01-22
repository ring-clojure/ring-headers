(ns ring.middleware.proxy-headers
  "Middleware for handling headers set by HTTP proxies."
  (:require [clojure.string :as str]))

(defn forwarded-remote-addr-request
  "Change the :remote-addr key of the request map to the last value present in
  the X-Forwarded-For header. See: wrap-forwarded-remote-addr."
  [request {:keys [proxy-count] :as options :or {proxy-count 1}}]
  (if-let [forwarded-for (get-in request [:headers "x-forwarded-for"])]
    (let [forwarded-addrs (str/split forwarded-for #",")
          remote-addr (some-> (nth forwarded-addrs (- (count forwarded-addrs) proxy-count) nil)
                              (str/trim))]
      (if remote-addr
        (assoc request :remote-addr remote-addr)
        request))
    request))

(defn wrap-forwarded-remote-addr
  "Middleware that changes the :remote-addr of the request map to the
  last value present in the X-Forwarded-For header.

  If a request passes through multiple trusted proxies before reaching your server,
  you can set :proxy-count in options."
  ([handler]
   (wrap-forwarded-remote-addr handler nil))
  ([handler options]
   {:pre [(or (nil? options) (< 0 (:proxy-count options)))]}
   (fn
     ([request]
      (handler (forwarded-remote-addr-request request options)))
     ([request respond raise]
      (handler (forwarded-remote-addr-request request options) respond raise)))))
