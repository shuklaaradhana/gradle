/*
 * Copyright 2016 the original author or authors.
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

package org.gradle.tooling.internal.connection;

import org.gradle.tooling.connection.GradleConnectionBuilder;

import java.io.File;
import java.util.concurrent.TimeUnit;

public interface GradleConnectionBuilderInternal extends GradleConnectionBuilder {

    GradleConnectionBuilderInternal daemonMaxIdleTime(int timeoutValue, TimeUnit timeoutUnits);

    GradleConnectionBuilderInternal daemonBaseDir(File daemonBaseDir);

    GradleConnectionBuilderInternal integratedComposite(boolean integrated);

    // The following only have an effect with `integratedComposite(true)
    GradleConnectionBuilderInternal embedded(boolean embedded);

    GradleConnectionBuilder useInstallation(File gradleHome);

    GradleConnectionBuilder useGradleVersion(String version);

    GradleConnectionBuilderInternal useClasspathDistribution();
}
