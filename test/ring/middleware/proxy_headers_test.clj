(ns ring.middleware.proxy-headers-test
  (:use clojure.test
        ring.middleware.proxy-headers
        [ring.mock.request :only [request header]]
        [ring.util.response :only [response]]))

(deftest test-wrap-forwarded-remote-addr
  (let [handler (wrap-forwarded-remote-addr (comp response :remote-addr))]
    (testing "without x-forwarded-for"
      (let [req  (assoc (request :get "/") :remote-addr "1.2.3.4")
            resp (handler req)]
        (is (= (:body resp) "1.2.3.4"))))

    (testing "with x-forwarded-for"
      (let [req  (-> (request :get "/")
                     (assoc :remote-addr "127.0.0.1")
                     (header "x-forwarded-for" "1.2.3.4"))
            resp (handler req)]
        (is (= (:body resp) "1.2.3.4"))))

    (testing "with multiple proxies"
      (let [req  (-> (request :get "/")
                     (assoc :remote-addr "127.0.0.1")
                     (header "x-forwarded-for" "10.0.1.9, 192.168.4.98, 1.2.3.4"))
            resp (handler req)]
        (is (= (:body resp) "1.2.3.4"))))))

(deftest test-wrap-forwarded-remote-addr-multiple-proxies
  (let [handler (wrap-forwarded-remote-addr (comp response :remote-addr) {:proxy-count 2})]
    (testing "without x-forwarded-for"
      (let [req (assoc (request :get "/") :remote-addr "1.2.3.4")
            resp (handler req)]
        (is (= (:body resp) "1.2.3.4"))))
    ;; TODO: what should happen if there aren't enough Forwarded addresses? Nothing?
    (testing "with not enough proxies"
      (let [req (-> (request :get "/")
                    (assoc :remote-addr "127.0.0.1")
                    (header "x-forwarded-for" "1.2.3.4"))
            resp (handler req)]
        (is (= (:body resp) "127.0.0.1"))))
    (testing "request with two proxies"
      (let [req (-> (request :get "/")
                    (assoc :remote-addr "127.0.0.1")
                    (header "x-forwarded-for" "122.54.196.223, 1.2.3.4"))
            resp (handler req)]
        (is (= (:body resp) "122.54.196.223"))))
    (testing "request with two trusted proxies and one untrusted proxy"
      (let [req (-> (request :get "/")
                    (assoc :remote-addr "127.0.0.1")
                    (header "x-forwarded-for" "10.0.1.9, 122.54.196.223, 1.2.3.4"))
            resp (handler req)]
        (is (= (:body resp) "122.54.196.223")))))
  (testing "proxy count of 0"
    (is (thrown?
          AssertionError
          (wrap-forwarded-remote-addr (comp response :remote-addr) {:proxy-count 0})))))

(deftest test-wrap-forwarded-remote-addr-cps
  (let [handler (wrap-forwarded-remote-addr
                 (fn [request respond _] (respond (response (:remote-addr request)))))]
    (testing "without x-forwarded-for"
      (let [req  (assoc (request :get "/") :remote-addr "1.2.3.4")
            resp (promise)
            ex   (promise)]
        (handler req resp ex)
        (is (not (realized? ex)))
        (is (= (:body @resp) "1.2.3.4"))))

    (testing "with x-forwarded-for"
      (let [req  (-> (request :get "/")
                     (assoc :remote-addr "127.0.0.1")
                     (header "x-forwarded-for" "1.2.3.4"))
            resp (promise)
            ex   (promise)]
        (handler req resp ex)
        (is (not (realized? ex)))
        (is (= (:body @resp) "1.2.3.4"))))))
