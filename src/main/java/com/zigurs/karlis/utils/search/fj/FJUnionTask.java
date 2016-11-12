/*
 *                                     //
 * Copyright 2016 Karlis Zigurs (http://zigurs.com)
 *                                   //
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zigurs.karlis.utils.search.fj;

import com.zigurs.karlis.utils.search.ImmutableSet;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RecursiveTask;
import java.util.function.Function;

public class FJUnionTask<T> extends RecursiveTask<Map<T, Double>> {

    private final ImmutableSet<String> keywords;
    private final ConcurrentHashMap<T, Double> accumulator;
    private final Function<String, Map<T, Double>> supplierFunction;

    public FJUnionTask(final ImmutableSet<String> keywords,
                       final Function<String, Map<T, Double>> supplierFunction) {
        this(keywords, supplierFunction, new ConcurrentHashMap<>());
    }

    private FJUnionTask(final ImmutableSet<String> keywords,
                        final Function<String, Map<T, Double>> supplierFunction,
                        final ConcurrentHashMap<T, Double> accumulator) {
        this.keywords = keywords;
        this.supplierFunction = supplierFunction;
        this.accumulator = accumulator;
    }

    @Override
    protected Map<T, Double> compute() {
        if (keywords.size() == 1)
            return supplierFunction.apply(keywords.getSingleElement());

        ImmutableSet<String>[] splits = keywords.split();

        FJUnionTask<T> left = new FJUnionTask<>(splits[0], supplierFunction, accumulator);
        left.fork();
        FJUnionTask<T> right = new FJUnionTask<>(splits[1], supplierFunction, accumulator);
        right.fork();

        left.join().forEach((k, v) -> accumulator.merge(k, v, (d1, d2) -> d1 + d2));
        right.join().forEach((k, v) -> accumulator.merge(k, v, (d1, d2) -> d1 + d2));

        return accumulator;
    }
}