package com.ioc

import com.google.testing.compile.JavaFileObjects

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

import java.util.Arrays

import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import javax.tools.JavaFileObject

import com.google.common.truth.Truth.assertAbout
import com.google.testing.compile.JavaSourcesSubject
import com.google.testing.compile.JavaSourcesSubjectFactory.javaSources

/**
 * Created by sergeygolishnikov on 08/08/2017.
 */
@RunWith(JUnit4::class)
class ModuleTests {
    @Test
    @Throws(Exception::class)
    fun emptyParams() {
        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Helpers.importType(Inject::class.java),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public DependencyModel dependency;",
            "}")

        val moduleFile = JavaFileObjects.forSourceLines("test.ModuleClass",
            "package test;",
            "",
            Helpers.importType(Dependency::class.java),
            "",
            "public class ModuleClass {",
            "",
            "   @Dependency",
            "   public static DependencyModel getDependency() { return new DependencyModel(); }",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "public class DependencyModel {}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       target.dependency = injectDependencyModelInDependency();",
            "   }",
            "",
            "   private final DependencyModel injectDependencyModelInDependency() {",
            "       DependencyModel dependencyModel = ModuleClass.getDependency();",
            "       return dependencyModel;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(Arrays.asList<JavaFileObject>(activityFile, moduleFile, dependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun emptyParamsMethodInjection() {
        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Helpers.importType(Inject::class.java),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   private DependencyModel dependency;",
            "   public void setDependency(DependencyModel dependency) {};",
            "   public DependencyModel getDependency() { return null; }",
            "}")

        val moduleFile = JavaFileObjects.forSourceLines("test.ModuleClass",
            "package test;",
            "",
            Helpers.importType(Dependency::class.java),
            "",
            "public class ModuleClass {",
            "",
            "   @Dependency",
            "   public static DependencyModel getDependency() { return new DependencyModel(); }",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "public class DependencyModel {}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       target.setDependency(injectDependencyModelInDependency());",
            "   }",
            "",
            "   private final DependencyModel injectDependencyModelInDependency() {",
            "       DependencyModel dependencyModel = ModuleClass.getDependency();",
            "       return dependencyModel;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(Arrays.asList<JavaFileObject>(activityFile, moduleFile, dependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun targetAsParam() {
        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Helpers.importType(Inject::class.java),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public DependencyModel dependency;",
            "}")

        val moduleFile = JavaFileObjects.forSourceLines("test.ModuleClass",
            "package test;",
            "",
            Helpers.importType(Dependency::class.java),
            "",
            "public class ModuleClass {",
            "",
            "   @Dependency",
            "   public static DependencyModel getDependency(Activity activity) { return new DependencyModel(); }",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "public class DependencyModel {}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       target.dependency = injectDependencyModelInDependency(target);",
            "   }",
            "",
            "   private final DependencyModel injectDependencyModelInDependency(@NonNull final Activity target) {",
            "       DependencyModel dependencyModel = ModuleClass.getDependency(target);",
            "       return dependencyModel;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(Arrays.asList<JavaFileObject>(activityFile, moduleFile, dependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun targetAsParamMethodInjection() {
        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Helpers.importType(Inject::class.java),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   private DependencyModel dependency;",
            "   public void setDependency(DependencyModel dep) {};",
            "   public DependencyModel getDependency() { return null; }",
            "}")

        val moduleFile = JavaFileObjects.forSourceLines("test.ModuleClass",
            "package test;",
            "",
            Helpers.importType(Dependency::class.java),
            "",
            "public class ModuleClass {",
            "",
            "   @Dependency",
            "   public static DependencyModel getDependency(Activity activity) { return new DependencyModel(); }",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "public class DependencyModel {}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       target.setDependency(injectDependencyModelInDependency(target));",
            "   }",
            "",
            "   private final DependencyModel injectDependencyModelInDependency(@NonNull final Activity target) {",
            "       DependencyModel dependencyModel = ModuleClass.getDependency(target);",
            "       return dependencyModel;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(Arrays.asList<JavaFileObject>(activityFile, moduleFile, dependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun targetAndDependencyAsParam() {
        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Helpers.importType(Inject::class.java),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public ParentDependency dependency;",
            "}")

        val moduleFile = JavaFileObjects.forSourceLines("test.ModuleClass",
            "package test;",
            "",
            Helpers.importType(Dependency::class.java),
            "",
            "public class ModuleClass {",
            "",
            "   @Dependency",
            "   public static ParentDependency getDependency(Activity activity, DependencyModel dependency) { return new ParentDependency(); }",
            "}")

        val parentDependencyFile = JavaFileObjects.forSourceLines("test.ParentDependency",
            "package test;",
            "",
            "public class ParentDependency {",
            "   public ParentDependency() {}",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            Helpers.importType(Inject::class.java),
            "",
            "public class DependencyModel {",
            "   public DependencyModel() {}",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       target.dependency = injectParentDependencyInDependency(target);",
            "   }",
            "",
            "   private final ParentDependency injectParentDependencyInDependency(@NonNull final Activity target) {",
            "       DependencyModel dependencyModel = new DependencyModel();",
            "       ParentDependency parentDependency = ModuleClass.getDependency(target, dependencyModel);",
            "       return parentDependency;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(Arrays.asList<JavaFileObject>(activityFile, moduleFile, parentDependencyFile, dependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun targetAndDependencyAsParamMethodInjection() {
        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Helpers.importType(Inject::class.java),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   private ParentDependency dependency;",
            "   public void setDependency(ParentDependency dep) {};",
            "   public ParentDependency getDependency() { return null; }",
            "}")

        val moduleFile = JavaFileObjects.forSourceLines("test.ModuleClass",
            "package test;",
            "",
            Helpers.importType(Dependency::class.java),
            "",
            "public class ModuleClass {",
            "",
            "   @Dependency",
            "   public static ParentDependency getDependency(Activity activity, DependencyModel dependency) { return new ParentDependency(); }",
            "}")

