package com.ioc

import android.app.Activity
import android.content.Context
import android.widget.FrameLayout
import com.google.common.truth.Truth
import com.google.testing.compile.JavaFileObjects
import com.google.testing.compile.JavaSourcesSubject
import com.google.testing.compile.JavaSourcesSubjectFactory.javaSources
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import javax.inject.Inject
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
            "public interface Session {",
            "}")

        val secondScoped = JavaFileObjects.forSourceLines("test.SecondScoped",
            "package test;",
            "public class SecondScoped {",
            "}")

        val sessionImpl = JavaFileObjects.forSourceLines("test.SessionImplementation",
            "package test;",
            Dependency::class.java.import(),
            "@Dependency",
            "public class SessionImplementation implements Session {",
            "   SessionImplementation(BrowserUi browserUi, SecondScoped secondScoped) {}",
            "}")

        val logger = JavaFileObjects.forSourceLines("test.Logger",
            "package test;",
            Dependency::class.java.import(),
            "@Dependency",
            "public class Logger {",
            "   Logger(BrowserUi browserUi) {}",
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

            "@Dependency",
            "public class PhoneBrowserUi extends BrowserUi {",
            "   PhoneBrowserUi(Context context) { super(context); }",
            "}")


        val activityFile = JavaFileObjects.forSourceLines("test.MainActivity",
            "package test;",
            "",
            Inject::class.java.import(),
            LocalScope::class.java.import(),
            Activity::class.java.import(),
            "",
            "public class MainActivity extends Activity {",
            "",
            "   @Inject",
            "   public Session session;",

            "   @Inject",
            "   public Logger logger;",

            "   @LocalScope",
            "   @Inject",
            "   public BrowserUi browserUi;",

            "   @LocalScope",
            "   @Inject",
            "   private SecondScoped secondScoped;",

            "   public void setSecondScoped(SecondScoped secondScoped) {};",
            "   public SecondScoped getSecondScoped() { return null; };",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.MainActivityInjector",
            "package test;",
            "",
            "import $keep",
            "import $nonNull",
            "",
            "@Keep",
            "public final class MainActivityInjector {",
            "",
            "   @Keep",
            "   public final void inject(@NonNull final MainActivity target) {",
            "       injectSecondScopedInSecondScoped(target);",
            "       injectSessionInSession(target);",
            "       injectLoggerInLogger(target);",
            "       injectBrowserUiInBrowserUi(target);",
            "   }",
            "",
            "   private final void injectSecondScopedInSecondScoped(@NonNull final MainActivity target) {",
            "       SecondScoped secondScoped = new SecondScoped();",
            "       target.setSecondScoped(secondScoped);",
            "   }",
            "",
            "   private final void injectSessionInSession(@NonNull final MainActivity target) {",
            "       Session session = new SessionImplementation(target.browserUi, target.getSecondScoped());",
            "       target.session = session;",
            "   }",
            "",
            "   private final void injectLoggerInLogger(@NonNull final MainActivity target) {",
            "       Logger logger = new Logger(target.browserUi);",
            "       target.logger = logger;",
            "   }",
            "",
            "   private final void injectBrowserUiInBrowserUi(@NonNull final MainActivity target) {",
            "       BrowserUi browserUi = new PhoneBrowserUi(target);",
            "       target.browserUi = browserUi;",
            "   }",
            "}")

        Truth.assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(activityFile, sessionImpl, secondScoped, logger, session, browserUi, phoneBrowserUi))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun localScopeInConstructor() {
        val alohabrowser = JavaFileObjects.forSourceLines("test.AlohaBrowserUi",
            "package test;",
            "public class AlohaBrowserUi {",
            "}")

        val autoCompleteListener = JavaFileObjects.forSourceLines("test.AutoCompleteListener",
            "package test;",
            "public interface AutoCompleteListener {",
            "}")

        val autoCompleteListenerImpl = JavaFileObjects.forSourceLines("test.AutoCompleteListenerImpl",
            "package test;",
            Dependency::class.java.import(),
            "@Dependency",
            "public class AutoCompleteListenerImpl implements AutoCompleteListener {",
            "   public AutoCompleteListenerImpl(AlohaBrowserUi browserUi) {}",
            "}")

        val autocompleteController = JavaFileObjects.forSourceLines("test.AutocompleteController",
            "package test;",
            "public class AutocompleteController {",
            "   public AutocompleteController(AutoCompleteListener listener) {}",
            "}")


        val logger = JavaFileObjects.forSourceLines("test.AddressBarListenerImpl",
            "package test;",
            LocalScope::class.java.import(),
            Inject::class.java.import(),
            "public class AddressBarListenerImpl {",
            "   @Inject",
            "   public AutocompleteController controller;",
            "   @LocalScope",
            "   private AlohaBrowserUi browserUi;",
            "   AddressBarListenerImpl(AlohaBrowserUi browserUi) {}",
            "   public AlohaBrowserUi getAlohaBrowserUi() { return null; }",
            "}")


        val injectedFile = JavaFileObjects.forSourceLines("test.AddressBarListenerImplInjector",
            "package test;",
            "",
            "import $keep",
            "import $nonNull",
            "",
            "@Keep",
            "public final class AddressBarListenerImplInjector {",
            "",
            "   @Keep",
            "   public final void inject(@NonNull final AddressBarListenerImpl target) {",
            "       injectAutocompleteControllerInController(target);",
            "   }",
            "",
            "   private final void injectAutocompleteControllerInController(@NonNull final AddressBarListenerImpl target) {",
            "       AutoCompleteListener autoCompleteListener = new AutoCompleteListenerImpl(target.getAlohaBrowserUi());",
            "       AutocompleteController autocompleteController = new AutocompleteController(autoCompleteListener);",
            "       target.controller = autocompleteController;",
            "   }",
            "}")

        Truth.assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(autoCompleteListener, autoCompleteListenerImpl, autocompleteController, logger, alohabrowser))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun localScopeOnMethod() {
        val alohabrowser = JavaFileObjects.forSourceLines("test.AlohaBrowserUi",
            "package test;",
            "public class AlohaBrowserUi {",
            "}")

        val autoCompleteListener = JavaFileObjects.forSourceLines("test.AutoCompleteListener",
            "package test;",
            "public interface AutoCompleteListener {",
            "}")

        val autoCompleteListenerImpl = JavaFileObjects.forSourceLines("test.AutoCompleteListenerImpl",
            "package test;",
            Dependency::class.java.import(),
            "@Dependency",
            "public class AutoCompleteListenerImpl implements AutoCompleteListener {",
            "   public AutoCompleteListenerImpl(AlohaBrowserUi browserUi) {}",
            "}")

        val autocompleteController = JavaFileObjects.forSourceLines("test.AutocompleteController",
            "package test;",
            "public class AutocompleteController {",
            "   public AutocompleteController(AutoCompleteListener listener) {}",
            "}")


        val logger = JavaFileObjects.forSourceLines("test.AddressBarListenerImpl",
            "package test;",
            LocalScope::class.java.import(),
            Inject::class.java.import(),
            "public class AddressBarListenerImpl {",
            "   @Inject",
            "   public AutocompleteController controller;",
            "   @LocalScope",
            "   public AlohaBrowserUi getAlohaBrowserUi() { return null; }",
            "}")


        val injectedFile = JavaFileObjects.forSourceLines("test.AddressBarListenerImplInjector",
            "package test;",
            "",
            "import $keep",
            "import $nonNull",
            "",
            "@Keep",
            "public final class AddressBarListenerImplInjector {",
            "",
            "   @Keep",
            "   public final void inject(@NonNull final AddressBarListenerImpl target) {",
            "       injectAutocompleteControllerInController(target);",
            "   }",
            "",
            "   private final void injectAutocompleteControllerInController(@NonNull final AddressBarListenerImpl target) {",
            "       AutoCompleteListener autoCompleteListener = new AutoCompleteListenerImpl(target.getAlohaBrowserUi());",
            "       AutocompleteController autocompleteController = new AutocompleteController(autoCompleteListener);",
            "       target.controller = autocompleteController;",
            "   }",
            "}")

        Truth.assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(autoCompleteListener, autoCompleteListenerImpl, autocompleteController, logger, alohabrowser))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun failLocalScopeMethodIsPrivate() {
        val alohabrowser = JavaFileObjects.forSourceLines("test.AlohaBrowserUi",
            "package test;",
            "public class AlohaBrowserUi {",
            "}")

        val autocompleteController = JavaFileObjects.forSourceLines("test.AutocompleteController",
            "package test;",
            "public class AutocompleteController {",
            "}")


        val logger = JavaFileObjects.forSourceLines("test.AddressBarListenerImpl",
            "package test;",
            LocalScope::class.java.import(),
            Inject::class.java.import(),
            "public class AddressBarListenerImpl {",
            "   @Inject",
            "   public AutocompleteController controller;",
            "   @LocalScope",
            "   private AlohaBrowserUi getAlohaBrowserUi() { return null; }",
            "}")

        Truth.assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(autocompleteController, logger, alohabrowser))
            .processedWith(IProcessor())
            .failsToCompile()
            .withErrorContaining("method test.AddressBarListenerImpl.getAlohaBrowserUi() annotated with @LocalScope must be public and not static.")
            .`in`(logger)
            .onLine(4)
    }

    @Test
    @Throws(Exception::class)
    fun failLocalScopeMethodIsVoidReturn() {
        val alohabrowser = JavaFileObjects.forSourceLines("test.AlohaBrowserUi",
            "package test;",
            "public class AlohaBrowserUi {",
            "}")

        val autocompleteController = JavaFileObjects.forSourceLines("test.AutocompleteController",
            "package test;",
            "public class AutocompleteController {",
            "}")


        val logger = JavaFileObjects.forSourceLines("test.AddressBarListenerImpl",
            "package test;",
            LocalScope::class.java.import(),
            Inject::class.java.import(),
            "public class AddressBarListenerImpl {",
            "   @Inject",
            "   public AutocompleteController controller;",
            "   @LocalScope",
            "   public void getAlohaBrowserUi() {  }",
            "}")

        Truth.assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(autocompleteController, logger, alohabrowser))
            .processedWith(IProcessor())
            .failsToCompile()
            .withErrorContaining("method test.AddressBarListenerImpl.getAlohaBrowserUi() annotated with @LocalScope must returns type.")
            .`in`(logger)
            .onLine(4)
    }

    @Test
    @Throws(Exception::class)
    fun failLocalScopeMethodIsHasParameters() {
        val alohabrowser = JavaFileObjects.forSourceLines("test.AlohaBrowserUi",
            "package test;",
            "public class AlohaBrowserUi {",
            "}")

        val autocompleteController = JavaFileObjects.forSourceLines("test.AutocompleteController",
            "package test;",
            "public class AutocompleteController {",
            "}")


        val logger = JavaFileObjects.forSourceLines("test.AddressBarListenerImpl",
            "package test;",
            LocalScope::class.java.import(),
            Inject::class.java.import(),
            "public class AddressBarListenerImpl {",
            "   @Inject",
            "   public AutocompleteController controller;",
            "   @LocalScope",
            "   public AlohaBrowserUi getAlohaBrowserUi(String s) { return null; }",
            "}")

        Truth.assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(autocompleteController, logger, alohabrowser))
            .processedWith(IProcessor())
            .failsToCompile()
            .withErrorContaining("method test.AddressBarListenerImpl.getAlohaBrowserUi(java.lang.String) annotated with @LocalScope must contains not contains parameters.")
            .`in`(logger)
            .onLine(4)
    }

    @Test
    @Throws(Exception::class)
    fun localScopeInConstructor2() {
        val webViewCoordinatorView = JavaFileObjects.forSourceLines("test.WebViewCoordinatorView",
            "package test;",
            Context::class.java.import(),
            "public class WebViewCoordinatorView {",
            "   public WebViewCoordinatorView(Context context) {}",
            "}")

        val mainView = JavaFileObjects.forSourceLines("test.MainView",
            "package test;",

            FrameLayout::class.java.import(),
            Inject::class.java.import(),
            LocalScope::class.java.import(),
            Context::class.java.import(),

            "public class MainView extends FrameLayout {",
            "   public MainView(Context context) { super(context); }",
            "   @Inject",
            "   public WebViewCoordinatorView webViewCoordinatorView;",
            "   @LocalScope",
            "   public Context localContext;",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.MainViewInjector",
            "package test;",
            "",
            "import $keep",
            "import $nonNull",
            "",
            "@Keep",
            "public final class MainViewInjector {",
            "   @Keep",
            "   public final void inject(@NonNull final MainView target) {",
            "       injectWebViewCoordinatorViewInWebViewCoordinatorView(target);",
            "   }",
            "",
            "   private final void injectWebViewCoordinatorViewInWebViewCoordinatorView(@NonNull final MainView target) {",
            "       WebViewCoordinatorView webViewCoordinatorView = new WebViewCoordinatorView(target.localContext);",
            "       target.webViewCoordinatorView = webViewCoordinatorView;",
            "   }",
            "}")

        Truth.assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(javaSources())
            .that(listOf(mainView, webViewCoordinatorView))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }
}