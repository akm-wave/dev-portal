FROM node:20-alpine AS build
WORKDIR /app

# Cache dependencies layer
COPY package*.json ./
RUN npm ci --silent

# Build application
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
