package net.the_last_sword.agent;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

public class HealthGetterTransformer implements ClassFileTransformer {

    //LivingEntity.getHealth() 的 SRG 混淆名
    private static final String TARGET_METHOD_NAME = "m_21223_";
    private static final String TARGET_METHOD_DESC = "()F";

    public HealthGetterTransformer() {
        //静默初始化，不输出日志
    }

    @Override
    public byte[] transform(
            ClassLoader loader,
            String className,
            Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain,
            byte[] classfileBuffer
    ) {
        //跳过 null 类名
        if (className == null) {
            return null;
        }

        //只处理非我们自己的 mod
        if (className.startsWith("net/the_last_sword/")) {
            return null;
        }

        //跳过 JDK 内部类和常见库类（这些不会重写 getHealth）
        if (className.startsWith("java/") ||
                className.startsWith("javax/") ||
                className.startsWith("sun/") ||
                className.startsWith("jdk/") ||
                className.startsWith("com/sun/") ||
                className.startsWith("org/objectweb/asm/")) {
            return null;
        }

        try {
            //第一遍：快速扫描，检查类是否定义了目标方法
            ClassReader scanReader = new ClassReader(classfileBuffer);
            MethodFinder finder = new MethodFinder();
            scanReader.accept(finder, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

            //如果没有定义目标方法，直接返回
            if (!finder.hasTargetMethod) {
                return null;
            }

            //第二遍：实际转换
            ClassReader cr = new ClassReader(classfileBuffer);
            ClassWriter cw = new SafeClassWriter(cr, ClassWriter.COMPUTE_MAXS);
            HealthGetterClassVisitor cv = new HealthGetterClassVisitor(cw, className);
            cr.accept(cv, ClassReader.EXPAND_FRAMES);

            //如果成功 hook 了方法
            if (cv.hookedGetHealth) {
                byte[] transformedBytes = cw.toByteArray();
                AgentLogWriter.log("INFO", "[Agent] Hooked getHealth() in: " + className);
                return transformedBytes;
            }
            return null;

        } catch (Throwable t) {
            AgentLogWriter.log("ERROR", "[Agent] Failed to transform class: " + className, t);
            return null;
        }
    }

    //快速扫描类是否定义了目标方法
    private static class MethodFinder extends ClassVisitor {
        boolean hasTargetMethod = false;

        MethodFinder() {
            super(Opcodes.ASM9);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            if (name.equals(TARGET_METHOD_NAME) && descriptor.equals(TARGET_METHOD_DESC)) {
                hasTargetMethod = true;
            }
            return null; //不需要访问方法体
        }
    }

    //安全的 ClassWriter，避免类加载问题
    private static class SafeClassWriter extends ClassWriter {
        private final ClassReader classReader;

        SafeClassWriter(ClassReader classReader, int flags) {
            super(classReader, flags);
            this.classReader = classReader;
        }

        @Override
        protected String getCommonSuperClass(String type1, String type2) {
            //避免在 Agent 中加载类，直接返回 Object
            return "java/lang/Object";
        }
    }

    //ClassVisitor：遍历类的所有方法
    private static class HealthGetterClassVisitor extends ClassVisitor {
        private final String className;
        public boolean hookedGetHealth = false;

        public HealthGetterClassVisitor(ClassWriter cw, String className) {
            super(Opcodes.ASM9, cw);
            this.className = className;
        }

        @Override
        public MethodVisitor visitMethod(
                int access,
                String name,
                String descriptor,
                String signature,
                String[] exceptions
        ) {
            MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);

            //只拦截特定方法名 + ()F 签名
            if (name.equals(TARGET_METHOD_NAME) && descriptor.equals(TARGET_METHOD_DESC)) {
                hookedGetHealth = true;
                return new HealthGetterMethodVisitor(mv, className);
            }

            return mv;
        }
    }

    //MethodVisitor：在返回前拦截并修改返回值
    private static class HealthGetterMethodVisitor extends MethodVisitor {
        private final String className;

        public HealthGetterMethodVisitor(MethodVisitor mv, String className) {
            super(Opcodes.ASM9, mv);
            this.className = className;
        }

        @Override
        public void visitInsn(int opcode) {
            if (opcode == Opcodes.FRETURN) {
                //在返回前修改栈顶的返回值
                //栈：[原始血量] → [原始血量, this, className] → [最终血量]
                mv.visitVarInsn(Opcodes.ALOAD, 0);  // 加载 this
                mv.visitLdcInsn(className.replace('/', '.'));  // 加载类名（使用点号格式）
                mv.visitMethodInsn(
                        Opcodes.INVOKESTATIC,
                        "net/the_last_sword/agent/HealNegationChecker",
                        "processGetHealth",
                        "(FLnet/minecraft/world/entity/LivingEntity;Ljava/lang/String;)F",
                        false
                );
            }
            super.visitInsn(opcode);
        }
    }
}