# Kubernetes manifests

> **These manifests have not been deployed.** No cluster was available in the
> environment this work was done in: `kind` could not be downloaded (the egress
> proxy returned 403) and there is no other cluster to hand. They validate against
> the Kubernetes API schemas, and the same images and the same configuration run
> green under `docker compose`, but "it applies cleanly" is a weaker claim than
> "it runs", and this file is not going to pretend otherwise. See `docs/STATUS.md`.

## What is here

```
k8s/
  base/
    00-namespace.yaml        the ecommerce namespace
    01-configmap.yaml        non-secret config shared by the services
    02-secret.example.yaml   TEMPLATE: placeholders only, never apply as-is
  infra/
    10-catalog-mongo.yaml    MongoDB for catalog-service   (StatefulSet + PVC)
    11-redis.yaml            Redis cache                   (Deployment, no PVC)
    12-auth-postgres.yaml    PostgreSQL for auth-service   (StatefulSet + PVC)
    13-order-postgres.yaml   PostgreSQL for order-service  (StatefulSet + PVC)
    14-kafka.yaml            Kafka in KRaft mode           (StatefulSet + PVC)
  services/
    api-gateway.yaml         Deployment + Service
    catalog-service.yaml     Deployment + Service
    auth-service.yaml        Deployment + Service
    order-service.yaml       Deployment + Service
```

## Applying it

The Secret is a template with placeholder values. Create the real one out of band
so it never goes near Git:

```bash
kubectl apply -f k8s/base/00-namespace.yaml
```

```bash
kubectl -n ecommerce create secret generic ecommerce-secrets \
  --from-literal=JWT_SECRET="$(openssl rand -hex 32)" \
  --from-literal=CATALOG_MONGO_USERNAME=catalog \
  --from-literal=CATALOG_MONGO_PASSWORD="$(openssl rand -hex 16)" \
  --from-literal=REDIS_PASSWORD="$(openssl rand -hex 16)" \
  --from-literal=AUTH_POSTGRES_DB=authdb \
  --from-literal=AUTH_POSTGRES_USER=auth \
  --from-literal=AUTH_POSTGRES_PASSWORD="$(openssl rand -hex 16)" \
  --from-literal=ORDER_POSTGRES_DB=orderdb \
  --from-literal=ORDER_POSTGRES_USER=orders \
  --from-literal=ORDER_POSTGRES_PASSWORD="$(openssl rand -hex 16)"
```

```bash
kubectl apply -f k8s/base/01-configmap.yaml
```

```bash
kubectl apply -f k8s/infra/
```

```bash
kubectl apply -f k8s/services/
```

Set `GATEWAY_ALLOWED_ORIGINS` in the ConfigMap to the real frontend origin first.
It defaults to `https://example.invalid` so that a forgotten value fails closed
rather than opening the API to every origin.

## Images

The Deployments reference `ecommerce/<service>:latest` with
`imagePullPolicy: IfNotPresent`, which is what lets a locally built image be used
without a registry. With `kind`:

```bash
for s in api-gateway catalog-service auth-service order-service; do docker build -t "ecommerce/$s:latest" "services/$s"; done
```

```bash
for s in api-gateway catalog-service auth-service order-service; do kind load docker-image "ecommerce/$s:latest"; done
```

For a real cluster, push tagged images to a registry and change both the tag and
`imagePullPolicy: Always`. A floating `latest` tag means a rollout cannot be
reproduced or rolled back.

## Things worth knowing

**Three probes, three questions.** `startupProbe` answers "has it finished
booting", and it exists so that a JVM doing Flyway migrations is not killed
mid-start by the liveness probe and put in a restart loop. `livenessProbe` answers
"is it wedged", so it points at `/actuator/health/liveness`, which deliberately
excludes dependency checks: pointing liveness at readiness would restart a perfectly
healthy service every time its database blipped. `readinessProbe` answers "should
it get traffic", and that one does include dependencies, so a service whose
database is unreachable is removed from the Service endpoints instead of serving
errors.

**Only the gateway should be exposed.** Everything here is `ClusterIP`. Exposing
the gateway needs an Ingress or a `LoadBalancer`, which is intentionally not
included because it is cluster specific. catalog-service's `/internal/stock`
endpoint is reachable only from inside the cluster, which is the only thing
protecting it.

**Two replicas each is safe.** The services hold no local state. order-service's
outbox publisher claims rows with `FOR UPDATE SKIP LOCKED`, so two instances take
different rows rather than publishing the same event twice, and catalog-service's
two consumers share the partitions in one consumer group with an inbox table
guarding against a rebalance replaying an event.

**The data stores are single-replica pods, which is not production.** One Kafka
broker means a replication factor of one and no fault tolerance; one PostgreSQL pod
means an outage on reschedule. In a real deployment the databases would be managed
services and Kafka would be three brokers with `min.insync.replicas=2`, which is
what order-service's `acks=all` producer is already written to take advantage of.

**No HorizontalPodAutoscaler, PodDisruptionBudget or NetworkPolicy.** All three are
things this would need in production. They are left out rather than guessed at,
and listed in `docs/STATUS.md` as not done.

**Product images.** catalog-service reads them from `CATALOG_IMAGES_BASE_PATH`,
which under compose is a bind mount of `backend/Products`. There is no volume for
them here, so that endpoint returns 404 in Kubernetes until the images are either
baked into the image or put in object storage. The frontend serves product images
from its own `public/` directory, so nothing user-visible depends on it.
