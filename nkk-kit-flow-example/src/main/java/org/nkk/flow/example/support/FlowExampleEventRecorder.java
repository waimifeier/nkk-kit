package org.nkk.flow.example.support;

import org.nkk.flow.autoconfigure.NkkFlowInstanceEvent;
import org.nkk.flow.autoconfigure.NkkFlowTaskEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 示例事件记录器，便于通过接口查看 starter 发布的 Spring 事件。
 */
@Component
public class FlowExampleEventRecorder {

    private static final int MAX_SIZE = 100;

    private final List<Map<String, Object>> events = Collections.synchronizedList(new ArrayList<Map<String, Object>>());

    @EventListener
    public void onTaskEvent(NkkFlowTaskEvent event) {
        Map<String, Object> data = base("task", event.getEventType().name());
        if (event.getTask() != null) {
            data.put("taskId", event.getTask().getId());
            data.put("instanceId", event.getTask().getInstanceId());
            data.put("taskKey", event.getTask().getTaskKey());
            data.put("taskName", event.getTask().getTaskName());
        }
        data.put("actorCount", event.getActors() == null ? 0 : event.getActors().size());
        add(data);
    }

    @EventListener
    public void onInstanceEvent(NkkFlowInstanceEvent event) {
        Map<String, Object> data = base("instance", event.getEventType().name());
        if (event.getInstance() != null) {
            data.put("instanceId", event.getInstance().getId());
            data.put("processId", event.getInstance().getProcessId());
            data.put("currentNodeKey", event.getInstance().getCurrentNodeKey());
            data.put("currentNodeName", event.getInstance().getCurrentNodeName());
        }
        add(data);
    }

    public List<Map<String, Object>> list() {
        synchronized (events) {
            return new ArrayList<>(events);
        }
    }

    public void clear() {
        events.clear();
    }

    private Map<String, Object> base(String category, String eventType) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("time", new Date());
        data.put("category", category);
        data.put("eventType", eventType);
        return data;
    }

    private void add(Map<String, Object> data) {
        synchronized (events) {
            events.add(data);
            while (events.size() > MAX_SIZE) {
                events.remove(0);
            }
        }
    }
}