        val parentDependencyFile = JavaFileObjects.forSourceLines("test.ParentDependency",
            "package test;",
            "",
            "public class ParentDependency {",
            "   public ParentDependency() {}",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            Helpers.importType(Inject::class.java),
            "",
            "public class DependencyModel {",
            "   public DependencyModel() {}",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       target.setDependency(injectParentDependencyInDependency(target));",
            "   }",
            "",
            "   private final ParentDependency injectParentDependencyInDependency(@NonNull final Activity target) {",
            "       DependencyModel dependencyModel = new DependencyModel();",
            "       ParentDependency parentDependency = ModuleClass.getDependency(target, dependencyModel);",
            "       return parentDependency;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(Arrays.asList<JavaFileObject>(activityFile, moduleFile, parentDependencyFile, dependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun TargetInDependencyOfParentDependencyAsParam() {
        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Helpers.importType(Inject::class.java),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public ParentDependency dependency;",
            "}")

        val moduleFile = JavaFileObjects.forSourceLines("test.ModuleClass",
            "package test;",
            "",
            Helpers.importType(Dependency::class.java),
            "",
            "public class ModuleClass {",
            "",
            "   @Dependency",
            "   public static ParentDependency getDependency(Activity activity, DependencyModel dependency) { return new ParentDependency(); }",
            "}")

        val parentDependencyFile = JavaFileObjects.forSourceLines("test.ParentDependency",
            "package test;",
            "",
            "public class ParentDependency {",
            "   public ParentDependency() {}",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            Helpers.importType(Inject::class.java),
            "",
            "public class DependencyModel {",
            "   @Inject",
            "   public DependencyModel(Activity activity) {}",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       target.dependency = injectParentDependencyInDependency(target);",
            "   }",
            "",
            "   private final ParentDependency injectParentDependencyInDependency(@NonNull final Activity target) {",
            "       DependencyModel dependencyModel = new DependencyModel(target);",
            "       ParentDependency parentDependency = ModuleClass.getDependency(target, dependencyModel);",
            "       return parentDependency;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(Arrays.asList<JavaFileObject>(activityFile, moduleFile, parentDependencyFile, dependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun TargetInDependencyOfParentDependencyAsParamMethodInjection() {
        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Helpers.importType(Inject::class.java),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   private ParentDependency dependency;",
            "   public void setDependency(ParentDependency dep) {};",
            "   public ParentDependency getDependency() { return null; }",
            "}")

        val moduleFile = JavaFileObjects.forSourceLines("test.ModuleClass",
            "package test;",
            "",
            Helpers.importType(Dependency::class.java),
            "",
            "public class ModuleClass {",
            "",
            "   @Dependency",
            "   public static ParentDependency getDependency(Activity activity, DependencyModel dependency) { return new ParentDependency(); }",
            "}")

        val parentDependencyFile = JavaFileObjects.forSourceLines("test.ParentDependency",
            "package test;",
            "",
            "public class ParentDependency {",
            "   public ParentDependency() {}",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            Helpers.importType(Inject::class.java),
            "",
            "public class DependencyModel {",
            "   @Inject",
            "   public DependencyModel(Activity activity) {}",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       target.setDependency(injectParentDependencyInDependency(target));",
            "   }",
            "",
            "   private final ParentDependency injectParentDependencyInDependency(@NonNull final Activity target) {",
            "       DependencyModel dependencyModel = new DependencyModel(target);",
            "       ParentDependency parentDependency = ModuleClass.getDependency(target, dependencyModel);",
            "       return parentDependency;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(Arrays.asList<JavaFileObject>(activityFile, moduleFile, parentDependencyFile, dependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun singletonAsDependency() {
        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Helpers.importType(Inject::class.java),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public ParentDependency dependency;",
            "}")

        val moduleFile = JavaFileObjects.forSourceLines("test.ModuleClass",
            "package test;",
            "",
            Helpers.importType(Dependency::class.java),
            Helpers.importType(Singleton::class.java),
            "",
            "public class ModuleClass {",
            "",
            "   @Dependency @Singleton",
            "   public static ParentDependency getDependency() { return new ParentDependency(); }",
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
            "import $keep",
            "import $nonNull",
            "import $ioc",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       target.dependency = Ioc.singleton(ParentDependency.class);",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, moduleFile, parentDependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun singletonAsDependencyMethodInjection() {
        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Helpers.importType(Inject::class.java),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   private ParentDependency dependency;",
            "   public void setDependency(ParentDependency dep) {};",
            "   public ParentDependency getDependency() { return null; }",
            "}")

        val moduleFile = JavaFileObjects.forSourceLines("test.ModuleClass",
            "package test;",
            "",
            Helpers.importType(Dependency::class.java),
            Helpers.importType(Singleton::class.java),
            "",
            "public class ModuleClass {",
            "",
            "   @Dependency @Singleton",
            "   public static ParentDependency getDependency() { return new ParentDependency(); }",
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
            "import $keep",
            "import $nonNull",
            "import $ioc",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       target.setDependency(Ioc.singleton(ParentDependency.class));",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, moduleFile, parentDependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun singletonWithArgumentsAsDependency() {
        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Helpers.importType(Inject::class.java),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public ParentDependency dependency;",
            "}")

        val moduleFile = JavaFileObjects.forSourceLines("test.ModuleClass",
            "package test;",
            "",
            Helpers.importType(Dependency::class.java),
            Helpers.importType(Singleton::class.java),
            "",
            "public class ModuleClass {",
            "",
            "   @Dependency @Singleton",
            "   public static ParentDependency getDependency(DependencyModel dependency) { return new ParentDependency(); }",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "public class DependencyModel {",
            "   public DependencyModel() {}",
            "}")

