FROM node:8-alpine

WORKDIR /opt/server

RUN apk add --no-cache git python make g++ && yarn global add node-gyp

# Copy package files first to enable caching
COPY package.json yarn.lock /opt/server/
RUN yarn

# Copy exactly the files needed to build the server to improve docker build caching
COPY *.js *.env* tsconfig.json tslint.json /opt/server/
COPY src /opt/server/src

ARG COMMIT_ID

RUN yarn build

FROM nginx:1-alpine

COPY --from=0 /opt/server/build /usr/share/nginx/html/

# The path inside the container _must not_ be changed because we
# overwrite the default config
COPY nginx.conf /etc/nginx/conf.d/default.conf