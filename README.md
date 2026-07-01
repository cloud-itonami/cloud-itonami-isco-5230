# cloud-itonami-isco-5230

Open Occupation Blueprint for **ISCO-08 5230**: Cashiers and Ticket Clerks.

This repository designs a forkable OSS business for an independent cashier/ticket clerk: a point-of-sale-assist robot performs physical bagging and receipt printing under a governor-gated actor, so the practice keeps its own transaction and reconciliation records instead of renting a closed POS SaaS.

## Robotics premise

All cloud-itonami verticals are designed on the premise that a **robot performs
the physical domain work**. Here a point-of-sale-assist robot performs physical bagging, receipt printing and till-drawer handling under an actor that proposes
actions and an independent **Cashier Ticketing Governor** that gates them. The governor never
dispatches hardware itself; `:high`/`:safety-critical` actions (such as
cash-drawer discrepancy above threshold, or a refund above a set limit) require human sign-off.

A live sample of the operator console (robotics safety console, shared template) is rendered in [docs/samples/operator-console.html](docs/samples/operator-console.html) — pure-data HTML output of `kotoba.robotics.ui`.

## Core Contract

```text
shift plan + till float + pricing rules
        |
        v
Cashier Advisor -> Cashier Ticketing Governor -> transact/reconcile, or human sign-off
        |
        v
robot actions (gated) + operating records + audit ledger
```

No automated advice can dispatch a robot action the governor refuses, suppress
an operating record, or disclose sensitive data without governor approval and
audit evidence.

## Capability layer

Resolves via [`kotoba-lang/occupation`](https://github.com/kotoba-lang/occupation)
(ISCO-08 `5230`). Required capabilities:

- :robotics
- :forms
- :identity
- :audit-ledger
- :bpmn

See [`docs/business-model.md`](docs/business-model.md) and
[`docs/operator-guide.md`](docs/operator-guide.md).

## License

AGPL-3.0-or-later.
