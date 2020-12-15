ConfigME
========
A Kubernetes operator to load the configmap data from external services.
There are situations where services in a Kubernetes cluster may need data provided by different datasources 
like HTTP-API, files in a GitServer or kafka topic to name a few.

If the data is cacheable, mapping them to a ConfigMap would contribute to the resiliency of the service as it wouldn't be dependent to the availability of the other service.

## Alternatives:

### caching the data in every pod

Not to mention that while the external service is down your service can't scale or the pods being respawned. 

### A central Cache like Redis, Memcached

Well, this solution is already better than the previous one... 
but really? you want to spend your resoures to implement a thing identical to what K8s already provides?

## How to install the operator

The simplest way to install the Configme on your K8s cluster is running the following command:

**⚠ WARNING:** it will create a new namespace called **configme-operator** and deploy the operator in it. 
If it is not desirable, consider to edit the file according to your requirements.
```
kubectl apply -f https://raw.githubusercontent.com/psycho-ir/configme/master/crd/ConfigSource.yml
kubectl apply -f https://raw.githubusercontent.com/psycho-ir/configme/master/deployment/operator.yml 
```

## How to define ConfigSource

Configme introduces a new Custom Resource Definition in your cluster called **ConfigSource**. 

A ConfigSource defining a mapping from a http endpoint to a Configmap in your cluster will look like this: 

```
apiVersion: configme.javaworm.com/v1beta1
kind: ConfigSource
metadata:
  name: simple-http-config
  namespace: test-configme
spec:
  sourceType: http
  targetConfigMapName: todos-config
  sourceConfig: 
    url: https://jsonplaceholder.typicode.com/todos
    intervalSeconds: 10
```

The example above will call the url https://jsonplaceholder.typicode.com/todos every 10 seconds and update the ConfigMap called `todos-config` in namespace `test-configme`. 



 

## TODO

- [X] Handle http errors may happen
- [X] Native image build
- [x] Docker Image
- [ ] Different Authentications methods
  - [x] none
  - [x] bearer
  - [ ] basic
  - [ ] api-key
  - ...
- [ ] Schema support for fetched configuration
- [ ] How to use document


