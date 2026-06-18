FROM mcr.microsoft.com/openjdk/jdk:17-ubuntu

ARG JENKINS_VERSION=2.440.1
ARG JENKINS_WAR_URL=https://get.jenkins.io/war-stable/${JENKINS_VERSION}/jenkins.war

ENV JENKINS_HOME=/var/jenkins_home
ENV JENKINS_SLAVE_AGENT_PORT=50000

RUN apt-get update \
    && apt-get install -y --no-install-recommends ca-certificates curl fontconfig git openssh-client tini \
    && rm -rf /var/lib/apt/lists/* \
    && useradd -m -d "${JENKINS_HOME}" -u 1000 jenkins \
    && mkdir -p /usr/share/jenkins/ref "${JENKINS_HOME}" \
    && curl -fsSL "${JENKINS_WAR_URL}" -o /usr/share/jenkins/jenkins.war \
    && chown -R jenkins:jenkins /usr/share/jenkins "${JENKINS_HOME}"

USER jenkins
WORKDIR ${JENKINS_HOME}
EXPOSE 8080 50000

ENTRYPOINT ["/usr/bin/tini", "--", "java", "-jar", "/usr/share/jenkins/jenkins.war"]
