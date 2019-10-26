/*
 * Copyright 2019 the original author or authors.
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

package org.gradle.internal.vfs.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class AbstractNodeWithMutableChildren implements Node {
    private final ConcurrentHashMap<String, Node> children = new ConcurrentHashMap<>();

    @Override
    public Node getOrCreateChild(String name, Function<Node, Node> nodeSupplier) {
        return getOrCreateChild(name, nodeSupplier, this);
    }

    protected Node getOrCreateChild(String name, Function<Node, Node> nodeSupplier, Node parent) {
        return children.computeIfAbsent(name, key -> nodeSupplier.apply(parent));
    }

    @Override
    public Node replaceChild(String name, Function<Node, Node> nodeSupplier, Predicate<Node> shouldReplaceExisting) {
        return replaceChild(name, nodeSupplier, shouldReplaceExisting, this);
    }

    @Override
    public void removeChild(String name) {
        children.remove(name);
    }

    public Node replaceChild(String name, Function<Node, Node> nodeSupplier, Predicate<Node> shouldReplaceExisting, Node parent) {
        return children.compute(
            name,
            (key, current) -> (current == null || shouldReplaceExisting.test(current))
                ? nodeSupplier.apply(parent)
                : current
        );
    }

    @Override
    public synchronized void underLock(Runnable action) {
        action.run();
    }

    public Map<String, Node> getChildren() {
        return children;
    }
}
