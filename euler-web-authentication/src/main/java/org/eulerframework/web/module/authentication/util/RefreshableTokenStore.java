/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eulerframework.web.module.authentication.util;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eulerframework.common.base.log.LogSupport;
import org.eulerframework.web.module.authentication.ExpireableToken;

/**
 * @author cFrost
 *
 */
public class RefreshableTokenStore<T> extends LogSupport {
    private ExpireableToken<T> token;
    private TokenGenerator<T> tokenGenerator;
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final static long REFRESH_TIME = 60 * 60 * 1000;
    
    public RefreshableTokenStore(TokenGenerator<T> tokenGenerator) {
        this.tokenGenerator = tokenGenerator;
    }
    
    public T getToken() {
        try {
            this.lock.readLock().lock();
            if(this.shouldRefresh(this.token)) {
                try {
                    this.lock.readLock().unlock();
                    this.lock.writeLock().lock();
                    this.lock.readLock().lock();
                    //Double check
                    if(this.shouldRefresh(this.token)) {
                        if(this.token == null) {
                            this.token = this.tokenGenerator.getToken();
                            this.logger.debug("Get a new token");
                        } else {
                            this.token = this.tokenGenerator.refreshToken(this.token);
                            this.logger.debug("Refresh token");
                        }
                    }
                } finally {
                    this.lock.writeLock().unlock();
                }
            }
            T readedToken = this.token.getToken();
            return readedToken;  
        } finally {
            this.lock.readLock().unlock();
        }
    }
    
    private boolean shouldRefresh(ExpireableToken<T> token) {
        return token == null || token.getExp() - new Date().getTime() < REFRESH_TIME;
    }

    public interface TokenGenerator<T> {
        ExpireableToken<T> getToken();
        
        ExpireableToken<T> refreshToken(ExpireableToken<T> existingToken);
    }
    
    public static void main(String[] args) {
        RefreshableTokenStore<String> store = new RefreshableTokenStore<>(new TokenGenerator<String>() {

            @Override
            public ExpireableToken<String> getToken() {
                System.out.println(Thread.currentThread().getName() + " get token");
                ExpireableToken<String> token = new ExpireableToken<>();
                token.setIat(new Date().getTime());
                token.setExp(new Date().getTime() + (10 + 60 * 60 ) * 1000);
                token.setToken(UUID.randomUUID().toString());
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return token;
            }

            @Override
            public ExpireableToken<String> refreshToken(ExpireableToken<String> existingToken) {
                // TODO Auto-generated method stub
                System.out.println(Thread.currentThread().getName() + " refresh token");
                ExpireableToken<String> token = new ExpireableToken<>();
                token.setIat(new Date().getTime());
                token.setExp(new Date().getTime() + (10 + 60 * 60 ) * 1000);
                token.setToken(UUID.randomUUID().toString());
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return token;
            }
            
        });
        
        Thread[] threads = new Thread[10];
        
        for(int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(String.valueOf(i)) {
                @Override
                public void run() {
                    while(true) {
                        System.out.println(Thread.currentThread().getName() + " read token: " + store.getToken());
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } 
                    }
                }
            };
            threads[i].start();
        }
    }
}
