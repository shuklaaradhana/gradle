/*
 * Copyright 2010 the original author or authors.
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
package org.gradle.api.internal.tasks

import org.gradle.api.Action
import org.gradle.api.file.FileCollection
import org.gradle.api.internal.TaskExecutionHistory
import org.gradle.api.internal.TaskInternal
import org.gradle.api.internal.file.FileResolver
import org.gradle.util.UsesNativeServices
import spock.lang.Specification

import java.util.concurrent.Callable

@UsesNativeServices
class DefaultTaskOutputsTest extends Specification {

    private TaskMutator taskStatusNagger = Stub() {
        mutate(_, _) >> { String method, def action ->
            if (action instanceof Runnable) {
                action.run()
            } else if (action instanceof Callable) {
                action.call()
            }
        }
    }
    def task = Mock(TaskInternal) {
        getName() >> "task"
        toString() >> "task 'task'"
    }
    private final DefaultTaskOutputs outputs = new DefaultTaskOutputs({new File(it)} as FileResolver, task, taskStatusNagger)

    public void hasNoOutputsByDefault() {
        setup:
        assert outputs.files.files.isEmpty()
        assert !outputs.hasOutput
    }

    public void outputFileCollectionIsBuiltByTask() {
        setup:
        assert outputs.files.buildDependencies.getDependencies(task) == [task] as Set
    }

    def "can register output file"() {
        when: outputs.file("a")
        then:
        outputs.files.files.toList() == [new File('a')]
        outputs.fileProperties*.propertyName.toList() == ['$1']
        outputs.fileProperties*.propertyFiles*.files.flatten() == [new File("a")]
    }

    def "can register output file with property name"() {
        when: outputs.file("a").withPropertyName("prop")
        then:
        outputs.files.files.toList() == [new File('a')]
        outputs.fileProperties*.propertyName.toList() == ['prop']
        outputs.fileProperties*.propertyFiles*.files.flatten() == [new File("a")]
    }

    def "can register output dir"() {
        when: outputs.file("a")
        then:
        outputs.files.files.toList() == [new File('a')]
        outputs.fileProperties*.propertyName.toList() == ['$1']
        outputs.fileProperties*.propertyFiles*.files.flatten() == [new File("a")]
    }

    def "can register output dir with property name"() {
        when: outputs.dir("a").withPropertyName("prop")
        then:
        outputs.files.files.toList() == [new File('a')]
        outputs.fileProperties*.propertyName.toList() == ['prop']
        outputs.fileProperties*.propertyFiles*.files.flatten() == [new File("a")]
    }

    def "cannot register output file with same property name"() {
        outputs.file("a").withPropertyName("alma")
        outputs.file("b").withPropertyName("alma")
        when:
        outputs.fileProperties
        then:
        def ex = thrown IllegalArgumentException
        ex.message == "Multiple file properties with name 'alma'"
    }

    public void canRegisterOutputFiles() {
        when:
        outputs.file('a')

        then:
        outputs.files.files == [new File('a')] as Set
    }

    public void hasOutputsWhenEmptyOutputFilesRegistered() {
        when:
        outputs.files([])

        then:
        outputs.hasOutput
    }

    public void hasOutputsWhenNonEmptyOutputFilesRegistered() {
        when:
        outputs.file('a')

        then:
        outputs.hasOutput
    }

    public void hasOutputsWhenUpToDatePredicateRegistered() {
        when:
        outputs.upToDateWhen { false }

        then:
        outputs.hasOutput
    }

    public void canSpecifyUpToDatePredicateUsingClosure() {
        boolean upToDate = false

        when:
        outputs.upToDateWhen { upToDate }

        then:
        !outputs.upToDateSpec.isSatisfiedBy(task)

        when:
        upToDate = true

        then:
        outputs.upToDateSpec.isSatisfiedBy(task)
    }

    public void getPreviousFilesDelegatesToTaskHistory() {
        TaskExecutionHistory history = Mock()
        FileCollection outputFiles = Mock()

        setup:
        outputs.history = history

        when:
        def f = outputs.previousFiles

        then:
        f == outputFiles
        1 * history.outputFiles >> outputFiles
    }

    public void getPreviousFilesFailsWhenNoTaskHistoryAvailable() {
        when:
        outputs.previousFiles

        then:
        def e = thrown(IllegalStateException)
        e.message == 'Task history is currently not available for this task.'
    }

    def "configuration actions are delayed"() {
        def action = Mock(Action)
        when:
        outputs.configure(action)

        then:
        0 * action.execute(_)

        when:
        outputs.ensureConfigured()
        then:
        1 * action.execute(outputs)
    }

    def "configuration actions can call configure"() {
        def action = Mock(Action)
        when:
        outputs.configure {
            it.configure(action)
        }

        then:
        0 * action.execute(_)

        when:
        outputs.ensureConfigured()
        then:
        1 * action.execute(outputs)
    }
}
