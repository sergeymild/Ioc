package com.ioc

import com.google.common.truth.Truth
import com.google.testing.compile.JavaFileObjects
import com.google.testing.compile.JavaSourcesSubject
import com.google.testing.compile.JavaSourcesSubjectFactory
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import javax.inject.Inject
import javax.tools.JavaFileObject

@RunWith(JUnit4::class)
class ViewModelTests {
    private val androidViewModel = JavaFileObjects.forSourceLines("$lifecyclePackage.ViewModel",
        "package $lifecyclePackage;",
        "",
        "public class ViewModel {",
        "   public ViewModel() {};",
        "}")

    private val androidViewModelProvider = JavaFileObjects.forSourceLines(viewModelProvider,
        "package $lifecyclePackage;",
        "import java.lang.Class;",
        "",
        "public class ViewModelProvider {",
        "   public ViewModelProvider() {};",
        "   public interface Factory {",
        "       <T extends ViewModel> T create(Class<T> modelClass);",
        "   }",
        "",
        "   public <T extends ViewModel> T get(Class<T> modelClass) { return null; }",
        "}")

    private val androidViewModelProviders = JavaFileObjects.forSourceLines(viewModelProviders,
        "package $lifecyclePackage;",
        "import $fragmentActivityPackage.FragmentActivity;",
        "import $lifecyclePackage.ViewModelProvider.Factory;",
        "",
        "public class ViewModelProviders {",
        "   public static ViewModelProvider of(FragmentActivity fragment, Factory factory) { return null; }",
        "}")

    private val androidFragmentActivity = JavaFileObjects.forSourceLines("$fragmentActivityPackage.FragmentActivity",
        "package $fragmentActivityPackage;",
        "",
        "public class FragmentActivity {",

        "}")

    private val androidAppCompatActivity = JavaFileObjects.forSourceLines("androidx.appcompat.app.AppCompatActivity",
        "package androidx.appcompat.app;",
        "import $fragmentActivityPackage.FragmentActivity;",
        "",
        "public class AppCompatActivity extends FragmentActivity {",

        "}")

    @Test
    @Throws(Exception::class)
    fun viewModel() {

        val activityViewModel = JavaFileObjects.forSourceLines("test.ActivityViewModel",
            "package test;",
            "import $lifecyclePackage.ViewModel;",
            "",
            "public class ActivityViewModel extends ViewModel {",
            "   public ActivityViewModel(DependencyModel dependency) {};",
            "}")

        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            Helpers.importType(Inject::class.java),
            "import androidx.appcompat.app.AppCompatActivity;",

            "public class Activity extends AppCompatActivity {",
            "",
            "   @Inject",
            "   public ActivityViewModel activityViewModel;",
            "}")

        val contextFile = JavaFileObjects.forSourceLines("test.Context",
            "package test;",
            "",
            Helpers.importType(Dependency::class.java),
            "",
            "@Dependency",
            "public class Context {",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            Helpers.importType(Inject::class.java),
            "",
            "public class DependencyModel {",
            "",
            "   @Inject",
            "   DependencyModel(Context context) {}",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "import $keep;",
            "import $nonNull;",
            "import $lifecyclePackage.ViewModel;",
            "import $viewModelProvider;",
            "import $viewModelProviders;",
            "import java.lang.Class;",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       injectActivityViewModelInActivityViewModel(target);",
            "   }",
            "",
            "   private final void injectActivityViewModelInActivityViewModel(@NonNull final Activity target) {",
            "       ViewModelProvider.Factory factory_activityViewModel = new ViewModelProvider.Factory() {",
            "           @NonNull",
            "           public <T extends ViewModel> T create(@NonNull final Class<T> modelClass) {",
            "               Context context = new Context();",
            "               DependencyModel dependencyModel = new DependencyModel(context);",
            "               return (T) new ActivityViewModel(dependencyModel);",
            "           }",
            "       };",
            "       ActivityViewModel activityViewModel = ViewModelProviders.of(target, factory_activityViewModel).get(ActivityViewModel.class);",
            "       target.activityViewModel = activityViewModel;",
            "   }",
            "}")

        Truth.assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(JavaSourcesSubjectFactory.javaSources())
            .that(listOf<JavaFileObject>(activityFile, androidViewModel, androidViewModelProvider, androidViewModelProviders, androidAppCompatActivity, androidFragmentActivity, activityViewModel, contextFile, dependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }

    @Test
    @Throws(Exception::class)
    fun viewModelWithLocalScopedArgument() {

        val activityViewModel = JavaFileObjects.forSourceLines("test.ActivityViewModel",
            "package test;",
            "import $lifecyclePackage.ViewModel;",
            "",
            "public class ActivityViewModel extends ViewModel {",
            "   public ActivityViewModel(DependencyModel dependency) {};",
            "}")

        val activityFile = JavaFileObjects.forSourceLines("test.Activity",
            "package test;",
            "",
            "import $inject;",
            "import $iocLocalScope;",
            "import androidx.appcompat.app.AppCompatActivity;",

            "public class Activity extends AppCompatActivity {",
            "",
            "   @Inject",
            "   public ActivityViewModel activityViewModel;",

            "   @Inject",
            "   @LocalScope",
            "   public DependencyModel dependency;",
            "}")


        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            "public class DependencyModel {",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            "import $keep;",
            "import $nonNull;",
            "import $lifecyclePackage.ViewModel;",
            "import $viewModelProvider;",
            "import $viewModelProviders;",
            "import java.lang.Class;",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public final void inject(@NonNull final Activity target) {",
            "       injectDependencyModelInDependency(target);",
            "       injectActivityViewModelInActivityViewModel(target);",
            "   }",
            "",
            "   private final void injectDependencyModelInDependency(@NonNull final Activity target) {",
            "       DependencyModel dependencyModel2 = new DependencyModel();",
            "       target.dependency = dependencyModel2;",
            "   }",
            "",
            "   private final void injectActivityViewModelInActivityViewModel(@NonNull final Activity target) {",
            "       ViewModelProvider.Factory factory_activityViewModel = new ViewModelProvider.Factory() {",
            "           @NonNull",
            "           public <T extends ViewModel> T create(@NonNull final Class<T> modelClass) {",
            "               return (T) new ActivityViewModel(target.dependency);",
            "           }",
            "       };",
            "       ActivityViewModel activityViewModel = ViewModelProviders.of(target, factory_activityViewModel).get(ActivityViewModel.class);",
            "       target.activityViewModel = activityViewModel;",
            "   }",
            "}")

        Truth.assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(JavaSourcesSubjectFactory.javaSources())
            .that(listOf<JavaFileObject>(activityFile, androidViewModel, androidViewModelProvider, androidViewModelProviders, androidAppCompatActivity, androidFragmentActivity, activityViewModel, dependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }
}