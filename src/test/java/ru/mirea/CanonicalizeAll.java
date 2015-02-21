package ru.mirea;

import com.sun.tools.javac.parser.JavacParser;
import com.sun.tools.javac.parser.ParserFactory;
import com.sun.tools.javac.tree.JCTree;
import ru.mirea.diff.jcanon.Canonicalizer;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

final class CanonicalizeAll {

    public static void main(String[] args) throws IOException {
        ParserFactory factory = Canonicalizer.createFactory();

        //Canonicalizer canonicalizer = new Canonicalizer(false, false, Reorder.WEAK, "    ");
        Canonicalizer canonicalizer = new Canonicalizer(true, true, Canonicalizer.Reorder.STRONG, "");

        try (ZipFile zip = new ZipFile("D:\\programs\\jdk1.8.0_25\\src.zip")) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (!entry.isDirectory() && entry.getName().toLowerCase().endsWith(".java")) {
                    System.out.println(entry.getName());
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    try (InputStream is = new BufferedInputStream(zip.getInputStream(entry))) {
                        while (true) {
                            int c = is.read();
                            if (c < 0)
                                break;
                            bos.write(c);
                        }
                    }
                    JavacParser parser = factory.newParser(bos.toString("UTF-8"), false, false, false);
                    JCTree.JCCompilationUnit jcu = parser.parseCompilationUnit();
                    canonicalizer.printUnit(jcu);
                }
            }
        }
    }
}
