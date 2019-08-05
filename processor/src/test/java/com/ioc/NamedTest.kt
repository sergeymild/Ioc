package com.ioc

import android.content.Context
import com.google.common.truth.Truth.assertAbout
import com.google.testing.compile.JavaFileObjects
import com.google.testing.compile.JavaSourcesSubjectFactory.javaSources
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.util.*
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Qualifier

/**
 * Created by sergeygolishnikov on 09/08/2017.
 */
@RunWith(JUnit4::class)
class NamedTest : BaseTest {
    @Test
    @Throws(Exception::class)
    fun moduleMethodNamedFieldNamed() {

        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Inject::class.java.import(),
            Named::class.java.import(),
            "",
            "public class Activity {",
            "",
            "   @Inject @Named(\"debug\")",
            "   public DependencyModel dependency;",
            "}")

        val moduleFile = JavaFileObjects.forSourceLines("test.ModuleFile",
            "package test;",
            "",
            Dependency::class.java.import(),
            Named::class.java.import(),
            "",
            "public class ModuleFile {",
            "",
            "   @Dependency @Named(\"production\")",
            "   public static DependencyModel production() { return null; }",
            "   @Dependency @Named(\"debug\")",
            "   public static DependencyModel debug() { return null; }",
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
            "   public final void inject(@NonNull final Activity target) {",
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
            Inject::class.java.import(),
            Named::class.java.import(),
            "",
            "public class Activity {",
            "",
            "   @Inject @Named(\"debug\")",
            "   public DependencyModel dependency;",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            Named::class.java.import(),
            "",
            "@Named(\"debug\")",
            "class DependencyModel {",
            "   public DependencyModel() {};",
            "}")

        val moduleFile = JavaFileObjects.forSourceLines("test.ModuleFile",
            "package test;",
            "",
            Dependency::class.java.import(),
            Named::class.java.import(),
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
            "import $keep",
            "import $nonNull",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
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
            Inject::class.java.import(),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public ParentDependency parentDependency;",
            "}")

        val parentDependencyFile = JavaFileObjects.forSourceLines("test.ParentDependency",
            "package test;",
            "",
            Named::class.java.import(),
            Inject::class.java.import(),
            "",
            "class ParentDependency {",
            "   @Inject",
            "   public ParentDependency(@Named(\"release\") DependencyModel dependency) {};",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "interface DependencyModel {",
            "}")

        val debugDependencyFile = JavaFileObjects.forSourceLines("test.DebugDependency",
            "package test;",
            "",
            "class DebugDependency implements DependencyModel {",
            "   public DebugDependency() {};",
            "}")

        val releaseDependencyFile = JavaFileObjects.forSourceLines("test.ReleaseDependency",
            "package test;",
            "",
            "class ReleaseDependency implements DependencyModel {",
            "   public ReleaseDependency() {};",
            "}")

