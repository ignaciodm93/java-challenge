# Java Challenge

Proyecto realizado con **Spring Boot** y **Docker**, orientado a la gestión de puntos de venta y costos de rutas, utilizando **Redis** como sistema de caché.

---

## Tecnologías Utilizadas

- **Java 23** – Lenguaje principal del proyecto.  
  Por mencionar algunas adiciones de esta versión, aunque no se usen en este proyecto:
    
    - *Scoped Values*: alternativa más segura y performante que ThreadLocal para pasar datos inmutables entre métodos y subprocesos, especialmente útil en aplicaciones concurrentes o reactivas.
    - *Structured Concurrency*: propone una forma moderna y más segura de manejar tareas concurrentes agrupándolas como una sola unidad de trabajo.
    - *Stream Gatherers*: extiende el API de Streams para permitir operaciones intermedias personalizadas más expresivas.
	
- **Spring Boot** – Creación y configuración rápida de aplicaciones web.  
- **Spring WebFlux** – Soporte para programación reactiva y alta concurrencia.  
   Aunque en este caso de prueba no se explotan a fondo las ventajas de WebFlux, el proyecto fue diseñado teniéndolas en cuenta, como requería el ejercicio.
- **RedisTemplate** – Manejo manual de caché con Redis.  
   - Inicialmente se evaluó **Redisson**, pero se optó por RedisTemplate por su mejor integración con **Spring Data Redis** y menor complejidad.
   - También se probaron anotaciones automáticas para cacheo, pero presentaron problemas relacionados con operaciones bloqueantes, lo cual motivó aún más el uso manual vía template para mantener el flujo reactivo.
- **Docker & Docker Compose** – Contenerización y gestión de servicios. Usados para ejecutar la app y sus servicios de forma aislada y fácil de levantar.
- **MongoDB** – Base de datos no relacional para persistencia de datos.  
- **Maven** – Herramienta para manejar las dependencias, compilar el proyecto y correr los tests. También se usa para empaquetar la aplicación y gestionarla durante el desarrollo.  
- **JUnit 5 & Mockito** – Frameworks para testing unitario. JUnit 5 se usa para escribir y ejecutar los tests, y Mockito para simular dependencias y probar cada parte por separado. Son útiles para asegurarse de que todo funciona como se espera.
- **SpringDoc (Swagger)** – Generación automática de documentación de la API REST.

---

## Consideraciones particulares para Ejecutar el Proyecto

### De forma local:

- Tener instalados:
  - **Redis**
  - **MongoDB**

### Con Docker:

- Tener instalado:
  - **Docker**
  
- Correr:

```bash
mvn clean compile
mvn clean package
docker-compose up --build
