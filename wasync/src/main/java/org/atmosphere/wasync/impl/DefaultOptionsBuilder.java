/*
 * Copyright 2017 Async-IO.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.atmosphere.wasync.impl;

import org.atmosphere.wasync.OptionsBuilder;

/**
 * Default implementation of the {@link org.atmosphere.wasync.OptionsBuilder}
 *
 * @author Jeanfrancois Arcand
 */
public class DefaultOptionsBuilder extends OptionsBuilder<DefaultOptions, DefaultOptionsBuilder> {

    public DefaultOptionsBuilder() {
        super(DefaultOptionsBuilder.class);
    }

    /**
     * Build an {@link org.atmosphere.wasync.Options}
     *
     * @return {@link org.atmosphere.wasync.Options}
     */
    @Override
    public DefaultOptions build() {
        return new DefaultOptions(this);
    }

}
