package ru.mirea.diff.jcanon;

import com.sun.source.tree.MemberReferenceTree;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.parser.ParserFactory;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Convert;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;

import javax.tools.JavaFileManager;
import javax.tools.ToolProvider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Function;

public final class Canonicalizer {

    public enum Reorder {
        NONE, WEAK, STRONG
    }

    private final boolean unident;
    private final boolean unlit;
    private final Reorder reorder;
    private final String tab;

    public Canonicalizer(boolean unident, boolean unlit, Reorder reorder, String tab) {
        this.unident = unident;
        this.unlit = unlit;
        this.reorder = reorder;
        this.tab = tab;
    }

    private static Block isStatic(long flags) {
        if ((flags & Flags.STATIC) != 0) {
            return new Block("static ");
        } else {
            return Block.EMPTY;
        }
    }

    private static <T> Block sortBlocks(Block result, List<T> items, String separator, Function<T, Block> mapper) {
        if (items != null) {
            Block[] blocks = new Block[items.size()];
            {
                int i = 0;
                for (T item : items) {
                    blocks[i++] = mapper.apply(item);
                }
            }
            Arrays.sort(blocks);
            int i = 0;
            for (Block block : blocks) {
                if (i > 0) {
                    result = result.append(separator);
                }
                result = result.append(block);
                i++;
            }
        }
        return result;
    }

    private static <T> Block printWithSeparator(Block result, List<T> items, String separator, Function<T, Block> mapper) {
        if (items != null) {
            int i = 0;
            for (T item : items) {
                if (i > 0) {
                    result = result.append(separator);
                }
                result = result.append(mapper.apply(item));
                i++;
            }
        }
        return result;
    }

    private <T> Block printWithSeparatorSorted(Block result, List<T> items, String separator, Function<T, Block> mapper) {
        if (reorder == Reorder.STRONG) {
            return sortBlocks(result, items, separator, mapper);
        } else {
            return printWithSeparator(result, items, separator, mapper);
        }
    }

    private <T> Block printWithSeparatorUnordered(Block result, List<T> items, String separator, Function<T, Block> mapper) {
        if (reorder != Reorder.NONE) {
            return sortBlocks(result, items, separator, mapper);
        } else {
            return printWithSeparator(result, items, separator, mapper);
        }
    }

    private String printName(Name name) {
        if (unident) {
            if (name == name.table.names._this)
                return "this";
            if (name == name.table.names._super)
                return "super";
            if (name == name.table.names._class)
                return "class";
            return "ident";
        } else {
            return name.toString();
        }
    }

    private Block printMembers(List<JCTree> defs) {
        Block result = Block.EMPTY;
        if (defs != null) {
            if (reorder != Reorder.NONE) {
                Block[] blocks = new Block[defs.size()];
                int i = 0;
                for (JCTree def : defs) {
                    blocks[i++] = printTree(def, true);
                }
                Arrays.sort(blocks);
                for (Block block : blocks) {
                    result = result.appendln(block);
                }
            } else {
                for (JCTree def : defs) {
                    result = result.appendln(printTree(def, true));
                }
            }
        }
        return result;
    }

    private Block printCatch(JCTree.JCCatch catcher) {
        Block decl = printVarDecl(catcher.param).prepend(" catch (").append(") ");
        return decl.append(printBlock(catcher.body));
    }

    private Block printTree(JCTree def, boolean typeArgs) {
        if (def instanceof JCTree.JCCompilationUnit) {
            return printUnit((JCTree.JCCompilationUnit) def);
        } else if (def instanceof JCTree.JCClassDecl) {
            return printClass((JCTree.JCClassDecl) def);
        } else if (def instanceof JCTree.JCMethodDecl) {
            return printMethod((JCTree.JCMethodDecl) def);
        } else if (def instanceof JCTree.JCStatement) {
            return printStatement((JCTree.JCStatement) def);
        } else if (def instanceof JCTree.JCExpression) {
            return printExpression((JCTree.JCExpression) def, typeArgs);
        } else if (def instanceof JCTree.JCCatch) {
            return printCatch((JCTree.JCCatch) def);
        } else if (def instanceof JCTree.JCTypeParameter) {
            JCTree.JCTypeParameter tp = (JCTree.JCTypeParameter) def;
            String name = printName(tp.name);
            if (tp.bounds.nonEmpty()) {
                return printWithSeparatorSorted(new Block(name + " extends "), tp.bounds, " & ", bound -> printExpression(bound, typeArgs));
            } else {
                return new Block(name);
            }
        } else if (def instanceof JCTree.JCImport) {
            return Block.EMPTY;
        } else {
            return new Block(def.toString());
        }
    }

