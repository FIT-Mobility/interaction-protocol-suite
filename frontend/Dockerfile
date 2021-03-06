FROM node:10

WORKDIR /opt/frontend

#RUN apk --no-cache --update add --virtual native-deps \
#  git g++ gcc libgcc libstdc++ linux-headers make python
RUN yarn global add node-gyp

# Copy package files first to enable caching
COPY package.json yarn.lock /opt/
COPY shared-js /opt/shared-js
COPY proto /opt/proto
COPY y-leveldb /opt/y-leveldb

COPY frontend/package.json /opt/frontend/

# Installing material-design-icons comes with a excessively long unpacking phase due to a high number of files.
# On slower machines, this leads to wrong error messages concerning the network connection.
# Therefore, set the timeout to 10 min.
# see https://github.com/yarnpkg/yarn/issues/6221#issuecomment-412844630
RUN yarn install --network-timeout=600000

# Copy exactly the files needed to build the server to improve docker build caching
COPY frontend/*.js frontend/.env* frontend/tsconfig.json frontend/tsconfig.lint.json frontend/tslint.json /opt/frontend/
COPY frontend/src /opt/frontend/src

ARG COMMIT_ID

RUN yarn build

FROM nginx:1-alpine

COPY --from=0 /opt/frontend/build /usr/share/nginx/html/

# The path inside the container _must not_ be changed because we
# overwrite the default config
COPY frontend/nginx.conf /etc/nginx/conf.d/default.conf
