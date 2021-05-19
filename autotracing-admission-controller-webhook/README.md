# Auto Tracing admission controller webhook

A kubernetes webhook to automatically instrument any java application with opentelemetry-java-instrumentation agent.
The webhook simply modifies the tagged deployment by adding the [opentelemetry-java-instrumentation](https://github.com/open-telemetry/opentelemetry-java-instrumentation) and setting up the environment correctly.

See also:
https://github.com/lucas-matt/auto-tracing-webhook
https://github.com/stackrox/admission-controller-webhook-demo

# Deploy

Build sidecar image `sidecar` folder`:
```shell
docker build --tag registry.hub.docker.com/stefvic/auto-tracing-sidecar:latest .
docker push registry.hub.docker.com/stefvic/auto-tracing-sidecar:latest
```

Generate Keys

```
mkdir keys
./generate-keys.sh keys
```

Check Certificate Expiry

```
openssl x509 -subject -enddate -noout -in keys/ca.crt
openssl x509 -subject -enddate -noout -in keys/webhook-server-tls.crt
```

Generate caBundle for webhook.yml

```
cat keys/ca.crt | base64 | tr -d "\n"
```

Add to webhook.yml

Build admission controller webhook image `auto-tracing-webhook` folder`:
```shell
docker build --tag registry.hub.docker.com/stefvic/auto-tracing-webhook:latest .
docker push registry.hub.docker.com/stefvic/auto-tracing-webhook:latest
```

Apply the webhook to kubernetes

```kubectl apply -f webhook.yaml```

Tag your namespace with the ```autotrace``` label:

```kubectl label namespace default autotrace=enabled```

Tag your deployment's pod template with the autotrace label:

```
spec:
  replicas: 1
  selector:
    matchLabels:
      app: service-a
  template:
    metadata:
      name: service-a
      labels:
        app: service-a
        autotrace: enabled
```

Now when you deploy your java app it should automatically instrument and begin tracing.

