(ns cashier-ticketing.store
  "SSoT for the ISCO-08 5230 independent cashier-and-ticketing
  sole-proprietor actor. Store is a protocol injected into the
  `cashier-ticketing.actor` StateGraph — `MemStore` is the default,
  deterministic, zero-dep backend; a Datomic/kotoba-server-backed
  implementation can be swapped in without touching the actor or
  governor (itonami actor pattern, per ADR-2607011000 / CLAUDE.md
  Actors section).

  Domain:

    shift    — a registered shift (:shift-id, :name)
    record   — a committed operating record under a shift (transact
               step, reconcile entry, cash-drawer discrepancy above
               threshold, refund above limit) — written ONLY via
               commit-record!, never mutated in place
    ledger   — an append-only audit trail of every proposal/verdict/
               disposition, regardless of outcome (commit or hold)")

(defprotocol Store
  (shift [s shift-id])
  (records-of [s shift-id])
  (ledger [s])
  (register-shift! [s shift])
  (commit-record! [s record])
  (append-ledger! [s fact]))

(defrecord MemStore [a]
  Store
  (shift [_ shift-id] (get-in @a [:shifts shift-id]))
  (records-of [_ shift-id] (filter #(= shift-id (:shift-id %)) (:records @a)))
  (ledger [_] (:ledger @a))
  (register-shift! [s shift]
    (swap! a assoc-in [:shifts (:shift-id shift)] shift) s)
  (commit-record! [s record]
    (swap! a update :records (fnil conj []) record) s)
  (append-ledger! [s fact]
    (swap! a update :ledger (fnil conj []) fact) s))

(defn mem-store
  ([] (mem-store {}))
  ([seed] (->MemStore (atom (merge {:shifts {} :records [] :ledger []} seed)))))
