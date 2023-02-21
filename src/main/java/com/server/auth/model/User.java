package com.server.auth.model;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "User")
public class User {
  @Field("_id")
  @Id
  private String id = UUID.randomUUID().toString();

  private String mobileNumber;
  private Boolean isActive = true;
  private List<String> roles;
  private Date createdAt = new Date(System.currentTimeMillis());
}
