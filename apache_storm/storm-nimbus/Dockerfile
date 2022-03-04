#FROM gradle:jdk8 AS TEMP_BUILD_IMAGE
#
#WORKDIR /home
#COPY code/build.gradle.kts code/gradle.properties code/settings.gradle.kts ./
#RUN gradle clean build --no-daemon > /dev/null 2>&1 || true
#
#COPY code/src ./code/
#RUN gradle clean build

FROM storm:2.3.0

ADD start-up.sh start-up.sh
ADD code/build/libs/storm-tutorial-2.3.0.jar .
RUN chmod 777 ./start-up.sh
#COPY --from=TEMP_BUILD_IMAGE /home/build/libs/$ARTIFACT_NAME .

ENTRYPOINT ./start-up.sh