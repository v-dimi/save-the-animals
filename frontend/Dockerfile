# build the static bundle
FROM node:24-alpine AS build

WORKDIR /app

# Copied first so a dependency install is only redone when the manifests change.
COPY package.json package-lock.json ./
RUN npm ci

COPY . .
RUN npm run build

# serve it, and act as the reverse proxy
FROM nginx:1.27-alpine

COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80
