package com.ioc

import com.google.common.truth.Truth
import com.google.testing.compile.JavaFileObjects
import com.google.testing.compile.JavaSourcesSubjectFactory
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by sergeygolishnikov on 03/09/2017.
 */
@RunWith(JUnit4::class)
class MethodInjection : BaseTest {
    @Test
    @Throws(Exception::class)
    fun findSetterMethod() {
        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Inject::class.java.import(),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   private DependencyModel dependency;",
            "",
            "   public void setDependency(DependencyModel dependency) {};",
            "   public DependencyModel getDependency() { return null; }",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "class DependencyModel {",
            "   public DependencyModel() {};",
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
            "       target.setDependency(new DependencyModel());",
            "   }",
            "}")

        Truth.assertAbout(JavaSourcesSubjectFactory.javaSources())
            .that(listOf(activityFile, dependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun targetAsParameterInConstructor() {
        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Inject::class.java.import(),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   private DependencyModel dependency;",
            "",
            "   public void setDependency(DependencyModel dependency) {};",
            "   public DependencyModel getDependency() { return null; }",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            Inject::class.java.import(),
            "",
            "public class DependencyModel {",
            "   @Inject",
            "   public DependencyModel(Activity activity) {}",
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
            "       target.setDependency(provideDependencyModel(target));",
            "   }",
            "",
            "   private static final DependencyModel provideDependencyModel(@NonNull final Activity target) {",
            "       DependencyModel dependencyModel = new DependencyModel(target);",
            "       return dependencyModel;",
            "   }",
            "}")

        Truth.assertAbout(JavaSourcesSubjectFactory.javaSources())
            .that(listOf(activityFile, dependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun injectWithTargetAndDependencyInConstructor() {
        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Inject::class.java.import(),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   private ParentDependency dependency;",
            "",
            "   public void setDependency(ParentDependency dependency) {};",
            "   public ParentDependency getDependency() { return null; };",
            "}")

        val parentDependencyFile = JavaFileObjects.forSourceLines("test.ParentDependency",
            "package test;",
            "",
            Inject::class.java.import(),
            "",
            "public class ParentDependency {",
            "   @Inject",
            "   public ParentDependency(Activity activity, DependencyModel dependency) {}",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            Inject::class.java.import(),
            "",
            "public class DependencyModel {",
            "   public DependencyModel() {}",
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
            "       target.setDependency(provideParentDependency(target));",
            "   }",
            "",
            "   private static final ParentDependency provideParentDependency(@NonNull final Activity target) {",
            "       DependencyModel dependencyModel = new DependencyModel();",
            "       ParentDependency parentDependency = new ParentDependency(target, dependencyModel);",
            "       return parentDependency;",
            "   }",
            "}")

        Truth.assertAbout(JavaSourcesSubjectFactory.javaSources())
            .that(listOf(activityFile, dependencyFile, parentDependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun injectWithDependencyInConstructor() {
        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Inject::class.java.import(),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   private ParentDependency dependency;",
            "",
            "   public void setDependency(ParentDependency dependency) {};",
            "   public ParentDependency getDependency() { return null; };",
            "}")

        val parentDependencyFile = JavaFileObjects.forSourceLines("test.ParentDependency",
            "package test;",
            "",
            Inject::class.java.import(),
            "",
            "public class ParentDependency {",
            "   @Inject",
            "   public ParentDependency(DependencyModel dependency) {}",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "public class DependencyModel {",
            "   public DependencyModel() {}",
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
            "       target.setDependency(provideParentDependency());",
            "   }",
            "",
            "   public static final ParentDependency provideParentDependency() {",
            "       DependencyModel dependencyModel = new DependencyModel();",
            "       ParentDependency parentDependency = new ParentDependency(dependencyModel);",
            "       return parentDependency;",
            "   }",
            "}")

        Truth.assertAbout(JavaSourcesSubjectFactory.javaSources())
            .that(listOf(activityFile, dependencyFile, parentDependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun preferConstructorWithArguments() {
        val parentActivityFile = JavaFileObjects.forSourceLines("test.ParentActivity",
            "package test;",
            "",
            "public class ParentActivity {",
            "",
            "}")

        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Inject::class.java.import(),
            "",
            "public class Activity extends ParentActivity {",
            "",
            "   @Inject",
            "   private ParentDependency dependency;",
            "   public void setDependency(ParentDependency dependency) {};",
            "   public ParentDependency getDependency() { return null; }",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            Inject::class.java.import(),
            "",
            "public class DependencyModel {",
            "",
            "   @Inject",
            "   public DependencyModel() {};",
            "}")

        val parentDependencyFile = JavaFileObjects.forSourceLines("test.ParentDependency",
            "package test;",
            "",
            Inject::class.java.import(),
            "",
            "public class ParentDependency {",
            "   @Inject",
            "   public ParentDependency(DependencyModel dependency) {}",
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
            "       target.setDependency(provideParentDependency());",
            "   }",
            "",
            "   public static final ParentDependency provideParentDependency() {",
            "       DependencyModel dependencyModel = new DependencyModel();",
            "       ParentDependency parentDependency = new ParentDependency(dependencyModel);",
            "       return parentDependency;",
            "   }",
            "}")

        Truth.assertAbout(JavaSourcesSubjectFactory.javaSources())
            .that(listOf(activityFile, dependencyFile, parentActivityFile, parentDependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun injectWithTargetInDependencyOfParentDependencyInConstructor() {
        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Inject::class.java.import(),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   private ParentDependency dependency;",
            "   public void setDependency(ParentDependency dependency) {};",
            "   public ParentDependency getDependency() { return null; };",
            "}")

        val parentDependencyFile = JavaFileObjects.forSourceLines("test.ParentDependency",
            "package test;",
            "",
            Inject::class.java.import(),
            "",
            "public class ParentDependency {",
            "   @Inject",
            "   public ParentDependency(DependencyModel dependency) {}",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            Inject::class.java.import(),
            "",
            "public class DependencyModel {",
            "   @Inject",
            "   public DependencyModel(Activity activity) {}",
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
            "       target.setDependency(provideParentDependency(target));",
            "   }",
            "",
            "   private static final ParentDependency provideParentDependency(@NonNull final Activity target) {",
            "       DependencyModel dependencyModel = new DependencyModel(target);",
            "       ParentDependency parentDependency = new ParentDependency(dependencyModel);",
            "       return parentDependency;",
            "   }",
            "}")

        Truth.assertAbout(JavaSourcesSubjectFactory.javaSources())
            .that(listOf(activityFile, dependencyFile, parentDependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }


    @Test
    @Throws(Exception::class)
    fun injectParent() {
        val parentFile = JavaFileObjects.forSourceLines("test.ParentActivity",
            "package test;",
            "",
            "public class ParentActivity extends BaseActivity {",
            "}")

        val baseFile = JavaFileObjects.forSourceLines("test.BaseActivity",
            "package test;",
            "",
            Inject::class.java.import(),
            "",
            "public class BaseActivity {",
            "",
            "   @Inject",
            "   DependencyModel dependencyModel;",
            "}")


        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Inject::class.java.import(),
            "",
            "public class Activity extends ParentActivity {",
            "",
            "   @Inject",
            "   public ParentDependency dependency;",
            "}")

        val parentDependencyFile = JavaFileObjects.forSourceLines("test.ParentDependency",
            "package test;",
            "",
            Inject::class.java.import(),
            "",
            "public class ParentDependency {",
            "   @Inject",
            "   public ParentDependency(DependencyModel dependency) {}",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            Inject::class.java.import(),
            Dependency::class.java.import(),
            "",
            "@Dependency",
            "public class DependencyModel {",
            "   @Inject",
            "   public DependencyModel(BaseActivity activity) {}",
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
            "       BaseActivityInjector.inject(target);",
            "       target.dependency = provideParentDependency(target);",
            "   }",
            "",
            "   private static final ParentDependency provideParentDependency(@NonNull final Activity target) {",
            "       DependencyModel dependencyModel = new DependencyModel(target);",
            "       ParentDependency parentDependency = new ParentDependency(dependencyModel);",
            "       return parentDependency;",
            "   }",
            "}")

        Truth.assertAbout(JavaSourcesSubjectFactory.javaSources())
            .that(listOf(activityFile, parentFile, baseFile, dependencyFile, parentDependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }


    @Test
    @Throws(Exception::class)
    fun injectParent2() {
        val parentFile = JavaFileObjects.forSourceLines("test.ParentActivity",
            "package test;",
            "",
            Inject::class.java.import(),
            "",
            "public class ParentActivity extends BaseActivity {",
            "",
            "   @Inject",
            "   Resources resources;",
            "}")

        val baseFile = JavaFileObjects.forSourceLines("test.BaseActivity",
            "package test;",
            "",
            Inject::class.java.import(),
            "",
            "public class BaseActivity {",
            "",
            "   @Inject",
            "   DependencyModel dependencyModel;",
            "}")


        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Inject::class.java.import(),
            "",
            "public class Activity extends ParentActivity {",
            "",
            "   @Inject",
            "   public ParentDependency dependency;",
            "}")

        val parentDependencyFile = JavaFileObjects.forSourceLines("test.ParentDependency",
            "package test;",
            "",
            Inject::class.java.import(),
            "",
            "public class ParentDependency {",
            "   @Inject",
            "   public ParentDependency() {}",
            "}")

        val resourcesFile = JavaFileObjects.forSourceLines("test.Resources",
            "package test;",
            "",
            Dependency::class.java.import(),
            "",
            "@Dependency",
            "public class Resources {",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            Inject::class.java.import(),
            Dependency::class.java.import(),
            "",
            "@Dependency",
            "public class DependencyModel {",
            "   @Inject",
            "   public DependencyModel() {}",
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
            "       ParentActivityInjector.inject(target);",
            "       target.dependency = new ParentDependency();",
            "   }",
            "}")

        Truth.assertAbout(JavaSourcesSubjectFactory.javaSources())
            .that(listOf(activityFile, resourcesFile, parentFile, baseFile, dependencyFile, parentDependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun injectParent3() {
        val parentFile = JavaFileObjects.forSourceLines("test.ParentActivity",
            "package test;",
            "",
            Inject::class.java.import(),
            "",
            "public class ParentActivity extends BaseActivity {",
            "",
            "   @Inject",
            "   Resources resources;",
            "}")

        val baseFile = JavaFileObjects.forSourceLines("test.BaseActivity",
            "package test;",
            "",
            Inject::class.java.import(),
            "",
            "public class BaseActivity {",
            "",
            "   @Inject",
            "   DependencyModel dependencyModel;",
            "}")


        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Inject::class.java.import(),
            "",
            "public class Activity extends ParentActivity {",
            "",
            "   @Inject",
            "   public ParentDependency dependency;",
            "}")

        val parentDependencyFile = JavaFileObjects.forSourceLines("test.ParentDependency",
            "package test;",
            "",
            Inject::class.java.import(),
            "",
            "public class ParentDependency {",
            "   @Inject",
            "   public ParentDependency() {}",
            "}")

        val resourcesFile = JavaFileObjects.forSourceLines("test.Resources",
            "package test;",
            "",
            Dependency::class.java.import(),
            "",
            "@Dependency",
            "public class Resources {",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            Inject::class.java.import(),
            Dependency::class.java.import(),
            "",
            "@Dependency",
            "public class DependencyModel {",
            "   @Inject",
            "   public DependencyModel() {}",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ParentActivityInjector",
            "package test;",
            "",
            "import $keep",
            "import $nonNull",
            "",
            "@Keep",
            "public final class ParentActivityInjector {",
            "   @Keep",
            "   public static final void inject(@NonNull final ParentActivity target) {",
            "       BaseActivityInjector.inject(target);",
            "       target.resources = new Resources();",
            "   }",
            "}")

        Truth.assertAbout(JavaSourcesSubjectFactory.javaSources())
            .that(listOf(activityFile, resourcesFile, parentFile, baseFile, dependencyFile, parentDependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun singletons() {

        val baseFile = JavaFileObjects.forSourceLines("test.BaseActivity",
            "package test;",
            "",
            "import $inject;",
            "",
            "public class BaseActivity {",
            "   @Inject",
            "   ParentDependency parentDependency;",
            "}")

        val singletonDependency = JavaFileObjects.forSourceLines("test.SingletonDependency",
            "package test;",
            "",
            Singleton::class.java.import(),
            "",
            "@Singleton",
            "public class SingletonDependency {",
            "}")

        val nextSingleton = JavaFileObjects.forSourceLines("test.NextSingleton",
            "package test;",
            "",
            Singleton::class.java.import(),
            "",
            "@Singleton",
            "public class NextSingleton {",
            "}")

        val parentDependencyFile = JavaFileObjects.forSourceLines("test.ParentDependency",
            "package test;",
            "",
            "",
            "public class ParentDependency {",
            "   public ParentDependency(SingletonDependency singletonDependency, NextSingleton nextSingleton, DependencyModel model, Resources resources) {}",
            "}")

        val resourcesFile = JavaFileObjects.forSourceLines("test.Resources",
            "package test;",
            "",
            "public class Resources {",
            "   public Resources(SingletonDependency singletonDependency) {}",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "public class DependencyModel {",
            "   public DependencyModel(SingletonDependency singletonDependency, NextSingleton nextSingleton) {}",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ParentActivityInjector",
            "package test;",
            "",
            "import $keep;",
            "import $nonNull;",
            "import $ioc;",
            "",
            "@Keep",
            "public final class BaseActivityInjector {",
            "   @Keep",
            "   public static final void inject(@NonNull final BaseActivity target) {",
            "       target.parentDependency = provideParentDependency();",
            "   }",
            "",
            "   public static final ParentDependency provideParentDependency() {",
            "       DependencyModel dependencyModel = new DependencyModel(Ioc.singleton(SingletonDependency.class), Ioc.singleton(NextSingleton.class));",
            "       Resources resources = new Resources(Ioc.singleton(SingletonDependency.class));",
            "       ParentDependency parentDependency = new ParentDependency(Ioc.singleton(SingletonDependency.class), Ioc.singleton(NextSingleton.class), dependencyModel, resources);",
            "       return parentDependency;",
            "   }",
            "}")

        Truth.assertAbout(JavaSourcesSubjectFactory.javaSources())
            .that(listOf(singletonDependency, nextSingleton, resourcesFile, baseFile, dependencyFile, parentDependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }
}