    private static String printPrimType(TypeTag typeTag) {
        switch (typeTag) {
        case BOOLEAN: return "boolean";
        case BYTE: return "byte";
        case CHAR: return "char";
        case DOUBLE: return "double";
        case FLOAT: return "float";
        case INT: return "int";
        case LONG: return "long";
        case SHORT: return "short";
        case VOID: return "void";
        }
        return typeTag.name();
    }

    private static String printOp(JCTree.Tag tag) {
        switch (tag) {
        case AND: return "&&";
        case ASSIGN: return "=";
        case BITAND: return "&";
        case BITAND_ASG: return "&=";
        case BITOR: return "|";
        case BITOR_ASG: return "|=";
        case BITXOR: return "^";
        case BITXOR_ASG: return "^=";
        case DIV: return "/";
        case DIV_ASG: return "/=";
        case EQ: return "==";
        case GE: return ">=";
        case GT: return ">";
        case LE: return "<=";
        case LT: return "<";
        case MINUS: return "-";
        case MINUS_ASG: return "-=";
        case MOD: return "%";
        case MOD_ASG: return "%=";
        case MUL: return "*";
        case MUL_ASG: return "*=";
        case NE: return "!=";
        case NEG: return "!";
        case NOT: return "~";
        case OR: return "||";
        case PLUS: return "+";
        case PLUS_ASG: return "+=";
        case POSTDEC: return "--";
        case POSTINC: return "++";
        case PREDEC: return "--";
        case PREINC: return "++";
        case SL: return "<<";
        case SL_ASG: return "<<=";
        case SR: return ">>";
        case SR_ASG: return ">>=";
        case USR: return ">>>";
        case USR_ASG: return ">>>=";
        }
        return tag.name();
    }

    private String printLiteral(JCTree.JCLiteral lit) {
        Object value = lit.value;
        if (value == null)
            return "null";
        switch (lit.typetag) {
        case BOT:
            return "null";
        case BOOLEAN:
            return String.valueOf(((Number) value).intValue() != 0);
        case CHAR:
            return "'" + Convert.quote((char) ((Number) value).intValue()) + "'";
        case DOUBLE:
            return value.toString();
        case FLOAT:
            return value + "f";
        case LONG:
            return value + "L";
        case BYTE:
        case INT:
        case SHORT:
            return value.toString();
        case CLASS:
            if (unlit) {
                return "\"string\"";
            } else {
                return "\"" + Convert.quote(value.toString()) + "\"";
            }
        }
        return value.toString();
    }

    private Block printExpression(JCTree.JCExpression expr) {
        return printExpression(expr, false);
    }

    private Block printExpressionParens(JCTree.JCExpression expr) {
        if (expr instanceof JCTree.JCParens) {
            return printExpression(expr);
        } else {
            return printExpression(expr).prepend("(").append(")");
        }
    }

