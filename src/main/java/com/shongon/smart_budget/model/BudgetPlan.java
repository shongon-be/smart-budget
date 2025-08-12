package com.shongon.smart_budget.model;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "budget_plans")
@CompoundIndex(name = "user_month_idx", def = "{'userId': 1, 'month': 1}")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BudgetPlan {
    @Id
    ObjectId id;
    ObjectId userId;
    String month;
    Double plannedAmount;
    LocalDateTime createdAt;
}
