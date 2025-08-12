package com.shongon.smart_budget.model;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "expenses")
@CompoundIndex(name = "user_date_idx", def = "{'userId': 1, 'date': -1}")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Expense {
    @Id
    ObjectId id;
    ObjectId userId;
    ObjectId categoryId;
    Double amount;
    String currency;
    String note;
    LocalDateTime date;
    LocalDateTime createdAt;
}
