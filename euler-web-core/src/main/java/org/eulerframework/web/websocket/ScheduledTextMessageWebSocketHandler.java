/*
 * Copyright 2013-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eulerframework.web.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class ScheduledTextMessageWebSocketHandler extends TextWebSocketHandler {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final static String SCHEDULE_ATTR = "SCHEDULE_ATTR";

    @Override
    public void afterConnectionEstablished(@Nonnull WebSocketSession session) throws Exception {
        try {
            Runnable task = this.createScheduleTask(session);
            this.schedule(session, task);
        } catch (Exception e) {
            this.logger.error(e.getMessage(), e);
            this.handleException(session, e);
            session.close(CloseStatus.NOT_ACCEPTABLE);
        }
    }

    @Override
    public void afterConnectionClosed(@Nonnull WebSocketSession session, @Nonnull CloseStatus status) throws Exception {
        this.logger.info("Web socket closed - {}", session.getId());
        this.cancelSchedule(session);
    }

    @Override
    public void handleTransportError(@Nonnull WebSocketSession session, @Nonnull Throwable exception) throws Exception {
        this.logger.error("Web socket transport error: {} - {}", exception.getMessage(), session.getId(), exception);
        this.cancelSchedule(session);
    }

    protected abstract void handleException(WebSocketSession session, Throwable exception);

    protected abstract Runnable createScheduleTask(WebSocketSession session) throws IOException;

    private void schedule(WebSocketSession session, Runnable task) {
        this.cancelSchedule(session);
        ScheduledExecutorService schedule = Executors.newScheduledThreadPool(1);
        session.getAttributes().put(SCHEDULE_ATTR, schedule);
        schedule.scheduleAtFixedRate(task, 1, 1, TimeUnit.SECONDS);
    }

    protected void cancelSchedule(WebSocketSession session) {
        session.getAttributes().computeIfPresent(SCHEDULE_ATTR, (key, existsSchedule) -> {
            if (existsSchedule instanceof ScheduledExecutorService) {
                this.logger.warn("Cancel existing schedule - {}", session.getId());
                ((ScheduledExecutorService) existsSchedule).shutdown();
            } else {
                this.logger.warn("Can not cancel existing schedule: unsupported schedule type: {} - {}", existsSchedule.getClass(), session.getId());
            }
            return null;
        });
    }
}
