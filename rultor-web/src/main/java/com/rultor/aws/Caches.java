/**
 * Copyright (c) 2009-2013, rultor.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the rultor.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.rultor.aws;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.jcabi.aspects.Loggable;
import com.jcabi.urn.URN;
import com.rultor.spi.Metricable;
import java.io.Flushable;
import java.io.IOException;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Mutable and thread-safe in-memory cache of S3 objects (singleton).
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@ToString
@EqualsAndHashCode
@Loggable(Loggable.DEBUG)
final class Caches implements Flushable, Metricable {

    /**
     * Instance of the singleton.
     */
    public static final Caches INSTANCE = new Caches();

    /**
     * All objects.
     */
    private final transient ConcurrentMap<Key, Cache> all =
        new ConcurrentSkipListMap<Key, Cache>();

    /**
     * Private ctor.
     */
    private Caches() {
        // it's a singleton
    }

    /**
     * Get cache by key.
     * @param key S3 key
     * @return Cache
     */
    public Cache get(final Key key) {
        this.all.putIfAbsent(key, new Cache(key));
        return this.all.get(key);
    }

    /**
     * Get a list of all keys available at the moment.
     * @param owner Owner
     * @param unit Unit
     * @return All keys
     */
    public SortedSet<Key> keys(final URN owner, final String unit) {
        final SortedSet<Key> keys = new TreeSet<Key>();
        for (Key key : this.all.keySet()) {
            if (key.belongsTo(owner, unit)) {
                keys.add(key);
            }
        }
        return keys;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void register(final MetricRegistry registry) {
        registry.register(
            MetricRegistry.name(this.getClass(), "keys-total"),
            new Gauge<Integer>() {
                @Override
                public Integer getValue() {
                    return Caches.this.all.size();
                }
            }
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void flush() throws IOException {
        for (Cache cache : this.all.values()) {
            cache.flush();
        }
    }

}