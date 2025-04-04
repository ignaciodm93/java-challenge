# Java Challenge

Proyecto con Docker y Spring Boot para gestionar puntos de venta y costos de rutas, utilizando Redis como caché.

## Tecnologías Utilizadas

Spring Boot: Para la creación rápida de aplicaciones web y gestión de sus endpoints.  
Spring WebFlux: Para la programación reactiva y permitir alto flujo de solicitudes. Si bien en este proyecto no se refleja su diferencial por el contexto de prueba en el que está hecho, se desarrolló teniendo en cuenta esta característica como se solicita en el ejercicio.
Redis Template: Para el manejo de la caché de datos. Inicialmente se consideró Redisson pero terminé decantandome por RedisTemplate por su integración directa con Spring Data Redis y el enfoque más simple, permitiendo un control preciso de Redis sin la complejidad adicional de la primera opción. Adicionalmente, se intentaron usar algunas annotations para el manejo de la actualización de la cache pero fui obteniendo algunos errores que, por lo que estuve investigando, se debían a que no eran 100% no bloqueantes, por lo que también apoyó la idea de pasar a redis template que tiene un uso más manual y que me permitía revisar el flujo paso a paso. 
Docker y Docker compose: Para la contenerización y gestión de servicios.  
Maven: Para la gestión de dependencias.  
JUnit 5 y Mockito: Para las pruebas unitarias.  
SpringDoc (Swagger): Para la documentación de la API.  
Java 23: El lenguaje de programación utilizado.

--corregir tests luego de los cambios subidos
--ver si puedo pasarlo a postman