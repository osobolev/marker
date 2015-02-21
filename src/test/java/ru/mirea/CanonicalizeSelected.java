package ru.mirea;

import com.sun.tools.javac.parser.JavacParser;
import com.sun.tools.javac.parser.ParserFactory;
import com.sun.tools.javac.tree.JCTree;
import ru.mirea.diff.jcanon.Block;
import ru.mirea.diff.jcanon.Canonicalizer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

final class CanonicalizeSelected {

    public static void main(String[] args) throws IOException {
        ParserFactory factory = Canonicalizer.createFactory();

        //Canonicalizer canonicalizer = new Canonicalizer(false, false, Reorder.WEAK, "    ");
        Canonicalizer canonicalizer = new Canonicalizer(true, true, Canonicalizer.Reorder.STRONG, "");

        //String src = new String(Files.readAllBytes(Paths.get("D:\\home\\projects\\TP\\course\\kkots\\src\\Experiment3.java")), "UTF-8");
        //String src = new String(Files.readAllBytes(Paths.get("D:\\home\\projects\\TP\\marker\\src\\main\\java\\ru\\mirea\\Test.java")), "UTF-8");
        String src = new String(Files.readAllBytes(Paths.get("D:\\home\\projects\\TP\\marker\\src\\main\\java\\ru\\mirea\\Tester.java")), "UTF-8");
        JavacParser parser = factory.newParser(src, false, false, false);
        JCTree.JCCompilationUnit jcu = parser.parseCompilationUnit();
        Block block = canonicalizer.printUnit(jcu);
        System.out.println(block);
    }
}
