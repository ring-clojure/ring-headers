(ns ring.middleware.authorization-test
  (:require [clojure.test :refer :all]
            [ring.middleware.authorization :refer :all]))

(deftest test-parse-authorization
  (testing "no authorization"
    (is (nil? (parse-credentials nil))))
  (testing "scheme without token"
    (is (= {:scheme "basic"}
           (parse-credentials "Basic"))))
  (testing "scheme with zero-length token"
    (is (= {:scheme "basic"}
           (parse-credentials "Basic "))))
  (testing "token68"
    (is (= {:scheme "basic"
            :token "dGVzdA=="}
           (parse-credentials "Basic dGVzdA=="))))
  (testing "auth-params, some malformed"
    (is (= {:scheme "digest"
            :params {"a"    "B"
                     "c"    "d"
                     "eeee" "dGVzdA=="
                     "k"    "1"
                     "l"    "234"}}
           (parse-credentials "Digest A=B, c=\"d\",
     eeee=\"dGVzdA==\", fparam=dGVzdA==, g, \"h\"=i, =j, = ,, , k=1, l = \"234\"")))))

(deftest test-authorization-request
  (testing "pre-existing authorization"
    (is (= "TEST"
           (-> {:headers       {"authorization" "Basic"}
                :authorization "TEST"}
               authorization-request
               :authorization))))
  (testing "no authorization"
    (is (nil? (-> {:headers {}}
                  authorization-request
                  :authorization))))
  (testing "with authorization"
    (is (some? (-> {:headers {"authorization" "Basic"}}
                   authorization-request
                   :authorization)))))

(deftest test-wrap-authorization-none
  (let [handler   (wrap-authorization (fn [req respond _] (respond req)))
        request   {:headers {}}
        response  (promise)
        exception (promise)]
    (handler request response exception)
    (is (nil? (:authorization @response)))
    (is (not (realized? exception)))))

(deftest test-wrap-authorization-scheme-only
  (let [handler   (wrap-authorization (fn [req respond _] (respond req)))
        request   {:headers {"authorization" "Basic"}}
        response  (promise)
        exception (promise)]
    (handler request response exception)
    (is (= {:scheme  "basic"}
           (:authorization @response)))
    (is (not (realized? exception)))))

(deftest test-wrap-authorization-token68
  (let [handler   (wrap-authorization (fn [req respond _] (respond req)))
        request   {:headers {"authorization" "Basic dGVzdA=="}}
        response  (promise)
        exception (promise)]
    (handler request response exception)
    (is (= {:scheme  "basic"
            :token "dGVzdA=="}
           (:authorization @response)))
    (is (not (realized? exception)))))

(deftest test-wrap-authorization-auth-params
  (let [handler   (wrap-authorization (fn [req respond _] (respond req)))
        request   {:headers {"authorization" "Digest A=\"B\""}}
        response  (promise)
        exception (promise)]
    (handler request response exception)
    (is (= {:params {"a" "B"}
            :scheme "digest"}
           (:authorization @response)))
    (is (not (realized? exception)))))

(deftest test-wrap-authorization-synchronous
  (let [request (atom nil)
        handler (wrap-authorization (fn [req] (reset! request req)))]
    (handler {:headers {"authorization" "Basic A=\"B\""}})
    (is (= {:params {"a" "B"}
            :scheme "basic"}
           (:authorization @request)))))