    private Block printExpression(JCTree.JCExpression expr, boolean typeArgs) {
        if (expr instanceof JCTree.JCAnnotatedType) {
            JCTree.JCAnnotatedType at = (JCTree.JCAnnotatedType) expr;
            return printExpression(at.underlyingType, typeArgs);
        } else if (expr instanceof JCTree.JCAnnotation) {
            return Block.EMPTY;
        } else if (expr instanceof JCTree.JCArrayAccess) {
            JCTree.JCArrayAccess aa = (JCTree.JCArrayAccess) expr;
            Block val = printExpression(aa.indexed);
            Block i = printExpression(aa.index);
            return val.append("[").append(i).append("]");
        } else if (expr instanceof JCTree.JCArrayTypeTree) {
            JCTree.JCArrayTypeTree att = (JCTree.JCArrayTypeTree) expr;
            return printExpression(att.elemtype, typeArgs).append("[]");
        } else if (expr instanceof JCTree.JCAssign) {
            JCTree.JCAssign ass = (JCTree.JCAssign) expr;
            Block left = printExpression(ass.lhs);
            Block right = printExpression(ass.rhs);
            return left.append(" = ").append(right);
        } else if (expr instanceof JCTree.JCAssignOp) {
            JCTree.JCAssignOp ass = (JCTree.JCAssignOp) expr;
            Block left = printExpression(ass.lhs);
            Block right = printExpression(ass.rhs);
            String op = printOp(ass.getTag());
            return left.append(" " + op + " ").append(right);
        } else if (expr instanceof JCTree.JCBinary) {
            JCTree.JCBinary bin = (JCTree.JCBinary) expr;
            Block left = printExpression(bin.lhs);
            Block right = printExpression(bin.rhs);
            String op = printOp(bin.getTag());
            return left.append(" " + op + " ").append(right);
        } else if (expr instanceof JCTree.JCConditional) {
            JCTree.JCConditional cond = (JCTree.JCConditional) expr;
            Block e1 = printExpression(cond.cond);
            Block e2 = printExpression(cond.truepart);
            Block e3 = printExpression(cond.falsepart);
            return e1.append(" ? ").append(e2).append(" : ").append(e3);
        } else if (expr instanceof JCTree.JCErroneous) {
            return new Block(expr.toString());
        } else if (expr instanceof JCTree.JCFieldAccess) {
            JCTree.JCFieldAccess fa = (JCTree.JCFieldAccess) expr;
            Block val = printExpression(fa.selected);
            return val.append("." + printName(fa.name));
        } else if (expr instanceof JCTree.JCIdent) {
            JCTree.JCIdent ident = (JCTree.JCIdent) expr;
            return new Block(printName(ident.name));
        } else if (expr instanceof JCTree.JCInstanceOf) {
            JCTree.JCInstanceOf io = (JCTree.JCInstanceOf) expr;
            Block left = printExpression(io.expr);
            Block type = printTree(io.clazz, false);
            return left.append(" instanceof ").append(type);
        } else if (expr instanceof JCTree.JCLambda) {
            JCTree.JCLambda lambda = (JCTree.JCLambda) expr;
            Block result;
            boolean explicit = lambda.paramKind == JCTree.JCLambda.ParameterKind.EXPLICIT;
            if (lambda.params.size() == 1) {
                JCTree.JCVariableDecl var = lambda.params.head;
                if (explicit) {
                    result = printVarDecl(var);
                } else {
                    result = new Block(printName(var.name));
                }
            } else {
                result = new Block("(");
                if (explicit) {
                    result = printWithSeparatorSorted(result, lambda.params, ", ", this::printVarDecl);
                } else {
                    result = printWithSeparatorSorted(result, lambda.params, ", ", var -> new Block(printName(var.name)));
                }
                result = result.append(")");
            }
            result = result.append(" -> ");
            Block body = printTree(lambda.body, false);
            return result.append(body);
        } else if (expr instanceof JCTree.JCLiteral) {
            JCTree.JCLiteral lit = (JCTree.JCLiteral) expr;
            return new Block(printLiteral(lit));
        } else if (expr instanceof JCTree.JCMemberReference) {
            JCTree.JCMemberReference mr = (JCTree.JCMemberReference) expr;
            Block result = printExpression(mr.expr).append("::");
            if (mr.typeargs != null) {
                result = result.append("<>");
            }
            return result.append(mr.mode == MemberReferenceTree.ReferenceMode.INVOKE ? printName(mr.name) : "new");
        } else if (expr instanceof JCTree.JCMethodInvocation) {
            JCTree.JCMethodInvocation mi = (JCTree.JCMethodInvocation) expr;
            Block result;
            if (mi.typeargs.nonEmpty()) {
                if (mi.meth instanceof JCTree.JCFieldAccess) {
                    JCTree.JCFieldAccess fa = (JCTree.JCFieldAccess) mi.meth;
                    result = printExpression(fa.selected).append(".<>" + printName(fa.name));
                } else {
                    result = printExpression(mi.meth).prepend("<>");
                }
            } else {
                result = printExpression(mi.meth);
            }
            result = result.append("(");
            result = printWithSeparatorSorted(result, mi.args, ", ", this::printExpression);
            return result.append(")");
        } else if (expr instanceof JCTree.JCNewArray) {
            JCTree.JCNewArray na = (JCTree.JCNewArray) expr;
            Block result;
            if (na.elemtype != null) {
                JCTree.JCExpression elemtype = na.elemtype;
                int emptyCount = 0;
                while (true) {
                    while (elemtype instanceof JCTree.JCAnnotatedType) {
                        JCTree.JCAnnotatedType at = (JCTree.JCAnnotatedType) elemtype;
                        elemtype = at.underlyingType;
                    }
                    if (elemtype instanceof JCTree.JCArrayTypeTree) {
                        JCTree.JCArrayTypeTree sub = (JCTree.JCArrayTypeTree) elemtype;
                        elemtype = sub.elemtype;
                        emptyCount++;
                    } else {
                        break;
                    }
                }
                result = printExpression(elemtype).prepend("new ");
                if (na.elems != null) {
                    result = result.append("[]");
                }
                List<JCTree.JCExpression> dims = na.dims;
                for (JCTree.JCExpression dim : dims) {
                    Block d = printExpression(dim);
                    result = result.append("[").append(d).append("]");
                }
                for (int i = 0; i < emptyCount; i++) {
                    result = result.append("[]");
                }
                if (na.elems != null) {
                    result = result.append(" ");
                }
            } else {
                result = Block.EMPTY;
            }
            if (na.elems != null) {
                result = result.append("{");
                result = printWithSeparator(result, na.elems, ", ", this::printExpression);
                result = result.append("}");
            }
            return result;
        } else if (expr instanceof JCTree.JCNewClass) {
            JCTree.JCNewClass nc = (JCTree.JCNewClass) expr;
            Block result;
            if (nc.encl != null) {
                result = printExpression(nc.encl).append(".");
            } else {
                result = Block.EMPTY;
            }
            result = result.append("new ");
            if (nc.typeargs.nonEmpty()) {
                result = result.append("<>");
            }
            result = result.append(printExpression(nc.clazz)).append("(");
            result = printWithSeparatorSorted(result, nc.args, ", ", this::printExpression);
            result = result.append(")");
            if (nc.def != null) {
                Block body = printMembers(nc.def.defs).indent(tab);
                result = result.append(" {").appendln(body).appendln(new Block("}"));
            }
            return result.prepend("new ");
        } else if (expr instanceof JCTree.JCParens) {
            JCTree.JCParens par = (JCTree.JCParens) expr;
            return printExpression(par.expr).prepend("(").append(")");
        } else if (expr instanceof JCTree.JCPrimitiveTypeTree) {
            JCTree.JCPrimitiveTypeTree prim = (JCTree.JCPrimitiveTypeTree) expr;
            return new Block(printPrimType(prim.typetag));
        } else if (expr instanceof JCTree.JCTypeApply) {
            JCTree.JCTypeApply ta = (JCTree.JCTypeApply) expr;
            Block result = printExpression(ta.clazz, typeArgs).append("<");
            if (typeArgs) {
                result = printWithSeparatorSorted(result, ta.arguments, ", ", e -> printExpression(e, true));
            }
            return result.append(">");
        } else if (expr instanceof JCTree.JCTypeCast) {
            JCTree.JCTypeCast cast = (JCTree.JCTypeCast) expr;
            Block type = printTree(cast.clazz, false).prepend("(").append(") ");
            Block val = printExpression(cast.expr);
            return type.append(val);
        } else if (expr instanceof JCTree.JCTypeIntersection) {
            JCTree.JCTypeIntersection ti = (JCTree.JCTypeIntersection) expr;
            return printWithSeparatorSorted(Block.EMPTY, ti.bounds, " & ", bound -> printExpression(bound, typeArgs));
        } else if (expr instanceof JCTree.JCTypeUnion) {
            JCTree.JCTypeUnion tu = (JCTree.JCTypeUnion) expr;
            return printWithSeparatorUnordered(Block.EMPTY, tu.alternatives, " | ", alternative -> printExpression(alternative, typeArgs));
        } else if (expr instanceof JCTree.JCUnary) {
            JCTree.JCUnary un = (JCTree.JCUnary) expr;
            Block right = printExpression(un.arg);
            JCTree.Tag tag = un.getTag();
            if (tag == JCTree.Tag.POSTDEC || tag == JCTree.Tag.POSTINC) {
                return right.append(printOp(tag));
            } else {
                return right.prepend(printOp(tag));
            }
        } else if (expr instanceof JCTree.JCWildcard) {
            JCTree.JCWildcard wc = (JCTree.JCWildcard) expr;
            String kind = wc.kind.kind.toString();
            if (wc.inner != null) {
                return printTree(wc.inner, typeArgs).prepend(kind);
            } else {
                return new Block(kind);
            }
        } else if (expr instanceof JCTree.LetExpr) {
            JCTree.LetExpr let = (JCTree.LetExpr) expr;
            Block decls = Block.EMPTY;
            for (JCTree.JCVariableDecl def : let.defs) {
                Block decl = printVarDecl(def).append(";");
                decls = decls.appendln(decl);
            }
            Block value = printTree(let.expr, false).append(";");
            return new Block("let").appendln(decls.indent(tab)).appendln(new Block("in")).appendln(value.indent(tab));
        } else {
            return new Block(expr.toString());
        }
    }

