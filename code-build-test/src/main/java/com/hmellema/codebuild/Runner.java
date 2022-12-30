package com.hmellema.codebuild;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.sun.source.util.JavacTask;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.lang.model.element.Modifier;
import javax.tools.DiagnosticCollector;
import javax.tools.FileObject;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

public class Runner {
  private static final int BUFFER_SIZE = 1024;
  private static final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
  private static final DiagnosticCollector<? super JavaFileObject> diagnostics = new DiagnosticCollector<>();

  public static void main(String[] args) {
    List<JavaFile> javaFiles = Collections.singletonList(createJavaFile());
    List<String> namePrefixes = javaFiles.stream()
        .map(file -> file.packageName)
        .map(pkg -> pkg.replace(".", "/") + "/")
        .toList();
    var sourceFiles = javaFiles.stream().map(JavaFile::toJavaFileObject).toList();
    var compiledClasses = compileClasses(sourceFiles);
    createSrcJar(sourceFiles);
    createLibraryJar(compiledClasses, namePrefixes);
  }

  private static void createSrcJar(Iterable<? extends JavaFileObject> sourceFiles) {
    var manifest = getBaseManifest();
    writeFilesToJar("srcjar", manifest, sourceFiles, null);
  }

  private static Manifest getBaseManifest() {
    final Manifest manifest = new Manifest();
    manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");


    return manifest;
  }

  private static void createLibraryJar(Iterable<? extends JavaFileObject> compiledClasses,  List<String> namePrefixes) {
    var manifest = getBaseManifest();
    writeFilesToJar("classjar", manifest, compiledClasses, namePrefixes);
  }

  private static void writeFilesToJar(String jarName, Manifest manifest, Iterable<? extends JavaFileObject> files, @Nullable List<String> namePrefixes) {
    try (var target = new JarOutputStream(new FileOutputStream( jarName + ".jar"), manifest)) {
      int idx = 0;
      for (var file : files) {
        var fileName = file.getName();
        if (namePrefixes != null) {
          fileName = namePrefixes.get(idx) + fileName;
        }
        writeJarEntry(fileName, file, target);
        idx++;
      }
    } catch (IOException exception) {
      System.out.println(exception);
      throw new RuntimeException("JAR FILE WRITE FAILED.");
    }
  }

  private static Iterable<? extends JavaFileObject> compileClasses(List<JavaFileObject> sourceList) {
    try (var fileManager = compiler.getStandardFileManager(null, null, null)) {
      System.out.println("Starting compilation");
      var task = (JavacTask) compiler.getTask(null, fileManager, diagnostics, null, null, sourceList);
      var generated = task.generate();
      System.out.println("COMPILATION FINISHED");
      System.out.println("Generated files: " + generated);
      return generated;
    } catch (IOException exception) {
      throw new RuntimeException("Compilation failed");
    }
  }

  private static void writeJarEntry(String fileName, JavaFileObject javaFile, JarOutputStream target) throws IOException {
    JarEntry entry = new JarEntry(fileName);

    target.putNextEntry(entry);
    try (var input = new BufferedInputStream(javaFile.openInputStream())) {
      writeEntry(input, target);
    }
    target.closeEntry();
  }

  private static void writeEntry(BufferedInputStream input, JarOutputStream target)
      throws IOException {
    byte[] buffer = new byte[BUFFER_SIZE];
    while (true) {
      int count = input.read(buffer);
      if (count <= 0) {
        break;
      }
      target.write(buffer, 0, count);
    }
  }

  private static JavaFile createJavaFile() {
    MethodSpec main = MethodSpec.methodBuilder("main")
        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
        .returns(void.class)
        .addParameter(String[].class, "args")
        .addStatement("$T.out.println($S)", System.class, "Hello, JavaPoet!")
        .build();

    TypeSpec helloWorld = TypeSpec.classBuilder("HelloWorld")
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
        .addMethod(main)
        .build();

    return JavaFile.builder("com.example.helloworld", helloWorld)
        .build();
  }
}