        val parentDependencyFile = JavaFileObjects.forSourceLines("test.ParentDependency",
            "package test;",
            "",
            "public class ParentDependency {",
            "   public ParentDependency() {}",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ParentDependencySingleton",
            "package test;",
            "",
            "import $keep;",
            "import $iocLazy;",
            "",
            "@Keep",
            "public final class ParentDependencySingleton extends IocLazy<ParentDependency> {",
            "   private static ParentDependencySingleton instance;",
            "",
            "   public static final ParentDependencySingleton getInstance() {",
            "       if (instance == null) instance = new ParentDependencySingleton();",
            "       return instance;",
            "   }",
            "",
            "   protected final ParentDependency initialize() {",
            "       DependencyModel dependencyModel = new DependencyModel();",
            "       return ModuleClass.getDependency(dependencyModel);",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, moduleFile, dependencyFile, parentDependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun singletonWithArgumentsAsDependencyInActivity() {
        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Helpers.importType(Inject::class.java),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public ParentDependency dependency;",
            "}")

        val moduleFile = JavaFileObjects.forSourceLines("test.ModuleClass",
            "package test;",
            "",
            Helpers.importType(Dependency::class.java),
            Helpers.importType(Singleton::class.java),
            "",
            "public class ModuleClass {",
            "",
            "   @Dependency @Singleton",
            "   public static ParentDependency getDependency(DependencyModel dependency) { return new ParentDependency(); }",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "public class DependencyModel {",
            "   public DependencyModel() {}",
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
            "import $keep",
            "import $nonNull",
            "import $ioc",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       target.dependency = Ioc.singleton(ParentDependency.class);",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, moduleFile, dependencyFile, parentDependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun singletonAndArgumentsAsDependencyInActivity() {
        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Helpers.importType(Inject::class.java),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public ParentDependency dependency;",
            "}")

        val moduleFile = JavaFileObjects.forSourceLines("test.ModuleClass",
            "package test;",
            "",
            Helpers.importType(Dependency::class.java),
            Helpers.importType(Singleton::class.java),
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
            "import $keep",
            "import $nonNull",
            "import $ioc",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       target.dependency = injectParentDependencyInDependency();",
            "   }",
            "",
            "   private final ParentDependency injectParentDependencyInDependency() {",
            "       DependencyModel dependencyModel = ModuleClass.getDependency();",
            "       ParentDependency parentDependency = ModuleClass.getParentDependency(dependencyModel, Ioc.singleton(Dependency2.class));",
            "       return parentDependency;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, moduleFile, dependencyFile, dependencyFile2, parentDependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun preferMethodOverEmptyConstructor() {
        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Helpers.importType(Inject::class.java),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public ParentDependency dependency;",
            "}")

        val moduleFile = JavaFileObjects.forSourceLines("test.ModuleClass",
            "package test;",
            "",
            Helpers.importType(Dependency::class.java),
            Helpers.importType(Singleton::class.java),
            "",
            "public class ModuleClass {",
            "",
            "   @Dependency",
            "   public static ParentDependency getParentDependency() { return new ParentDependency(); }",
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
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       target.dependency = injectParentDependencyInDependency();",
            "   }",
            "",
            "   private final ParentDependency injectParentDependencyInDependency() {",
            "       ParentDependency parentDependency = ModuleClass.getParentDependency();",
            "       return parentDependency;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(Arrays.asList<JavaFileObject>(activityFile, moduleFile, parentDependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun preferMethodOverEmptyConstructorInSingleton() {
        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Helpers.importType(Inject::class.java),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public ParentDependency dependency;",
            "}")

        val moduleFile = JavaFileObjects.forSourceLines("test.ModuleClass",
            "package test;",
            "",
            Helpers.importType(Dependency::class.java),
            Helpers.importType(Singleton::class.java),
            "",
            "public class ModuleClass {",
            "",
            "   @Singleton",
            "   @Dependency",
            "   public static ParentDependency getParentDependency(DependencyModel dependency) { return new ParentDependency(); }",
            "   @Dependency",
            "   public static DependencyModel dependency() { return null; }",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "public class DependencyModel {",
            "   public DependencyModel() {}",
            "}")

        val parentDependencyFile = JavaFileObjects.forSourceLines("test.ParentDependency",
            "package test;",
            "",
            "public class ParentDependency {",
            "   public ParentDependency() {}",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ParentDependencySingleton",
            "package test;",
            "",
            "import $keep",
            "import $iocLazy",
            "",
            "@Keep",
            "public final class ParentDependencySingleton extends IocLazy<ParentDependency> {",
            "   private static ParentDependencySingleton instance;",
            "",
            "   public static final ParentDependencySingleton getInstance() {",
            "       if (instance == null) instance = new ParentDependencySingleton();",
            "       return instance;",
            "   }",
            "",
            "   protected final ParentDependency initialize() {",
            "       DependencyModel dependencyModel = ModuleClass.dependency();",
            "       return ModuleClass.getParentDependency(dependencyModel);",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, moduleFile, dependencyFile, parentDependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun singletonFromClass() {
        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Helpers.importType(Inject::class.java),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public ParentDependency dependency;",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "public class DependencyModel {",
            "   public DependencyModel() {}",
            "}")

        val parentDependencyFile = JavaFileObjects.forSourceLines("test.ParentDependency",
            "package test;",
            "",
            Helpers.importType(Inject::class.java),
            Helpers.importType(Singleton::class.java),
            "",
            "@Singleton",
            "public class ParentDependency {",
            "   @Inject",
            "   public ParentDependency(DependencyModel dependency) {}",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ParentDependencySingleton",
            "package test;",
            "",
            "import $keep",
            "import $iocLazy",
            "",
            "@Keep",
            "public final class ParentDependencySingleton extends IocLazy<ParentDependency> {",
            "   private static ParentDependencySingleton instance;",
            "",
            "   public static final ParentDependencySingleton getInstance() {",
            "       if (instance == null) instance = new ParentDependencySingleton();",
            "       return instance;",
            "   }",
            "",
            "   protected final ParentDependency initialize() {",
            "       DependencyModel dependencyModel = new DependencyModel();",
            "       return new ParentDependency(dependencyModel);",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, dependencyFile, parentDependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun singletonFromClass2() {
        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Helpers.importType(Inject::class.java),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public ParentDependency dependency;",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "public class DependencyModel {",
            "   public DependencyModel() {}",
            "}")

        val parentDependencyFile = JavaFileObjects.forSourceLines("test.ParentDependency",
            "package test;",
            "",
            Helpers.importType(Inject::class.java),
            Helpers.importType(Singleton::class.java),
            "",
            "@Singleton",
            "public class ParentDependency {",
            "   @Inject",
            "   public ParentDependency(DependencyModel dependency) {}",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import $keep",
            "import $nonNull",
            "import $ioc",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       target.dependency = Ioc.singleton(ParentDependency.class);",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, dependencyFile, parentDependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun singletonFromClass2MethodInjectionSingleton() {
        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Helpers.importType(Inject::class.java),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   private ParentDependency dependency;",
            "   public void setDependency(ParentDependency dep) {};",
            "   public ParentDependency getDependency() { return null; }",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "public class DependencyModel {",
            "   public DependencyModel() {}",
            "}")

        val parentDependencyFile = JavaFileObjects.forSourceLines("test.ParentDependency",
            "package test;",
            "",
            Helpers.importType(Inject::class.java),
            Helpers.importType(Singleton::class.java),
            "",
            "@Singleton",
            "public class ParentDependency {",
            "   @Inject",
            "   public ParentDependency(DependencyModel dependency) {}",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ParentDependencySingleton",
            "package test;",
            "",
            "import $keep",
            "import $iocLazy",
            "",
            "@Keep",
            "public final class ParentDependencySingleton extends IocLazy<ParentDependency> {",
            "   private static ParentDependencySingleton instance;",
            "   public static final ParentDependencySingleton getInstance() {",
            "       if (instance == null) instance = new ParentDependencySingleton();",
            "       return instance;",
            "   }",
            "",
            "   protected final ParentDependency initialize() {",
            "       DependencyModel dependencyModel = new DependencyModel();",
            "       return new ParentDependency(dependencyModel);",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, dependencyFile, parentDependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun singletonFromClass2MethodInjection() {
        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Helpers.importType(Inject::class.java),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   private ParentDependency dependency;",
            "   public void setDependency(ParentDependency dep) {};",
            "   public ParentDependency getDependency() { return null; }",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "public class DependencyModel {",
            "   public DependencyModel() {}",
            "}")

        val parentDependencyFile = JavaFileObjects.forSourceLines("test.ParentDependency",
            "package test;",
            "",
            Helpers.importType(Inject::class.java),
            Helpers.importType(Singleton::class.java),
            "",
            "@Singleton",
            "public class ParentDependency {",
            "   @Inject",
            "   public ParentDependency(DependencyModel dependency) {}",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import $keep;",
            "import $nonNull;",
            "import $ioc;",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       target.setDependency(Ioc.singleton(ParentDependency.class));",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, dependencyFile, parentDependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun singletonFromClass3() {
        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Helpers.importType(Inject::class.java),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public ParentDependency dependency;",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "public class DependencyModel {",
            "   public DependencyModel() {}",
            "}")

        val singletonFile = JavaFileObjects.forSourceLines("test.Singleton2",
            "package test;",
            "",
            Helpers.importType(Inject::class.java),
            Helpers.importType(Singleton::class.java),
            "",
            "@Singleton",
            "public class Singleton2 {",
            "}")

        val parentDependencyFile = JavaFileObjects.forSourceLines("test.ParentDependency",
            "package test;",
            "",
            Helpers.importType(Inject::class.java),
            Helpers.importType(Singleton::class.java),
            "",
            "@Singleton",
            "public class ParentDependency {",
            "   @Inject",
            "   public ParentDependency(DependencyModel dependency, Singleton2 singleton2) {}",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ParentDependencySingleton",
            "package test;",
            "",
            "import $keep;",
            "import $ioc;",
            "import $iocLazy;",
            "",
            "@Keep",
            "public final class ParentDependencySingleton extends IocLazy<ParentDependency> {",
            "   private static ParentDependencySingleton instance;",
            "",
            "   public static final ParentDependencySingleton getInstance() {",
            "       if (instance == null) instance = new ParentDependencySingleton();",
            "       return instance;",
            "   }",
            "",
            "   protected final ParentDependency initialize() {",
            "       DependencyModel dependencyModel = new DependencyModel();",
            "       return new ParentDependency(dependencyModel, Ioc.singleton(Singleton2.class));",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, dependencyFile, singletonFile, parentDependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun onlyOneSingleton() {
        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Helpers.importType(Inject::class.java),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public ParentDependency dependency;",
            "   @Inject",
            "   public ParentDependency oneMoreDependency;",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "public class DependencyModel {",
            "   public DependencyModel() {}",
            "}")

        val singletonFile = JavaFileObjects.forSourceLines("test.Singleton2",
            "package test;",
            "",
            Helpers.importType(Singleton::class.java),
            "",
            "@Singleton",
            "public class Singleton2 {",
            "}")

        val parentDependencyFile = JavaFileObjects.forSourceLines("test.ParentDependency",
            "package test;",
            "",
            Helpers.importType(Inject::class.java),
            Helpers.importType(Singleton::class.java),
            "",
            "@Singleton",
            "public class ParentDependency {",
            "   @Inject",
            "   public ParentDependency(DependencyModel dependency, Singleton2 singleton2) {}",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ParentDependencySingleton",
            "package test;",
            "",
            "import $keep",
            "import $ioc",
            "import $iocLazy",
            "",
            "@Keep",
            "public final class ParentDependencySingleton extends IocLazy<ParentDependency> {",
            "   private static ParentDependencySingleton instance;",
            "",
            "   public static final ParentDependencySingleton getInstance() {",
            "       if (instance == null) instance = new ParentDependencySingleton();",
            "       return instance;",
            "   }",
            "",
            "   protected final ParentDependency initialize() {",
            "       DependencyModel dependencyModel = new DependencyModel();",
            "       return new ParentDependency(dependencyModel, Ioc.singleton(Singleton2.class));",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, dependencyFile, singletonFile, parentDependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun singletonFromClass4() {
        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Helpers.importType(Inject::class.java),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public ParentDependency dependency;",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "public class DependencyModel {",
            "   public DependencyModel() {}",
            "}")

        val singletonFile = JavaFileObjects.forSourceLines("test.Singleton2",
            "package test;",
            "",
            Helpers.importType(Inject::class.java),
            Helpers.importType(Singleton::class.java),
            "",
            "@Singleton",
            "public class Singleton2 {",
            "}")

        val parentDependencyFile = JavaFileObjects.forSourceLines("test.ParentDependency",
            "package test;",
            "",
            Helpers.importType(Inject::class.java),
            "",
            "public class ParentDependency {",
            "   @Inject",
            "   public ParentDependency(DependencyModel dependency, Singleton2 singleton2) {}",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import $keep",
            "import $nonNull;",
            "import $ioc;",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       target.dependency = injectParentDependencyInDependency();",
            "   }",
            "",
            "   private final ParentDependency injectParentDependencyInDependency() {",
            "       DependencyModel dependencyModel = new DependencyModel();",
            "       ParentDependency parentDependency = new ParentDependency(dependencyModel, Ioc.singleton(Singleton2.class));",
            "       return parentDependency;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, dependencyFile, singletonFile, parentDependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun singletonFromClass4MethodInjection() {
        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Helpers.importType(Inject::class.java),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   private ParentDependency dependency;",
            "   public void setDependency(ParentDependency dep) {};",
            "   public ParentDependency getDependency() { return null; }",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "public class DependencyModel {",
            "   public DependencyModel() {}",
            "}")

        val singletonFile = JavaFileObjects.forSourceLines("test.Singleton2",
            "package test;",
            "",
            Helpers.importType(Inject::class.java),
            Helpers.importType(Singleton::class.java),
            "",
            "@Singleton",
            "public class Singleton2 {",
            "}")

        val parentDependencyFile = JavaFileObjects.forSourceLines("test.ParentDependency",
            "package test;",
            "",
            Helpers.importType(Inject::class.java),
            "",
            "public class ParentDependency {",
            "   @Inject",
            "   public ParentDependency(DependencyModel dependency, Singleton2 singleton2) {}",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import $keep",
            "import $nonNull",
            "import $ioc",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       target.setDependency(injectParentDependencyInDependency());",
            "   }",
            "",
            "   private final ParentDependency injectParentDependencyInDependency() {",
            "       DependencyModel dependencyModel = new DependencyModel();",
            "       ParentDependency parentDependency = new ParentDependency(dependencyModel, Ioc.singleton(Singleton2.class));",
            "       return parentDependency",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf<JavaFileObject>(activityFile, dependencyFile, singletonFile, parentDependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun correctParamInjection() {
        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Helpers.importType(Inject::class.java),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public GetCountryService service;",
            "}")

        val retrofitFile = JavaFileObjects.forSourceLines("test.Retrofit",
            "package test;",
            "",
            "public class Retrofit {",
            "}")

        val countryServiceFile = JavaFileObjects.forSourceLines("test.CountryService",
            "package test;",
            "",
            "public interface CountryService {",
            "}")

        val getCountryServiceFile = JavaFileObjects.forSourceLines("test.GetCountryService",
            "package test;",
            "",
            Helpers.importType(Singleton::class.java),
            "",
            "@Singleton",
            "public class GetCountryService {",
            "   GetCountryService(CountryService countryService) {}",
            "}")

        val moduleFile = JavaFileObjects.forSourceLines("test.ModuleFile",
            "package test;",
            "",
            Helpers.importType(Dependency::class.java),
            "",
            "public class ModuleFile {",
            "   @Dependency",
            "   public static CountryService getService(Retrofit retrofit) {return null;}",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.GetCountryServiceSingleton",
            "package test;",
            "",
            "import $keep",
            "import $iocLazy",
            "",
            "@Keep",
            "public final class GetCountryServiceSingleton extends IocLazy<GetCountryService> {",
            "   private static GetCountryServiceSingleton instance;",
            "",
            "   public static final GetCountryServiceSingleton getInstance() {",
            "       if (instance == null) instance = new GetCountryServiceSingleton();",
            "       return instance;",
            "   }",
            "",
            "   protected final GetCountryService initialize() {",
            "       Retrofit retrofit = new Retrofit();",
            "       CountryService countryService = ModuleFile.getService(retrofit);",
            "       return new GetCountryService(countryService);",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf<JavaFileObject>(activityFile, retrofitFile, countryServiceFile, getCountryServiceFile, moduleFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun interfaceModules() {
        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Helpers.importType(Inject::class.java),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public CountryService service;",
            "}")

        val countryServiceFile = JavaFileObjects.forSourceLines("test.CountryService",
            "package test;",
            "",
            "public interface CountryService {",
            "}")

        val countryServiceImplementation = JavaFileObjects.forSourceLines("test.CountryServiceImplementation",
            "package test;",

            "public class CountryServiceImplementation implements CountryService {",
            "}")

        val moduleFile = JavaFileObjects.forSourceLines("test.ModuleFile",
            "package test;",
            "",
            Helpers.importType(Dependency::class.java),
            "",
            "interface ModuleFile {",
            "   @Dependency",
            "   public CountryService getService(CountryServiceImplementation implementation);",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep",
            "import android.support.annotation.NonNull",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       target.service = injectCountryServiceInService();",
            "   }",
            "",
            "   private final CountryService injectCountryServiceInService() {",
            "       CountryService countryService = new CountryServiceImplementation();",
            "       return countryService;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(Arrays.asList<JavaFileObject>(activityFile, countryServiceImplementation, countryServiceFile, moduleFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun abstractModules() {
        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Helpers.importType(Inject::class.java),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public CountryService service;",
            "   @Inject",
            "   public DependencyService dependencyService;",
            "}")

        val countryServiceFile = JavaFileObjects.forSourceLines("test.CountryService",
            "package test;",
            "",
            "public interface CountryService {",
            "}")

        val dependencyService = JavaFileObjects.forSourceLines("test.DependencyService",
            "package test;",
            "",
            "public class DependencyService {",
            "}")

        val countryServiceImplementation = JavaFileObjects.forSourceLines("test.CountryServiceImplementation",
            "package test;",

            "public class CountryServiceImplementation implements CountryService {",
            "}")

        val moduleFile = JavaFileObjects.forSourceLines("test.ModuleFile",
            "package test;",
            "",
            Helpers.importType(Dependency::class.java),
            Helpers.importType(Singleton::class.java),
            "",
            "public abstract class ModuleFile {",
            "   @Dependency",
            "   @Singleton",
            "   public abstract CountryService getService(CountryServiceImplementation implementation);",
            "   @Dependency",
            "   public abstract DependencyService getDependencyService();",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import $keep",
            "import $nonNull",
            "import $ioc",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       target.service = Ioc.singleton(CountryServiceImplementation.class);",
            "       target.dependencyService = injectDependencyServiceInDependencyService();",
            "   }",
            "",
            "   private final DependencyService injectDependencyServiceInDependencyService() {",
            "       DependencyService dependencyService = new DependencyService();",
            "       return dependencyService;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, dependencyService, countryServiceImplementation, countryServiceFile, moduleFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun failNotReturnImplementation() {
        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Helpers.importType(Inject::class.java),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public CountryService service;",
            "   @Inject",
            "   public DependencyService dependencyService;",
            "}")

        val countryServiceFile = JavaFileObjects.forSourceLines("test.CountryService",
            "package test;",
            "",
            "public interface CountryService {",
            "}")

        val moduleFile = JavaFileObjects.forSourceLines("test.ModuleFile",
            "package test;",
            "",
            Helpers.importType(Dependency::class.java),
            "",
            "public abstract class ModuleFile {",
            "   @Dependency",
            "   public abstract CountryService getService();",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(Arrays.asList<JavaFileObject>(activityFile, countryServiceFile, moduleFile))
            .processedWith(IProcessor())
            .failsToCompile()
            .withErrorContaining("ModuleFile.getService() returns test.CountryService which is interface also must contain implementation as parameter")
            .`in`(moduleFile)
            .onLine(7)
    }

    @Test
    @Throws(Exception::class)
    fun failPassInterfaceAsParameter() {
        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Helpers.importType(Inject::class.java),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public CountryService service;",
            "   @Inject",
            "   public DependencyService dependencyService;",
            "}")

        val countryServiceFile = JavaFileObjects.forSourceLines("test.CountryService",
            "package test;",
            "",
            "public interface CountryService {",
            "}")

        val moduleFile = JavaFileObjects.forSourceLines("test.ModuleFile",
            "package test;",
            "",
            Helpers.importType(Dependency::class.java),
            "",
            "public abstract class ModuleFile {",
            "   @Dependency",
            "   public abstract CountryService getService(CountryService service);",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(Arrays.asList<JavaFileObject>(activityFile, countryServiceFile, moduleFile))
            .processedWith(IProcessor())
            .failsToCompile()
            .withErrorContaining("ModuleFile.getService(test.CountryService) returns test.CountryService which is interface also contains interface as parameter must be implementation")
            .`in`(moduleFile)
            .onLine(7)
    }

    @Test
    @Throws(Exception::class)
    fun methodMustBeStatic() {
        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Helpers.importType(Inject::class.java),
            Helpers.importType(Named::class.java),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   @Named(\"named\")",
            "   public String serviceName;",
            "}")

        val moduleFile = JavaFileObjects.forSourceLines("test.ModuleFile",
            "package test;",
            "",
            Helpers.importType(Dependency::class.java),
            Helpers.importType(Named::class.java),
            "",
            "public abstract class ModuleFile {",
            "   @Dependency",
            "   @Named(\"named\")",
            "   public String getServiceName() { return \"some name\"; }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(Arrays.asList<JavaFileObject>(activityFile, moduleFile))
            .processedWith(IProcessor())
            .failsToCompile()
            .withErrorContaining("ModuleFile.getServiceName() is annotated with @Dependency must be static and public")
            .`in`(moduleFile)
            .onLine(9)
    }

    @Test
    @Throws(Exception::class)
    fun injectStringFromModuleMethod() {
        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Helpers.importType(Inject::class.java),
            Helpers.importType(Named::class.java),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   @Named(\"named\")",
            "   public String serviceName;",
            "   @Inject",
            "   public Service service;",
            "}")

        val service = JavaFileObjects.forSourceLines("test.Service",
            "package test;",
            "",
            Helpers.importType(Inject::class.java),
            Helpers.importType(Named::class.java),
            "",
            "public class Service {",
            "   Service(@Named(\"named\") String serviceName) {}",
            "",
            "   @Inject",
            "   @Named(\"named\")",
            "   public String serviceName;",
            "}")

        val moduleFile = JavaFileObjects.forSourceLines("test.ModuleFile",
            "package test;",
            "",
            Helpers.importType(Dependency::class.java),
            Helpers.importType(Named::class.java),
            "",
            "public class ModuleFile {",
            "   @Dependency",
            "   @Named(\"named\")",
            "   public static String getServiceName() { return \"some name\"; }",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep;",
            "import android.support.annotation.NonNull;",
            "import java.lang.String;",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       target.serviceName = injectStringInServiceName();",
            "       target.service = injectServiceInService();",
            "   }",
            "",
            "   private final String injectStringInServiceName() {",
            "       String string = ModuleFile.getServiceName();",
            "       return string;",
            "   }",
            "",
            "   private final Service injectServiceInService() {",
            "       String string2 = ModuleFile.getServiceName();",
            "       Service service = new Service(string2);",
            "       return service;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(Arrays.asList<JavaFileObject>(activityFile, service, moduleFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun injectIntegerFromModuleMethod() {
        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Helpers.importType(Inject::class.java),
            Helpers.importType(Named::class.java),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   @Named(\"named\")",
            "   public Integer serviceNumber;",
            "   @Inject",
            "   public Service service;",
            "}")

        val service = JavaFileObjects.forSourceLines("test.Service",
            "package test;",
            "",
            Helpers.importType(Inject::class.java),
            Helpers.importType(Named::class.java),
            "",
            "public class Service {",
            "   Service(@Named(\"named\") Integer serviceNumber) {}",
            "",
            "   @Inject",
            "   @Named(\"named\")",
            "   public Integer serviceNumber;",
            "}")

        val moduleFile = JavaFileObjects.forSourceLines("test.ModuleFile",
            "package test;",
            "",
            Helpers.importType(Dependency::class.java),
            Helpers.importType(Named::class.java),
            "",
            "public class ModuleFile {",
            "   @Dependency",
            "   @Named(\"named\")",
            "   public static Integer getServiceName() { return 10; }",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep;",
            "import android.support.annotation.NonNull;",
            "import java.lang.Integer;",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       target.serviceNumber = injectIntegerInServiceNumber();",
            "       target.service = injectServiceInService();",
            "   }",
            "",
            "   private final Integer injectIntegerInServiceNumber() {",
            "       Integer integer = ModuleFile.getServiceName();",
            "       return integer;",
            "   }",
            "",
            "   private final Service injectServiceInService() {",
            "       Integer integer2 = ModuleFile.getServiceName();",
            "       Service service = new Service(integer2);",
            "       return service;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(Arrays.asList<JavaFileObject>(activityFile, service, moduleFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun nestedModule() {
        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Helpers.importType(Inject::class.java),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public CountryService service;",
            "",
            "   @Inject",
            "   public String someString;",
            "}")

        val countryService = JavaFileObjects.forSourceLines("test.CountryService",
            "package test;",
            "",
            Helpers.importType(Inject::class.java),
            "",
            "public class CountryService {}")

        val moduleFile = JavaFileObjects.forSourceLines("test.ModuleFile",
            "package test;",
            "",
            Helpers.importType(Dependency::class.java),
            "",
            "public abstract class ModuleFile {",
            "   public abstract static class NestedModule {",
            "       @Dependency",
            "       public abstract CountryService getService();",
            "       @Dependency",
            "       public static String getString() { return null; };",
            "   }",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep;",
            "import android.support.annotation.NonNull;",
            "import java.lang.String;",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       target.service = injectCountryServiceInService();",
            "       target.someString = injectStringInSomeString();",
            "   }",
            "",
            "   private final CountryService injectCountryServiceInService() {",
            "       CountryService countryService = new CountryService();",
            "       return countryService;",
            "   }",
            "",
            "   private final String injectStringInSomeString() {",
            "       String string = ModuleFile.NestedModule.getString();",
            "       return string;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(Arrays.asList<JavaFileObject>(activityFile, moduleFile, countryService))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun kotlinModuleInField() {
        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Helpers.importType(Inject::class.java),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public CountryService service;",
            "}")

        val countryService = JavaFileObjects.forSourceLines("test.CountryService",
            "package test;",
            "",
            Helpers.importType(Inject::class.java),
            "",
            "public class CountryService {}")

        val moduleFile = JavaFileObjects.forSourceLines("test.Module",
            "package test;",
            "",
            Helpers.importType(Dependency::class.java),
            Helpers.importType(Metadata::class.java),
            "",
            "@Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {\"\\u0000\\u0012\\n\\u0002\\u0018\\u0002\\n\\u0002\\u0010\\u0000\\n\\u0002\\b\\u0002\\n\\u0002\\u0018\\u0002\\n\\u0000\\bÆ\\u0002\\u0018\\u00002\\u00020\\u0001B\\u0007\\b\\u0002¢\\u0006\\u0002\\u0010\\u0002J\\b\\u0010\\u0003\\u001a\\u00020\\u0004H\\u0007¨\\u0006\\u0005\"}, d2 = {\"Ltest/Module;\", \"\", \"()V\", \"getCountryService\", \"Ltest/CountryService;\", \"sample_debug\"})",
            "public final class Module {",
            "   public static final Module INSTANCE;",
            "   @Dependency",
            "   public final CountryService getCountryService() { return new CountryService(); }",
            "",
            "   static {",
            "     Module var0 = new Module();",
            "     INSTANCE = var0;",
            "   }",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep;",
            "import android.support.annotation.NonNull;",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       target.service = injectCountryServiceInService();",
            "   }",
            "",
            "   private final CountryService injectCountryServiceInService() {",
            "       CountryService countryService = Module.INSTANCE.getCountryService();",
            "       return countryService;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(Arrays.asList<JavaFileObject>(activityFile, moduleFile, countryService))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun kotlinModuleInFieldWithParameter() {
        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Helpers.importType(Inject::class.java),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public CountryService service;",
            "}")

        val countryService = JavaFileObjects.forSourceLines("test.CountryService",
            "package test;",
            "",
            Helpers.importType(Inject::class.java),
            "",
            "public class CountryService {}")

        val countryRepository = JavaFileObjects.forSourceLines("test.CountryRepository",
            "package test;",
            "",
            "public class CountryRepository {",
            "   CountryRepository() {}",
            "}")

        val moduleFile = JavaFileObjects.forSourceLines("test.Module",
            "package test;",
            "",
            Helpers.importType(Dependency::class.java),
            Helpers.importType(Metadata::class.java),
            "",
            "@Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {\"\\u0000\\u0012\\n\\u0002\\u0018\\u0002\\n\\u0002\\u0010\\u0000\\n\\u0002\\b\\u0002\\n\\u0002\\u0018\\u0002\\n\\u0000\\bÆ\\u0002\\u0018\\u00002\\u00020\\u0001B\\u0007\\b\\u0002¢\\u0006\\u0002\\u0010\\u0002J\\b\\u0010\\u0003\\u001a\\u00020\\u0004H\\u0007¨\\u0006\\u0005\"}, d2 = {\"Ltest/Module;\", \"\", \"()V\", \"getCountryService\", \"Ltest/CountryService;\", \"sample_debug\"})",
            "public final class Module {",
            "   public static final Module INSTANCE;",
            "   @Dependency",
            "   public final CountryService getCountryService(CountryRepository repository) { return new CountryService(); }",
            "",
            "   static {",
            "     Module var0 = new Module();",
            "     INSTANCE = var0;",
            "   }",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep;",
            "import android.support.annotation.NonNull;",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       target.service = injectCountryServiceInService();",
            "   }",
            "",
            "   private final CountryService injectCountryServiceInService() {",
            "       CountryRepository countryRepository = new CountryRepository();",
            "       CountryService countryService = Module.INSTANCE.getCountryService(countryRepository);",
            "       return countryService;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(Arrays.asList<JavaFileObject>(activityFile, moduleFile, countryRepository, countryService))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun kotlinModuleInConstructor() {
        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Helpers.importType(Inject::class.java),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public CountryProvider service;",
            "}")

        val countryService = JavaFileObjects.forSourceLines("test.CountryService",
            "package test;",
            "public class CountryService {}")

        val countryProvider = JavaFileObjects.forSourceLines("test.CountryProvider",
            "package test;",
            "",
            "public class CountryProvider {",
            "   CountryProvider(CountryService service) {}",
            "}")

        val moduleFile = JavaFileObjects.forSourceLines("test.Module",
            "package test;",
            "",
            Helpers.importType(Dependency::class.java),
            Helpers.importType(Metadata::class.java),
            "",
            "@Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {\"\\u0000\\u0012\\n\\u0002\\u0018\\u0002\\n\\u0002\\u0010\\u0000\\n\\u0002\\b\\u0002\\n\\u0002\\u0018\\u0002\\n\\u0000\\bÆ\\u0002\\u0018\\u00002\\u00020\\u0001B\\u0007\\b\\u0002¢\\u0006\\u0002\\u0010\\u0002J\\b\\u0010\\u0003\\u001a\\u00020\\u0004H\\u0007¨\\u0006\\u0005\"}, d2 = {\"Ltest/Module;\", \"\", \"()V\", \"getCountryService\", \"Ltest/CountryService;\", \"sample_debug\"})",
            "public final class Module {",
            "   public static final Module INSTANCE;",
            "   @Dependency",
            "   public final CountryService getCountryService() { return new CountryService(); }",
            "",
            "   static {",
            "     Module var0 = new Module();",
            "     INSTANCE = var0;",
            "   }",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep;",
            "import android.support.annotation.NonNull;",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       target.service = injectCountryProviderInService();",
            "   }",
            "",
            "   private final CountryProvider injectCountryProviderInService() {",
            "       CountryService countryService = Module.INSTANCE.getCountryService();",
            "       CountryProvider countryProvider = new CountryProvider(countryService);",
            "       return countryProvider;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(Arrays.asList<JavaFileObject>(activityFile, moduleFile, countryProvider, countryService))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun kotlinModuleInConstructorWithParameters() {
        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Helpers.importType(Inject::class.java),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public CountryProvider service;",
            "}")

        val countryService = JavaFileObjects.forSourceLines("test.CountryService",
            "package test;",
            "public class CountryService {}")

        val countryRepository = JavaFileObjects.forSourceLines("test.CountryRepository",
            "package test;",
            "",
            "public class CountryRepository {",
            "   CountryRepository() {}",
            "}")

        val countryProvider = JavaFileObjects.forSourceLines("test.CountryProvider",
            "package test;",
            "",
            "public class CountryProvider {",
            "   CountryProvider(CountryService service) {}",
            "}")

        val moduleFile = JavaFileObjects.forSourceLines("test.Module",
            "package test;",
            "",
            Helpers.importType(Dependency::class.java),
            Helpers.importType(Metadata::class.java),
            "",
            "@Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {\"\\u0000\\u0012\\n\\u0002\\u0018\\u0002\\n\\u0002\\u0010\\u0000\\n\\u0002\\b\\u0002\\n\\u0002\\u0018\\u0002\\n\\u0000\\bÆ\\u0002\\u0018\\u00002\\u00020\\u0001B\\u0007\\b\\u0002¢\\u0006\\u0002\\u0010\\u0002J\\b\\u0010\\u0003\\u001a\\u00020\\u0004H\\u0007¨\\u0006\\u0005\"}, d2 = {\"Ltest/Module;\", \"\", \"()V\", \"getCountryService\", \"Ltest/CountryService;\", \"sample_debug\"})",
            "public final class Module {",
            "   public static final Module INSTANCE;",
            "   @Dependency",
            "   public final CountryService getCountryService(CountryRepository arg0) { return new CountryService(); }",
            "",
            "   static {",
            "     Module var0 = new Module();",
            "     INSTANCE = var0;",
            "   }",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import android.support.annotation.Keep;",
            "import android.support.annotation.NonNull;",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       target.service = injectCountryProviderInService();",
            "   }",
            "",
            "   private final CountryProvider injectCountryProviderInService() {",
            "       CountryRepository countryRepository = new CountryRepository();",
            "       CountryService countryService = Module.INSTANCE.getCountryService(countryRepository);",
            "       CountryProvider countryProvider = new CountryProvider(countryService);",
            "       return countryProvider;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(Arrays.asList<JavaFileObject>(activityFile, moduleFile, countryProvider, countryRepository, countryService))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun kotlinModuleSingletonParameter() {
        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Helpers.importType(Inject::class.java),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public Factory fy;",
            "}")

        val singletonParameter = JavaFileObjects.forSourceLines("test.SingletonParameter",
            "package test;",
            Helpers.importType(Singleton::class.java),
            "@Singleton",
            "public class SingletonParameter {}")

        val countryProvider = JavaFileObjects.forSourceLines("test.CountryProvider",
            "package test;",
            "",
            "public class CountryProvider {",
            "   CountryProvider(SingletonParameter sp) {}",
            "}")

        val searchEngineService = JavaFileObjects.forSourceLines("test.SearchEngineService",
            "package test;",
            "",
            "public class SearchEngineService {",
            "}")

        val factory = JavaFileObjects.forSourceLines("test.Factory",
            "package test;",


            "public class Factory {",
            "   Factory(SearchEngineService s) {}",
            "}")

        val moduleFile = JavaFileObjects.forSourceLines("test.Module",
            "package test;",
            "",
            Helpers.importType(Dependency::class.java),
            Helpers.importType(Metadata::class.java),
            "",
            "@Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {\"\\u0000\\u0012\\n\\u0002\\u0018\\u0002\\n\\u0002\\u0010\\u0000\\n\\u0002\\b\\u0002\\n\\u0002\\u0018\\u0002\\n\\u0000\\bÆ\\u0002\\u0018\\u00002\\u00020\\u0001B\\u0007\\b\\u0002¢\\u0006\\u0002\\u0010\\u0002J\\b\\u0010\\u0003\\u001a\\u00020\\u0004H\\u0007¨\\u0006\\u0005\"}, d2 = {\"Ltest/Module;\", \"\", \"()V\", \"getCountryService\", \"Ltest/CountryService;\", \"sample_debug\"})",
            "public final class Module {",
            "   public static final Module INSTANCE;",
            "   @Dependency",
            "   public final CountryProvider getCountryService(SingletonParameter arg0) { return null; }",
            "",
            "   static {",
            "     Module var0 = new Module();",
            "     INSTANCE = var0;",
            "   }",
            "}")

        val suggestionModule = JavaFileObjects.forSourceLines("test.SuggestionModule",
            "package test;",
            "",
            Helpers.importType(Dependency::class.java),
            Helpers.importType(Metadata::class.java),
            "",
            "@Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {\"\\u0000\\u0012\\n\\u0002\\u0018\\u0002\\n\\u0002\\u0010\\u0000\\n\\u0002\\b\\u0002\\n\\u0002\\u0018\\u0002\\n\\u0000\\bÆ\\u0002\\u0018\\u00002\\u00020\\u0001B\\u0007\\b\\u0002¢\\u0006\\u0002\\u0010\\u0002J\\b\\u0010\\u0003\\u001a\\u00020\\u0004H\\u0007¨\\u0006\\u0005\"}, d2 = {\"Ltest/Module;\", \"\", \"()V\", \"getCountryService\", \"Ltest/CountryService;\", \"sample_debug\"})",
            "public final class SuggestionModule {",
            "   public static final SuggestionModule INSTANCE;",
            "   @Dependency",
            "   public final SearchEngineService getSearchEngineService(CountryProvider arg0) { return null; }",
            "",
            "   static {",
            "     SuggestionModule var0 = new SuggestionModule();",
            "     INSTANCE = var0;",
            "   }",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            "import $keep;",
            "import $nonNull;",
            "import $ioc;",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       target.fy = injectFactoryInFy();",
            "   }",
            "",
            "   private final Factory injectFactoryInFy() {",
            "       CountryProvider countryProvider = Module.INSTANCE.getCountryService(Ioc.singleton(SingletonParameter.class));",
            "       SearchEngineService searchEngineService = SuggestionModule.INSTANCE.getSearchEngineService(countryProvider);",
            "       Factory factory = new Factory(searchEngineService);",
            "       return factory;",
            "   }",
            "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, moduleFile, factory, suggestionModule, searchEngineService, singletonParameter, countryProvider))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }
}
