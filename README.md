# Proyecto Gestión Franquicias Seti

## Introducción

Mono repositorio de APIs reactivas para la gestion de franquicias, sucursales y productos (con stock), construida con arquitectura hexagonal [(Scaffold Clean Architecture)](https://bancolombia.github.io/scaffold-clean-architecture/docs/intro)  sobre Spring Boot 3.5.4 + WebFlux + R2DBC.
Incluye documentación OpenAPI/Swagger, despliegue en AWS, CI/CD con Github Actions, y cobertura de pruebas con JaCoCo/PITest.

Actualmente puedes probar la api en el siguiente enlace: https://3ftdty3n33.us-east-1.awsapprunner.com/webjars/swagger-ui/index.html#/

## 📑 Índice
1. [📂 Estructura general del proyecto](#1-estructura-general-del-proyecto)  
2. [🛠️ Requisitos despliegue local](#2-requisitos)  
3. [⚙️ Configuración de entorno local](#3-configuración-de-entorno)
4. [🐳 Arranque local con Docker](#4-arranque-con-docker)
5. [🏛️ Diagrama de arquitectura](#5-diagrama-de-arquitectura)
6. [🗄️ Persistencia y modelo de datos](#6-persistencia-y-modelo-de-datos)
7. [📋 Endpoints principales](#7-endpoints-principales)
8. [💡 Decisiones técnicas y buenas practicas](#8-decisiones-técnicas)
9. [🧪 Testing y cobertura](#9-testing-y-cobertura)
10. [🚧 Propuestas de mejora](#10-propuestas-de-mejora)

<a id="1-estructura-general-del-proyecto"></a>
## 1. 📂 Estructura general del proyecto

```
├─ application
│  ├─ /main/config/                               #UseCasesConfig, ObjectMapperConfig
│  ├─ /main/resources/
│  │  ├─ application.yml                          # perfiles (local, cloud), CORS, R2DBC, Swagger
│  └─ build.gradle
├─ domain
│  ├─ model/
│  │  ├─ franquicia/                        #Entidad de dominio Franquicia, gateways
│  │  ├─ sucursal/                          #Entidad de dominio Sucursal, gateways
│  │  └─ producto/                          #Entidad de dominio Producto y ProductoTopPorSucursal, vista gateways
│  └─ usecase/                                    # Casos de uso (Mono/Flux, sin framework)
│     └─ support/Validations.java                 # Utilidades normalizeNombre, normalizeStock, etc.
└─ infrastructure/                                # Implementa puertos + expone endpoints
│  ├─ entry-points/
│  │  └─ reactive-web/
│  │     ├─ config/ # orígenes, headers y métodos permitidos, mapeo 400/404/409/500 + JSON de error
│  │     ├─ dto/   # request, response
│  │     ├─ handlers/                             # Lógica WebFlux (funcional) por agregado
│  │     ├─ routers/                              # Routers + @RouterOperation (Swagger)
│  │     └─ support/                              # Utilidades Web, readRequiredBody, createdJson, selectOnErrorResponse, mapToResponse, validateLongId etc
│  └─ driven-adapters/
│     └─ r2dbc/                                   # PostgreSQL (reactivo), Entidades de persistencia reactivas, ReactiveCrudRepository, lecturas especializadas (vista SQL)
│        ├─ franquicia/
│        ├─ sucursal/
│        └─ producto/
│           └─ view/                             # vistas
# build.gradle (root) + settings.gradle
```
<a id="2-requisitos"></a>
## 2. 🛠️ Requisitos despliegue local
- **Git**
- **Docker** (versión ≥ 20.10)  
- **Docker Compose** (versión ≥ 1.29)  
- **Opcional** (local): cliente `psql` o cualquier GUI de PostgreSQL, **Java 21**  

<a id="3-configuración-de-entorno"></a>
## 3. ⚙️ Configuración del entorno local
1. **Clona el repositorio**  
   ```bash
   git clone https://github.com/JulianDGP/Seti-Franquicias.git
   ```
2. **Asegurate de estar en la carpeta raiz correcta del repositorio**  
    ```bash
   cd Seti-Franquicias
   ```
3. **Crea tu .env (puedes copiar orenombrar el .env-example del repo)**
   ```bash
   DB_HOST=localhost
   DB_PORT=5432
   DB_USERNAME=postgres
   DB_PASSWORD=postgres
   ```

<a id="4-arranque-con-docker"></a>
## 4. 🐳 Arranque local con Docker
En construccion...

<a id="5-diagrama-de-arquitectura"></a>
## 5.🏛️ Diagrama de infraestructura

<img width="804" height="476" alt="image" src="https://github.com/user-attachments/assets/ab82b85e-3668-4ff2-86aa-33e3a914daf9" />

<a id="6-persistencia-y-modelo-de-datos"></a>
## 6.🗄️Persistencia y modelo de datos
- **Diagrama de base de datos**

<img width="793" height="496" alt="image" src="https://github.com/user-attachments/assets/7e36d5c2-05aa-4f85-a977-8d89244245cb" />

- **PostgreSQL (R2DBC)**
- Tablas: `franquicia`, `sucursal`, `producto` con claves foráneas y restricciones de unicidad contextuales.
- **Vista** `dbo.v_producto_max_stock_por_sucursal` para obtener el producto de mayor stock por sucursal (desempate por `nombre`):
  - Menos round-trips e I/O
  - El back recibe un **Flux** de filas pre-agregadas (menos heap)
  - Evita N+1

<a id="7-endpoints-principales"></a>
## 7.📋 Endpoints principales
<img width="989" height="476" alt="image" src="https://github.com/user-attachments/assets/d133e5b7-4457-47e4-a057-ea84517982c2" />

<a id="8-decisiones-técnicas"></a>
## 8.💡 Decisiones técnicas y buenas practicas
- **Records** en modelos/DTOs (menos boilerplate).
- **Encadenamiento reactivo** con map/flatMap/switchIfEmpty/zipWith/then, WebFlux + R2DBC para IO no bloqueante.
- **SOLID + DRY**  nombres claros, métodos pequeños, utilidades comunes para evitar duplicacion.
- **Logging (SLF4J)** con contexto: `doOnSubscribe`, `doOnSuccess`, `doOnError`.
- **Swagger/OpenAPI** con `@RouterOperation` sobre routers funcionales.
- **Sonar** reducción al minimo de code smells/bugs.
- **Vista SQL** para el endpoint de top por sucursal ⇒ reduce complejidad en el back y el costo de IO
- **Gitflow + CI/CD 🔁 **:
  - Ramas claras y pipelines automáticos.
  - Imagen Docker generada en CI y publicada en ECR.
  - Despliegue automático en AWS App Runner (ver pipeline).

<a id="9-testing-y-cobertura"></a>
## 9.🧪 Testing y cobertura

Se alcanzó una cobertura del 100% en las pruebas unitarias de los casos de uso.

<img width="882" height="494" alt="image" src="https://github.com/user-attachments/assets/b5973a9f-1515-4e80-aa2c-e7d12c4558a7" />

<a id="10-propuestas-de-mejora"></a>
## 10. 🚧 Propuestas de mejora
