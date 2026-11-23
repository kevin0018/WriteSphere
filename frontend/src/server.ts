import {
  AngularNodeAppEngine,
  createNodeRequestHandler,
  isMainModule,
  writeResponseToNodeResponse,
} from '@angular/ssr/node';
import express from 'express';
import { request as httpRequest } from 'node:http';
import { request as httpsRequest } from 'node:https';
import { join } from 'node:path';

const browserDistFolder = join(import.meta.dirname, '../browser');

const app = express();
const angularApp = new AngularNodeAppEngine({ allowedHosts: ['localhost', '127.0.0.1'] });

const apiOrigin = new URL(process.env['WRITESPHERE_API_ORIGIN'] ?? 'http://127.0.0.1:8080');
const requestApi = apiOrigin.protocol === 'https:' ? httpsRequest : httpRequest;

app.use('/api', (req, res) => {
  const upstream = requestApi(apiOrigin, {
    path: req.originalUrl,
    method: req.method,
    headers: { ...req.headers, host: apiOrigin.host },
  }, (response) => {
    res.writeHead(response.statusCode ?? 502, response.headers);
    response.on('error', (error) => res.destroy(error));
    response.pipe(res);
  });
  upstream.setTimeout(15_000, () => upstream.destroy(new Error('API timeout')));
  upstream.on('error', (error) => {
    if (res.headersSent) {
      res.destroy(error);
    } else {
      res.status(502).json({ code: 'api_unavailable' });
    }
  });
  res.on('close', () => {
    if (!res.writableEnded) upstream.destroy();
  });
  req.pipe(upstream);
});

/**
 * Serve static files from /browser
 */
app.use(
  express.static(browserDistFolder, {
    maxAge: '1y',
    index: false,
    redirect: false,
  }),
);

/**
 * Handle all other requests by rendering the Angular application.
 */
app.use((req, res, next) => {
  angularApp
    .handle(req)
    .then((response) =>
      response ? writeResponseToNodeResponse(response, res) : next(),
    )
    .catch(next);
});

/**
 * Start the server if this module is the main entry point.
 * The server listens on the port defined by the `PORT` environment variable, or defaults to 4000.
 */
if (isMainModule(import.meta.url)) {
  const port = process.env['PORT'] || 4000;
  app.listen(port, (error) => {
    if (error) {
      throw error;
    }

    console.log(`Node Express server listening on http://localhost:${port}`);
  });
}

/**
 * Request handler used by the Angular CLI for development and builds.
 */
export const reqHandler = createNodeRequestHandler(app);
