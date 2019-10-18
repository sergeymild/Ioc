package com.ioc

import com.google.common.truth.Truth
import com.google.testing.compile.JavaFileObjects
import com.google.testing.compile.JavaSourcesSubjectFactory
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.util.*

/**
 * Created by sergeygolishnikov on 20/11/2017.
 */
@RunWith(JUnit4::class)
class ConstructorTests {

    @Test
    @Throws(Exception::class)
    fun constructorWithArgumentsWithoutInjectAnnotation() {

        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public ParentDependency dependency;",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "class DependencyModel {",
            "   public DependencyModel() {};",
            "}")

        val parentDependencyFile = JavaFileObjects.forSourceLines("test.ParentDependency",
            "package test;",
            "",
            importInjectAnnotation,
            "public class ParentDependency {",
            "   @Inject",
            "   public ParentDependency(DependencyModel argument) {}",
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
            "       target.dependency = provideParentDependency();",
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
    fun unsupportedConstructor() {

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
            importInjectAnnotation,
            "",
            "class DependencyModel {",
            "   @Inject",
            "   public DependencyModel(int number) {};",
            "}")

        Truth.assertAbout(JavaSourcesSubjectFactory.javaSources())
            .that(listOf(activityFile, dependencyFile))
            .processedWith(IProcessor())
            .failsToCompile()
            .withErrorContaining("@Inject annotation placed on constructor in test.DependencyModel which have unsupported parameters.")
            .`in`(dependencyFile)
            .onLine(7)
    }


    @Test
    @Throws(Exception::class)
    fun privatePostInitialization() {

        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            importPostInitializationAnnotation,
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public DependencyModel dependency;",
            "   @PostInitialization",
            "   private void postInitialization() {}",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "class DependencyModel {",
            "   @Inject",
            "   public DependencyModel(String string) {};",
            "}")

        Truth.assertAbout(JavaSourcesSubjectFactory.javaSources())
            .that(Arrays.asList(activityFile, dependencyFile))
            .processedWith(IProcessor())
            .failsToCompile()
            .withErrorContaining("@PostInitialization placed on `postInitialization` in test.Activity with private access")
            .`in`(activityFile)
            .onLine(11)
    }

    @Test
    @Throws(Exception::class)
    fun postInitializationWithParameters() {

        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            importPostInitializationAnnotation,
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public DependencyModel dependency;",
            "   @PostInitialization",
            "   public void postInitialization(String string) {}",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "class DependencyModel {",
            "   @Inject",
            "   public DependencyModel(String string) {};",
            "}")

        Truth.assertAbout(JavaSourcesSubjectFactory.javaSources())
            .that(Arrays.asList(activityFile, dependencyFile))
            .processedWith(IProcessor())
            .failsToCompile()
            .withErrorContaining("@PostInitialization placed on `postInitialization` in test.Activity must not have parameters")
            .`in`(activityFile)
            .onLine(11)
    }

    @Test
    @Throws(Exception::class)
    fun postInitializationWithReturnType() {

        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            importPostInitializationAnnotation,
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public DependencyModel dependency;",
            "   @PostInitialization",
            "   public String postInitialization() {return null;}",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "class DependencyModel {",
            "   @Inject",
            "   public DependencyModel() {};",
            "}")

        Truth.assertAbout(JavaSourcesSubjectFactory.javaSources())
            .that(Arrays.asList(activityFile, dependencyFile))
            .processedWith(IProcessor())
            .failsToCompile()
            .withErrorContaining("@PostInitialization placed on `postInitialization` in test.Activity must not have return type")
            .`in`(activityFile)
            .onLine(11)
    }

    @Test
    @Throws(Exception::class)
    fun successPostInitialization() {

        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            importPostInitializationAnnotation,
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public DependencyModel dependency;",
            "   @PostInitialization",
            "   public void postInitialization() {}",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "class DependencyModel {}")

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
            "       target.postInitialization();",
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
    fun successNameCollision() {

        val dbMapper = JavaFileObjects.forSourceLines("test.DbMapper",
            "package test;",
            "",
            "public interface DbMapper {",
            "}")

        val dbMapperImpl = JavaFileObjects.forSourceLines("test.DbMapperImpl",
            "package test;",
            "",
            importDependencyAnnotation,
            "",
            "@Dependency",
            "public class DbMapperImpl implements DbMapper {",
            "}")

        val dbRepository = JavaFileObjects.forSourceLines("test.DbRepository",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public class DbRepository {",
            "   @Inject",
            "   public DbRepository(DbMapper mapper) {}",
            "}")

        val managerFile = JavaFileObjects.forSourceLines("test.Manager",
            "package test;",
            "",
            importInjectAnnotation,
            importSingletonAnnotation,
            "",
            "@Singleton",
            "public class Manager {",
            "   @Inject",
            "   public Manager(DbMapper mapper, DbRepository repository) {}",
            "}")

        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            importPostInitializationAnnotation,
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public Manager manager;",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ManagerSingleton",
            "package test;",
            "",
            importKeepAnnotation,
            importProvider,
            "",
            "@Keep",
            "public final class ManagerSingleton implements Provider<Manager> {",
            "",
            "   public final Manager get() {",
            "       DbMapperImpl dbMapper = new DbMapperImpl();",
            "       DbMapperImpl dbMapper2 = new DbMapperImpl();",
            "       DbRepository dbRepository = new DbRepository(dbMapper2);",
            "       return new Manager(dbMapper, dbRepository);",
            "   }",
            "}")

        Truth.assertAbout(JavaSourcesSubjectFactory.javaSources())
            .that(listOf(dbMapper, dbMapperImpl, dbRepository, managerFile, activityFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun constructorString() {


        val managerFile = JavaFileObjects.forSourceLines("test.Manager",
            "package test;",
            importQualifierAnnotation,
            "class Manager {",
            "   public Manager(@Qualifier(\"apiVersion\") String string) {}",
            "}")

        val moduleFile = JavaFileObjects.forSourceLines("test.Module",
            "package test;",
            importDependencyAnnotation,
            importQualifierAnnotation,
            "class Module {",
            "   @Dependency",
            "   @Qualifier(\"apiVersion\")",
            "   public static String getVersion() { return null; }",
            "}")

        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            importInjectAnnotation,
            importPostInitializationAnnotation,
            "",
            "public class Activity {",
            "",
            "   @Inject",
            "   public Manager manager;",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "",
            importKeepAnnotation,
            importNonNullAnnotation,
            "import java.lang.String;",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "",
            "   @Keep",
            "   public static final void inject(@NonNull final Activity target) {",
            "       target.manager = provideManager();",
            "   }",
            "",
            "   public static final Manager provideManager() {",
            "       String string = Module.getVersion();",
            "       Manager manager = new Manager(string);",
            "       return manager;",
            "   }",
            "}")

        Truth.assertAbout(JavaSourcesSubjectFactory.javaSources())
            .that(listOf(moduleFile, managerFile, activityFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }
}