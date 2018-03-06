# Spring Boot 2.0 

springboot 2.0 sample originated from Brian Clozel's talk 'From Zero to Hero with Spring Boot' [github](https://github.com/bclozel/issues-dashboard)

## Changes

- spring-boot 2.0 GA release
- ssl keystore changed to PKCS12 format

WebSecurityConfiguration:
- user password encoder, replace deprecated User.withDefaultPasswordEncoder()
- PathRequest.toStaticResources replace StaticResourceRequest.toCommonLocations() 

RepositoryEvent:
- add new event type "review_requested"

application.properties:
- management config property change


## Notes
### H2 connection URL

```
jdbc:h2:mem:testdb
```

### Generate ssl keystore

```
keytool -genkey -alias client -storetype PKCS12 -keyalg RSA -keysize 2048 -keystore keystore.j12 -validity 3650
```

