package com.ioc

import com.google.common.truth.Truth.assertAbout
import com.google.testing.compile.JavaFileObjects
import com.google.testing.compile.JavaSourcesSubjectFactory.javaSources
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.util.*

/**
 * Created by sergeygolishnikov on 09/08/2017.
 */
@RunWith(JUnit4::class)
class NamedTest {
    @Test
    @Throws(Exception::class)
    fun moduleMethodNamedFieldNamed() {

        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            importQualifierAnnotation,
            "",
            "public class Activity {",
            "",
            "   @Inject @Qualifier(\"debug\")",
            "   public DependencyModel dependency;",
            "}")

        val moduleFile = JavaFileObjects.forSourceLines("test.ModuleFile",
            "package test;",
            "",
            importDependencyAnnotation,
            importQualifierAnnotation,
            "",
            "public class ModuleFile {",
            "",
            "   @Dependency @Qualifier(\"production\")",
            "   public static DependencyModel production() { return null; }",
            "   @Dependency @Qualifier(\"debug\")",
            "   public static DependencyModel debug() { return null; }",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "public class DependencyModel {",
            "   public DependencyModel() {};",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            importKeepAnnotation,
            importNonNullAnnotation,
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public static final void inject(@NonNull final Activity target) {",
            "       target.dependency = ModuleFile.debug();",
            "   }",
            "}")

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, moduleFile, dependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun classNamedFieldNamed() {

        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            importQualifierAnnotation,
            "",
            "public class Activity {",
            "",
            "   @Inject @Qualifier(\"debug\")",
            "   public DependencyModel dependency;",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            importQualifierAnnotation,
            "",
            "@Qualifier(\"debug\")",
            "public class DependencyModel {",
            "   public DependencyModel() {};",
            "}")

        val moduleFile = JavaFileObjects.forSourceLines("test.ModuleFile",
            "package test;",
            "",
            importDependencyAnnotation,
            importQualifierAnnotation,
            "",
            "public class ModuleFile {",
            "",
            "   @Dependency",
            "   public static DependencyModel production() { return null; }",
            "   @Dependency",
            "   public static DependencyModel debug() { return null; }",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            importKeepAnnotation,
            importNonNullAnnotation,
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public static final void inject(@NonNull final Activity target) {",
            "       target.dependency = new DependencyModel();",
            "   }",
            "}")

        assertAbout(javaSources())
            .that(listOf(activityFile, dependencyFile, moduleFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun methodNamedArgumentNamed() {

        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public ParentDependency parentDependency;",
            "}")

        val parentDependencyFile = JavaFileObjects.forSourceLines("test.ParentDependency",
            "package test;",
            "",
            importQualifierAnnotation,
            importInjectAnnotation,
            "",
            "public class ParentDependency {",
            "   @Inject",
            "   public ParentDependency(@Qualifier(\"release\") DependencyModel dependency) {};",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "public interface DependencyModel {",
            "}")

        val debugDependencyFile = JavaFileObjects.forSourceLines("test.DebugDependency",
            "package test;",
            "",
            "public class DebugDependency implements DependencyModel {",
            "   public DebugDependency() {};",
            "}")

        val releaseDependencyFile = JavaFileObjects.forSourceLines("test.ReleaseDependency",
            "package test;",
            "",
            "public class ReleaseDependency implements DependencyModel {",
            "   public ReleaseDependency() {};",
            "}")

        val moduleFile = JavaFileObjects.forSourceLines("test.DependencyModuleFile",
            "package test;",
            "",
            importDependencyAnnotation,
            importQualifierAnnotation,
            "",
            "public class DependencyModuleFile {",
            "",
            "   @Dependency",
            "   @Qualifier(\"release\")",
            "   public static DependencyModel release() { return null; }",
            "   @Dependency",
            "   @Qualifier(\"debug\")",
            "   public static DependencyModel debug() { return null; }",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            importKeepAnnotation,
            importNonNullAnnotation,
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public static final void inject(@NonNull final Activity target) {",
            "       target.parentDependency = provideParentDependency();",
            "   }",
            "",
            "   public static final ParentDependency provideParentDependency() {",
            "       DependencyModel dependencyModel = DependencyModuleFile.release();",
            "       ParentDependency parentDependency = new ParentDependency(dependencyModel);",
            "       return parentDependency;",
            "   }",
            "}")

        assertAbout(javaSources())
            .that(listOf(activityFile, parentDependencyFile, debugDependencyFile, releaseDependencyFile, dependencyFile, moduleFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }


    @Test
    @Throws(Exception::class)
    fun classNamedArgumentNamed() {

        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            importQualifierAnnotation,
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   @Qualifier(\"release\")",
            "   public DependencyModel dependency;",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "public interface DependencyModel {",
            "}")

        val debugDependencyFile = JavaFileObjects.forSourceLines("test.DebugDependency",
            "package test;",
            "",
            importQualifierAnnotation,
            importDependencyAnnotation,
            "",
            "@Dependency",
            "@Qualifier(\"debug\")",
            "public class DebugDependency implements DependencyModel {",
            "   public DebugDependency() {};",
            "}")

        val releaseDependencyFile = JavaFileObjects.forSourceLines("test.ReleaseDependency",
            "package test;",
            "",
            importQualifierAnnotation,
            importDependencyAnnotation,
            "",
            "@Dependency",
            "@Qualifier(\"release\")",
            "public class ReleaseDependency implements DependencyModel {",
            "   public ReleaseDependency() {};",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            importKeepAnnotation,
            importNonNullAnnotation,
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public static final void inject(@NonNull final Activity target) {",
            "       target.dependency = new ReleaseDependency();",
            "   }",
            "}")

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, debugDependencyFile, releaseDependencyFile, dependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun classNamedAsArgumentNamed() {

        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            importQualifierAnnotation,
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public MainPresenter presenter;",
            "}")

        val presenter = JavaFileObjects.forSourceLines("test.MainPresenter",
            "package test;",
            "",
            importInjectAnnotation,
            importQualifierAnnotation,
            "",
            "public class MainPresenter {",
            "   @Inject",
            "   MainPresenter(@Qualifier(\"release\") DependencyModel dependency) {}",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "public interface DependencyModel {",
            "}")

        val debugDependencyFile = JavaFileObjects.forSourceLines("test.DebugDependency",
            "package test;",
            "",
            importQualifierAnnotation,
            importDependencyAnnotation,
            "",
            "@Dependency",
            "@Qualifier(\"debug\")",
            "public class DebugDependency implements DependencyModel {",
            "   public DebugDependency() {};",
            "}")

        val releaseDependencyFile = JavaFileObjects.forSourceLines("test.ReleaseDependency",
            "package test;",
            "",
            importQualifierAnnotation,
            importDependencyAnnotation,
            "",
            "@Dependency",
            "@Qualifier(\"release\")",
            "public class ReleaseDependency implements DependencyModel {",
            "   public ReleaseDependency() {};",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            importKeepAnnotation,
            importNonNullAnnotation,
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public static final void inject(@NonNull final Activity target) {",
            "       target.presenter = provideMainPresenter();",
            "   }",
            "",
            "   public static final MainPresenter provideMainPresenter() {",
            "       ReleaseDependency dependencyModel = new ReleaseDependency();",
            "       MainPresenter mainPresenter = new MainPresenter(dependencyModel);",
            "       return mainPresenter;",
            "   }",
            "}")

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, debugDependencyFile, presenter, releaseDependencyFile, dependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun classNamedAsArgumentNamed3() {

        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            importQualifierAnnotation,
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public MainPresenter presenter;",
            "}")

        val presenter = JavaFileObjects.forSourceLines("test.MainPresenter",
            "package test;",
            "",
            importInjectAnnotation,
            importQualifierAnnotation,
            "",
            "public class MainPresenter {",
            "   @Inject",
            "   MainPresenter(DependencyModel dependency) {}",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            importInjectAnnotation,
            importQualifierAnnotation,
            "",
            "public class DependencyModel {",
            "   @Inject",
            "   DependencyModel(@Qualifier(\"cappuccino\") Coffee coffee) {}",
            "}")

        val coffee = JavaFileObjects.forSourceLines("test.Coffee",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public interface Coffee {",
            "}")

        val cappuccino = JavaFileObjects.forSourceLines("test.Cappuccino",
            "package test;",
            "",
            importQualifierAnnotation,
            importDependencyAnnotation,
            "",
            "@Dependency",
            "@Qualifier(\"cappuccino\")",
            "public class Cappuccino implements Coffee {",
            "   public Cappuccino() {};",
            "}")

        val nescafe = JavaFileObjects.forSourceLines("test.Nescafe",
            "package test;",
            "",
            importQualifierAnnotation,
            importDependencyAnnotation,
            "",
            "@Dependency",
            "@Qualifier(\"nescafe\")",
            "public class Nescafe implements Coffee {",
            "   public Nescafe() {};",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            importKeepAnnotation,
            importNonNullAnnotation,
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public static final void inject(@NonNull final Activity target) {",
            "       target.presenter = provideMainPresenter();",
            "   }",
            "",
            "   public static final MainPresenter provideMainPresenter() {",
            "       Cappuccino coffee = new Cappuccino();",
            "       DependencyModel dependencyModel = new DependencyModel(coffee);",
            "       MainPresenter mainPresenter = new MainPresenter(dependencyModel);",
            "       return mainPresenter;",
            "   }",
            "}")

        assertAbout(javaSources())
            .that(listOf(activityFile, cappuccino, nescafe, presenter, coffee, dependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun classNamedAsArgumentNamed2() {

        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            importQualifierAnnotation,
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public MainPresenter presenter;",
            "}")

        val presenter = JavaFileObjects.forSourceLines("test.MainPresenter",
            "package test;",
            "",
            importInjectAnnotation,
            importQualifierAnnotation,
            "",
            "public class MainPresenter {",
            "   @Inject",
            "   MainPresenter(@Qualifier(\"release\") DependencyModel dependency) {}",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "public interface DependencyModel {",
            "}")

        val release = JavaFileObjects.forSourceLines("test.ReleaseModel",
            "package test;",
            "",
            importQualifierAnnotation,
            importInjectAnnotation,
            importDependencyAnnotation,
            "",
            "@Dependency",
            "@Qualifier(\"release\")",
            "public class ReleaseModel implements DependencyModel {",
            "   @Inject",
            "   ReleaseModel(@Qualifier(\"cappuccino\") Coffee coffee) {}",
            "}")

        val debug = JavaFileObjects.forSourceLines("test.DebugModel",
            "package test;",
            "",
            importQualifierAnnotation,
            importInjectAnnotation,
            importDependencyAnnotation,
            "",
            "@Dependency",
            "@Qualifier(\"debug\")",
            "public class DebugModel implements DependencyModel {",
            "   @Inject",
            "   DebugModel(@Qualifier(\"debug\") Coffee coffee) {}",
            "}")

        val coffee = JavaFileObjects.forSourceLines("test.Coffee",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public interface Coffee {",
            "}")

        val cappuccino = JavaFileObjects.forSourceLines("test.Cappuccino",
            "package test;",
            "",
            importQualifierAnnotation,
            importDependencyAnnotation,
            "",
            "@Dependency",
            "@Qualifier(\"cappuccino\")",
            "public class Cappuccino implements Coffee {",
            "   public Cappuccino() {};",
            "}")

        val nescafe = JavaFileObjects.forSourceLines("test.Nescafe",
            "package test;",
            "",
            importQualifierAnnotation,
            importDependencyAnnotation,
            "",
            "@Dependency",
            "@Qualifier(\"nescafe\")",
            "public class Nescafe implements Coffee {",
            "   public Nescafe() {};",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            importKeepAnnotation,
            importNonNullAnnotation,
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public static final void inject(@NonNull final Activity target) {",
            "       target.presenter = provideMainPresenter();",
            "   }",
            "",
            "   public static final MainPresenter provideMainPresenter() {",
            "       Cappuccino coffee = new Cappuccino();",
            "       ReleaseModel dependencyModel = new ReleaseModel(coffee);",
            "       MainPresenter mainPresenter = new MainPresenter(dependencyModel);",
            "       return mainPresenter;",
            "   }",
            "}")

        assertAbout(javaSources())
            .that(Arrays.asList(activityFile, cappuccino, nescafe, release, debug, presenter, coffee, dependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun classNamedAsArgumentNamedDebug() {

        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            importQualifierAnnotation,
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public MainPresenter presenter;",
            "}")

        val presenter = JavaFileObjects.forSourceLines("test.MainPresenter",
            "package test;",
            "",
            importInjectAnnotation,
            importQualifierAnnotation,
            "",
            "public class MainPresenter {",
            "   @Inject",
            "   MainPresenter(@Qualifier(\"debug\") DependencyModel dependency) {}",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "public interface DependencyModel {",
            "}")

        val release = JavaFileObjects.forSourceLines("test.ReleaseModel",
            "package test;",
            "",
            importQualifierAnnotation,
            importInjectAnnotation,
            importDependencyAnnotation,
            "",
            "@Dependency",
            "@Qualifier(\"release\")",
            "public class ReleaseModel implements DependencyModel {",
            "   @Inject",
            "   ReleaseModel(@Qualifier(\"cappuccino\") Coffee coffee) {}",
            "}")

        val debug = JavaFileObjects.forSourceLines("test.DebugModel",
            "package test;",
            "",
            importQualifierAnnotation,
            importInjectAnnotation,
            importDependencyAnnotation,
            "",
            "@Dependency",
            "@Qualifier(\"debug\")",
            "public class DebugModel implements DependencyModel {",
            "   @Inject",
            "   DebugModel(@Qualifier(\"cappuccino\") Coffee coffee) {}",
            "}")

        val coffee = JavaFileObjects.forSourceLines("test.Coffee",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public interface Coffee {",
            "}")

        val cappuccino = JavaFileObjects.forSourceLines("test.Cappuccino",
            "package test;",
            "",
            importQualifierAnnotation,
            importDependencyAnnotation,
            "",
            "@Dependency",
            "@Qualifier(\"cappuccino\")",
            "public class Cappuccino implements Coffee {",
            "   public Cappuccino() {};",
            "}")

        val nescafe = JavaFileObjects.forSourceLines("test.Nescafe",
            "package test;",
            "",
            importQualifierAnnotation,
            importDependencyAnnotation,
            "",
            "@Dependency",
            "@Qualifier(\"nescafe\")",
            "public class Nescafe implements Coffee {",
            "   public Nescafe() {};",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            importKeepAnnotation,
            importNonNullAnnotation,
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public static final void inject(@NonNull final Activity target) {",
            "       target.presenter = provideMainPresenter();",
            "   }",
            "",
            "   public static final MainPresenter provideMainPresenter() {",
            "       Cappuccino coffee = new Cappuccino();",
            "       DebugModel dependencyModel = new DebugModel(coffee);",
            "       MainPresenter mainPresenter = new MainPresenter(dependencyModel);",
            "       return mainPresenter;",
            "   }",
            "}")

        assertAbout(javaSources())
            .that(listOf(activityFile, cappuccino, nescafe, release, debug, presenter, coffee, dependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun classNamedAsArgumentNamedDebugNescafe() {

        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            importQualifierAnnotation,
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public MainPresenter presenter;",
            "}")

        val presenter = JavaFileObjects.forSourceLines("test.MainPresenter",
            "package test;",
            "",
            importInjectAnnotation,
            importQualifierAnnotation,
            "",
            "public class MainPresenter {",
            "   @Inject",
            "   MainPresenter(@Qualifier(\"debug\") DependencyModel dependency) {}",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "public interface DependencyModel {",
            "}")

        val release = JavaFileObjects.forSourceLines("test.ReleaseModel",
            "package test;",
            "",
            importQualifierAnnotation,
            importInjectAnnotation,
            importDependencyAnnotation,
            "",
            "@Dependency",
            "@Qualifier(\"release\")",
            "public class ReleaseModel implements DependencyModel {",
            "   @Inject",
            "   ReleaseModel(@Qualifier(\"cappuccino\") Coffee coffee) {}",
            "}")

        val debug = JavaFileObjects.forSourceLines("test.DebugModel",
            "package test;",
            "",
            importQualifierAnnotation,
            importInjectAnnotation,
            importDependencyAnnotation,
            "",
            "@Dependency",
            "@Qualifier(\"debug\")",
            "public class DebugModel implements DependencyModel {",
            "   @Inject",
            "   DebugModel(@Qualifier(\"nescafe\") Coffee coffee) {}",
            "}")

        val coffee = JavaFileObjects.forSourceLines("test.Coffee",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public interface Coffee {",
            "}")

        val cappuccino = JavaFileObjects.forSourceLines("test.Cappuccino",
            "package test;",
            "",
            importQualifierAnnotation,
            importDependencyAnnotation,
            "",
            "@Dependency",
            "@Qualifier(\"cappuccino\")",
            "public class Cappuccino implements Coffee {",
            "   public Cappuccino() {};",
            "}")

        val nescafe = JavaFileObjects.forSourceLines("test.Nescafe",
            "package test;",
            "",
            importQualifierAnnotation,
            importDependencyAnnotation,
            "",
            "@Dependency",
            "@Qualifier(\"nescafe\")",
            "public class Nescafe implements Coffee {",
            "   public Nescafe() {};",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            importKeepAnnotation,
            importNonNullAnnotation,
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public static final void inject(@NonNull final Activity target) {",
            "       target.presenter = provideMainPresenter();",
            "   }",
            "",
            "   public static final MainPresenter provideMainPresenter() {",
            "       Nescafe coffee = new Nescafe();",
            "       DebugModel dependencyModel = new DebugModel(coffee);",
            "       MainPresenter mainPresenter = new MainPresenter(dependencyModel);",
            "       return mainPresenter;",
            "   }",
            "}")

        assertAbout(javaSources())
            .that(listOf(activityFile, cappuccino, nescafe, release, debug, presenter, coffee, dependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun qualifierOnField() {

        val debugQualifierFile = JavaFileObjects.forSourceLines("test.DebugQualifier",
            "package test;",
            "",
            importQualifierAnnotation,
            "",
            "@Qualifier",
            "public @interface DebugQualifier {",
            "}")


        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public class Activity {",
            "",
            "   @Inject @DebugQualifier",
            "   public DependencyModel dependency;",
            "}")

        val moduleFile = JavaFileObjects.forSourceLines("test.ModuleFile",
            "package test;",
            "",
            importDependencyAnnotation,
            "",
            "public class ModuleFile {",
            "",
            "   @Dependency",
            "   public static DependencyModel production() { return null; }",
            "   @Dependency @DebugQualifier",
            "   public static DependencyModel debug() { return null; }",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "public class DependencyModel {",
            "   public DependencyModel() {};",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            importKeepAnnotation,
            importNonNullAnnotation,
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public static final void inject(@NonNull final Activity target) {",
            "       target.dependency = ModuleFile.debug();",
            "   }",
            "}")

        assertAbout(javaSources())
            .that(listOf(activityFile, debugQualifierFile, moduleFile, dependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }


    @Test
    @Throws(Exception::class)
    fun classQualifiedAsArgumentQualifiedDebugNescafe() {

        val releaseQualifierFile = JavaFileObjects.forSourceLines("test.ReleaseQualifier",
            "package test;",
            "",
            importQualifierAnnotation,
            "",
            "@Qualifier",
            "public @interface ReleaseQualifier {",
            "}")

        val debugQualifierFile = JavaFileObjects.forSourceLines("test.DebugQualifier",
            "package test;",
            "",
            importQualifierAnnotation,
            "",
            "@Qualifier",
            "public @interface DebugQualifier {",
            "}")

        val cappuccinoQualifierFile = JavaFileObjects.forSourceLines("test.CappuccinoQualifier",
            "package test;",
            "",
            importQualifierAnnotation,
            "",
            "@Qualifier",
            "public @interface CappuccinoQualifier {",
            "}")

        val nescafeQualifierFile = JavaFileObjects.forSourceLines("test.NescafeQualifier",
            "package test;",
            "",
            importQualifierAnnotation,
            "",
            "@Qualifier",
            "public @interface NescafeQualifier {",
            "}")

        val viewModelFile = JavaFileObjects.forSourceLines("test.ViewModel",
            "package test;",
            "public class ViewModel {",
            "}")

        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public MainPresenter presenter;",
            "   @Inject",
            "   public ViewModel viewModel;",
            "}")

        val presenter = JavaFileObjects.forSourceLines("test.MainPresenter",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public class MainPresenter {",
            "   @Inject",
            "   MainPresenter(@DebugQualifier DependencyModel dependency) {}",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "public interface DependencyModel {",
            "}")

        val release = JavaFileObjects.forSourceLines("test.ReleaseModel",
            "package test;",
            "",
            importInjectAnnotation,
            importDependencyAnnotation,
            "",
            "@Dependency",
            "@ReleaseQualifier",
            "public class ReleaseModel implements DependencyModel {",
            "   @Inject",
            "   ReleaseModel(@CappuccinoQualifier Coffee coffee) {}",
            "}")

        val debug = JavaFileObjects.forSourceLines("test.DebugModel",
            "package test;",
            "",
            importQualifierAnnotation,
            importInjectAnnotation,
            importDependencyAnnotation,
            "",
            "@Dependency",
            "@DebugQualifier",
            "public class DebugModel implements DependencyModel {",
            "   @Inject",
            "   DebugModel(@NescafeQualifier Coffee coffee) {}",
            "}")

        val coffee = JavaFileObjects.forSourceLines("test.Coffee",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public interface Coffee {",
            "}")

        val cappuccino = JavaFileObjects.forSourceLines("test.Cappuccino",
            "package test;",
            "",
            importQualifierAnnotation,
            importDependencyAnnotation,
            "",
            "@Dependency",
            "@CappuccinoQualifier",
            "public class Cappuccino implements Coffee {",
            "   public Cappuccino() {};",
            "}")

        val nescafe = JavaFileObjects.forSourceLines("test.Nescafe",
            "package test;",
            "",
            importQualifierAnnotation,
            importDependencyAnnotation,
            "",
            "@Dependency",
            "@NescafeQualifier",
            "public class Nescafe implements Coffee {",
            "   public Nescafe() {};",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            importKeepAnnotation,
            importNonNullAnnotation,
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public static final void inject(@NonNull final Activity target) {",
            "       target.viewModel = new ViewModel();",
            "       target.presenter = provideMainPresenter();",
            "   }",
            "",
            "   public static final MainPresenter provideMainPresenter() {",
            "       Nescafe coffee = new Nescafe();",
            "       DebugModel dependencyModel = new DebugModel(coffee);",
            "       MainPresenter mainPresenter = new MainPresenter(dependencyModel);",
            "       return mainPresenter;",
            "   }",
            "}")

        assertAbout(javaSources())
            .that(listOf(activityFile, debugQualifierFile, viewModelFile, releaseQualifierFile, cappuccinoQualifierFile, nescafeQualifierFile, cappuccino, nescafe, release, debug, presenter, coffee, dependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun abuseError() {

        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public DependencyModel dependency;",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "public interface DependencyModel {",
            "}")

        val release = JavaFileObjects.forSourceLines("test.ReleaseModel",
            "package test;",
            importDependencyAnnotation,
            "",
            "@Dependency",
            "public class ReleaseModel implements DependencyModel {",
            "}")

        val debug = JavaFileObjects.forSourceLines("test.DebugModel",
            "package test;",
            importDependencyAnnotation,
            "",
            "@Dependency",
            "public class DebugModel implements DependencyModel {",
            "}")

        assertAbout(javaSources())
            .that(listOf(activityFile, release, debug, dependencyFile))
            .processedWith(IProcessor())
            .failsToCompile()
            .withErrorContaining("Found more than one implementation of `test.DependencyModel` with qualifier `@Default` [test.ReleaseModel, test.DebugModel]")
            .`in`(activityFile)
            .onLine(8)
    }

    @Test
    @Throws(Exception::class)
    fun named() {

        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            importQualifierAnnotation,
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   @Qualifier(\"test\")",
            "   public DependencyModel dependency;",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            importQualifierAnnotation,
            "",
            "@Qualifier(\"test\")",
            "abstract class DependencyModel {",
            "}")

        val implementation = JavaFileObjects.forSourceLines("test.DependencyImpl",
            "package test;",
            importDependencyAnnotation,
            "",
            "@Dependency",
            "public class DependencyImpl extends DependencyModel {",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            importKeepAnnotation,
            importNonNullAnnotation,
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public static final void inject(@NonNull final Activity target) {",
            "       target.dependency = new DependencyImpl();",
            "   }",
            "}")

        assertAbout(javaSources())
            .that(listOf(activityFile, implementation, dependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }


    @Test
    @Throws(Exception::class)
    fun uniqueNames() {

        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public Presenter presenter;",
            "}")

        val stringProvider = JavaFileObjects.forSourceLines("test.StringProvider",
            "package test;",
            "",
            "public interface StringProvider {",
            "}")

        val applicationContextProvider = JavaFileObjects.forSourceLines("test.ApplicationContextProvider",
            "package test;",
            importAndroidContext,
            "public interface ApplicationContextProvider {",
            "   public Context context();",
            "}")

        val appStringProvider = JavaFileObjects.forSourceLines("test.AppStringProvider",
            "package test;",
            importAndroidContext,
            importDependencyAnnotation,
            "@Dependency",
            "public class AppStringProvider implements StringProvider {",
            "   AppStringProvider(ApplicationContextProvider context) {}",
            "}")

        val contextModule = JavaFileObjects.forSourceLines("test.ContextModule",
            "package test;",
            importAndroidContext,
            importDependencyAnnotation,

            "public class ContextModule {",
            "   @Dependency",
            "   public static ApplicationContextProvider context() { return null; }",
            "}")

        val firstDependency = JavaFileObjects.forSourceLines("test.FirstDependency",
            "package test;",

            "public class FirstDependency {",
            "   FirstDependency(StringProvider stringProvider) {}",
            "}")

        val secondDependency = JavaFileObjects.forSourceLines("test.SecondDependency",
            "package test;",

            "public class SecondDependency {",
            "   SecondDependency(StringProvider stringProvider) {}",
            "}")

        val thirdDependency = JavaFileObjects.forSourceLines("test.ThirdDependency",
            "package test;",

            "public class ThirdDependency {",
            "   ThirdDependency(StringProvider stringProvider) {}",
            "}")

        val presenter = JavaFileObjects.forSourceLines("test.Presenter",
            "package test;",

            "public class Presenter {",
            "   Presenter(FirstDependency firstDependency, SecondDependency secondDependency, ThirdDependency thirdDependency) {}",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            importKeepAnnotation,
            importNonNullAnnotation,
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public static final void inject(@NonNull final Activity target) {",
            "       target.presenter = providePresenter();",
            "   }",
            "",
            "   public static final Presenter providePresenter() {",
            "       ApplicationContextProvider applicationContextProvider = ContextModule.context();",
            "       AppStringProvider stringProvider = new AppStringProvider(applicationContextProvider);",
            "       FirstDependency firstDependency = new FirstDependency(stringProvider);",
            "       ApplicationContextProvider applicationContextProvider2 = ContextModule.context();",
            "       AppStringProvider stringProvider2 = new AppStringProvider(applicationContextProvider2);",
            "       SecondDependency secondDependency = new SecondDependency(stringProvider2);",
            "       ApplicationContextProvider applicationContextProvider3 = ContextModule.context();",
            "       AppStringProvider stringProvider3 = new AppStringProvider(applicationContextProvider3);",
            "       ThirdDependency thirdDependency = new ThirdDependency(stringProvider3);",
            "       Presenter presenter = new Presenter(firstDependency, secondDependency, thirdDependency);",
            "       return presenter;",
            "   }",
            "}")

        assertAbout(javaSources())
            .that(listOf(activityFile, stringProvider, contextModule, applicationContextProvider, appStringProvider, firstDependency, secondDependency, presenter, thirdDependency))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun namedOnSetterMethod() {

        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            importQualifierAnnotation,
            "",
            "public class Activity {",
            "   @Inject",
            "   @Qualifier(\"Debug\")",
            "   public void setDebugFromMethod(DependencyModel dependency) {};",
            "   @Inject",
            "   @Qualifier(\"Release\")",
            "   public void setReleaseFromMethod(DependencyModel dependency) {};",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "public interface DependencyModel {",
            "}")

        val debugDependencyFile = JavaFileObjects.forSourceLines("test.DebugDependency",
            "package test;",
            importSingletonAnnotation,
            importQualifierAnnotation,
            importDependencyAnnotation,
            "@Singleton",
            "@Dependency",
            "@Qualifier(\"Debug\")",
            "public class DebugDependency implements DependencyModel {",
            "   public DebugDependency() {};",
            "}")

        val releaseDependencyFile = JavaFileObjects.forSourceLines("test.ReleaseDependency",
            "package test;",
            importSingletonAnnotation,
            importQualifierAnnotation,
            importDependencyAnnotation,
            "@Singleton",
            "@Dependency",
            "@Qualifier(\"Release\")",
            "public class ReleaseDependency implements DependencyModel {",
            "   public ReleaseDependency() {};",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            importKeepAnnotation,
            importNonNullAnnotation,
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public static final void inject(@NonNull final Activity target) {",
            "       target.setDebugFromMethod(DebugDependencySingleton.getInstance());",
            "       target.setReleaseFromMethod(ReleaseDependencySingleton.getInstance());",
            "   }",
            "}")

        assertAbout(javaSources())
            .that(listOf(activityFile, debugDependencyFile, releaseDependencyFile, dependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    fun failMethodProviderSingletonReturnsAbstractType() {

        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            importQualifierAnnotation,
            "",
            "public class Activity {",
            "   @Inject",
            "   @Qualifier(\"Debug\")",
            "   public void setDebugFromMethod(DependencyModel dependency) {};",
            "   @Inject",
            "   @Qualifier(\"Release\")",
            "   public void setReleaseFromMethod(DependencyModel dependency) {};",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "public interface DependencyModel {",
            "}")

        val debugDependencyFile = JavaFileObjects.forSourceLines("test.DebugDependency",
            "package test;",
            importSingletonAnnotation,
            "@Singleton",
            "public class DebugDependency implements DependencyModel {",
            "   public DebugDependency() {};",
            "}")

        val releaseDependencyFile = JavaFileObjects.forSourceLines("test.ReleaseDependency",
            "package test;",
            importSingletonAnnotation,
            "@Singleton",
            "public class ReleaseDependency implements DependencyModel {",
            "   public ReleaseDependency() {};",
            "}")

        val module = JavaFileObjects.forSourceLines("test.Module",
            "package test;",
            "",
            importQualifierAnnotation,
            importDependencyAnnotation,
            importSingletonAnnotation,
            "public class Module {",
            "   @Qualifier(\"Debug\")",
            "   @Dependency",
            "   @Singleton",
            "   public static DependencyModel debug() { return null; }",
            "",
            "   @Qualifier(\"Release\")",
            "   @Dependency",
            "   public static DependencyModel release() { return null; }",
            "}")

        assertAbout(javaSources())
            .that(listOf(activityFile, debugDependencyFile, module, releaseDependencyFile, dependencyFile))
            .processedWith(IProcessor())
            .failsToCompile()
            .withErrorContaining("`test.Module.debug()` annotated with @Singleton must returns implementation not abstract type")
            .`in`(module)
            .onLine(10)
    }
}