(ns ring.middleware.x-headers-test
  (:use clojure.test
        ring.middleware.x-headers
        [ring.mock.request :only [request]]
        [ring.util.response :only [redirect response content-type]]))

(deftest test-wrap-frame-options
  (let [handle-hello (constantly (response "hello"))]
    (testing "deny"
      (let [handler (wrap-frame-options handle-hello :deny)
            resp    (handler (request :get "/"))]
        (is (= (:headers resp) {"X-Frame-Options" "DENY"}))))

    (testing "sameorigin"
      (let [handler (wrap-frame-options handle-hello :sameorigin)
            resp    (handler (request :get "/"))]
        (is (= (:headers resp) {"X-Frame-Options" "SAMEORIGIN"}))))

    (testing "allow-from"
      (let [handler (wrap-frame-options handle-hello {:allow-from "http://example.com/"})
            resp    (handler (request :get "/"))]
        (is (= (:headers resp) {"X-Frame-Options" "ALLOW-FROM http://example.com/"}))))

    (testing "bad arguments"
      (is (thrown? AssertionError (wrap-frame-options handle-hello :foobar)))
      (is (thrown? AssertionError (wrap-frame-options handle-hello {:allowfrom "foo"})))
      (is (thrown? AssertionError (wrap-frame-options handle-hello {:allow-from nil}))))

    (testing "response fields"
      (let [handler (constantly
                     (-> (response "hello")
                         (content-type "text/plain")))
            resp    ((wrap-frame-options handler :deny)
                     (request :get "/"))]
        (is (= resp {:status  200
                     :headers {"X-Frame-Options" "DENY"
                               "Content-Type" "text/plain"}
                     :body    "hello"}))))

    (testing "nil response"
      (let [handler (wrap-frame-options (constantly nil) :deny)]
        (is (nil? (handler (request :get "/"))))))))

(deftest test-wrap-frame-options-cps
  (testing "deny"
    (let [handler (-> (fn [_ respond _] (respond (response "hello")))
                      (wrap-frame-options :deny))
          resp    (promise)
          ex      (promise)]
      (handler (request :get "/") resp ex)
      (is (not (realized? ex)))
      (is (= (:headers @resp) {"X-Frame-Options" "DENY"}))))

  (testing "nil response"
    (let [handler (-> (fn [_ respond _] (respond nil))
                      (wrap-frame-options :deny))
          resp    (promise)
          ex      (promise)]
      (handler (request :get "/") resp ex)
      (is (not (realized? ex)))
      (is (nil? @resp)))))

(deftest test-wrap-content-type-options
  (let [handle-hello (constantly (-> (response "hello") (content-type "text/plain")))]
    (testing "nosniff"
      (let [handler (wrap-content-type-options handle-hello :nosniff)
            resp    (handler (request :get "/"))]
        (is (= resp {:status  200
                     :headers {"X-Content-Type-Options" "nosniff"
                               "Content-Type" "text/plain"}
                     :body    "hello"}))))

    (testing "bad arguments"
      (is (thrown? AssertionError (wrap-content-type-options handle-hello :foo))))

    (testing "nil response"
      (let [handler (wrap-content-type-options (constantly nil) :nosniff)]
        (is (nil? (handler (request :get "/"))))))))

(deftest test-wrap-content-type-options-cps
  (testing "nosniff"
    (let [handler (-> (fn [_ respond _]
                        (respond (-> (response "hello") (content-type "text/plain"))))
                      (wrap-content-type-options :nosniff))
          resp    (promise)
          ex      (promise)]
      (handler (request :get "/") resp ex)
      (is (not (realized? ex)))
      (is (= @resp {:status  200
                    :headers {"X-Content-Type-Options" "nosniff"
                              "Content-Type" "text/plain"}
                    :body    "hello"}))))

  (testing "nil response"
    (let [handler (-> (fn [_ respond _] (respond nil))
                      (wrap-content-type-options :nosniff))
          resp    (promise)
          ex      (promise)]
      (handler (request :get "/") resp ex)
      (is (not (realized? ex)))
      (is (nil? @resp)))))

(deftest test-wrap-xss-protection
  (let [handle-hello (constantly (response "hello"))]
    (testing "enable"
      (let [handler (wrap-xss-protection handle-hello true)
            resp    (handler (request :get "/"))]
        (is (= (:headers resp) {"X-XSS-Protection" "1"}))))

    (testing "disable"
      (let [handler (wrap-xss-protection handle-hello false)
            resp    (handler (request :get "/"))]
        (is (= (:headers resp) {"X-XSS-Protection" "0"}))))

    (testing "enable with block"
      (let [handler (constantly
                     (-> (response "hello")
                         (content-type "text/plain")))
            resp    ((wrap-xss-protection handler true {:mode :block})
                     (request :get "/"))]
        (is (= resp {:status  200
                     :headers {"X-XSS-Protection" "1; mode=block"
                               "Content-Type" "text/plain"}
                     :body    "hello"}))))

    (testing "bad arguments"
      (is (thrown? AssertionError
                   (wrap-xss-protection handle-hello true {:mode :blob}))))

    (testing "nil response"
      (let [handler (wrap-xss-protection (constantly nil) true)]
        (is (nil? (handler (request :get "/"))))))))
