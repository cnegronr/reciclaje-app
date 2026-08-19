# Multi-stage Dockerfile para Frontend React (Vite + Nginx - Compatibilidad ARM64 / x86_64)
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
ARG VITE_SHOW_TEST_CREDENTIALS=false
ARG VITE_ADMIN_INITIAL_EMAIL=admin@reciclajelitoral.cl
ARG VITE_ADMIN_INITIAL_PASSWORD=
ENV VITE_SHOW_TEST_CREDENTIALS=$VITE_SHOW_TEST_CREDENTIALS
ENV VITE_ADMIN_INITIAL_EMAIL=$VITE_ADMIN_INITIAL_EMAIL
ENV VITE_ADMIN_INITIAL_PASSWORD=$VITE_ADMIN_INITIAL_PASSWORD
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