        val moduleFile = JavaFileObjects.forSourceLines("test.DependencyModuleFile",
            "package test;",
            "",
            Dependency::class.java.import(),
            Named::class.java.import(),
            "",
            "public class DependencyModuleFile {",
            "",
            "   @Dependency",
            "   @Named(\"release\")",
            "   public static DependencyModel release() { return null; }",
            "   @Dependency",
            "   @Named(\"debug\")",
            "   public static DependencyModel debug() { return null; }",
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
            "   public final void inject(@NonNull final Activity target) {",
            "       target.parentDependency = provideParentDependency();",
            "   }",
            "",
            "   private final ParentDependency provideParentDependency() {",
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
            Inject::class.java.import(),
            Named::class.java.import(),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   @Named(\"release\")",
            "   public DependencyModel dependency;",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "interface DependencyModel {",
            "}")

        val debugDependencyFile = JavaFileObjects.forSourceLines("test.DebugDependency",
            "package test;",
            "",
            Named::class.java.import(),
            Dependency::class.java.import(),
            "",
            "@Dependency",
            "@Named(\"debug\")",
            "class DebugDependency implements DependencyModel {",
            "   public DebugDependency() {};",
            "}")

        val releaseDependencyFile = JavaFileObjects.forSourceLines("test.ReleaseDependency",
            "package test;",
            "",
            Named::class.java.import(),
            Dependency::class.java.import(),
            "",
            "@Dependency",
            "@Named(\"release\")",
            "class ReleaseDependency implements DependencyModel {",
            "   public ReleaseDependency() {};",
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
            "   public final void inject(@NonNull final Activity target) {",
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
            Inject::class.java.import(),
            Named::class.java.import(),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public MainPresenter presenter;",
            "}")

        val presenter = JavaFileObjects.forSourceLines("test.MainPresenter",
            "package test;",
            "",
            Inject::class.java.import(),
            Named::class.java.import(),
            "",
            "class MainPresenter {",
            "   @Inject",
            "   MainPresenter(@Named(\"release\") DependencyModel dependency) {}",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "interface DependencyModel {",
            "}")

        val debugDependencyFile = JavaFileObjects.forSourceLines("test.DebugDependency",
            "package test;",
            "",
            Named::class.java.import(),
            Dependency::class.java.import(),
            "",
            "@Dependency",
            "@Named(\"debug\")",
            "class DebugDependency implements DependencyModel {",
            "   public DebugDependency() {};",
            "}")

        val releaseDependencyFile = JavaFileObjects.forSourceLines("test.ReleaseDependency",
            "package test;",
            "",
            Named::class.java.import(),
            Dependency::class.java.import(),
            "",
            "@Dependency",
            "@Named(\"release\")",
            "class ReleaseDependency implements DependencyModel {",
            "   public ReleaseDependency() {};",
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
            "   public final void inject(@NonNull final Activity target) {",
            "       target.presenter = provideMainPresenter();",
            "   }",
            "",
            "   private final MainPresenter provideMainPresenter() {",
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
            Inject::class.java.import(),
            Named::class.java.import(),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public MainPresenter presenter;",
            "}")

        val presenter = JavaFileObjects.forSourceLines("test.MainPresenter",
            "package test;",
            "",
            Inject::class.java.import(),
            Named::class.java.import(),
            "",
            "class MainPresenter {",
            "   @Inject",
            "   MainPresenter(DependencyModel dependency) {}",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            Inject::class.java.import(),
            Named::class.java.import(),
            "",
            "class DependencyModel {",
            "   @Inject",
            "   DependencyModel(@Named(\"cappuccino\") Coffee coffee) {}",
            "}")

        val coffee = JavaFileObjects.forSourceLines("test.Coffee",
            "package test;",
            "",
            Inject::class.java.import(),
            "",
            "interface Coffee {",
            "}")

        val cappuccino = JavaFileObjects.forSourceLines("test.Cappuccino",
            "package test;",
            "",
            Named::class.java.import(),
            Dependency::class.java.import(),
            "",
            "@Dependency",
            "@Named(\"cappuccino\")",
            "class Cappuccino implements Coffee {",
            "   public Cappuccino() {};",
            "}")

        val nescafe = JavaFileObjects.forSourceLines("test.Nescafe",
            "package test;",
            "",
            Named::class.java.import(),
            Dependency::class.java.import(),
            "",
            "@Dependency",
            "@Named(\"nescafe\")",
            "class Nescafe implements Coffee {",
            "   public Nescafe() {};",
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
            "   public final void inject(@NonNull final Activity target) {",
            "       target.presenter = provideMainPresenter();",
            "   }",
            "",
            "   private final MainPresenter provideMainPresenter() {",
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
            Inject::class.java.import(),
            Named::class.java.import(),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public MainPresenter presenter;",
            "}")

        val presenter = JavaFileObjects.forSourceLines("test.MainPresenter",
            "package test;",
            "",
            Inject::class.java.import(),
            Named::class.java.import(),
            "",
            "class MainPresenter {",
            "   @Inject",
            "   MainPresenter(@Named(\"release\") DependencyModel dependency) {}",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "interface DependencyModel {",
            "}")

        val release = JavaFileObjects.forSourceLines("test.ReleaseModel",
            "package test;",
            "",
            Named::class.java.import(),
            Inject::class.java.import(),
            Dependency::class.java.import(),
            "",
            "@Dependency",
            "@Named(\"release\")",
            "class ReleaseModel implements DependencyModel {",
            "   @Inject",
            "   ReleaseModel(@Named(\"cappuccino\") Coffee coffee) {}",
            "}")

        val debug = JavaFileObjects.forSourceLines("test.DebugModel",
            "package test;",
            "",
            Named::class.java.import(),
            Inject::class.java.import(),
            Dependency::class.java.import(),
            "",
            "@Dependency",
            "@Named(\"debug\")",
            "class DebugModel implements DependencyModel {",
            "   @Inject",
            "   DebugModel(@Named(\"debug\") Coffee coffee) {}",
            "}")

        val coffee = JavaFileObjects.forSourceLines("test.Coffee",
            "package test;",
            "",
            Inject::class.java.import(),
            "",
            "interface Coffee {",
            "}")

        val cappuccino = JavaFileObjects.forSourceLines("test.Cappuccino",
            "package test;",
            "",
            Named::class.java.import(),
            Dependency::class.java.import(),
            "",
            "@Dependency",
            "@Named(\"cappuccino\")",
            "class Cappuccino implements Coffee {",
            "   public Cappuccino() {};",
            "}")

        val nescafe = JavaFileObjects.forSourceLines("test.Nescafe",
            "package test;",
            "",
            Named::class.java.import(),
            Dependency::class.java.import(),
            "",
            "@Dependency",
            "@Named(\"nescafe\")",
            "class Nescafe implements Coffee {",
            "   public Nescafe() {};",
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
            "   public final void inject(@NonNull final Activity target) {",
            "       target.presenter = provideMainPresenter();",
            "   }",
            "",
            "   private final MainPresenter provideMainPresenter() {",
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
            Inject::class.java.import(),
            Named::class.java.import(),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public MainPresenter presenter;",
            "}")

        val presenter = JavaFileObjects.forSourceLines("test.MainPresenter",
            "package test;",
            "",
            Inject::class.java.import(),
            Named::class.java.import(),
            "",
            "class MainPresenter {",
            "   @Inject",
            "   MainPresenter(@Named(\"debug\") DependencyModel dependency) {}",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "interface DependencyModel {",
            "}")

        val release = JavaFileObjects.forSourceLines("test.ReleaseModel",
            "package test;",
            "",
            Named::class.java.import(),
            Inject::class.java.import(),
            Dependency::class.java.import(),
            "",
            "@Dependency",
            "@Named(\"release\")",
            "class ReleaseModel implements DependencyModel {",
            "   @Inject",
            "   ReleaseModel(@Named(\"cappuccino\") Coffee coffee) {}",
            "}")

        val debug = JavaFileObjects.forSourceLines("test.DebugModel",
            "package test;",
            "",
            Named::class.java.import(),
            Inject::class.java.import(),
            Dependency::class.java.import(),
            "",
            "@Dependency",
            "@Named(\"debug\")",
            "class DebugModel implements DependencyModel {",
            "   @Inject",
            "   DebugModel(@Named(\"cappuccino\") Coffee coffee) {}",
            "}")

        val coffee = JavaFileObjects.forSourceLines("test.Coffee",
            "package test;",
            "",
            Inject::class.java.import(),
            "",
            "interface Coffee {",
            "}")

        val cappuccino = JavaFileObjects.forSourceLines("test.Cappuccino",
            "package test;",
            "",
            Named::class.java.import(),
            Dependency::class.java.import(),
            "",
            "@Dependency",
            "@Named(\"cappuccino\")",
            "class Cappuccino implements Coffee {",
            "   public Cappuccino() {};",
            "}")

        val nescafe = JavaFileObjects.forSourceLines("test.Nescafe",
            "package test;",
            "",
            Named::class.java.import(),
            Dependency::class.java.import(),
            "",
            "@Dependency",
            "@Named(\"nescafe\")",
            "class Nescafe implements Coffee {",
            "   public Nescafe() {};",
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
            "   public final void inject(@NonNull final Activity target) {",
            "       target.presenter = provideMainPresenter();",
            "   }",
            "",
            "   private final MainPresenter provideMainPresenter() {",
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
            Inject::class.java.import(),
            Named::class.java.import(),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public MainPresenter presenter;",
            "}")

        val presenter = JavaFileObjects.forSourceLines("test.MainPresenter",
            "package test;",
            "",
            Inject::class.java.import(),
            Named::class.java.import(),
            "",
            "class MainPresenter {",
            "   @Inject",
            "   MainPresenter(@Named(\"debug\") DependencyModel dependency) {}",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "interface DependencyModel {",
            "}")

        val release = JavaFileObjects.forSourceLines("test.ReleaseModel",
            "package test;",
            "",
            Named::class.java.import(),
            Inject::class.java.import(),
            Dependency::class.java.import(),
            "",
            "@Dependency",
            "@Named(\"release\")",
            "class ReleaseModel implements DependencyModel {",
            "   @Inject",
            "   ReleaseModel(@Named(\"cappuccino\") Coffee coffee) {}",
            "}")

        val debug = JavaFileObjects.forSourceLines("test.DebugModel",
            "package test;",
            "",
            Named::class.java.import(),
            Inject::class.java.import(),
            Dependency::class.java.import(),
            "",
            "@Dependency",
            "@Named(\"debug\")",
            "class DebugModel implements DependencyModel {",
            "   @Inject",
            "   DebugModel(@Named(\"nescafe\") Coffee coffee) {}",
            "}")

        val coffee = JavaFileObjects.forSourceLines("test.Coffee",
            "package test;",
            "",
            Inject::class.java.import(),
            "",
            "interface Coffee {",
            "}")

        val cappuccino = JavaFileObjects.forSourceLines("test.Cappuccino",
            "package test;",
            "",
            Named::class.java.import(),
            Dependency::class.java.import(),
            "",
            "@Dependency",
            "@Named(\"cappuccino\")",
            "class Cappuccino implements Coffee {",
            "   public Cappuccino() {};",
            "}")

        val nescafe = JavaFileObjects.forSourceLines("test.Nescafe",
            "package test;",
            "",
            Named::class.java.import(),
            Dependency::class.java.import(),
            "",
            "@Dependency",
            "@Named(\"nescafe\")",
            "class Nescafe implements Coffee {",
            "   public Nescafe() {};",
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
            "   public final void inject(@NonNull final Activity target) {",
            "       target.presenter = provideMainPresenter();",
            "   }",
            "",
            "   private final MainPresenter provideMainPresenter() {",
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
            Qualifier::class.java.import(),
            "",
            "@Qualifier",
            "public @interface DebugQualifier {",
            "}")


        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Inject::class.java.import(),
            "",
            "public class Activity {",
            "",
            "   @Inject @DebugQualifier",
            "   public DependencyModel dependency;",
            "}")

