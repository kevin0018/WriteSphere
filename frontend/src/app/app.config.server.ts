import { mergeApplicationConfig, ApplicationConfig } from '@angular/core';
import { provideServerRendering, withRoutes } from '@angular/ssr';
import { appConfig } from './app.config';
import { API_ORIGIN } from './shared/infrastructure/api';
import { serverRoutes } from './app.routes.server';

const serverConfig: ApplicationConfig = {
  providers: [
    provideServerRendering(withRoutes(serverRoutes)),
    {
      provide: API_ORIGIN,
      useFactory: () => new URL(process.env['WRITESPHERE_API_ORIGIN'] ?? 'http://127.0.0.1:8080').origin,
    },
  ]
};

export const config = mergeApplicationConfig(appConfig, serverConfig);
