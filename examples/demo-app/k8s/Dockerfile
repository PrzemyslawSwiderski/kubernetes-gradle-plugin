FROM adoptopenjdk/openjdk11:alpine-jre-nightly
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

ARG MAIN_CLASS
RUN test -n "${MAIN_CLASS}"
ENV MAIN_CLASS_ENV=${MAIN_CLASS}


COPY BOOT-INF/lib /app/lib
COPY META-INF /app/META-INF
COPY BOOT-INF/classes /app

ENTRYPOINT java -cp "app:app/lib/*" $MAIN_CLASS_ENV