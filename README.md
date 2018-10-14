[![Build Status](https://travis-ci.org/systelab/k8s-control-plane.svg?branch=master)](https://travis-ci.org/systelab/k8s-control-plane)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/7ce4e563c45b4d09a975d61bed7d5d50)](https://www.codacy.com/app/alfonsserra/k8s-control-plane?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=systelab/k8s-control-plane&amp;utm_campaign=Badge_Grade)
[![Known Vulnerabilities](https://snyk.io/test/github/systelab/k8s-control-plane/badge.svg?targetFile=pom.xml)](https://snyk.io/test/github/systelab/k8s-control-plane?targetFile=pom.xml)

#  Control Plane `k8s-control-plane`


## Getting Started

To get you started you can simply clone the `k8s-control-plane` repository and install the dependencies:

### Prerequisites

You need [git][git] to clone the `k8s-control-plane` repository.

You will need [Java™ SE Development Kit 8][jdk-download] and [Maven][maven].

### Clone `k8s-control-plane`

Clone the `k8s-control-plane` repository using git:

```bash
git clone https://github.com/systelab/k8s-control-plane.git
cd k8s-control-plane
```

### Install Dependencies

In order to install the dependencies and generate the Uber jar you must run:

```bash
mvn clean install
```

### Run

To launch the server, simply run with java -jar the generated jar file.

```bash
cd target
java -jar k8s-control-plane-1.0.jar
```

## API

You will find the swagger UI at http://localhost:8080/swagger-ui.html

## Docker

### Build docker image

There is an Automated Build Task in Docker Cloud in order to build the Docker Image. 
This task, triggers a new build with every git push to your source code repository to create a 'latest' image.
There is another build rule to trigger a new tag and create a 'version-x.y.z' image

You can always manually create the image with the following command:

```bash
docker build -t systelab/k8s-control-plane . 
```

### Run the container

```bash
docker run -p 8080:8080 systelab/k8s-control-plane
```

The app will be available at http://localhost:8080/swagger-ui.html

## Other configuration:

### User "system:serviceaccount:default:default" cannot get at the cluster scope

You should bind service account system:serviceaccount:default:default (which is the default account bound to Pod) with role cluster-admin, just create a yaml (named like fabric8-rbac.yaml) with following contents:

``` yaml
# NOTE: The service account `default:default` already exists in k8s cluster.
# You can create a new account following like this:
#---
#apiVersion: v1
#kind: ServiceAccount
#metadata:
#  name: <new-account-name>
#  namespace: <namespace>

---
apiVersion: rbac.authorization.k8s.io/v1beta1
kind: ClusterRoleBinding
metadata:
  name: fabric8-rbac
subjects:
  - kind: ServiceAccount
    # Reference to upper's `metadata.name`
    name: default
    # Reference to upper's `metadata.namespace`
    namespace: default
roleRef:
  kind: ClusterRole
  name: cluster-admin
  apiGroup: rbac.authorization.k8s.io
```
Then, apply it by running kubectl apply -f fabric8-rbac.yaml.

If you want unbind them, just run kubectl delete -f fabric8-rbac.yaml.


[git]: https://git-scm.com/
[sboot]: https://projects.spring.io/spring-boot/
[maven]: https://maven.apache.org/download.cgi
[jdk-download]: http://www.oracle.com/technetwork/java/javase/downloads
[JEE]: http://www.oracle.com/technetwork/java/javaee/tech/index.html
