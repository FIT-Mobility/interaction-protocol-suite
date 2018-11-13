#!/usr/bin/env bash

# The following command stores the returned HTTP status code in a variable
response=$(curl --write-out %{http_code} --silent --output /dev/null http://127.0.0.1:8081/api/snapshots/archive)

if [ $response == "200" ]
then
  echo "Nothing had to be backed up. Leaving servers running."
  exit 0
fi

if [ $response == "201" ]
then
  echo "Projects were backed up. Stopping API and MongoDB server"
  # Make sure the changes of the backup script are actually persisted before stopping the servers
  sleep 1s
  sudo docker-compose stop api mongo

  # Create the backup dir (fail silently if it already does)
  mkdir backups > /dev/null

  echo "Backing up data incrementally"
  archivename=$(date +'%y-%m-%d_%H-%M')
  # Note: maybe use a different snapshotting mechanism that is FS-specific? BtrFS?
  tar --create --listed-incremental=backups/archive.snar --verbose --verbose --file="backups/${archivename}.tar" data

  echo "Restarting servers"
  sudo docker-compose up -d api mongo

  exit 0
fi

echo "Something went wrong, unexpected HTTP status code $response was returned!"
exit 1