package com.server.auth.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;

@Configuration
public class MongoConfig extends AbstractMongoClientConfiguration {

  @Value("${mongo.uri}")
  private String mongoUri;

  @Value("${mongo.dbName}")
  private String mongoDB;

  @Override
  public MongoClient mongoClient() {
    return MongoClients.create(this.mongoUri);
  }

  @Override
  protected String getDatabaseName() {
    return this.mongoDB;
  }
}
