# Test Environment

Evidence date: 2026-06-25, Asia/Shanghai.

## Host

| Item | Value |
| --- | --- |
| OS | Microsoft Windows 11 Home, 10.0.26200, 64-bit |
| CPU | Intel Core i7-14650HX |
| Memory | 34,075,353,088 bytes |
| Timezone | Asia/Shanghai |

## Local Toolchain

| Tool | Version |
| --- | --- |
| Java | Oracle JDK 21.0.9 |
| Maven | Apache Maven 3.8.9 |
| Node.js | v24.11.0 |
| npm | 11.6.1 |
| Docker | 28.5.1 |
| Docker Compose | v2.40.2-desktop.1 |
| JMeter | Apache JMeter 5.6.3 |
| Browser | Chrome Headless, desktop and 390x844 mobile viewport |

## Runtime Services

| Service | Image / version | Exposure | Notes |
| --- | --- | --- | --- |
| MySQL | mysql:8.0.33 | 127.0.0.1:3307 | healthy, volume `mysql-data` |
| Redis | redis:7.2.4 | 127.0.0.1:6379 | healthy, password enabled, volume `redis-data` |
| Backend | lifelong-learning-credit-bank-backend | 127.0.0.1:8080 | non-root container user |
| Nginx | nginx:1.25.5 | 0.0.0.0:80 | API gateway used by performance tests |
| Jenkins | jenkins/jenkins:2.541.3-lts-jdk17 | 127.0.0.1:8081 | volume `jenkins-home` |

## Image Identities

| Image | Digest / ID |
| --- | --- |
| Backend image ID | sha256:5b80fcc5b2e9d9920e9433e7a5d68d9d4d7144ae498de51817288f646c90025f |
| Jenkins image | jenkins/jenkins@sha256:cce1eeb79902722f3cf7a8831c41e95c39f4a42b9a2981959630cc66539b3c2d |
| Nginx image | nginx:1.25.5@sha256:a484819eb60211f5299034ac80f6a681b06f89e65866ce91f356ed7c72af059c |
| Redis image | redis:7.2.4@sha256:5a93f6b2e391b78e8bd3f9e7e1e1e06aeb5295043b4703fb88392835cec924a0 |
| MySQL image | mysql:8.0.33, image ID ea68e51ffe9b |

## Data Scale At Final Audit

| Item | Count |
| --- | ---: |
| Users | 23 |
| QA users | 15 |
| Credit flows | 34 |
| Conversion transactions | 7 |
| Operation logs | 25,136 |
| Negative credit accounts | 0 |
| Duplicate certification outcomes | 0 |
| Duplicate conversion transactions | 0 |

The data scale is a local integration/performance baseline, not a production
capacity model.
