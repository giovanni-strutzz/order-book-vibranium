# Read Me First
The following was discovered as part of building this project:

* The original package name 'com.br.strutz.order-book' is invalid and this project uses 'com.br.strutz.order_book' instead.

# Getting Started

### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https:
* [Spring Boot Maven Plugin Reference Guide](https:
* [Create an OCI image](https:
* [Spring Web](https:
* [Spring Security](https:
* [Spring for Apache Kafka](https:
* [Apache Kafka Streams Support](https:
* [Apache Kafka Streams Binding Capabilities of Spring Cloud Stream](https:
* [Spring Data Redis (Access+Driver)](https:
* [Spring Data Reactive Redis](https:
* [MongoDB](https:

### Guides
The following guides illustrate how to use some features concretely:

* [Building a RESTful Web Service](https:
* [Serving Web Content with Spring MVC](https:
* [Building REST services with Spring](https:
* [Securing a Web Application](https:
* [Spring Boot and OAuth2](https:
* [Authenticating a User with LDAP](https:
* [Samples for using Apache Kafka Streams with Spring Cloud stream](https:
* [Messaging with Redis](https:
* [Messaging with Redis](https:

### Maven Parent overrides

Due to Maven's design, elements are inherited from the parent POM to the project POM.
While most of the inheritance is fine, it also inherits unwanted elements like `<license>` and `<developers>` from the parent.
To prevent this, the project POM contains empty overrides for these elements.
If you manually switch to a different parent and actually want the inheritance, you need to remove those overrides.

