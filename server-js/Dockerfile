FROM node:10
WORKDIR /opt/server-js

#RUN apk --no-cache --update add --virtual native-deps \
#  git g++ gcc libgcc libstdc++ linux-headers make python

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

COPY server-js/.env server-js/.env.example /opt/server-js/

EXPOSE 8080
CMD node build/src/index.js
