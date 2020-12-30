FROM oracle/graalvm-ce:latest

MAINTAINER robin_dutertre

WORKDIR /app

COPY . .

RUN npm install express cors

EXPOSE 8000
EXPOSE 7000
CMD ./startup.sh
