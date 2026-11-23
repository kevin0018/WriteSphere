# Operación de WriteSphere

Consulta los requisitos y el arranque local en el [README del repositorio](../README.md).

## PostgreSQL en producción

Usar `compose.production.yaml` directamente. Crea un proyecto y un volumen
propios. PostgreSQL queda en una red interna de Docker y la web escucha en el
bucle local para un proxy inverso con HTTPS. Configurar el proxy para conservar
el encabezado `Host` público.

Preparar tres contraseñas distintas antes de inicializar el volumen. Los
archivos quedan legibles para los usuarios de los contenedores. En el host,
el acceso se controla con el directorio privado que los contiene.

```sh
export WRITESPHERE_SECRETS_DIR="$HOME/.config/writesphere/secrets"
install -d -m 0700 "$WRITESPHERE_SECRETS_DIR"
(
  set -eC
  umask 077
  for role in admin migrator api; do
    secret_file="$WRITESPHERE_SECRETS_DIR/db_${role}_password"
    openssl rand -hex 32 > "$secret_file"
    chmod 0444 "$secret_file"
  done
)

export WRITESPHERE_PUBLIC_HOST=escritura.example.org
export WRITESPHERE_PUBLIC_ORIGIN=https://escritura.example.org
docker compose -f compose.production.yaml up --build -d --wait
```

`writesphere_admin` inicializa PostgreSQL. El servicio temporal `migrate`
aplica Flyway como `writesphere_migrator` y concede por tabla los permisos de
`writesphere_api`. La API recibe solo su propia contraseña. Puede operar sobre
los datos del producto con los permisos de cada tabla. La creación de
cuentas usa el rol `MEMBER` asignado por PostgreSQL. El usuario de la API
carece de permisos para cambiar ese rol, administrar el esquema o consultar
el historial de Flyway.

Para conceder el rol de moderación, abrir `psql` como administrador con
`docker compose -f compose.production.yaml exec db psql -U writesphere_admin -d writesphere`
y ejecutar la actualización de rol de la sección de [moderación local](#moderación-local).

Los scripts de inicialización se ejecutan al crear un volumen vacío. Para
incorporar datos de una instalación anterior, restaurarlos en el volumen de
producción con los roles ya creados y conservar la propiedad de las tablas en
`writesphere_migrator`. Cambiar un archivo de secreto de un volumen existente
requiere rotar también la contraseña del rol en PostgreSQL.

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

## Inicio de sesión con passkeys

En local, abrir [localhost:4000](http://localhost:4000), o
[localhost:4200](http://localhost:4200) con el servidor de desarrollo.
Tras iniciar sesión, abrir **Passkeys**, asignar un nombre y confirmar la
contraseña actual para añadir una. En los siguientes accesos, **Usar una
passkey** permite iniciar sesión sin introducir correo ni contraseña. En
**Passkeys** se pueden retirar credenciales de la cuenta.

Para desplegar con HTTPS, configurar en el backend
`WRITESPHERE_PASSKEY_RP_ID` con el dominio público sin puerto y
`WRITESPHERE_PASSKEY_ORIGINS` con el origen HTTPS autorizado, incluyendo el
puerto si no es el predeterminado. La configuración acepta una lista de
orígenes separada por comas. Activar también `WRITESPHERE_SECURE_COOKIE=true`.
Las credenciales quedan asociadas al dominio con el que se registraron.
