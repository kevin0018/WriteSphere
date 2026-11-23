# WriteSphere

WriteSphere es una aplicación web para escribir, publicar y leer poemas y
microrrelatos en español. Incluye un editor con autoguardado y control de
versiones, una estantería personal, acceso con contraseña o passkey y
moderación de reportes.

## Arquitectura

| Componente | Implementación |
| --- | --- |
| [Frontend](frontend/) | Angular 22 con TypeScript, renderizado en servidor para las vistas públicas, Tailwind CSS y Spartan NG. |
| [Backend](backend/) | API REST con Spring Boot 4, Java 25, Spring Security y Spring Data JPA. |
| [Persistencia](database/database-schema.md) | PostgreSQL 18 y migraciones versionadas con Flyway. |

## Ejecución con Docker

Con Docker y Docker Compose instalados, ejecuta desde la raíz del repositorio:

```sh
docker compose up --build -d --wait
```

Abre [localhost:4000](http://localhost:4000). El volumen `postgres` conserva los
datos al detener el entorno con `docker compose down`. Si la API ya está en
marcha, puedes reconstruir solo el frontend con
`docker compose up -d --build --no-deps web`.

## Desarrollo local

Necesitas Node.js 26.13.1, pnpm 11.25.0, JDK 25 y Docker Compose. El Maven
Wrapper utiliza Maven 3.9.11.

Inicia PostgreSQL y la API desde la raíz:

```sh
docker compose up -d --wait db
cd backend
./mvnw spring-boot:run
```

En Windows, usa `mvnw.cmd`. La API escucha en el puerto 8080 y Flyway aplica
las migraciones al arrancar. Para usar otra instancia de PostgreSQL,
configura `WRITESPHERE_DB_URL`, `WRITESPHERE_DB_USER` y
`WRITESPHERE_DB_PASSWORD`.

En otra terminal, arranca el frontend:

```sh
cd frontend
pnpm install --frozen-lockfile
pnpm start
```

Abre [localhost:4200](http://localhost:4200) para desarrollar con recarga
automática. Para probar el renderizado en servidor, ejecuta `pnpm build` y
`pnpm serve:ssr:frontend` desde `frontend` y abre
[localhost:4000](http://localhost:4000).

## Pruebas

- **Frontend:** ejecuta `pnpm test --watch=false` desde `frontend`.
- **Backend:** con PostgreSQL en marcha, ejecuta `./mvnw test` desde `backend`.

## Despliegue

[compose.production.yaml](compose.production.yaml) define un entorno con
PostgreSQL en una red interna y credenciales separadas para administración,
migraciones y API. La [guía de operación](docs/operations.md) documenta los
secretos, el proxy HTTPS, las passkeys y la gestión de moderadores.