        val moduleFile = JavaFileObjects.forSourceLines("test.ModuleFile",
            "package test;",
            "",
            Dependency::class.java.import(),
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
            "   public final void inject(@NonNull final Activity target) {",
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
            Qualifier::class.java.import(),
            "",
            "@Qualifier",
            "public @interface ReleaseQualifier {",
            "}")

        val debugQualifierFile = JavaFileObjects.forSourceLines("test.DebugQualifier",
            "package test;",
            "",
            Qualifier::class.java.import(),
            "",
            "@Qualifier",
            "public @interface DebugQualifier {",
            "}")

        val cappuccinoQualifierFile = JavaFileObjects.forSourceLines("test.CappuccinoQualifier",
            "package test;",
            "",
            Qualifier::class.java.import(),
            "",
            "@Qualifier",
            "public @interface CappuccinoQualifier {",
            "}")

        val nescafeQualifierFile = JavaFileObjects.forSourceLines("test.NescafeQualifier",
            "package test;",
            "",
            Qualifier::class.java.import(),
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
            Inject::class.java.import(),
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
            Inject::class.java.import(),
            "",
            "class MainPresenter {",
            "   @Inject",
            "   MainPresenter(@DebugQualifier DependencyModel dependency) {}",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "interface DependencyModel {",
            "}")

