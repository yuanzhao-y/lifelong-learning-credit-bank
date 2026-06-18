FROM mcr.microsoft.com/openjdk/jdk:17-ubuntu AS build

ARG REDIS_VERSION=7.2.4

RUN apt-get update \
    && apt-get install -y --no-install-recommends build-essential ca-certificates curl tcl \
    && rm -rf /var/lib/apt/lists/* \
    && curl -fsSL "https://download.redis.io/releases/redis-${REDIS_VERSION}.tar.gz" -o /tmp/redis.tar.gz \
    && mkdir -p /tmp/redis-src \
    && tar -xzf /tmp/redis.tar.gz -C /tmp/redis-src --strip-components=1 \
    && make -C /tmp/redis-src -j"$(nproc)" BUILD_TLS=no \
    && make -C /tmp/redis-src install PREFIX=/usr/local

FROM mcr.microsoft.com/openjdk/jdk:17-ubuntu

COPY --from=build /usr/local/bin/redis-server /usr/local/bin/redis-server
COPY --from=build /usr/local/bin/redis-cli /usr/local/bin/redis-cli

RUN mkdir -p /data
WORKDIR /data
EXPOSE 6379

CMD ["redis-server", "--appendonly", "yes", "--bind", "0.0.0.0", "--protected-mode", "no"]
