package com.order.jobs;

import com.order.listener.OrderListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ScheduledOrderTask {

    private final OrderListener orderListener;

    public ScheduledOrderTask(OrderListener orderListener) {
        this.orderListener = orderListener;
    }

    @Scheduled(fixedRate = 30000)
    public void triggerOrderListener() {
        orderListener.receiveMessages();
    }
}