        val release = JavaFileObjects.forSourceLines("test.ReleaseModel",
            "package test;",
            "",
            Inject::class.java.import(),
            Dependency::class.java.import(),
            "",
            "@Dependency",
            "@ReleaseQualifier",
            "class ReleaseModel implements DependencyModel {",
            "   @Inject",
            "   ReleaseModel(@CappuccinoQualifier Coffee coffee) {}",
            "}")

        val debug = JavaFileObjects.forSourceLines("test.DebugModel",
            "package test;",
            "",
            Named::class.java.import(),
            Inject::class.java.import(),
            Dependency::class.java.import(),
            "",
            "@Dependency",
            "@DebugQualifier",
            "class DebugModel implements DependencyModel {",
            "   @Inject",
            "   DebugModel(@NescafeQualifier Coffee coffee) {}",
            "}")

        val coffee = JavaFileObjects.forSourceLines("test.Coffee",
            "package test;",
            "",
            Inject::class.java.import(),
            "",
            "interface Coffee {",
            "}")

        val cappuccino = JavaFileObjects.forSourceLines("test.Cappuccino",
            "package test;",
            "",
            Named::class.java.import(),
            Dependency::class.java.import(),
            "",
            "@Dependency",
            "@CappuccinoQualifier",
            "class Cappuccino implements Coffee {",
            "   public Cappuccino() {};",
            "}")

