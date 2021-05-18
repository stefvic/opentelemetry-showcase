## How to
Install Loki stack
```shell
cd deploy
kubectl create ns loki
helm repo add grafana https://grafana.github.io/helm-charts
helm repo add prometheus https://prometheus-community.github.io/helm-charts
helm repo add elastic https://helm.elastic.co
helm repo update
helm  dependency update loki-stack
helm package loki-stack
helm upgrade --install loki loki-stack/loki-stack-2.4.0.tgz --namespace loki -f loki-stack-overwrite.yaml
kubectl apply -n loki -f loki-grafana-mapping.yaml
```
Get Grafana password
```shell
kubectl get secret --namespace loki loki-grafana -o jsonpath="{.data.admin-password}" | base64 --decode ; echo
```
Install Tempo
```shell
helm upgrade --install tempo grafana/tempo-distributed --namespace loki -f tempo-distributed-overwrite.yaml
kubectl edit deployments.apps -n loki tempo-tempo-distributed-query-frontend
#   from         - --query.base-path=/
#   to           - --query.base-path=/jaeger
kubectl apply -n loki -f temp-jaeger-ui-mapping.yaml
```


## Build
```shell
./gradlew clean build
```
