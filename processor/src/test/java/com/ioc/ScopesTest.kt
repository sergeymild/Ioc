package com.ioc

import android.app.Activity
import android.content.Context
import com.google.common.truth.Truth
import com.google.common.truth.Truth.assertAbout
import com.google.testing.compile.JavaFileObjects
import com.google.testing.compile.JavaSourcesSubject
import com.google.testing.compile.JavaSourcesSubjectFactory.javaSources
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.util.*
import javax.inject.Inject
import javax.inject.Scope
import javax.tools.JavaFileObject

/**
 * Created by sergeygolishnikov on 28/12/2017.
 */
@RunWith(JUnit4::class)
class ScopesTest : BaseTest {


    /**
     *  NEW TESTS
     * */

    @Test
    @Throws(Exception::class)
    fun nestedInjection() {


        val session = JavaFileObjects.forSourceLines("test.Session",
                "package test;",
                Context::class.java.import(),

                "@PerActivity",
                "public class Session {",
                "   Session(BrowserUi browserUi) {}",
                "}")

        val perActivity = JavaFileObjects.forSourceLines("test.PerActivity",
                "package test;",
                Context::class.java.import(),
                Scope::class.java.import(),
                Retention::class.java.import(),
                RetentionPolicy::class.java.import(),

                "@Scope",
                "@Retention(RetentionPolicy.RUNTIME)",
                "public @interface PerActivity {",
                "}")

        val browserUi = JavaFileObjects.forSourceLines("test.BrowserUi",
                "package test;",
                Context::class.java.import(),

                "public abstract class BrowserUi {",
                "   BrowserUi(Context context) {}",
                "}")

        val phoneBrowserUi = JavaFileObjects.forSourceLines("test.PhoneBrowserUi",
                "package test;",
                Context::class.java.import(),
                Dependency::class.java.import(),

                "@PerActivity",
                "@Dependency",
                "public class PhoneBrowserUi extends BrowserUi {",
                "   PhoneBrowserUi(Context context) { super(context); }",
                "}")


        val activityFile = JavaFileObjects.forSourceLines("test.MainActivity",
                "package test;",
                "",
                Inject::class.java.import(),
                ScopeRoot::class.java.import(),
                Activity::class.java.import(),
                "",
                "@ScopeRoot",
                "@PerActivity",
                "public class MainActivity extends Activity {",
                "",
                "   @Inject",
                "   public Session session;",
                "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.MainActivityInjector",
                "package test;",
                "",
                keepAnnotation,
                nonNullAnnotation,
                ScopeFactory::class.java.import(),
                "",
                "@Keep",
                "public final class MainActivityInjector {",
                "",
                "   @Keep",
                "   public final void inject(@NonNull final MainActivity target) {",
                "       cacheBrowserUi(target);",
                "       injectSessionInSession(target);",
                "   }",
                "",
                "   private final void cacheBrowserUi(@NonNull final MainActivity target) {",
                "       PhoneBrowserUi browserUi = new PhoneBrowserUi(target);",
                "       ScopeFactory.cache(target, \"PerActivity\", \"browserUi\", browserUi);",
                "   }",
                "",
                "   private final void injectSessionInSession(@NonNull final MainActivity target) {",
                "       PhoneBrowserUi browserUi = ScopeFactory.get(target, \"PerActivity\", \"browserUi\");",
                "       Session session = new Session(browserUi);",
                "       target.session = session;",
                "       ScopeFactory.cache(target, \"PerActivity\", \"session\", session);",
                "   }",
                "}")

        Truth.assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
                .that(Arrays.asList(activityFile, session, perActivity, browserUi, phoneBrowserUi))
                .processedWith(IProcessor())
                .compilesWithoutError()
                .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun nestedInjection2() {


        val session = JavaFileObjects.forSourceLines("test.Session",
                "package test;",
                Context::class.java.import(),

                "@PerActivity",
                "public class Session {",
                "   Session(BrowserUi browserUi) {}",
                "}")

        val logger = JavaFileObjects.forSourceLines("test.Logger",
                "package test;",

                "public class Logger {",
                "}")

        val perActivity = JavaFileObjects.forSourceLines("test.PerActivity",
                "package test;",
                Scope::class.java.import(),
                Retention::class.java.import(),
                RetentionPolicy::class.java.import(),

                "@Scope",
                "@Retention(RetentionPolicy.RUNTIME)",
                "public @interface PerActivity {",
                "}")

        val browserUi = JavaFileObjects.forSourceLines("test.BrowserUi",
                "package test;",
                Context::class.java.import(),

                "public abstract class BrowserUi {",
                "   BrowserUi(Context context) {}",
                "}")

        val phoneBrowserUi = JavaFileObjects.forSourceLines("test.PhoneBrowserUi",
                "package test;",
                Context::class.java.import(),
                Dependency::class.java.import(),

                "@PerActivity",
                "@Dependency",
                "public class PhoneBrowserUi extends BrowserUi {",
                "   PhoneBrowserUi(Context context) { super(context); }",
                "}")


        val activityFile = JavaFileObjects.forSourceLines("test.MainActivity",
                "package test;",
                "",
                Inject::class.java.import(),
                ScopeRoot::class.java.import(),
                Activity::class.java.import(),
                "",
                "@ScopeRoot",
                "@PerActivity",
                "public class MainActivity extends Activity {",
                "",
                "   @Inject",
                "   public Session session;",
                "}")

        val secondActivityFile = JavaFileObjects.forSourceLines("test.SecondActivity",
                "package test;",
                "",
                Inject::class.java.import(),
                ScopeRoot::class.java.import(),
                Activity::class.java.import(),
                "",
                "@ScopeRoot",
                "@PerActivity",
                "public class SecondActivity extends Activity {",
                "",
                "   @Inject",
                "   public Session session;",
                "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.SecondActivityInjector",
                "package test;",
                "",
                keepAnnotation,
                nonNullAnnotation,
                ScopeFactory::class.java.import(),
                "",
                "@Keep",
                "public final class SecondActivityInjector {",
                "",
                "   @Keep",
                "   public final void inject(@NonNull final SecondActivity target) {",
                "       cacheBrowserUi(target);",
                "       injectSessionInSession(target);",
                "   }",
                "",
                "   private final void cacheBrowserUi(@NonNull final SecondActivity target) {",
                "       PhoneBrowserUi browserUi = new PhoneBrowserUi(target);",
                "       ScopeFactory.cache(target, \"PerActivity\", \"browserUi\", browserUi);",
                "   }",
                "",
                "   private final void injectSessionInSession(@NonNull final SecondActivity target) {",
                "       PhoneBrowserUi browserUi = ScopeFactory.get(target, \"PerActivity\", \"browserUi\");",
                "       Session session2 = new Session(browserUi);",
                "       target.session = session2;",
                "       ScopeFactory.cache(target, \"PerActivity\", \"session\", session2);",
                "   }",
                "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
                .that(Arrays.asList(activityFile, secondActivityFile, session, perActivity, logger, browserUi, phoneBrowserUi))
                .processedWith(IProcessor())
                .compilesWithoutError()
                .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun scopedDependency() {


        val session = JavaFileObjects.forSourceLines("test.Session",
                "package test;",
                Context::class.java.import(),

                "@PerActivity",
                "public class Session {",
                "   Session(BrowserUi browserUi) {}",
                "}")

        val perActivity = JavaFileObjects.forSourceLines("test.PerActivity",
                "package test;",
                Context::class.java.import(),
                Scope::class.java.import(),
                Retention::class.java.import(),
                RetentionPolicy::class.java.import(),

                "@Scope",
                "@Retention(RetentionPolicy.RUNTIME)",
                "public @interface PerActivity {",
                "}")

        val browserUi = JavaFileObjects.forSourceLines("test.BrowserUi",
                "package test;",
                Context::class.java.import(),

                "public abstract class BrowserUi {",
                "   BrowserUi(Context context) {}",
                "}")

        val phoneBrowserUi = JavaFileObjects.forSourceLines("test.PhoneBrowserUi",
                "package test;",
                Context::class.java.import(),
                Dependency::class.java.import(),

                "@PerActivity",
                "@Dependency",
                "public class PhoneBrowserUi extends BrowserUi {",
                "   PhoneBrowserUi(Context context) { super(context); }",
                "}")


        val activityFile = JavaFileObjects.forSourceLines("test.MainActivity",
                "package test;",
                "",
                ScopeRoot::class.java.import(),
                Inject::class.java.import(),
                Activity::class.java.import(),
                "",
                "@ScopeRoot",
                "@PerActivity",
                "public class MainActivity extends Activity {",
                "",
                "   @Inject",
                "   public Session session;",
                "",
                "   @Inject",
                "   public BrowserUi browser;",
                "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.MainActivityInjector",
                "package test;",
                "",
                keepAnnotation,
                nonNullAnnotation,
                ScopeFactory::class.java.import(),
                "",
                "@Keep",
                "public final class MainActivityInjector {",
                "",
                "   @Keep",
                "   public final void inject(@NonNull final MainActivity target) {",
                "       injectBrowserUiInBrowser(target);",
                "       injectSessionInSession(target);",
                "   }",
                "",
                "   private final void injectBrowserUiInBrowser(@NonNull final MainActivity target) {",
                "       PhoneBrowserUi browserUi2 = new PhoneBrowserUi(target);",
                "       target.browser = browserUi2;",
                "       ScopeFactory.cache(target, \"PerActivity\", \"browserUi\", browserUi2);",
                "   }",
                "",
                "   private final void injectSessionInSession(@NonNull final MainActivity target) {",
                "       PhoneBrowserUi browserUi = ScopeFactory.get(target, \"PerActivity\", \"browserUi\");",
                "       Session session = new Session(browserUi);",
                "       target.session = session;",
                "       ScopeFactory.cache(target, \"PerActivity\", \"session\", session);",
                "   }",
                "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
                .that(Arrays.asList(activityFile, session, perActivity, browserUi, phoneBrowserUi))
                .processedWith(IProcessor())
                .compilesWithoutError()
                .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun parentScope2() {


        val session = JavaFileObjects.forSourceLines("test.Session",
                "package test;",
                Context::class.java.import(),

                "@PerActivity",
                "public class Session {",
                "   Session(BrowserUi browserUi) {}",
                "}")

        val perActivity = JavaFileObjects.forSourceLines("test.PerActivity",
                "package test;",
                Context::class.java.import(),
                Scope::class.java.import(),
                Retention::class.java.import(),
                RetentionPolicy::class.java.import(),

                "@Scope",
                "@Retention(RetentionPolicy.RUNTIME)",
                "public @interface PerActivity {",
                "}")

        val browserUi = JavaFileObjects.forSourceLines("test.BrowserUi",
                "package test;",
                Context::class.java.import(),

                "public abstract class BrowserUi {",
                "   BrowserUi(Context context) {}",
                "}")

        val phoneBrowserUi = JavaFileObjects.forSourceLines("test.PhoneBrowserUi",
                "package test;",
                Context::class.java.import(),
                Dependency::class.java.import(),

                "@PerActivity",
                "@Dependency",
                "public class PhoneBrowserUi extends BrowserUi {",
                "   PhoneBrowserUi(Context context) { super(context); }",
                "}")

        val parentActivityFile = JavaFileObjects.forSourceLines("test.ParentActivity",
                "package test;",
                "",
                ScopeRoot::class.java.import(),
                Inject::class.java.import(),
                Activity::class.java.import(),
                "",
                "public class ParentActivity extends Activity {",
                "",
                "   @Inject",
                "   public BrowserUi browser;",
                "}")

        val activityFile = JavaFileObjects.forSourceLines("test.MainActivity",
                "package test;",
                "",
                ScopeRoot::class.java.import(),
                Inject::class.java.import(),
                Activity::class.java.import(),
                "",
                "@ScopeRoot",
                "@PerActivity",
                "public class MainActivity extends ParentActivity {",
                "",
                "   @Inject",
                "   public Session session;",
                "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.MainActivityInjector",
                "package test;",
                "",
                keepAnnotation,
                nonNullAnnotation,
                ScopeFactory::class.java.import(),
                "",
                "@Keep",
                "public final class MainActivityInjector {",
                "",
                "   @Keep",
                "   public final void inject(@NonNull final MainActivity target) {",
                "       new ParentActivityInjector().inject(target);",
                "       injectSessionInSession(target);",
                "   }",
                "",
                "   private final void injectSessionInSession(@NonNull final MainActivity target) {",
                "       PhoneBrowserUi browserUi = ScopeFactory.get(target, \"PerActivity\", \"browserUi\");",
                "       Session session = new Session(browserUi);",
                "       target.session = session;",
                "       ScopeFactory.cache(target, \"PerActivity\", \"session\", session);",
                "   }",
                "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
                .that(Arrays.asList(activityFile, parentActivityFile, session, perActivity, browserUi, phoneBrowserUi))
                .processedWith(IProcessor())
                .compilesWithoutError()
                .and().generatesSources(injectedFile)
    }


    @Test
    @Throws(Exception::class)
    fun injectScopedDependencyFromParents() {


        val session = JavaFileObjects.forSourceLines("test.Session",
                "package test;",
                Context::class.java.import(),

                "@PerActivity",
                "public class Session {",
                "   Session(BrowserUi browserUi, Logger logger) {}",
                "}")

        val logger = JavaFileObjects.forSourceLines("test.Logger",
                "package test;",
                Context::class.java.import(),

                "@PerActivity",
                "public class Logger {",
                "}")

        val perActivity = JavaFileObjects.forSourceLines("test.PerActivity",
                "package test;",
                Context::class.java.import(),
                Scope::class.java.import(),
                Retention::class.java.import(),
                RetentionPolicy::class.java.import(),

                "@Scope",
                "@Retention(RetentionPolicy.RUNTIME)",
                "public @interface PerActivity {",
                "}")

        val browserUi = JavaFileObjects.forSourceLines("test.BrowserUi",
                "package test;",
                Context::class.java.import(),

                "public abstract class BrowserUi {",
                "   BrowserUi(Context context) {}",
                "}")

        val phoneBrowserUi = JavaFileObjects.forSourceLines("test.PhoneBrowserUi",
                "package test;",
                Context::class.java.import(),
                Dependency::class.java.import(),

                "@PerActivity",
                "@Dependency",
                "public class PhoneBrowserUi extends BrowserUi {",
                "   PhoneBrowserUi(Context context) { super(context); }",
                "}")

        val parentParentActivityFile = JavaFileObjects.forSourceLines("test.SuperActivity",
                "package test;",
                "",
                ScopeRoot::class.java.import(),
                Inject::class.java.import(),
                Activity::class.java.import(),
                "",
                "public class SuperActivity extends Activity {",
                "",
                "   @Inject",
                "   public Logger logger;",
                "}")

        val parentActivityFile = JavaFileObjects.forSourceLines("test.ParentActivity",
                "package test;",
                "",
                ScopeRoot::class.java.import(),
                Inject::class.java.import(),
                Activity::class.java.import(),
                "",
                "public class ParentActivity extends SuperActivity {",
                "",
                "   @Inject",
                "   public BrowserUi browser;",
                "}")

        val activityFile = JavaFileObjects.forSourceLines("test.MainActivity",
                "package test;",
                "",
                ScopeRoot::class.java.import(),
                Inject::class.java.import(),
                Activity::class.java.import(),
                "",
                "@ScopeRoot",
                "@PerActivity",
                "public class MainActivity extends ParentActivity {",
                "",
                "   @Inject",
                "   public Session session;",
                "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.MainActivityInjector",
                "package test;",
                "",
                keepAnnotation,
                nonNullAnnotation,
                ScopeFactory::class.java.import(),
                "",
                "@Keep",
                "public final class MainActivityInjector {",
                "",
                "   @Keep",
                "   public final void inject(@NonNull final MainActivity target) {",
                "       new ParentActivityInjector().inject(target);",
                "       injectSessionInSession(target);",
                "   }",
                "",
                "   private final void injectSessionInSession(@NonNull final MainActivity target) {",
                "       PhoneBrowserUi browserUi = ScopeFactory.get(target, \"PerActivity\", \"browserUi\");",
                "       Logger logger = ScopeFactory.get(target, \"PerActivity\", \"logger\");",
                "       Session session = new Session(browserUi, logger);",
                "       target.session = session;",
                "       ScopeFactory.cache(target, \"PerActivity\", \"session\", session);",
                "   }",
                "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
                .that(Arrays.asList(activityFile, parentParentActivityFile, logger, parentActivityFile, session, perActivity, browserUi, phoneBrowserUi))
                .processedWith(IProcessor())
                .compilesWithoutError()
                .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun injectScopedDependencyFromParents2() {


        val session = JavaFileObjects.forSourceLines("test.Session",
                "package test;",
                Context::class.java.import(),

                "@PerActivity",
                "public class Session {",
                "   Session(BrowserUi browserUi, Logger logger) {}",
                "}")

        val logger = JavaFileObjects.forSourceLines("test.Logger",
                "package test;",
                Context::class.java.import(),

                "@PerActivity",
                "public class Logger {",
                "}")

        val perActivity = JavaFileObjects.forSourceLines("test.PerActivity",
                "package test;",
                Context::class.java.import(),
                Scope::class.java.import(),
                Retention::class.java.import(),
                RetentionPolicy::class.java.import(),

                "@Scope",
                "@Retention(RetentionPolicy.RUNTIME)",
                "public @interface PerActivity {",
                "}")

        val browserUi = JavaFileObjects.forSourceLines("test.BrowserUi",
                "package test;",
                Context::class.java.import(),

                "public abstract class BrowserUi {",
                "   BrowserUi(Context context) {}",
                "}")

        val phoneBrowserUi = JavaFileObjects.forSourceLines("test.PhoneBrowserUi",
                "package test;",
                Context::class.java.import(),
                Dependency::class.java.import(),

                "@PerActivity",
                "@Dependency",
                "public class PhoneBrowserUi extends BrowserUi {",
                "   PhoneBrowserUi(Context context) { super(context); }",
                "}")

        val parentParentActivityFile = JavaFileObjects.forSourceLines("test.SuperActivity",
                "package test;",
                "",
                ScopeRoot::class.java.import(),
                Inject::class.java.import(),
                Activity::class.java.import(),
                "",
                "public class SuperActivity extends Activity {",
                "",
                "   @Inject",
                "   public Logger logger;",
                "}")

        val parentActivityFile = JavaFileObjects.forSourceLines("test.ParentActivity",
                "package test;",
                "",
                ScopeRoot::class.java.import(),
                Inject::class.java.import(),
                Activity::class.java.import(),
                "",
                "public class ParentActivity extends SuperActivity {",
                "",
                "   @Inject",
                "   public BrowserUi browser;",
                "}")

        val activityFile = JavaFileObjects.forSourceLines("test.MainActivity",
                "package test;",
                "",
                ScopeRoot::class.java.import(),
                Inject::class.java.import(),
                Activity::class.java.import(),
                "",
                "@ScopeRoot",
                "@PerActivity",
                "public class MainActivity extends ParentActivity {",
                "",
                "   @Inject",
                "   public Session session;",
                "}")

        //SuperActivityInjector
        val injectedFile = JavaFileObjects.forSourceLines("test.ParentActivityInjector",
                "package test;",
                "",
                keepAnnotation,
                nonNullAnnotation,
                ScopeFactory::class.java.import(),
                "",
                "@Keep",
                "public final class ParentActivityInjector {",
                "",
                "   @Keep",
                "   public final void inject(@NonNull final ParentActivity target) {",
                "       new SuperActivityInjector().inject(target);",
                "       injectBrowserUiInBrowser(target);",
                "   }",
                "",
                "   private final void injectBrowserUiInBrowser(@NonNull final ParentActivity target) {",
                "       PhoneBrowserUi browserUi2 = new PhoneBrowserUi(target);",
                "       target.browser = browserUi2;",
                "       ScopeFactory.cache(target, \"PerActivity\", \"browserUi\", browserUi2);",
                "   }",
                "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
                .that(Arrays.asList(activityFile, parentParentActivityFile, logger, parentActivityFile, session, perActivity, browserUi, phoneBrowserUi))
                .processedWith(IProcessor())
                .compilesWithoutError()
                .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun injectScopedDependencyFromParents3() {


        val session = JavaFileObjects.forSourceLines("test.Session",
                "package test;",
                Context::class.java.import(),

                "@PerActivity",
                "public class Session {",
                "   Session(BrowserUi browserUi, Logger logger) {}",
                "}")

        val logger = JavaFileObjects.forSourceLines("test.Logger",
                "package test;",
                Context::class.java.import(),

                "@PerActivity",
                "public class Logger {",
                "}")

        val perActivity = JavaFileObjects.forSourceLines("test.PerActivity",
                "package test;",
                Context::class.java.import(),
                Scope::class.java.import(),
                Retention::class.java.import(),
                RetentionPolicy::class.java.import(),

                "@Scope",
                "@Retention(RetentionPolicy.RUNTIME)",
                "public @interface PerActivity {",
                "}")

        val browserUi = JavaFileObjects.forSourceLines("test.BrowserUi",
                "package test;",
                Context::class.java.import(),

                "public abstract class BrowserUi {",
                "   BrowserUi(Context context) {}",
                "}")

        val phoneBrowserUi = JavaFileObjects.forSourceLines("test.PhoneBrowserUi",
                "package test;",
                Context::class.java.import(),
                Dependency::class.java.import(),

                "@PerActivity",
                "@Dependency",
                "public class PhoneBrowserUi extends BrowserUi {",
                "   PhoneBrowserUi(Context context) { super(context); }",
                "}")

        val parentParentActivityFile = JavaFileObjects.forSourceLines("test.SuperActivity",
                "package test;",
                "",
                ScopeRoot::class.java.import(),
                Inject::class.java.import(),
                Activity::class.java.import(),
                "",
                "public class SuperActivity extends Activity {",
                "",
                "   @Inject",
                "   public Logger logger;",
                "}")

        val parentActivityFile = JavaFileObjects.forSourceLines("test.ParentActivity",
                "package test;",
                "",
                ScopeRoot::class.java.import(),
                Inject::class.java.import(),
                Activity::class.java.import(),
                "",
                "public class ParentActivity extends SuperActivity {",
                "",
                "   @Inject",
                "   public BrowserUi browser;",
                "}")

        val activityFile = JavaFileObjects.forSourceLines("test.MainActivity",
                "package test;",
                "",
                ScopeRoot::class.java.import(),
                Inject::class.java.import(),
                Activity::class.java.import(),
                "",
                "@ScopeRoot",
                "@PerActivity",
                "public class MainActivity extends ParentActivity {",
                "",
                "   @Inject",
                "   public Session session;",
                "}")


        val injectedFile = JavaFileObjects.forSourceLines("test.SuperActivityInjector",
                "package test;",
                "",
                keepAnnotation,
                nonNullAnnotation,
                ScopeFactory::class.java.import(),
                "",
                "@Keep",
                "public final class SuperActivityInjector {",
                "",
                "   @Keep",
                "   public final void inject(@NonNull final SuperActivity target) {",
                "       injectLoggerInLogger(target);",
                "   }",
                "",
                "   private final void injectLoggerInLogger(@NonNull final SuperActivity target) {",
                "       Logger logger2 = new Logger();",
                "       target.logger = logger2;",
                "       ScopeFactory.cache(target, \"PerActivity\", \"logger\", logger2);",
                "   }",
                "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
                .that(Arrays.asList(activityFile, parentParentActivityFile, logger, parentActivityFile, session, perActivity, browserUi, phoneBrowserUi))
                .processedWith(IProcessor())
                .compilesWithoutError()
                .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun injectScopedDependencyFromParents4() {


        val session = JavaFileObjects.forSourceLines("test.Session",
                "package test;",
                Context::class.java.import(),

                "@PerActivity",
                "public class Session {",
                "   Session(BrowserUi browserUi, Logger logger) {}",
                "}")

        val logger = JavaFileObjects.forSourceLines("test.Logger",
                "package test;",
                Context::class.java.import(),

                "@PerActivity",
                "public class Logger {",
                "}")

        val perActivity = JavaFileObjects.forSourceLines("test.PerActivity",
                "package test;",
                Context::class.java.import(),
                Scope::class.java.import(),
                Retention::class.java.import(),
                RetentionPolicy::class.java.import(),

                "@Scope",
                "@Retention(RetentionPolicy.RUNTIME)",
                "public @interface PerActivity {",
                "}")

        val browserUi = JavaFileObjects.forSourceLines("test.BrowserUi",
                "package test;",
                Context::class.java.import(),

                "public abstract class BrowserUi {",
                "   BrowserUi(Context context) {}",
                "}")

        val phoneBrowserUi = JavaFileObjects.forSourceLines("test.PhoneBrowserUi",
                "package test;",
                Context::class.java.import(),
                Dependency::class.java.import(),

                "@PerActivity",
                "@Dependency",
                "public class PhoneBrowserUi extends BrowserUi {",
                "   PhoneBrowserUi(Context context) { super(context); }",
                "}")

        val parentParentActivityFile = JavaFileObjects.forSourceLines("test.SuperActivity",
                "package test;",
                "",
                ScopeRoot::class.java.import(),
                Inject::class.java.import(),
                Activity::class.java.import(),
                "",
                "public class SuperActivity extends Activity {",
                "",
                "   @Inject",
                "   public Logger logger;",
                "}")

        val parentActivityFile = JavaFileObjects.forSourceLines("test.ParentActivity",
                "package test;",
                "",
                ScopeRoot::class.java.import(),
                Inject::class.java.import(),
                Activity::class.java.import(),
                "",
                "public class ParentActivity extends SuperActivity {",
                "",
                "   @Inject",
                "   public BrowserUi browser;",
                "}")


        val injectedFile = JavaFileObjects.forSourceLines("test.ParentActivityInjector",
                "package test;",
                "",
                keepAnnotation,
                nonNullAnnotation,
                "",
                "@Keep",
                "public final class ParentActivityInjector {",
                "",
                "   @Keep",
                "   public final void inject(@NonNull final ParentActivity target) {",
                "       new SuperActivityInjector().inject(target);",
                "       injectBrowserUiInBrowser(target);",
                "   }",
                "",
                "   private final void injectBrowserUiInBrowser(@NonNull final ParentActivity target) {",
                "       PhoneBrowserUi browserUi = new PhoneBrowserUi(target);",
                "       target.browser = browserUi;",
                "   }",
                "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
                .that(Arrays.asList(parentParentActivityFile, logger, parentActivityFile, session, perActivity, browserUi, phoneBrowserUi))
                .processedWith(IProcessor())
                .compilesWithoutError()
                .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun injectScopedDependencyFromParents5() {


        val session = JavaFileObjects.forSourceLines("test.Session",
                "package test;",
                Context::class.java.import(),

                "@PerActivity",
                "public class Session {",
                "   Session(BrowserUi browserUi, Logger logger) {}",
                "}")

        val logger = JavaFileObjects.forSourceLines("test.Logger",
                "package test;",
                Context::class.java.import(),

                "@PerActivity",
                "public class Logger {",
                "}")

        val perActivity = JavaFileObjects.forSourceLines("test.PerActivity",
                "package test;",
                Context::class.java.import(),
                Scope::class.java.import(),
                Retention::class.java.import(),
                RetentionPolicy::class.java.import(),

                "@Scope",
                "@Retention(RetentionPolicy.RUNTIME)",
                "public @interface PerActivity {",
                "}")

        val browserUi = JavaFileObjects.forSourceLines("test.BrowserUi",
                "package test;",
                Context::class.java.import(),

                "public abstract class BrowserUi {",
                "   BrowserUi(Context context) {}",
                "}")

        val phoneBrowserUi = JavaFileObjects.forSourceLines("test.PhoneBrowserUi",
                "package test;",
                Context::class.java.import(),
                Dependency::class.java.import(),

                "@PerActivity",
                "@Dependency",
                "public class PhoneBrowserUi extends BrowserUi {",
                "   PhoneBrowserUi(Context context) { super(context); }",
                "}")

        val parentParentActivityFile = JavaFileObjects.forSourceLines("test.SuperActivity",
                "package test;",
                "",
                ScopeRoot::class.java.import(),
                Inject::class.java.import(),
                Activity::class.java.import(),
                "",
                "public class SuperActivity extends Activity {",
                "",
                "   @Inject",
                "   public Logger logger;",
                "}")

        val parentActivityFile = JavaFileObjects.forSourceLines("test.ParentActivity",
                "package test;",
                "",
                ScopeRoot::class.java.import(),
                Inject::class.java.import(),
                Activity::class.java.import(),
                "",
                "public class ParentActivity extends SuperActivity {",
                "",
                "   @Inject",
                "   public BrowserUi browser;",
                "}")


        val injectedFile = JavaFileObjects.forSourceLines("test.SuperActivityInjector",
                "package test;",
                "",
                keepAnnotation,
                nonNullAnnotation,
                "",
                "@Keep",
                "public final class SuperActivityInjector {",
                "",
                "   @Keep",
                "   public final void inject(@NonNull final SuperActivity target) {",
                "       injectLoggerInLogger(target);",
                "   }",
                "",
                "   private final void injectLoggerInLogger(@NonNull final SuperActivity target) {",
                "       Logger logger = new Logger();",
                "       target.logger = logger;",
                "   }",
                "}")

        assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
                .that(Arrays.asList(parentParentActivityFile, logger, parentActivityFile, session, perActivity, browserUi, phoneBrowserUi))
                .processedWith(IProcessor())
                .compilesWithoutError()
                .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun scopeOnMethod() {


        val session = JavaFileObjects.forSourceLines("test.Session",
                "package test;",
                Context::class.java.import(),

                "public class Session {",
                "",
                "}")

        val sessionModule = JavaFileObjects.forSourceLines("test.SessionModule",
                "package test;",
                Context::class.java.import(),
                Dependency::class.java.import(),

                "public class SessionModule {",
                "   @PerActivity",
                "   @Dependency",
                "   public static Session session() { return null; }",
                "",
                "}")



        val perActivity = JavaFileObjects.forSourceLines("test.PerActivity",
                "package test;",
                Context::class.java.import(),
                Scope::class.java.import(),
                Retention::class.java.import(),
                RetentionPolicy::class.java.import(),

                "@Scope",
                "@Retention(RetentionPolicy.RUNTIME)",
                "public @interface PerActivity {",
                "}")


        val activityFile = JavaFileObjects.forSourceLines("test.MainActivity",
                "package test;",
                "",
                Inject::class.java.import(),
                ScopeRoot::class.java.import(),
                Activity::class.java.import(),
                "",
                "@ScopeRoot",
                "@PerActivity",
                "public class MainActivity extends Activity {",
                "",
                "   @Inject",
                "   public Session session;",
                "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.MainActivityInjector",
                "package test;",
                "",
                keepAnnotation,
                nonNullAnnotation,
                ScopeFactory::class.java.import(),
                "",
                "@Keep",
                "public final class MainActivityInjector {",
                "",
                "   @Keep",
                "   public final void inject(@NonNull final MainActivity target) {",
                "       injectSessionInSession(target);",
                "   }",
                "",
                "   private final void injectSessionInSession(@NonNull final MainActivity target) {",
                "       Session session = SessionModule.session();",
                "       target.session = session;",
                "       ScopeFactory.cache(target, \"PerActivity\", \"session\", session);",
                "   }",
                "}")

        Truth.assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
                .that(Arrays.asList(activityFile, session, perActivity, sessionModule))
                .processedWith(IProcessor())
                .compilesWithoutError()
                .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun scopeOnMethodAndPassToConstructor() {


        val session = JavaFileObjects.forSourceLines("test.Session",
                "package test;",
                Context::class.java.import(),

                "public class Session {",
                "",
                "}")

        val sessionModule = JavaFileObjects.forSourceLines("test.SessionModule",
                "package test;",
                Context::class.java.import(),
                Dependency::class.java.import(),

                "public class SessionModule {",
                "   @PerActivity",
                "   @Dependency",
                "   public static Session session() { return null; }",
                "",
                "}")

        val browserUi = JavaFileObjects.forSourceLines("test.BrowserUi",
                "package test;",
                Context::class.java.import(),

                "@PerActivity",
                "public class BrowserUi {",
                "   BrowserUi(@PerActivity Session session) {}",
                "}")



        val perActivity = JavaFileObjects.forSourceLines("test.PerActivity",
                "package test;",
                Context::class.java.import(),
                Scope::class.java.import(),
                Retention::class.java.import(),
                RetentionPolicy::class.java.import(),

                "@Scope",
                "@Retention(RetentionPolicy.RUNTIME)",
                "public @interface PerActivity {",
                "}")


        val activityFile = JavaFileObjects.forSourceLines("test.MainActivity",
                "package test;",
                "",
                Inject::class.java.import(),
                ScopeRoot::class.java.import(),
                Activity::class.java.import(),
                "",
                "@ScopeRoot",
                "@PerActivity",
                "public class MainActivity extends Activity {",
                "",
                "   @Inject",
                "   public Session session;",
                "   @Inject",
                "   public BrowserUi ui;",
                "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.MainActivityInjector",
                "package test;",
                "",
                keepAnnotation,
                nonNullAnnotation,
                ScopeFactory::class.java.import(),
                "",
                "@Keep",
                "public final class MainActivityInjector {",
                "",
                "   @Keep",
                "   public final void inject(@NonNull final MainActivity target) {",
                "       injectSessionInSession(target);",
                "       injectBrowserUiInUi(target);",
                "   }",
                "",
                "   private final void injectSessionInSession(@NonNull final MainActivity target) {",
                "       Session session = SessionModule.session();",
                "       target.session = session;",
                "       ScopeFactory.cache(target, \"PerActivity\", \"session\", session);",
                "   }",
                "",
                "   private final void injectBrowserUiInUi(@NonNull final MainActivity target) {",
                "       Session session2 = ScopeFactory.get(target, \"PerActivity\", \"session\");",
                "       BrowserUi browserUi = new BrowserUi(session2);",
                "       target.ui = browserUi;",
                "       ScopeFactory.cache(target, \"PerActivity\", \"browserUi\", browserUi);",
                "}")

        Truth.assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
                .that(Arrays.asList(activityFile, session, perActivity, browserUi, sessionModule))
                .processedWith(IProcessor())
                .compilesWithoutError()
                .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun scopeOnMethodAndPassToMethodAsParameter() {


        val session = JavaFileObjects.forSourceLines("test.Session",
                "package test;",
                Context::class.java.import(),

                "public class Session {",
                "",
                "}")

        val sessionModule = JavaFileObjects.forSourceLines("test.SessionModule",
                "package test;",
                Context::class.java.import(),
                Dependency::class.java.import(),

                "public class SessionModule {",
                "   @PerActivity",
                "   @Dependency",
                "   public static Session session() { return null; }",

                "   @Dependency",
                "   public static BrowserUi browserUi(@PerActivity Session session) { return null; }",
                "",
                "}")

        val browserUi = JavaFileObjects.forSourceLines("test.BrowserUi",
                "package test;",
                Context::class.java.import(),

                "public class BrowserUi {",
                "   BrowserUi(Session session) {}",
                "}")



        val perActivity = JavaFileObjects.forSourceLines("test.PerActivity",
                "package test;",
                Context::class.java.import(),
                Scope::class.java.import(),
                Retention::class.java.import(),
                RetentionPolicy::class.java.import(),

                "@Scope",
                "@Retention(RetentionPolicy.RUNTIME)",
                "public @interface PerActivity {",
                "}")


        val activityFile = JavaFileObjects.forSourceLines("test.MainActivity",
                "package test;",
                "",
                Inject::class.java.import(),
                ScopeRoot::class.java.import(),
                Activity::class.java.import(),
                "",
                "@ScopeRoot",
                "@PerActivity",
                "public class MainActivity extends Activity {",
                "",
                "   @Inject",
                "   public Session session;",
                "   @Inject",
                "   public BrowserUi ui;",
                "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.MainActivityInjector",
                "package test;",
                "",
                keepAnnotation,
                nonNullAnnotation,
                ScopeFactory::class.java.import(),
                "",
                "@Keep",
                "public final class MainActivityInjector {",
                "",
                "   @Keep",
                "   public final void inject(@NonNull final MainActivity target) {",
                "       injectSessionInSession(target);",
                "       injectBrowserUiInUi(target);",
                "   }",
                "",
                "   private final void injectSessionInSession(@NonNull final MainActivity target) {",
                "       Session session = SessionModule.session();",
                "       target.session = session;",
                "       ScopeFactory.cache(target, \"PerActivity\", \"session\", session);",
                "   }",
                "",
                "   private final void injectBrowserUiInUi(@NonNull final MainActivity target) {",
                "       Session session2 = ScopeFactory.get(target, \"PerActivity\", \"session\");",
                "       BrowserUi browserUi = SessionModule.browserUi(session2);",
                "       target.ui = browserUi;",
                "}")

        Truth.assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
                .that(Arrays.asList(activityFile, session, perActivity, browserUi, sessionModule))
                .processedWith(IProcessor())
                .compilesWithoutError()
                .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun scopeOnMethodAndPassToMethodAsParameterInNestedMethod() {


        val session = JavaFileObjects.forSourceLines("test.DataSource",
                "package test;",
                Context::class.java.import(),

                "public class DataSource {",
                "",
                "}")

        val browser = JavaFileObjects.forSourceLines("test.FavoritesRepository",
                "package test;",
                Context::class.java.import(),

                "public class FavoritesRepository {",
                "",
                "}")

        val sessionModule = JavaFileObjects.forSourceLines("test.SessionModule",
                "package test;",
                Context::class.java.import(),
                Dependency::class.java.import(),

                "public class SessionModule {",
                "   @PerActivity",
                "   @Dependency",
                "   public static DataSource session() { return null; }",

                "   @Dependency",
                "   @PerActivity",
                "   public static FavoritesRepository browserUi(@PerActivity DataSource session) { return null; }",
                "   @Dependency",
                "   public static BrowserUi string(@PerActivity FavoritesRepository session) { return null; }",
                "",
                "}")

        val browserUi = JavaFileObjects.forSourceLines("test.BrowserUi",
                "package test;",
                Context::class.java.import(),

                "public class BrowserUi {",
                "}")



        val perActivity = JavaFileObjects.forSourceLines("test.PerActivity",
                "package test;",
                Context::class.java.import(),
                Scope::class.java.import(),
                Retention::class.java.import(),
                RetentionPolicy::class.java.import(),

                "@Scope",
                "@Retention(RetentionPolicy.RUNTIME)",
                "public @interface PerActivity {",
                "}")


        val activityFile = JavaFileObjects.forSourceLines("test.MainActivity",
                "package test;",
                "",
                Inject::class.java.import(),
                ScopeRoot::class.java.import(),
                Activity::class.java.import(),
                "",
                "@ScopeRoot",
                "@PerActivity",
                "public class MainActivity extends Activity {",
                "",
                "   @Inject",
                "   public DataSource session;",
                "   @Inject",
                "   public BrowserUi browser;",
                "   @Inject",
                "   public FavoritesRepository ui;",
                "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.MainActivityInjector",
                "package test;",
                "",
                keepAnnotation,
                nonNullAnnotation,
                ScopeFactory::class.java.import(),
                "",
                "@Keep",
                "public final class MainActivityInjector {",
                "",
                "   @Keep",
                "   public final void inject(@NonNull final MainActivity target) {",
                "       injectDataSourceInSession(target);",
                "       injectFavoritesRepositoryInUi(target);",
                "       injectBrowserUiInBrowser(target);",
                "   }",
                "",
                "   private final void injectDataSourceInSession(@NonNull final MainActivity target) {",
                "       DataSource dataSource = SessionModule.session();",
                "       target.session = dataSource;",
                "       ScopeFactory.cache(target, \"PerActivity\", \"dataSource\", dataSource);",
                "   }",
                "",
                "   private final void injectFavoritesRepositoryInUi(@NonNull final MainActivity target) {",
                "       DataSource dataSource3 = ScopeFactory.get(target, \"PerActivity\", \"dataSource\");",
                "       FavoritesRepository favoritesRepository2 = SessionModule.browserUi(dataSource3);",
                "       target.ui = favoritesRepository2;",
                "       ScopeFactory.cache(target, \"PerActivity\", \"favoritesRepository\", favoritesRepository2);",
                "   }",
                "",
                "   private final void injectBrowserUiInBrowser(@NonNull final MainActivity target) {",
                "       FavoritesRepository favoritesRepository = ScopeFactory.get(target, \"PerActivity\", \"favoritesRepository\");",
                "       BrowserUi browserUi = SessionModule.string(favoritesRepository);",
                "       target.browser = browserUi;",
                "   }",
                "}")

        Truth.assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
                .that(Arrays.asList(activityFile, session, perActivity, browserUi, browser, sessionModule))
                .processedWith(IProcessor())
                .compilesWithoutError()
                .and().generatesSources(injectedFile)
    }
}