(ns ring.middleware.default-charset-test
  (:use clojure.test
        ring.middleware.default-charset
        [ring.mock.request :only [request]]
        [ring.util.response :only [charset content-type]]))

(deftest test-wrap-charset
  (testing "no content-type header present"
    (let [handler (wrap-default-charset (constantly {}) "utf-16")
          resp    (handler request)]
      (is (= resp {}))))

  (testing "content-type header without charset present"
    (let [handler (wrap-default-charset
                   (constantly (content-type {} "text/html"))
                   "utf-16")
          resp    (handler request)]
      (is (= (:headers resp)
             {"Content-Type" "text/html; charset=utf-16"}))))

  (testing "content-type header with charset present"
    (let [handler (wrap-default-charset
                   (constantly (-> {} (content-type "text/html") (charset "utf-8")))
                   "utf-16")
          resp    (handler request)]
      (is (= (:headers resp)
             {"Content-Type" "text/html; charset=utf-8"}))))

  (testing "non-text-based content-type"
    (let [handler (wrap-default-charset
                   (constantly (content-type {} "application/gzip"))
                   "utf-8")
          resp    (handler request)]
      (is (= (:headers resp)
             {"Content-Type" "application/gzip"})))))

(deftest test-wrap-charset-cps
  (testing "content-type header without charset present"
    (let [handler (wrap-default-charset
                   (fn [_ respond _] (respond (content-type {} "text/html")))
                   "utf-16")
          resp    (promise)
          ex      (promise)]
      (handler request resp ex)
      (is (not (realized? ex)))
      (is (= (:headers @resp) {"Content-Type" "text/html; charset=utf-16"}))))

  (testing "content-type header with charset present"
    (let [handler (wrap-default-charset
                   (fn [_ respond _]
                     (respond (-> {} (content-type "text/html") (charset "utf-8"))))
                   "utf-16")
          resp    (promise)
          ex      (promise)]
      (handler request resp ex)
      (is (not (realized? ex)))
      (is (= (:headers @resp) {"Content-Type" "text/html; charset=utf-8"})))))
