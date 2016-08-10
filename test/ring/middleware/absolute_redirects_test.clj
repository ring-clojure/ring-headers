(ns ring.middleware.absolute-redirects-test
  (:use clojure.test
        ring.middleware.absolute-redirects
        [ring.mock.request :only [request]]
        [ring.util.response :only [redirect response content-type created]]))

(deftest test-wrap-absolute-redirects
  (testing "relative redirects"
    (let [handler (wrap-absolute-redirects (constantly (redirect "/foo")))
          resp    (handler (request :get "/"))]
      (is (= (:status resp) 302))
      (is (= (:headers resp) {"Location" "http://localhost/foo"}))))

  (testing "absolute redirects"
    (let [handler (wrap-absolute-redirects (constantly (redirect "http://example.com")))
          resp    (handler (request :get "/"))]
      (is (= (:status resp) 302))
      (is (= (:headers resp) {"Location" "http://example.com"}))))

  (testing "up path redirects"
    (let [handler (wrap-absolute-redirects (constantly (redirect "../foo")))
          resp    (handler (request :get "/bar/baz.html"))]
      (is (= (:status resp) 302))
      (is (= (:headers resp) {"Location" "http://localhost/foo"}))))

  (testing "no redirects"
    (let [handler (wrap-absolute-redirects (constantly (response "hello")))
          resp    (handler (request :get "/bar/baz.html"))]
      (is (= (:status resp) 200))
      (is (= (:headers resp) {}))
      (is (= (:body resp) "hello"))))

  (testing "additional headers"
    (let [handler (wrap-absolute-redirects
                   (constantly (-> (redirect "/foo")
                                   (content-type "text/plain"))))
          resp    (handler (request :get "/"))]
      (is (= (:status resp) 302))
      (is (= (:headers resp) {"Location" "http://localhost/foo"
                              "Content-Type" "text/plain"}))))
  (testing "resource creation"
    (let [handler (wrap-absolute-redirects (constantly (created "/bar/1")))
          resp    (handler (request :post "/bar"))]
      (is (= (:status resp) 201))
      (is (= (:headers resp) {"Location" "http://localhost/bar/1"})))))

(deftest test-wrap-absolute-redirects-cps
  (testing "relative redirects"
    (let [handler (wrap-absolute-redirects (fn [_ respond _] (respond (redirect "/foo"))))
          resp    (promise)
          ex      (promise)]
      (handler (request :get "/") resp ex)
      (is (not (realized? ex)))
      (is (= (:status @resp) 302))
      (is (= (:headers @resp) {"Location" "http://localhost/foo"}))))

  (testing "absolute redirects"
    (let [handler (wrap-absolute-redirects
                   (fn [_ respond _] (respond (redirect "http://example.com"))))
          resp    (promise)
          ex      (promise)]
      (handler (request :get "/") resp ex)
      (is (not (realized? ex)))
      (is (= (:status @resp) 302))
      (is (= (:headers @resp) {"Location" "http://example.com"}))))

  (testing "no redirects"
    (let [handler (wrap-absolute-redirects (fn [_ respond _] (respond (response "hello"))))
          resp    (promise)
          ex      (promise)]
      (handler (request :get "/bar/baz.html") resp ex)
      (is (not (realized? ex)))
      (is (= (:status @resp) 200))
      (is (= (:headers @resp) {}))
      (is (= (:body @resp) "hello")))))
