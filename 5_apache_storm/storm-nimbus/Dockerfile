FROM storm:2.3.0

ADD start-up.sh start-up.sh
ADD code/build/libs/* .
RUN chmod 777 ./start-up.sh
#COPY --from=TEMP_BUILD_IMAGE /home/build/libs/$ARTIFACT_NAME .

ENTRYPOINT ./start-up.sh