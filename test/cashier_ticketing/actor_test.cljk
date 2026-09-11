(ns cashier-ticketing.actor-test
  (:require [clojure.test :refer [deftest is testing]]
            [cashier-ticketing.actor :as actor]
            [cashier-ticketing.store :as store]))

(defn- fresh-store []
  (let [st (store/mem-store)]
    (store/register-shift! st {:shift-id "shift-1" :name "Saturday Morning Register"})
    st))

(deftest commits-a-clean-low-risk-request
  (let [st (fresh-store)
        graph (actor/build-graph {:store st})
        request {:shift-id "shift-1" :op :transact :stake :low}
        result (actor/run-request! graph request {} "thread-1")]
    (is (= :done (:status result)))
    (is (some? (get-in result [:state :record])))
    (is (= 1 (count (store/records-of st "shift-1"))))))

(deftest holds-on-unregistered-shift-without-committing
  (let [st (fresh-store)
        graph (actor/build-graph {:store st})
        request {:shift-id "no-such-shift" :op :transact :stake :low}
        result (actor/run-request! graph request {} "thread-2")]
    (is (= :done (:status result)))
    (is (nil? (get-in result [:state :record])))
    (is (empty? (store/records-of st "no-such-shift")))
    (is (= :hold (:disposition (:state result))))))

(deftest interrupts-then-commits-on-human-approval
  (let [st (fresh-store)
        graph (actor/build-graph {:store st})
        ;; cash-drawer discrepancy above threshold always escalates (governor invariant)
        request {:shift-id "shift-1" :op :cash-drawer-discrepancy-above-threshold :stake :high}
        interrupted (actor/run-request! graph request {} "thread-3")]
    (is (= :interrupted (:status interrupted)))
    (is (empty? (store/records-of st "shift-1")))
    (let [resumed (actor/approve! graph "thread-3")]
      (is (= :done (:status resumed)))
      (is (some? (get-in resumed [:state :record])))
      (is (= 1 (count (store/records-of st "shift-1")))))))