        val nescafe = JavaFileObjects.forSourceLines("test.Nescafe",
            "package test;",
            "",
            Named::class.java.import(),
            Dependency::class.java.import(),
            "",
            "@Dependency",
            "@NescafeQualifier",
            "class Nescafe implements Coffee {",
            "   public Nescafe() {};",
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
            "   public final void inject(@NonNull final Activity target) {",
            "       target.viewModel = new ViewModel();",
            "       target.presenter = provideMainPresenter();",
            "   }",
            "",
            "   private final MainPresenter provideMainPresenter() {",
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
            Inject::class.java.import(),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public DependencyModel dependency;",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "interface DependencyModel {",
            "}")

        val release = JavaFileObjects.forSourceLines("test.ReleaseModel",
            "package test;",
            Dependency::class.java.import(),
            "",
            "@Dependency",
            "class ReleaseModel implements DependencyModel {",
            "}")

        val debug = JavaFileObjects.forSourceLines("test.DebugModel",
            "package test;",
            Dependency::class.java.import(),
            "",
            "@Dependency",
            "class DebugModel implements DependencyModel {",
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
            Inject::class.java.import(),
            Named::class.java.import(),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   @Named(\"test\")",
            "   public DependencyModel dependency;",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            Named::class.java.import(),
            "",
            "@Named(\"test\")",
            "abstract class DependencyModel {",
            "}")

        val implementation = JavaFileObjects.forSourceLines("test.DependencyImpl",
            "package test;",
            Dependency::class.java.import(),
            "",
            "@Dependency",
            "class DependencyImpl extends DependencyModel {",
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
            "   public final void inject(@NonNull final Activity target) {",
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
            Inject::class.java.import(),
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public Presenter presenter;",
            "}")

        val stringProvider = JavaFileObjects.forSourceLines("test.StringProvider",
            "package test;",
            "",
            "interface StringProvider {",
            "}")

        val applicationContextProvider = JavaFileObjects.forSourceLines("test.ApplicationContextProvider",
            "package test;",
            Context::class.java.import(),
            "interface ApplicationContextProvider {",
            "   public Context context();",
            "}")

        val appStringProvider = JavaFileObjects.forSourceLines("test.AppStringProvider",
            "package test;",
            Context::class.java.import(),
            Dependency::class.java.import(),
            "@Dependency",
            "class AppStringProvider implements StringProvider {",
            "   AppStringProvider(ApplicationContextProvider context) {}",
            "}")

        val contextModule = JavaFileObjects.forSourceLines("test.ContextModule",
            "package test;",
            Context::class.java.import(),
            Dependency::class.java.import(),

            "class ContextModule {",
            "   @Dependency",
            "   public static ApplicationContextProvider context() { return null; }",
            "}")

        val firstDependency = JavaFileObjects.forSourceLines("test.FirstDependency",
            "package test;",

            "class FirstDependency {",
            "   FirstDependency(StringProvider stringProvider) {}",
            "}")

        val secondDependency = JavaFileObjects.forSourceLines("test.SecondDependency",
            "package test;",

            "class SecondDependency {",
            "   SecondDependency(StringProvider stringProvider) {}",
            "}")

        val thirdDependency = JavaFileObjects.forSourceLines("test.ThirdDependency",
            "package test;",

            "class ThirdDependency {",
            "   ThirdDependency(StringProvider stringProvider) {}",
            "}")

        val presenter = JavaFileObjects.forSourceLines("test.Presenter",
            "package test;",

            "class Presenter {",
            "   Presenter(FirstDependency firstDependency, SecondDependency secondDependency, ThirdDependency thirdDependency) {}",
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
            "   public final void inject(@NonNull final Activity target) {",
            "       target.presenter = providePresenter();",
            "   }",
            "",
            "   private final Presenter providePresenter() {",
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
            "import $inject;",
            "import $named;",
            "",
            "public class Activity {",
            "   @Inject",
            "   @Named(\"Debug\")",
            "   public void setDebugFromMethod(DependencyModel dependency) {};",
            "   @Inject",
            "   @Named(\"Release\")",
            "   public void setReleaseFromMethod(DependencyModel dependency) {};",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "interface DependencyModel {",
            "}")

        val debugDependencyFile = JavaFileObjects.forSourceLines("test.DebugDependency",
            "package test;",
            "import $singleton;",
            "import $named;",
            "import $dependency;",
            "@Singleton",
            "@Dependency",
            "@Named(\"Debug\")",
            "class DebugDependency implements DependencyModel {",
            "   public DebugDependency() {};",
            "}")

        val releaseDependencyFile = JavaFileObjects.forSourceLines("test.ReleaseDependency",
            "package test;",
            "import $singleton;",
            "import $named;",
            "import $dependency;",
            "@Singleton",
            "@Dependency",
            "@Named(\"Release\")",
            "class ReleaseDependency implements DependencyModel {",
            "   public ReleaseDependency() {};",
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
            "       target.setDebugFromMethod(Ioc.singleton(DebugDependency.class));",
            "       target.setReleaseFromMethod(Ioc.singleton(ReleaseDependency.class));",
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
            "import $inject;",
            "import $named;",
            "",
            "public class Activity {",
            "   @Inject",
            "   @Named(\"Debug\")",
            "   public void setDebugFromMethod(DependencyModel dependency) {};",
            "   @Inject",
            "   @Named(\"Release\")",
            "   public void setReleaseFromMethod(DependencyModel dependency) {};",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "interface DependencyModel {",
            "}")

        val debugDependencyFile = JavaFileObjects.forSourceLines("test.DebugDependency",
            "package test;",
            "import $singleton;",
            "@Singleton",
            "class DebugDependency implements DependencyModel {",
            "   public DebugDependency() {};",
            "}")

        val releaseDependencyFile = JavaFileObjects.forSourceLines("test.ReleaseDependency",
            "package test;",
            "import $singleton;",
            "@Singleton",
            "class ReleaseDependency implements DependencyModel {",
            "   public ReleaseDependency() {};",
            "}")

        val module = JavaFileObjects.forSourceLines("test.Module",
            "package test;",
            "",
            "import $named;",
            "import $dependency;",
            "import $singleton;",
            "class Module {",
            "   @Named(\"Debug\")",
            "   @Dependency",
            "   @Singleton",
            "   public static DependencyModel debug() { return null; }",
            "",
            "   @Named(\"Release\")",
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