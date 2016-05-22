/*
 * Javassist, a Java-bytecode translator toolkit.
 * Copyright (C) 1999- Shigeru Chiba. All Rights Reserved.
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License.  Alternatively, the contents of this file may be used under
 * the terms of the GNU Lesser General Public License Version 2.1 or later,
 * or the Apache License Version 2.0.
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 */

package javassist.bytecode;

import java.io.PrintWriter;
import javassist.Modifier;
import java.util.List;

/**
 * A utility class for priting the contents of a class file.
 * It prints a constant pool table, fields, and methods in a
 * human readable representation.
 */
public class ClassFilePrinter {
    /**
     * Prints the contents of a class file to the standard output stream.
     */
    public static void print(ClassFile cf) {
        print(cf, new PrintWriter(System.out, true));
    }

    /**
     * Prints the contents of a class file.
     */
    public static void print(ClassFile cf, PrintWriter out) {
        /* 0x0020 (SYNCHRONIZED) means ACC_SUPER if the modifiers
         * are of a class.
         */
		int mod = AccessFlag.toModifier(cf.getAccessFlags() & ~AccessFlag.SYNCHRONIZED);
        out.println(new StringBuffer(80)
        		.append("major: ").append(cf.major)
        		.append(", minor: ").append(cf.minor)
        		.append(" modifiers: ").append(Integer.toHexString(cf.getAccessFlags()))
        		.append(System.getProperty("line.separator"))
        		.append(Modifier.toString(mod))
        		.append(" class ").append(cf.getName())
        		.append(" extends ").append(cf.getSuperclass())
        		.toString());

		String[] infs = cf.getInterfaces();
		if (infs != null && infs.length > 0) {
			out.print("    implements ");
			StringBuilder sb = new StringBuilder(infs[0]);
			for (int i = 1, n = infs.length; i < n; ++i)
				sb.append(", ").append(infs[i]);
			
			out.println(sb.toString());
		}
		out.println();
        
        for (FieldInfo finfo : cf.getFields()) {
            int acc = finfo.getAccessFlags();
            out.println(new StringBuffer(Modifier.toString(AccessFlag.toModifier(acc)))
            		.append(' ').append(finfo.getName())
            		.append('\t').append(finfo.getDescriptor())
            		.toString());
            printAttributes(finfo.getAttributes(), out, 'f');
        }
        out.println();
        
        for (MethodInfo minfo : cf.getMethods()) {
            int acc = minfo.getAccessFlags();
            out.println(new StringBuffer(Modifier.toString(AccessFlag.toModifier(acc)))
            		.append(' ').append(minfo.getName())
            		.append('\t').append(minfo.getDescriptor())
            		.toString());
            printAttributes(minfo.getAttributes(), out, 'm');
            out.println();
        }

        out.println();
        printAttributes(cf.getAttributes(), out, 'c');
    }

    static void printAttributes(List<AttributeInfo> list, PrintWriter out, char kind) {
        if (list == null)
            return;

        for (AttributeInfo ai : list) {
            if (ai instanceof CodeAttribute) {
                CodeAttribute ca = (CodeAttribute)ai;
                out.println("attribute: " + ai.getName() + ": "
                            + ai.getClass().getName());
                out.println("max stack " + ca.getMaxStack()
                            + ", max locals " + ca.getMaxLocals()
                            + ", " + ca.getExceptionTable().size()
                            + " catch blocks");
                out.println("<code attribute begin>");
                printAttributes(ca.getAttributes(), out, kind);
                out.println("<code attribute end>");
			} else if (ai instanceof AnnotationsAttribute) {
				out.println("annnotation: " + ai.toString());
			} else if (ai instanceof ParameterAnnotationsAttribute) {
				out.println("parameter annnotations: " + ai.toString());
			} else if (ai instanceof StackMapTable) {
				out.println("<stack map table begin>");
				StackMapTable.Printer.print((StackMapTable) ai, out);
				out.println("<stack map table end>");
			} else if (ai instanceof StackMap) {
                out.println("<stack map begin>");
                ((StackMap)ai).print(out);
                out.println("<stack map end>");
            } else if (ai instanceof SignatureAttribute) {
                SignatureAttribute sa = (SignatureAttribute)ai;
                String sig = sa.getSignature();
                out.println("signature: " + sig);
                try {
                    String s;
                    if (kind == 'c')
                        s = SignatureAttribute.toClassSignature(sig).toString();
                    else if (kind == 'm')
                        s = SignatureAttribute.toMethodSignature(sig).toString();
                    else
                        s = SignatureAttribute.toFieldSignature(sig).toString();

                    out.println("           " + s);
				} catch (BadBytecode e) {
					out.println("           syntax error");
				}
            }
            else
				out.println(new StringBuilder(60)
						.append("attribute: ").append(ai.getName()) 
						.append(" (").append(ai.get().length).append(" byte): ")
						.append(ai.getClass().getName())
						.toString());
        }
    }
}