    private Block printCase(JCTree.JCCase aCase) {
        Block pattern;
        if (aCase.pat == null) {
            pattern = new Block("default:");
        } else {
            pattern = printExpression(aCase.pat).prepend("case ").append(":");
        }
        Block result = Block.EMPTY;
        for (JCTree.JCStatement stat : aCase.stats) {
            result = result.appendln(printStatement(stat));
        }
        return pattern.appendln(result.indent(tab));
    }

    private Block printStatementForceBlock(JCTree.JCStatement stmt) {
        if (stmt instanceof JCTree.JCBlock) {
            return printBlock((JCTree.JCBlock) stmt);
        } else {
            return printBlock(List.of(stmt));
        }
    }

    private Block printStatement(JCTree.JCStatement stmt) {
        if (stmt instanceof JCTree.JCAssert) {
            JCTree.JCAssert ass = (JCTree.JCAssert) stmt;
            Block result = printExpression(ass.cond).prepend("assert ");
            if (ass.detail != null) {
                Block expr = printExpression(ass.detail);
                result =  result.append(" : ").append(expr);
            }
            return result.append(";");
        } else if (stmt instanceof JCTree.JCBlock) {
            return printBlock((JCTree.JCBlock) stmt);
        } else if (stmt instanceof JCTree.JCBreak) {
            JCTree.JCBreak brk = (JCTree.JCBreak) stmt;
            if (brk.label != null) {
                return new Block("break " + printName(brk.label) + ";");
            } else {
                return new Block("break;");
            }
        } else if (stmt instanceof JCTree.JCCase) {
            JCTree.JCCase aCase = (JCTree.JCCase) stmt;
            return printCase(aCase);
        } else if (stmt instanceof JCTree.JCClassDecl) {
            return printClass((JCTree.JCClassDecl) stmt);
        } else if (stmt instanceof JCTree.JCContinue) {
            JCTree.JCContinue cont = (JCTree.JCContinue) stmt;
            if (cont.label != null) {
                return new Block("continue " + printName(cont.label) + ";");
            } else {
                return new Block("continue;");
            }
        } else if (stmt instanceof JCTree.JCDoWhileLoop) {
            JCTree.JCDoWhileLoop d = (JCTree.JCDoWhileLoop) stmt;
            Block body = printStatementForceBlock(d.body);
            Block cond = printExpressionParens(d.cond).prepend(" while ");
            return body.prepend("do ").append(cond).append(";");
        } else if (stmt instanceof JCTree.JCEnhancedForLoop) {
            JCTree.JCEnhancedForLoop f = (JCTree.JCEnhancedForLoop) stmt;
            Block decl = printVarDecl(f.var);
            Block expr = printExpression(f.expr);
            Block body = printStatementForceBlock(f.body);
            return decl.prepend("for (").append(" : ").append(expr).append(") ").append(body);
        } else if (stmt instanceof JCTree.JCExpressionStatement) {
            JCTree.JCExpressionStatement expr = (JCTree.JCExpressionStatement) stmt;
            return printExpression(expr.expr).append(";");
        } else if (stmt instanceof JCTree.JCForLoop) {
            JCTree.JCForLoop f = (JCTree.JCForLoop) stmt;
            Block result = new Block("for (");
            result = printWithSeparatorSorted(result, f.init, " ", this::printStatement);
            result = result.append(" ");
            if (f.cond != null) {
                result = result.append(printExpression(f.cond));
            }
            result = result.append("; ");
            result = printWithSeparatorSorted(result, f.step, ", ", s -> printExpression(s.expr));
            Block body = printStatementForceBlock(f.body);
            return result.append(") ").append(body);
        } else if (stmt instanceof JCTree.JCIf) {
            JCTree.JCIf i = (JCTree.JCIf) stmt;
            Block cond = printExpressionParens(i.cond).prepend("if ").append(" ");
            Block result = cond.append(printStatementForceBlock(i.thenpart));
            JCTree.JCStatement elsePart = i.elsepart;
            while (elsePart instanceof JCTree.JCIf) {
                JCTree.JCIf nested = (JCTree.JCIf) elsePart;
                Block cond2 = printExpression(nested.cond).prepend(" else if ").append(" ");
                Block result2 = cond2.append(printStatementForceBlock(nested.thenpart));
                result = result.append(result2);
                elsePart = nested.elsepart;
            }
            if (elsePart != null) {
                Block e = printStatementForceBlock(elsePart);
                result = result.append(" else ").append(e);
            }
            return result;
        } else if (stmt instanceof JCTree.JCLabeledStatement) {
            JCTree.JCLabeledStatement lbl = (JCTree.JCLabeledStatement) stmt;
            return printStatement(lbl.body).prepend(printName(lbl.label) + ":");
        } else if (stmt instanceof JCTree.JCReturn) {
            JCTree.JCReturn ret = (JCTree.JCReturn) stmt;
            if (ret.expr != null) {
                return printExpression(ret.expr).prepend("return ").append(";");
            } else {
                return new Block("return;");
            }
        } else if (stmt instanceof JCTree.JCSkip) {
            return Block.EMPTY;
        } else if (stmt instanceof JCTree.JCSwitch) {
            JCTree.JCSwitch sw = (JCTree.JCSwitch) stmt;
            Block result = printExpressionParens(sw.selector).prepend("switch ").append(" {");
            List<JCTree.JCCase> cases = sw.cases;
            for (JCTree.JCCase aCase : cases) {
                result = result.appendln(printCase(aCase));
            }
            return result.appendln(new Block("}"));
        } else if (stmt instanceof JCTree.JCSynchronized) {
            JCTree.JCSynchronized sync = (JCTree.JCSynchronized) stmt;
            return printBlock(sync.body);
        } else if (stmt instanceof JCTree.JCThrow) {
            JCTree.JCThrow th = (JCTree.JCThrow) stmt;
            return printExpression(th.expr).prepend("throw ").append(";");
        } else if (stmt instanceof JCTree.JCTry) {
            JCTree.JCTry t = (JCTree.JCTry) stmt;
            Block result = new Block("try ");
            if (t.resources.nonEmpty()) {
                result = result.append("(");
                result = printWithSeparator(result, t.resources, "; ", res -> printTree(res, false));
                result = result.append(") ");
            }
            result = result.append(printBlock(t.body));
            for (JCTree.JCCatch catcher : t.catchers) {
                result = result.append(printCatch(catcher));
            }
            if (t.finalizer != null) {
                result = result.append(" finally ").append(printBlock(t.finalizer));
            }
            return result;
        } else if (stmt instanceof JCTree.JCVariableDecl) {
            JCTree.JCVariableDecl var = (JCTree.JCVariableDecl) stmt;
            return printVarDecl(var).append(";");
        } else if (stmt instanceof JCTree.JCWhileLoop) {
            JCTree.JCWhileLoop w = (JCTree.JCWhileLoop) stmt;
            Block cond = printExpressionParens(w.cond).prepend("while ").append(" ");
            Block body = printStatementForceBlock(w.body);
            return cond.append(body);
        } else {
            return new Block(stmt.toString());
        }
    }

