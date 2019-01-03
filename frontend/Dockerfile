FROM node:8-alpine

WORKDIR /opt/frontend

RUN apk --no-cache --update add --virtual native-deps \
  git g++ gcc libgcc libstdc++ linux-headers make python
RUN yarn global add node-gyp

# Copy package files first to enable caching
COPY package.json yarn.lock /opt/
COPY shared-js /opt/shared-js
COPY proto /opt/proto
COPY proto-node /opt/proto-node
COPY y-leveldb /opt/y-leveldb

COPY frontend/package.json /opt/frontend

RUN yarn install

# Copy exactly the files needed to build the server to improve docker build caching
COPY frontend/*.js frontend/*.env* frontend/tsconfig.json frontend/tsconfig.lint.json frontend/tslint.json /opt/frontend/
COPY frontend/src /opt/frontend/src

ARG COMMIT_ID

RUN yarn build

FROM nginx:1-alpine

COPY --from=0 /opt/frontend/build /usr/share/nginx/html/

# The path inside the container _must not_ be changed because we
# overwrite the default config
COPY nginx.conf /etc/nginx/conf.d/default.conf