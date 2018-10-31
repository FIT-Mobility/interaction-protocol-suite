# DiMo tool suite JavaScript backend

This is the DiMo tool suite backend server that handles user authentication, file storage and report generation. The current implementation is based on `docker-compose`.

## Starting the server

1. Adjust .env files in `./api`.
2. Run `docker-compose build && docker-compose up`. This will automatically start and link the database, web server, etc.
3. Navigate to http://localhost:8081 to view the page

## Production usage
Start the server with `docker-compose -f docker-compose.yml -f docker-compose.prod.yml build && docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d` instead.
This will merge the production config into the dev config and enable the SSL-serving frontend.
You may need to make sure your ssl key files match the expected names, see `./proxy/nginx.conf`.

## User Data

User-generated data is automatically saved in `./data`. E.g. mongodb writes to `./data/mongo` and users upload their files to `./data/uploads`.
This also means that while the containers can be created and destroyed at any time, `./data` must persist and must be secured.