    private Block printVarDecl(JCTree.JCVariableDecl var) {
        Block result;
        if ((var.mods.flags & Flags.ENUM) != 0) {
            result = new Block(printName(var.name));
            if (var.init != null) {
                if (var.init instanceof JCTree.JCNewClass) {
                    JCTree.JCNewClass nc = (JCTree.JCNewClass) var.init;
                    if (nc.args != null && nc.args.nonEmpty()) {
                        result = result.append("(");
                        result = printWithSeparatorSorted(result, nc.args, ", ", this::printExpression);
                        result = result.append(")");
                    }
                    if (nc.def != null && nc.def.defs != null) {
                        result = result.append(" {");
                        result = result.appendln(printMembers(nc.def.defs).indent(tab)).appendln(new Block("}"));
                    }
                }
            }
        } else {
            result = isStatic(var.mods.flags);
            if ((var.mods.flags & Flags.VARARGS) != 0) {
                JCTree.JCArrayTypeTree arrayType = (JCTree.JCArrayTypeTree) var.vartype;
                result = printExpression(arrayType.elemtype).append("... ");
            } else {
                if (var.vartype != null) {
                    Block type = printExpression(var.vartype, true);
                    result = result.append(type).append(" ");
                }
            }
            result = result.append(printName(var.name));
            if (var.init != null) {
                Block init = printExpression(var.init);
                result = result.append(" = ").append(init);
            }
        }
        return result;
    }

