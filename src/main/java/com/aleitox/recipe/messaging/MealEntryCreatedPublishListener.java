package com.aleitox.recipe.messaging;

import com.aleitox.recipe.messaging.event.MealEntryCreatedNotification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class MealEntryCreatedPublishListener {

    private final MealEntryEventPublisher mealEntryEventPublisher;

    public MealEntryCreatedPublishListener(MealEntryEventPublisher mealEntryEventPublisher) {
        this.mealEntryEventPublisher = mealEntryEventPublisher;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMealEntryCreated(MealEntryCreatedNotification notification) {
        mealEntryEventPublisher.publishMealEntryCreated(notification.mealEntryId());
    }
}
