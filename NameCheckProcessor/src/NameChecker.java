import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.util.ElementScanner8;
import javax.tools.Diagnostic;
import java.util.EnumSet;

import static java.lang.System.*;
import static javax.lang.model.element.Modifier.*;
import static javax.tools.Diagnostic.Kind.WARNING;

/**
 * 程序名称规范的编译器插件
 * 如果程序命名不合规范，将会输出一个编译器的WARNING信息
 */
public class NameChecker {
    private final Messager messager;

    NameCheckScanner nameCheckScanner = new NameCheckScanner();

    NameChecker(ProcessingEnvironment processingEnv) {
        this.messager = processingEnv.getMessager();
    }

    /**
     * 对Java程序命名进行检查，根据《Java语言规范（第三版）》第6.8节的要求，Java程序命名应当符合下列格式：
     *
     * <uI>
     *     <li> 类或接口：符合驼峰式命名法，首字母大写。
     *     <li> 方法：符合驼峰式命名法，首字母小写。
     *     <li> 字段：
     *     <uI>
     *         <li> 类、实例变量：符合驼峰式命名法，首字母小写。</li>
     *         <li> 常量：要求全部大写</>
     *     </uI>
     * </uI>
     */
    public void checkNames(Element element) {
        nameCheckScanner.scan(element);
    }

    /**
     * 名称检查器实现类，继承了JDK 中提供的ElementScanner8
     * 将会以Visitor模式访问抽象语法树中的元素
     */
    private class NameCheckScanner extends ElementScanner8<Void, Void> {
        /**
         * 此方法用于检查Java类
         */
        @Override
        public Void visitType(TypeElement e, Void p) {
            scan(e.getTypeParameters(), p);
            checkCamelCase(e, true);
            super.visitType(e, p);
            return null;
        }

        /**
         * 检查方法命名是否合法
         */
        @Override
        public Void visitExecutable(ExecutableElement e, Void p) {
            if (e.getKind() == ElementKind.METHOD) {
                Name name = e.getSimpleName();
                if (name.contentEquals(e.getEnclosingElement().getSimpleName())) {
                    messager.printMessage(WARNING, "一个普通方法 " + name +
                            "不应当与类名重复，避免产生与构造函数混淆", e);
                }
                checkCamelCase(e, false);
            }
            super.visitExecutable(e, p);
            return null;
        }

        /**
         * 检查变量命名是否合法
         */
        @Override
        public Void visitVariable(VariableElement e, Void p) {
            //如果这个Variable是枚举或常量，则按大写命名检查，否则按照驼峰式命名规则检查
            if (e.getKind() == ElementKind.ENUM_CONSTANT || e.getConstantValue() != null ||
            heuristicallyConstant(e)) {
                checkAllCaps(e);
            } else {
                checkCamelCase(e, false);
            }
            return null;
        }

        /**
         * 判断一个变量是否是常量
         */
        private boolean heuristicallyConstant(VariableElement e) {
            if (e.getEnclosingElement().getKind() == ElementKind.INTERFACE) {
                return true;
            } else if (e.getKind() == ElementKind.FIELD && e.getModifiers().containsAll(EnumSet.of(PUBLIC, STATIC, FINAL))) {
                return true;
            } else {
                return false;
            }
        }
        //检查传入的Element是否符合驼峰式命名法，如果不符合，则输出警告信息
        public void checkCamelCase(Element e, boolean initialCaps) {
            String name = e.getSimpleName().toString();
            boolean previousUpper = false;
            boolean conventional = true;
            int firstCodePoint = name.codePointAt(0);

            if (Character.isUpperCase(firstCodePoint)){
                previousUpper = true;
                if (!initialCaps){
                    messager.printMessage(WARNING, "名称"+name+"应当以小写字母开头", e);
                    return;
                }
            } else if (Character.isLowerCase(firstCodePoint)){
                if (initialCaps) {
                    messager.printMessage(WARNING, "名称"+name+"应当以大写字母开头", e);
                    return;
                }
            }else
                conventional = false;

            if (conventional) {
                int cp = firstCodePoint;
                for(int i = Character.charCount(cp); i < name.length(); i += Character.charCount(cp)){
                    cp = name.codePointAt(i);
                    if(Character.isUpperCase(cp)){
                        if (previousUpper){
                            conventional = false;
                            break;
                        }
                        previousUpper = true;
                    } else
                        previousUpper = false;
                }
            }

            if (!conventional)
                messager.printMessage(WARNING, "名称"+name+"应当符合驼峰式命名法(Camel case Names)", e);
        }
        //大写命名检查，要求第一个字母必须是大写的英文字母，其余部分可以是下划线或大写字母
        private void checkAllCaps(Element e){
            String name = e.getSimpleName().toString();

            boolean conventional = true;
            int firstCodePoint = name.codePointAt(0);

            if(!Character.isUpperCase(firstCodePoint))
                conventional = false;
            else{
                boolean previousUnderscore = false;
                int cp = firstCodePoint;
                for(int i = Character.charCount(cp);i<name.length();i+=Character.charCount(cp)){
                    cp = name.codePointAt(i);
                    if(cp == (int)'_'){
                        if(previousUnderscore){
                            conventional = false;
                            break;
                        }
                        previousUnderscore = true;
                    }else{
                        previousUnderscore = false;
                        if(Character.isUpperCase(cp)&& !Character.isDigit(cp)){
                            conventional = false;
                            break;
                        }
                    }
                }
            }

            if(!conventional) {
                messager.printMessage(WARNING, "名称" + name + "应当全部以大写字母或下划线命名，并且以字母开头", e);
            }
        }

    }
}
