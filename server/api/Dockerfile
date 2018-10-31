FROM node:8-alpine
WORKDIR /opt/server

RUN apk --no-cache --update add --virtual native-deps \
  git g++ gcc libgcc libstdc++ linux-headers make python

# Copy package files first to enable caching
COPY package.json yarn.lock /opt/server/
RUN yarn install

COPY tsconfig.json tslint.json /opt/server/
COPY src /opt/server/src
RUN yarn lint && yarn build

# Use two-stage build for optimized build caching & output container size
FROM node:8-alpine
WORKDIR /opt/server

COPY --from=0 /opt/server/node_modules /opt/server/node_modules
COPY --from=0 /opt/server/build /opt/server/build
COPY .env .env.example /opt/server/

EXPOSE 8080
CMD node build/index.js
