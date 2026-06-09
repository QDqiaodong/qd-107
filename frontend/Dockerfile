ARG DOCKER_REGISTRY
ARG NODE_IMAGE
ARG NGINX_IMAGE

FROM ${DOCKER_REGISTRY}${NODE_IMAGE} AS builder

WORKDIR /app

COPY package*.json ./
COPY .npmrc ./

RUN npm config set registry https://mirrors.163.com/npm/ && \
    npm install

COPY . .

RUN npm run build

FROM ${DOCKER_REGISTRY}${NGINX_IMAGE}

COPY --from=builder /app/dist /usr/share/nginx/html

COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
