package com.ioc

import com.google.common.truth.Truth
import com.google.common.truth.Truth.assertAbout
import com.google.testing.compile.JavaFileObjects
import com.google.testing.compile.JavaSourcesSubject
import com.google.testing.compile.JavaSourcesSubjectFactory
import com.google.testing.compile.JavaSourcesSubjectFactory.javaSources
import com.ioc.Helpers.importType
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.util.*
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import javax.tools.JavaFileObject

/**
 * Created by sergeygolishnikov on 14/08/2017.
 */
@RunWith(JUnit4::class)
class SingletonNameTests : BaseTest {

    @Test
    @Throws(Exception::class)
    fun crashlitycs() {

        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Inject::class.java.import(),
            Named::class.java.import(),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public CrashlitycsService service;",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.CrashlitycsService",
            "package test;",
            "",
            "public interface CrashlitycsService {",
            "}")

        val release = JavaFileObjects.forSourceLines("test.CrashlyticsLogger",
            "package test;",
            "",
            Inject::class.java.import(),
            Singleton::class.java.import(),
            Dependency::class.java.import(),
            "",
            "@Dependency",
            "@Singleton",
            "public class CrashlyticsLogger implements CrashlitycsService {",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import $keep;",
            "import $nonNull;",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public static final void inject(@NonNull final Activity target) {",
            "     target.service = CrashlyticsLoggerSingleton.getInstance();",
            "   }",
            "}")

        Truth.assertAbout(javaSources())
            .that(listOf(activityFile, release, dependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun correctInjection6() {

        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importType(Inject::class.java),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public DependencyModel dependency;",
            "}")

        val moduleFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            importType(com.ioc.Dependency::class.java),
            importType(Singleton::class.java),
            importType(Inject::class.java),
            "",
            "@Dependency",
            "@Singleton",
            "public class DependencyModel {",
            "",
            "   @Inject",
            "   public DependencyModel(CrashlitycsService service) {}",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.CrashlitycsService",
            "package test;",
            "",
            "public interface CrashlitycsService {",
            "}")

        val release = JavaFileObjects.forSourceLines("test.CrashlyticsLogger",
            "package test;",
            "",
            Inject::class.java.import(),
            Singleton::class.java.import(),
            Dependency::class.java.import(),
            "",
            "@Dependency",
            "@Singleton",
            "public class CrashlyticsLogger implements CrashlitycsService {",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.DependencyModelSingleton",
            "package test;",
            "",
            "import $keep",
            "import $iocLazy",
            "",
            "@Keep",
            "public final class DependencyModelSingleton extends IocLazy<DependencyModel> {",
            "   private static DependencyModelSingleton instance;",
            "",
            "   public static final DependencyModel getInstance() {",
            "       if (instance == null) instance = new DependencyModelSingleton();",
            "       return instance.get();",
            "   }",
            "",
            "   protected final DependencyModel initialize() {",
            "       return new DependencyModel(CrashlyticsLoggerSingleton.getInstance());",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, dependencyFile, moduleFile, release))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun singletonAndArgumentsAsDependencyInActivityMethodInjection() {
        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            "import $inject;",
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   private ParentDependency dependency;",
            "   public void setDependency(ParentDependency dep) {};",
            "   public ParentDependency getDependency() { return  null; };",
            "}")

        val moduleFile = JavaFileObjects.forSourceLines("test.ModuleClass",
            "package test;",
            "",
            "import $dependency;",
            "import $singleton;",
            "",
            "public class ModuleClass {",
            "",
            "   @Dependency",
            "   public static ParentDependency getParentDependency(DependencyModel dependency, Dependency2 dependency2) { return new ParentDependency(); }",
            "   @Dependency",
            "   public static DependencyModel getDependency() { return new DependencyModel(); }",
            "   @Dependency @Singleton",
            "   public static Dependency2 getDependency2() { return new Dependency2(); }",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "public class DependencyModel {",
            "   public DependencyModel() {}",
            "}")

        val dependencyFile2 = JavaFileObjects.forSourceLines("test.Dependency2",
            "package test;",
            "",
            "public class Dependency2 {",
            "}")

        val parentDependencyFile = JavaFileObjects.forSourceLines("test.ParentDependency",
            "package test;",
            "",
            "public class ParentDependency {",
            "   public ParentDependency() {}",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import $keep;",
            "import $nonNull;",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public static final void inject(@NonNull final Activity target) {",
            "       target.setDependency(provideParentDependency());",
            "   }",
            "",
            "   public static final ParentDependency provideParentDependency() {",
            "       DependencyModel dependencyModel = ModuleClass.getDependency();",
            "       ParentDependency parentDependency = ModuleClass.getParentDependency(dependencyModel,Dependency2Singleton.getInstance());",
            "       return parentDependency;",
            "   }",
            "}")

        assertAbout(javaSources())
            .that(listOf(activityFile, moduleFile, dependencyFile, dependencyFile2, parentDependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun testInjectSingletone() {

        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Inject::class.java.import(),
            Named::class.java.import(),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public CrashlyticsLogger service;",
            "}")

        val release = JavaFileObjects.forSourceLines("test.CrashlyticsLogger",
            "package test;",
            "",
            Inject::class.java.import(),
            Singleton::class.java.import(),
            Dependency::class.java.import(),
            "",
            "@Dependency",
            "@Singleton",
            "public class CrashlyticsLogger {",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import $keep",
            "import $nonNull",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public static final void inject(@NonNull final Activity target) {",
            "     target.service = CrashlyticsLoggerSingleton.getInstance();",
            "   }",
            "}")

        Truth.assertAbout(javaSources())
            .that(listOf(activityFile, release))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }
}