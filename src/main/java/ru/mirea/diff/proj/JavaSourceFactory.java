package ru.mirea.diff.proj;

import com.sun.tools.javac.parser.JavacParser;
import com.sun.tools.javac.parser.ParserFactory;
import com.sun.tools.javac.tree.JCTree;
import ru.mirea.diff.jcanon.Canonicalizer;
import ru.mirea.diff.jcanon.SourceClass;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class JavaSourceFactory implements SourceFactory {

    private final ParserFactory factory;
    private final Canonicalizer canonicalizer;

    public JavaSourceFactory(ParserFactory factory, Canonicalizer canonicalizer) {
        this.factory = factory;
        this.canonicalizer = canonicalizer;
    }

    public List<Source> parse(String fileName, InputStream is) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while (true) {
            int c = is.read();
            if (c < 0)
                break;
            bos.write(c);
        }
        String src = bos.toString("UTF-8");
        JavacParser parser = factory.newParser(src, false, false, false);
        JCTree.JCCompilationUnit jcu = parser.parseCompilationUnit();
        List<SourceClass> sourceClasses = canonicalizer.parseUnit(jcu);
        if (sourceClasses.size() == 1) {
            return Collections.singletonList(new Source(fileName, sourceClasses.get(0).code.getLines()));
        } else {
            List<Source> result = new ArrayList<>(sourceClasses.size());
            for (SourceClass sourceClass : sourceClasses) {
                String className = fileName + "/" + sourceClass.name;
                result.add(new Source(className, sourceClass.code.getLines()));
            }
            return result;
        }
    }
}
