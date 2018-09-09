/*
 * Copyright 2008-2010 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

/*
 * MemoryJavaFileManager.java
 * @author A. Sundararajan
 */
package com.bytelightning.oss.lib.fio;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.nio.CharBuffer;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Map;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.SimpleJavaFileObject;

/**
 * JavaFileManager that keeps compiled .class bytes in memory. And also can expose input .java "files" from Strings.
 *
 * @author A. Sundararajan
 */
public final class MemoryJavaFileManager extends ForwardingJavaFileManager<JavaFileManager> {

	public MemoryJavaFileManager(ClassLoader cl, ProtectionDomain domain, JavaFileManager fileManager) {
		super(fileManager);
		classBytes = new HashMap<String, byte[]>();
		this.cl = cl;
		this.domain = domain;
	}
	ClassLoader cl;
	ProtectionDomain domain;

	public Map<String, byte[]> getClassBytes() {
		return classBytes;
	}
	private Map<String, byte[]> classBytes;

	@Override
	public void close() throws IOException {
		classBytes = new HashMap<String, byte[]>();
	}

	@Override
	public void flush() throws IOException {
	}

	/**
	 * A file object used to represent Java source coming from a string.
	 */
	public static class StringInputBuffer extends SimpleJavaFileObject {
		public StringInputBuffer(String name, String code) {
			super(toURI(name), Kind.SOURCE);
			this.code = code;
		}
		final String code;

		@Override
		public CharBuffer getCharContent(boolean ignoreEncodingErrors) {
			return CharBuffer.wrap(code);
		}
		public Reader openReader() {
			return new StringReader(code);
		}
	}

	/**
	 * A file object that stores Java bytecode into the classBytes map.
	 */
	private class ClassOutputBuffer extends SimpleJavaFileObject {

		private String name;

		ClassOutputBuffer(String name) {
			super(toURI(name), Kind.CLASS);
			this.name = name;
		}

		@Override
		public OutputStream openOutputStream() {
			return new FilterOutputStream(new ByteArrayOutputStream()) {

				@Override
				public void close() throws IOException {
					out.close();
					ByteArrayOutputStream bos = (ByteArrayOutputStream) out;
					classBytes.put(name, bos.toByteArray());
				}
			};
		}
	}

	@Override
	public JavaFileObject getJavaFileForOutput(JavaFileManager.Location location, String className, Kind kind, FileObject sibling) throws IOException {
		if (kind == Kind.CLASS) {
			return new ClassOutputBuffer(className);
		}
		else {
			return super.getJavaFileForOutput(location, className, kind, sibling);
		}
	}

	@Override
	public JavaFileObject getJavaFileForInput(JavaFileManager.Location location, String className, Kind kind) throws IOException {
		JavaFileObject result = super.getJavaFileForInput(location, className, kind);
		return result;
	}

	static URI toURI(String name) {
		File file = new File(name);
		if (file.exists())
			return file.toURI();
		else {
			try {
				return URI.create("mfm:///" + name);
			}
			catch (Exception exp) {
				return URI.create("mfm:///com/sun/script/java/java_source");
			}
		}
	}

	@Override
	public ClassLoader getClassLoader(Location location) {
		return new java.security.SecureClassLoader(cl) {
			@SuppressWarnings("rawtypes")
			public Class defineClass(String name, byte[] b) {
				if (domain != null)
					return defineClass(name, b, 0, b.length, domain);
				return defineClass(name, b, 0, b.length);
			}
			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			protected Class findClass(String name) throws ClassNotFoundException {
				byte[] b = classBytes.get(name);
				if (b != null)
					return defineClass(name, b);
				return super.findClass(name);
			}};
	}
}
