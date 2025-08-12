package com.shongon.smart_budget.model;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "categories")
@CompoundIndex(name = "user_name_idx", def = "{'userId': 1, 'name': 1}")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Category {
    @Id
    ObjectId id;
    String name;
    String description;
    ObjectId userId;
}
