# https://github.com/elastic/logstash-docker
FROM docker.elastic.co/logstash/logstash:7.2.1

COPY currentAvroSchema.avsc /tmp/schema.avsc
# Add your logstash plugins setup here
# Example: RUN logstash-plugin install logstash-filter-json
RUN logstash-plugin install logstash-input-log4j
