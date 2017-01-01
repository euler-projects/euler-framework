/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015-2016 cFrost.sun(孙宾, SUN BIN)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * For more information, please visit the following website
 *
 * https://github.com/euler-form/web-form
 * http://eulerframework.net
 * http://cfrost.net
 */
package net.eulerframework.cache;

import java.util.Date;

/**
 * Created by cFrost on 16/10/17.
 */
public class DefaultObjectCache<KEY_T, DATA_T> extends AbstractObjectCache<KEY_T, DATA_T> {

    protected long dataLife;

    public void setDataLife(long dataLife) {
        this.dataLife = dataLife;
    }

    protected DefaultObjectCache() {
    }

    protected DefaultObjectCache(long dataLife) {
        this.dataLife = dataLife;
    }

    @Override
    public boolean isTimeout(DataStore<DATA_T> storedData) {
        if(storedData == null || new Date().getTime() - storedData.getAddTime() >= this.dataLife) {
            return true;
        }

        return false;
    }

    @Override
    public boolean isEnable() {
        return this.dataLife > 0 ? true : false;
    }
}
