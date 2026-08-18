# Multi-stage Dockerfile para Frontend React (Vite + Nginx - Compatibilidad ARM64 / x86_64)
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
ARG VITE_SHOW_TEST_CREDENTIALS=false
ENV VITE_SHOW_TEST_CREDENTIALS=$VITE_SHOW_TEST_CREDENTIALS
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
