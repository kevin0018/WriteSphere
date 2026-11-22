# WriteSphere · propuesta de MVP

Alcance funcional acordado y encargo de implementación, 22 de noviembre de 2026. El esquema conceptual anterior recoge también ideas para futuras versiones. Este documento define la primera entrega.

## Experiencia de producto

WriteSphere es una publicación literaria abierta de poemas y microrrelatos en español. Cualquiera puede leer una obra completa desde un enlace. Una cuenta permite firmar obras con seudónimo, trabajar en privado y guardar lecturas para volver a ellas.

| Recorrido | Criterio de aceptación |
| --- | --- |
| Descubrir | La portada ofrece las últimas obras publicadas, paginadas por fecha de publicación e identificador, con filtro por género. Cada ficha muestra título, firma y un fragmento. |
| Leer | La página pública tiene URL permanente, título, firma enlazada al perfil y contenido servido en el HTML inicial. Conserva versos, líneas en blanco y párrafos. Un verso largo se ajusta en móvil sin generar desplazamiento horizontal. |
| Identificarse | Registro con correo privado, seudónimo único y contraseña. Inicio y cierre de sesión. El perfil público lista solo las obras publicadas de esa persona. |
| Escribir | El autor elige género, escribe en texto plano y alterna edición y vista previa. El borrador se guarda automáticamente en el servidor. La interfaz distingue guardando, guardado y error, conserva el texto tras un fallo y permite reintentar. |
| Publicar | Publicar exige título y cuerpo con contenido. El autor puede corregir la obra conservando su enlace o retirarla a borradores. Editar conserva la fecha de primera publicación. Una edición desde otra pestaña avisa de conflicto y conserva la copia del editor. |
| Guardar | Un lector con sesión puede añadir o quitar una obra de su estantería privada. Repetir la operación es seguro. Una obra retirada desaparece de la estantería y del acceso público por enlace. |
| Moderar | Un lector con sesión puede reportar una obra indicando un motivo. Un moderador puede desestimar el reporte u ocultar la publicación, registrando decisión y fecha. El autor no puede volver a publicar una obra oculta por moderación. |

La primera entrega termina cuando dos cuentas pueden completar esos recorridos con persistencia real. Para una segunda entrega quedan comentarios, seguidores, notificaciones y colecciones públicas. La estantería permite comprobar si los lectores vuelven a una obra antes de introducir métricas públicas de popularidad.

## Stack propuesto

- Angular 22.x, CLI y SSR alineados. Node 26.x, TypeScript 6.0.x y npm con lockfile. Consultar la versión estable publicada y respetar los rangos de compatibilidad en el momento de instalar. [Matriz oficial](https://angular.dev/reference/versions)
- Decisión de frontend pendiente. Kevin ha identificado shadcn/ui como la biblioteca de referencia. Para usar sus componentes originales con SSR se propone React con Next.js y Tailwind. La alternativa para conservar Angular es Tailwind CSS con spartan/ui. Las versiones de Angular y el uso de Brain y Helm corresponden a esta segunda opción. [Instalación de shadcn/ui](https://ui.shadcn.com/docs/installation)
- Para la opción Angular con spartan/ui: Usar Brain para interacción y foco, e incorporar solo las piezas Helm necesarias. Empezar por el diálogo de retirada de una obra. Verificar sus dependencias declaradas, navegación por teclado e hidratación SSR antes de extender la biblioteca. [Instalación oficial](https://spartan.ng/documentation/installation)
- Spring Boot 4.2.0 como referencia estable consultada, Java 25 LTS y Maven Wrapper. Spring MVC, Security con sesión por cookie, CSRF y despliegue bajo el mismo origen. PostgreSQL con Flyway y Spring Data JPA. Resolver parches estables compatibles y fijarlos en el primer hito. [Spring Boot](https://docs.spring.io/spring-boot/) · [Requisitos](https://docs.spring.io/spring-boot/system-requirements.html)
- Estética editorial. Texto de lectura en serif, controles en sans serif, columna de lectura estrecha y vista previa con el mismo componente que la obra publicada. Probar con poemas de versos largos, estrofas de un verso y microrrelatos de varios párrafos.

## Arquitectura y datos

Un monolito modular y una base PostgreSQL. Desde el primer caso de uso, cada módulo separa dominio (reglas), aplicación (casos de uso y puertos) e infraestructura (HTTP y persistencia). El dominio se puede probar sin Spring ni JPA.

Aplicar CQRS dentro de la capa de aplicación. Los comandos expresan intenciones como `PublishWork` o `WithdrawWork`. Su manejador recibe la identidad autenticada, comprueba permisos y versión, aplica las reglas de dominio y persiste el cambio en una transacción. Las consultas, como `ListPublishedWorks` o `GetMyBookshelf`, devuelven proyecciones de solo lectura con los campos que necesita la pantalla. Los controladores delegan directamente en el manejador correspondiente.

Ambos caminos usan las mismas tablas y se ejecutan de forma síncrona. Tras confirmar una retirada, la siguiente consulta pública debe excluir la obra de la portada y de la estantería, y el enlace debe responder 404 a terceros. Cubrir ese recorrido con una prueba de integración. Un bus de mensajes, event sourcing y una segunda base de lectura quedan fuera del MVP.

| Módulo | Datos y reglas |
| --- | --- |
| Identidad | Cuenta, seudónimo y correo únicos normalizados, hash de contraseña y rol. El registro público solo crea autores/lectores. |
| Publicación | Obra con autor único, género, título, cuerpo, estado, versión y fechas. Centralizar la comprobación de visibilidad para portada, perfil, enlace y estantería. El editor usa control optimista de versión. |
| Biblioteca | Marcador privado con clave única por cuenta y obra. Sustituye la relación ambigua UserPost del diseño inicial. |
| Moderación | Reporte con lector, obra, motivo, estado y resolución. Resolver y ocultar en una transacción. |

El frontend se organiza en lectura pública, sesión, editor, estantería y moderación. SSR para portada, perfil y obra publicada. Las vistas privadas y el editor se cargan en cliente. La API comprueba permisos en cada operación y devuelve 404 a terceros por obras privadas o retiradas.

## Entrega para GPT-6.2 Sol

1. Actualizar herramientas y dependencias, registrar versiones exactas, verificar que el test de cabecera corresponde a la interfaz elegida y actualizar el README con instrucciones comprobadas. Preparar PostgreSQL local reproducible y migración inicial. Verificar instalación limpia, build SSR y arranque del backend.
2. Entregar registro, sesión, borrador y publicación de extremo a extremo. Probar persistencia tras reinicio, recuperación de errores de guardado y edición concurrente.
3. Completar portada, filtro, perfil, lectura y estantería. Comprobar HTML público con JavaScript desactivado y privacidad con dos cuentas.
4. Entregar moderación y retirada. Ejecutar pruebas de dominio, integración sobre PostgreSQL y un recorrido de navegador desde escritura hasta lectura y guardado. Verificar teclado y móvil, documentar el arranque y los comandos de pruebas.

Confirmar con Kevin la opción de frontend antes de instalar dependencias de interfaz. Si se elige Next.js, sustituir la línea Angular por las versiones estables compatibles de React, Next.js, Node y TypeScript en el primer hito. Usar commits locales por recorrido verificado y seguir las instrucciones del repositorio para publicar cambios.
