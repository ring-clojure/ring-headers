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
                     :body    "hello"}))))))
