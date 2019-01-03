FROM node:8-alpine
WORKDIR /opt/server-js

RUN apk --no-cache --update add --virtual native-deps \
  git g++ gcc libgcc libstdc++ linux-headers make python

# Copy package files first to enable caching
COPY package.json yarn.lock /opt/
COPY shared-js /opt/shared-js
COPY proto /opt/proto
COPY proto-node /opt/proto-node
COPY y-leveldb /opt/y-leveldb

COPY server-js/package.json /opt/server-js/

RUN yarn install

COPY server-js/tsconfig.json server-js/tsconfig.lint.json server-js/tslint.json /opt/server-js/
COPY server-js/src /opt/server-js/src
RUN yarn build

# Use two-stage build for optimized build caching & output container size
FROM node:8-alpine
WORKDIR /opt/server

COPY --from=0 /opt/server-js/node_modules /opt/server/node_modules
COPY --from=0 /opt/server-js/build /opt/server/build
COPY server-js/.env server-js/.env.example /opt/server/

EXPOSE 8080
CMD node build/index.js
