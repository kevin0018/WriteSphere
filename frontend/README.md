# Frontend de WriteSphere

Angular 22 sirve la portada, las obras y los perfiles públicos mediante SSR.
Las vistas de cuenta, editor, estantería y moderación se cargan en el cliente.

El frontend requiere Node.js 26.13.1 y pnpm 11.25.0. Ejecutar los comandos desde este directorio.

```sh
pnpm install --frozen-lockfile
pnpm test --watch=false
pnpm build
pnpm serve:ssr:frontend
```

La aplicación SSR está disponible en [localhost:4000](http://localhost:4000).
Para desarrollo con recarga automática, `pnpm start` sirve la aplicación en
[localhost:4200](http://localhost:4200). Ambos servidores usan la API local
por defecto. El servidor SSR admite `WRITESPHERE_API_ORIGIN` para apuntar a
otro origen de backend.

El [README del proyecto](../README.md) describe el arranque completo con
PostgreSQL y Docker Compose.