    private Block printBlock(JCTree.JCBlock block) {
        Block stat = isStatic(block.flags);
        return stat.append(printBlock(block.stats));
    }

    private Block printBlock(List<JCTree.JCStatement> stats) {
        Block result = Block.EMPTY;
        for (JCTree.JCStatement stat : stats) {
            result = result.appendln(printStatement(stat));
        }
        return new Block("{").appendln(result.indent(tab)).appendln(new Block("}"));
    }

    private Block printMethod(JCTree.JCMethodDecl method) {
        Block result = isStatic(method.mods.flags);
        if (method.typarams.nonEmpty()) {
            result = result.append("<");
            result = printWithSeparatorSorted(result, method.typarams, ", ", param -> printTree(param, true));
            result = result.append("> ");
        }
        if (method.name == method.name.table.names.init) {
            result = result.append(printName(method.name));
        } else {
            Block type = printExpression(method.restype, true);
            result = result.append(type).append(" " + printName(method.name));
        }
        result = result.append("(");
        if (method.recvparam != null) {
            result = result.append(printVarDecl(method.recvparam));
            if (method.params.nonEmpty()) {
                result = result.append(", ");
            }
        }
        result = printWithSeparatorSorted(result, method.params, ", ", this::printVarDecl);
        result = result.append(")");
        if (method.defaultValue != null) {
            result = result.append(" default ").append(printExpression(method.defaultValue));
        }
        if (method.body != null) {
            result = result.append(" ").append(printBlock(method.body));
        } else {
            result = result.append(";");
        }
        return result;
    }

