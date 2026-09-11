(ns cashier-ticketing.governor-test
  (:require [clojure.test :refer [deftest is testing]]
            [cashier-ticketing.store :as store]
            [cashier-ticketing.governor :as governor]))

(defn- fresh-store []
  (let [st (store/mem-store)]
    (store/register-shift! st {:shift-id "shift-1" :name "Saturday Morning Register"})
    st))

(deftest ok-on-clean-transact
  (let [st (fresh-store)
        proposal {:op :transact :effect :propose :confidence 0.9 :stake :low}
        v (governor/check {:shift-id "shift-1"} {} proposal st)]
    (is (:ok? v))
    (is (not (:hard? v)))
    (is (not (:escalate? v)))))

(deftest hard-on-unregistered-shift
  (let [st (fresh-store)
        proposal {:op :transact :effect :propose :confidence 0.9 :stake :low}
        v (governor/check {:shift-id "no-such-shift"} {} proposal st)]
    (is (:hard? v))
    (is (some #(= :no-shift (:rule %)) (:violations v)))))

(deftest hard-on-no-actuation-violation
  (let [st (fresh-store)
        proposal {:op :transact :effect :direct-write :confidence 0.9 :stake :low}
        v (governor/check {:shift-id "shift-1"} {} proposal st)]
    (is (:hard? v))
    (is (some #(= :no-actuation (:rule %)) (:violations v)))))

(deftest escalates-on-cash-drawer-discrepancy-above-threshold
  (let [st (fresh-store)
        proposal {:op :cash-drawer-discrepancy-above-threshold :effect :propose :confidence 0.9 :stake :high}
        v (governor/check {:shift-id "shift-1"} {} proposal st)]
    (is (:escalate? v))
    (is (not (:hard? v)))))

(deftest escalates-on-refund-above-limit
  (let [st (fresh-store)
        proposal {:op :refund-above-limit :effect :propose :confidence 0.9 :stake :high}
        v (governor/check {:shift-id "shift-1"} {} proposal st)]
    (is (:escalate? v))
    (is (not (:hard? v)))))

(deftest escalates-on-low-confidence
  (let [st (fresh-store)
        proposal {:op :transact :effect :propose :confidence 0.2 :stake :low}
        v (governor/check {:shift-id "shift-1"} {} proposal st)]
    (is (:escalate? v))
    (is (not (:hard? v)))))

(deftest store-records-and-ledger-append-only
  (let [st (fresh-store)]
    (store/commit-record! st {:shift-id "shift-1" :op :reconcile})
    (store/append-ledger! st {:disposition :commit})
    (is (= 1 (count (store/records-of st "shift-1"))))
    (is (= 1 (count (store/ledger st))))))
