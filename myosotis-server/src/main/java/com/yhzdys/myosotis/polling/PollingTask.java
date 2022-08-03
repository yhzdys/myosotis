package com.yhzdys.myosotis.polling;

import com.yhzdys.myosotis.entity.MyosotisEvent;
import com.yhzdys.myosotis.entity.PollingData;
import com.yhzdys.myosotis.web.ResponseSerializer;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class PollingTask implements Callable<byte[]> {

    private final String id;
    private final PollingService pollingService;
    private final List<PollingData> pollingData;
    private final Object lock;

    public PollingTask(PollingService pollingService, List<PollingData> pollingData) {
        this.id = UUID.randomUUID().toString();
        this.pollingService = pollingService;
        this.pollingData = pollingData;
        this.lock = new Object();
    }

    public String getId() {
        return id;
    }

    public List<PollingData> getPollingData() {
        return pollingData;
    }

    @Override
    public byte[] call() throws Exception {
        List<MyosotisEvent> events = pollingService.pollingEvents(pollingData);
        if (CollectionUtils.isNotEmpty(events)) {
            return ResponseSerializer.events(events);
        }
        // wait for new events
        PollingSupport.register(this);
        synchronized (lock) {
            lock.wait(TimeUnit.SECONDS.toMillis(10));
        }
        PollingSupport.unregister(this);
        events = pollingService.pollingEvents(pollingData);
        return ResponseSerializer.events(events);
    }

    public void wakeUp() {
        synchronized (lock) {
            lock.notifyAll();
        }
    }
}
