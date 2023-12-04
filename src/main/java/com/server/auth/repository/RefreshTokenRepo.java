package com.server.auth.repository;

import com.server.auth.model.RefreshToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface RefreshTokenRepo extends MongoRepository<RefreshToken, String> {
  @Query("{refreshToken: ?0}")
  RefreshToken findByRefreshToken(String refreshToken);

  @Query(value = "{refreshToken: ?0}", delete = true)
  Integer deleteByRefreshToken(String refreshToken);
}
