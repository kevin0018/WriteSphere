FROM node:26.13.1-bookworm-slim AS build
WORKDIR /app
RUN apt-get update && apt-get install -y --no-install-recommends curl ca-certificates \
    && rm -rf /var/lib/apt/lists/* \
    && mkdir -p /opt/pnpm \
    && curl -fsSL https://registry.npmjs.org/pnpm/-/pnpm-11.25.0.tgz -o /tmp/pnpm.tgz \
    && tar -xzf /tmp/pnpm.tgz -C /opt/pnpm --strip-components=1 \
    && rm /tmp/pnpm.tgz
COPY package.json pnpm-lock.yaml pnpm-workspace.yaml ./
RUN node /opt/pnpm/bin/pnpm.cjs install --frozen-lockfile
COPY . .
RUN node /opt/pnpm/bin/pnpm.cjs build

FROM node:26.13.1-bookworm-slim
WORKDIR /app
COPY --from=build /app/dist/frontend ./dist/frontend
USER node
EXPOSE 4000
CMD ["node", "dist/frontend/server/server.mjs"]