    private Block printClass(JCTree.JCClassDecl cls) {
        Block result = isStatic(cls.mods.flags);
        boolean isIface = (cls.mods.flags & Flags.INTERFACE) != 0;
        boolean isEnum = (cls.mods.flags & Flags.ENUM) != 0;
        if (isIface) {
            result = result.append("interface " + printName(cls.name));
        } else if (isEnum) {
            result = result.append("enum " + printName(cls.name));
        } else {
            result = result.append("class " + printName(cls.name));
        }
        if (cls.typarams.nonEmpty()) {
            result = result.append("<");
            result = printWithSeparatorSorted(result, cls.typarams, ", ", param -> printTree(param, true));
            result = result.append(">");
        }
        if (isIface) {
            if (cls.implementing.nonEmpty()) {
                result = result.append(" extends ");
                result = printWithSeparatorUnordered(result, cls.implementing, ", ", iface -> printExpression(iface, true));
            }
        } else {
            if (cls.extending != null) {
                Block extend = printExpression(cls.extending, true);
                result = result.append(" extends ").append(extend);
            }
            if (cls.implementing.nonEmpty()) {
                result = result.append(" implements ");
                result = printWithSeparatorUnordered(result, cls.implementing, ", ", iface -> printExpression(iface, true));
            }
        }
        result = result.append(" {").appendln(printMembers(cls.defs).indent(tab));
        return result.appendln(new Block("}"));
    }

    public Block printUnit(JCTree.JCCompilationUnit jcu) {
        return printMembers(jcu.defs);
    }

    public java.util.List<SourceClass> parseUnit(JCTree.JCCompilationUnit jcu) {
        java.util.List<SourceClass> classes = new ArrayList<>();
        List<JCTree> defs = jcu.defs;
        for (JCTree def : defs) {
            if (def instanceof JCTree.JCClassDecl) {
                JCTree.JCClassDecl cls = (JCTree.JCClassDecl) def;
                Block code = printClass(cls);
                classes.add(new SourceClass(cls.name.toString(), code));
            }
        }
        return classes;
    }

    public static ParserFactory createFactory() {
        Context ctx = new Context();
        JavaFileManager fileManager = ToolProvider.getSystemJavaCompiler().getStandardFileManager(null, null, null);
        ctx.put(JavaFileManager.class, fileManager);
        return ParserFactory.instance(ctx);
    }
}
