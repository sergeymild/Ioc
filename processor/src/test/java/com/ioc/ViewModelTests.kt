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
class ViewModelTests {
    private val androidViewModel = JavaFileObjects.forSourceLines("$lifecyclePackage.ViewModel",
        "package $lifecyclePackage;",
        "",
        "public class ViewModel {",
        "   public ViewModel() {};",
        "}")

    private val androidLiveData = JavaFileObjects.forSourceLines("$lifecyclePackage.LiveData",
        "package $lifecyclePackage;",
        "",
        "public class LiveData<T> {",
        "   public void observe(LifecycleOwner owner, Observer<? super T> observer) {}",
        "   public void observeForever(Observer<? super T> observer) {}",
        "}")

    private val androidMutableLiveData = JavaFileObjects.forSourceLines("$lifecyclePackage.MutableLiveData",
        "package $lifecyclePackage;",
        "public class MutableLiveData<T> extends LiveData<T> {",
        "}")

    private val androidLifecycleOwner = JavaFileObjects.forSourceLines("$lifecyclePackage.LifecycleOwner",
        "package $lifecyclePackage;",
        "",
        "public interface LifecycleOwner {",
        "}")

    private val androidLiveDataObserver = JavaFileObjects.forSourceLines("$lifecyclePackage.Observer",
        "package $lifecyclePackage;",
        "",
        "public interface Observer<T> {",
        "   void onChanged(T t);",
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
        "import $lifecyclePackage.LifecycleOwner;",
        "",
        "public class FragmentActivity implements LifecycleOwner {",

        "}")

    private val androidAppCompatActivity = JavaFileObjects.forSourceLines("androidx.appcompat.app.AppCompatActivity",
        "package androidx.appcompat.app;",
        "import $fragmentActivityPackage.FragmentActivity;",
        "",
        "public class AppCompatActivity extends FragmentActivity {",
        "}")

    private val androidFragment = JavaFileObjects.forSourceLines("androidx.fragment.app.Fragment",
        "package androidx.fragment.app;",
        "import $lifecyclePackage.LiveData;",
        "import $lifecyclePackage.LifecycleOwner;",
        "",
        "public class Fragment implements LifecycleOwner {",
        "   public LifecycleOwner getViewLifecycleOwner() { return null; }",
        "}")


    private val dependencyWithEmptyConstructor = JavaFileObjects.forSourceLines("test.DependencyWithEmptyConstructor",
        "package test;",
        "",
        "public class DependencyWithEmptyConstructor {}")

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
            importInjectAnnotation,
            "import androidx.appcompat.app.AppCompatActivity;",

            "public class Activity extends AppCompatActivity {",
            "",
            "   @Inject",
            "   public ActivityViewModel activityViewModel;",
            "}")

        val contextFile = JavaFileObjects.forSourceLines("test.Context",
            "package test;",
            "",
            importDependencyAnnotation,
            "",
            "@Dependency",
            "public class Context {",
            "}")

        val dependencyFile = JavaFileObjects.forSourceLines("test.DependencyModel",
            "package test;",
            "",
            importInjectAnnotation,
            "",
            "public class DependencyModel {",
            "",
            "   @Inject",
            "   DependencyModel(Context context) {}",
            "}")

        val injectedFile = JavaFileObjects.forSourceLines("test.ActivityInjector",
            "package test;",
            importKeepAnnotation,
            importNonNullAnnotation,
            "import $lifecyclePackage.ViewModel;",
            "import $viewModelProvider;",
            "import $viewModelProviders;",
            "import java.lang.Class;",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public static final void inject(@NonNull final Activity target) {",
            "       target.activityViewModel = provideActivityViewModel(target);",
            "   }",
            "",
            "   private static final ActivityViewModel provideActivityViewModel(@NonNull final Activity target) {",
            "       ViewModelProvider.Factory factory_activityViewModel = new ViewModelProvider.Factory() {",
            "           @NonNull",
            "           public <T extends ViewModel> T create(@NonNull final Class<T> modelClass) {",
            "               Context context = new Context();",
            "               DependencyModel dependencyModel = new DependencyModel(context);",
            "               return (T) new ActivityViewModel(dependencyModel);",
            "           }",
            "       };",
            "       ActivityViewModel activityViewModel = ViewModelProviders.of(target, factory_activityViewModel).get(ActivityViewModel.class);",
            "       return activityViewModel;",
            "   }",
            "}")

        Truth.assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(JavaSourcesSubjectFactory.javaSources())
            .that(listOf<JavaFileObject>(activityFile, androidViewModel, androidViewModelProvider, androidViewModelProviders, androidAppCompatActivity, androidFragmentActivity, androidLifecycleOwner, activityViewModel, contextFile, dependencyFile))
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
            importInjectAnnotation,
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
            importKeepAnnotation,
            importNonNullAnnotation,
            "import $lifecyclePackage.ViewModel;",
            "import $viewModelProvider;",
            "import $viewModelProviders;",
            "import java.lang.Class;",
            "",
            "@Keep",
            "public final class ActivityInjector {",
            "   @Keep",
            "   public static final void inject(@NonNull final Activity target) {",
            "       target.dependency = new DependencyModel();",
            "       target.activityViewModel = provideActivityViewModel(target);",
            "   }",
            "",
            "   private static final ActivityViewModel provideActivityViewModel(@NonNull final Activity target) {",
            "       ViewModelProvider.Factory factory_activityViewModel = new ViewModelProvider.Factory() {",
            "           @NonNull",
            "           public <T extends ViewModel> T create(@NonNull final Class<T> modelClass) {",
            "               return (T) new ActivityViewModel(target.dependency);",
            "           }",
            "       };",
            "       ActivityViewModel activityViewModel = ViewModelProviders.of(target, factory_activityViewModel).get(ActivityViewModel.class);",
            "       return activityViewModel;",
            "   }",
            "}")

        Truth.assertAbout<JavaSourcesSubject, Iterable<JavaFileObject>>(JavaSourcesSubjectFactory.javaSources())
            .that(listOf(activityFile, androidViewModel, androidViewModelProvider, androidViewModelProviders, androidAppCompatActivity, androidFragmentActivity, androidLifecycleOwner, activityViewModel, dependencyFile))
            .processedWith(IProcessor())
            .compilesWithoutError()
            .and().generatesSources(injectedFile)
    }
}