# CMS Scope API

The scope API imports sparse CMS requests, resolves effective domains, generates draft planning artifacts, and validates dependency order.

## CMS4-4 Smoke Flow

Create the request:

```bash
curl -X POST http://localhost:3000/scope-requests \
  -H 'Content-Type: application/json' \
  -d '{
    "requestCode": "CMS4-4",
    "slug": "cms4",
    "requestType": "new",
    "title": "cms",
    "goal": "cms",
    "selectedDomains": [],
    "autoAddedDomains": [],
    "declaredEffectiveDomains": [],
    "dependencies": [],
    "acceptanceCriteria": [
      "selected and auto-added effective domains have spec.md, plan.md, and tasks.md drafts",
      "generation_order stage order is not violated",
      "each domain requires dependency is satisfied in the same or previous stage"
    ],
    "sourceText": "SPEC-REQUEST.md"
  }'
```

Then run:

```bash
curl -X POST http://localhost:3000/scope-requests/{requestId}/resolve-domains
curl -X POST http://localhost:3000/scope-requests/{requestId}/generate-artifacts -H 'Content-Type: application/json' -d '{"forceRegenerate": false}'
curl -X POST http://localhost:3000/scope-requests/{requestId}/validate
```

Expected result: one default `cms-core` domain in generation stage 1, one complete artifact set containing `spec`, `plan`, and `tasks`, and validation status `passed`.

## Dependency Failure Example

Create a request with `cms-admin` requiring `cms-core`, then set `cms-admin` to stage 1 and `cms-core` to stage 2:

```bash
curl -X PUT http://localhost:3000/scope-requests/{requestId}/generation-order \
  -H 'Content-Type: application/json' \
  -d '{"stages":[{"stageNumber":1,"domainKeys":["cms-admin"]},{"stageNumber":2,"domainKeys":["cms-core"]}]}'
```

Validation returns `failed` with a `later_stage_dependency` finding and a recommended action to move `cms-core` to the same or an earlier stage.
