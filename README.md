# WriteSphere

Publicación abierta de poemas y microrrelatos en español. El alcance y los
criterios de aceptación están en [MVP.md](MVP.md).

## Arranque con Docker

Desde la raíz, ejecutar `docker compose up --build -d --wait` y abrir
[localhost:4000](http://localhost:4000). Compose conserva los datos en el volumen
`postgres`. Para detener los servicios, ejecutar `docker compose down`.

## Arranque local

Se necesitan Node.js 26.13.1 con pnpm 11.25.0, un JDK 25 y Docker Compose.
El proyecto usa Angular, CLI y SSR 22.3.0, TypeScript 6.0.3, Tailwind CSS 4.3.5,
spartan/ui Brain 1.10.0, Spring Boot 4.2.0 y PostgreSQL 18.7.
El Maven Wrapper fija Maven 3.9.11.

Desde la raíz, iniciar PostgreSQL y después la API.

```sh
docker compose up -d --wait db
cd backend
./mvnw spring-boot:run
```

En Windows se puede usar `mvnw.cmd`. Flyway aplica las migraciones al iniciar
la API en el puerto 8080. Para usar otra instancia de PostgreSQL, configurar
`WRITESPHERE_DB_URL`, `WRITESPHERE_DB_USER` y `WRITESPHERE_DB_PASSWORD`.
Las credenciales predeterminadas corresponden a la base local de Compose.

En otra terminal, instalar y compilar el frontend.

```sh
cd frontend
pnpm install --frozen-lockfile
pnpm build
pnpm serve:ssr:frontend
```

La aplicación SSR se abre en [localhost:4000](http://localhost:4000).
Para desarrollo con recarga automática, usar `pnpm start` y abrir
[localhost:4200](http://localhost:4200).

## Pruebas

Desde `frontend`, ejecutar `pnpm test --watch=false`. Desde `backend`,
con PostgreSQL en marcha, ejecutar `./mvnw test`.

## Moderación local

Registrar una cuenta con el correo elegido para moderación. Desde la raíz del
repositorio, ejecutar lo siguiente con el correo de esa cuenta.

```sh
docker compose exec db psql -U writesphere -d writesphere
```

```sql
UPDATE accounts SET role = 'MODERATOR' WHERE email = 'moderacion@example.org';
```

El correo se guarda normalizado en minúsculas. Salir de la sesión de la cuenta y
volver a entrar para acceder a **Moderación**. La cuenta puede consultar los
reportes pendientes y las resoluciones recientes.

Las pruebas de backend usan PostgreSQL y cubren el control de versiones, la
paginación con fechas empatadas, la retirada de una obra guardada y la resolución
de reportes. Cada prueba de integración crea y elimina sus propias cuentas y
obras.

## Entrar con passkey

En local, abrir [localhost:4000](http://localhost:4000), o
[localhost:4200](http://localhost:4200) con el servidor de desarrollo.
Tras iniciar sesión, abrir **Passkeys**, asignar un nombre y confirmar la
contraseña actual para añadir una. En los siguientes accesos, **Entrar con
passkey** funciona sin introducir correo ni contraseña. En **Passkeys** se
pueden retirar credenciales de la cuenta.

Para desplegar con HTTPS, configurar en el backend
`WRITESPHERE_PASSKEY_RP_ID` con el dominio público sin puerto y
`WRITESPHERE_PASSKEY_ORIGINS` con el origen HTTPS autorizado, incluyendo el
puerto si no es el predeterminado. La configuración acepta una lista de
orígenes separada por comas. Activar también `WRITESPHERE_SECURE_COOKIE=true`.
Las credenciales quedan asociadas al dominio con el que se registraron.
