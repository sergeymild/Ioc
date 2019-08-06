package com.ioc

import com.google.common.truth.Truth
import com.google.testing.compile.JavaFileObjects
import com.google.testing.compile.JavaSourcesSubject
import com.google.testing.compile.JavaSourcesSubjectFactory
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import javax.tools.JavaFileObject

@RunWith(JUnit4::class)
class SingletonsFactoryTest {
    @Test
    fun generateFactory() {

        val secondInterface = JavaFileObjects.forSourceLines("test.SecondInterface",
            "package test;",
            "public interface SecondInterface {}")

        val firstInterface = JavaFileObjects.forSourceLines("test.FirstInterface",
            "package test;",
            "public interface FirstInterface extends SecondInterface {}")

        val secondClass = JavaFileObjects.forSourceLines("test.SecondClass",
            "package test;",
            "public class SecondClass {}")

        val firstClass = JavaFileObjects.forSourceLines("test.FirstClass",
            "package test;",
            "public class FirstClass extends SecondClass {}")

        val singletonClass = JavaFileObjects.forSourceLines("test.SingletonClass",
            "package test;",
            "",
            "import $singleton;",
            "import $dependency;",
            "",
            "@Singleton",
            "@Dependency",
            "public class SingletonClass extends FirstClass implements FirstInterface {}")

        val secondSingletonClass = JavaFileObjects.forSourceLines("test.SecondSingletonClass",
            "package test;",
            "",
            "import $singleton;",
            "",
            "@Singleton",
            "public class SecondSingletonClass {}")

        val singletonFromModule = JavaFileObjects.forSourceLines("test.SingletonFromModule",
            "package test;",
            "public class SingletonFromModule {}")


        val singletonModule = JavaFileObjects.forSourceLines("test.SingletonModule",
            "package test;",
            "import $singleton;",
            "public class SingletonModule {",
            "   @Singleton",
            "   public static SingletonFromModule fromModule() { return null; }",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("com.ioc.SingletonsFactoryImplementation",
            "package com.ioc;",
            "",
            "import $keep",
            "import java.util.HashMap;",
            "import test.FirstClass;",
            "import test.FirstInterface;",
            "import test.SecondClass;",
            "import test.SecondInterface;",
            "import test.SecondSingletonClass;",
            "import test.SecondSingletonClassSingleton;",
            "import test.SingletonClass;",
            "import test.SingletonClassSingleton;",
            "",
            "@Keep",
            "final class SingletonsFactoryImplementation extends SingletonFactory {",
            "   static {",
            "       map = new HashMap<>(6);",
            "       cachedSingletons = new HashMap<>(2);",
            "       map.put(SingletonClass.class, SingletonClassSingleton.class);",
            "       map.put(FirstInterface.class, SingletonClassSingleton.class);",
            "       map.put(FirstClass.class, SingletonClassSingleton.class);",
            "       map.put(SecondInterface.class, SingletonClassSingleton.class);",
            "       map.put(SecondClass.class, SingletonClassSingleton.class);",
            "       map.put(SecondSingletonClass.class, SecondSingletonClassSingleton.class);",
            "   }",
            "}")

        Truth.assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(JavaSourcesSubjectFactory.javaSources())
            .that(listOf(secondInterface, firstInterface, secondClass, firstClass, singletonClass, secondSingletonClass, singletonFromModule, singletonModule))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    fun failSingletonClassMustBePublic() {

        val singletonClass = JavaFileObjects.forSourceLines("test.SingletonClass",
            "package test;",
            "",
            "import $singleton;",
            "",
            "@Singleton",
            "class SingletonClass {}")


        Truth.assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(JavaSourcesSubjectFactory.javaSources())
            .that(listOf(singletonClass))
            .processedWith(IProcessor())
            .failsToCompile()
            .withErrorContaining("test.SingletonClass annotated with @Singleton must be public")
            .`in`(singletonClass)
            .onLine(6)
    }

    @Test
    fun failSingletonMethodMustReturnType() {

        val singletonModule = JavaFileObjects.forSourceLines("test.SingletonModule",
            "package test;",
            "import $singleton;",
            "public class SingletonModule {",
            "   @Singleton",
            "   public static void fromModule() { return null; }",
            "}")


        Truth.assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(JavaSourcesSubjectFactory.javaSources())
            .that(listOf(singletonModule))
            .processedWith(IProcessor())
            .failsToCompile()
            .withErrorContaining("test.SingletonModule.fromModule() annotated with @Singleton must return type")
            .`in`(singletonModule)
            .onLine(5)
    }

    @Test
    fun failSingletonMethodMustReturnValidType() {

        val singletonModule = JavaFileObjects.forSourceLines("test.SingletonModule",
            "package test;",
            "import $singleton;",
            "public class SingletonModule {",
            "   @Singleton",
            "   public static Object fromModule() { return null; }",
            "}")


        Truth.assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(JavaSourcesSubjectFactory.javaSources())
            .that(listOf(singletonModule))
            .processedWith(IProcessor())
            .failsToCompile()
            .withErrorContaining("test.SingletonModule.fromModule() annotated with @Singleton return type is not valid.")
            .`in`(singletonModule)
            .onLine(5)
    }
}