/*
 * Copyright (c) 2005, 2014, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
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
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package com.sun.tools.javac.file;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.Objects;

import javax.tools.JavaFileObject;

import com.sun.tools.javac.util.DefinedBy;
import com.sun.tools.javac.util.DefinedBy.Api;

/**
 * A subclass of JavaFileObject representing regular files.
 *
 * <p><b>This is NOT part of any supported API.
 * If you write code that depends on this, you do so at your own risk.
 * This code and its internal interfaces are subject to change or
 * deletion without notice.</b>
 */
class RegularFileObject extends BaseFileObject {

    /** Have the parent directories been created?
     */
    private boolean hasParents = false;
    private String name;
    final Path file;
    private Reference<Path> absFileRef;
    final static boolean isMacOS = System.getProperty("os.name", "").contains("OS X");

    public RegularFileObject(JavacFileManager fileManager, Path f) {
        this(fileManager, f.getFileName().toString(), f);
    }

    public RegularFileObject(JavacFileManager fileManager, String name, Path f) {
        super(fileManager);
        if (Files.isDirectory(f)) {
            throw new IllegalArgumentException("directories not supported");
        }
        this.name = name;
        this.file = f;
        if (getLastModified() > System.currentTimeMillis())
            fileManager.log.warning("file.from.future", f);
    }

    @Override @DefinedBy(Api.COMPILER)
    public URI toUri() {
        return file.toUri().normalize();
    }

    @Override @DefinedBy(Api.COMPILER)
    public String getName() {
        return file.toString();
    }

    @Override
    public String getShortName() {
        return name;
    }

    @Override @DefinedBy(Api.COMPILER)
    public JavaFileObject.Kind getKind() {
        return getKind(name);
    }

    @Override @DefinedBy(Api.COMPILER)
    public InputStream openInputStream() throws IOException {
        return Files.newInputStream(file);
    }

    @Override @DefinedBy(Api.COMPILER)
    public OutputStream openOutputStream() throws IOException {
        fileManager.flushCache(this);
        ensureParentDirectoriesExist();
        return Files.newOutputStream(file);
    }

    @Override @DefinedBy(Api.COMPILER)
    public CharBuffer getCharContent(boolean ignoreEncodingErrors) throws IOException {
        CharBuffer cb = fileManager.getCachedContent(this);
        if (cb == null) {
            try (InputStream in = Files.newInputStream(file)) {
                ByteBuffer bb = fileManager.makeByteBuffer(in);
                JavaFileObject prev = fileManager.log.useSource(this);
                try {
                    cb = fileManager.decode(bb, ignoreEncodingErrors);
                } finally {
                    fileManager.log.useSource(prev);
                }
                fileManager.recycleByteBuffer(bb);
                if (!ignoreEncodingErrors) {
                    fileManager.cache(this, cb);
                }
            }
        }
        return cb;
    }

    @Override @DefinedBy(Api.COMPILER)
    public Writer openWriter() throws IOException {
        fileManager.flushCache(this);
        ensureParentDirectoriesExist();
        return new OutputStreamWriter(Files.newOutputStream(file), fileManager.getEncodingName());
    }

    @Override @DefinedBy(Api.COMPILER)
    public long getLastModified() {
        try {
            return Files.getLastModifiedTime(file).toMillis();
        } catch (IOException e) {
            return 0;
        }
    }

    @Override @DefinedBy(Api.COMPILER)
    public boolean delete() {
        try {
            Files.delete(file);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    protected CharsetDecoder getDecoder(boolean ignoreEncodingErrors) {
        return fileManager.getDecoder(fileManager.getEncodingName(), ignoreEncodingErrors);
    }

    @Override
    protected String inferBinaryName(Iterable<? extends Path> path) {
        String fPath = file.toString();
        //System.err.println("RegularFileObject " + file + " " +r.getPath());
        for (Path dir: path) {
            //System.err.println("dir: " + dir);
            String sep = dir.getFileSystem().getSeparator();
            String dPath = dir.toString();
            if (dPath.length() == 0)
                dPath = System.getProperty("user.dir");
            if (!dPath.endsWith(sep))
                dPath += sep;
            if (fPath.regionMatches(true, 0, dPath, 0, dPath.length())
                && Paths.get(fPath.substring(0, dPath.length())).equals(Paths.get(dPath))) {
                String relativeName = fPath.substring(dPath.length());
                return removeExtension(relativeName).replace(sep, ".");
            }
        }
        return null;
    }

    @Override @DefinedBy(Api.COMPILER)
    public boolean isNameCompatible(String cn, JavaFileObject.Kind kind) {
        Objects.requireNonNull(cn);
        // null check
        if (kind == Kind.OTHER && getKind() != kind) {
            return false;
        }
        String n = cn + kind.extension;
        if (name.equals(n)) {
            return true;
        }
        if (isMacOS && Normalizer.isNormalized(name, Normalizer.Form.NFD)
            && Normalizer.isNormalized(n, Normalizer.Form.NFC)) {
            // On Mac OS X it is quite possible to file name and class
            // name normalized in a different way - in that case we have to normalize file name
            // to the Normal Form Compised (NFC)
            String normName = Normalizer.normalize(name, Normalizer.Form.NFC);
            if (normName.equals(n)) {
                this.name = normName;
                return true;
            }
        }

            if (name.equalsIgnoreCase(n)) {
            try {
                // allow for Windows
                return file.toRealPath().getFileName().toString().equals(n);
            } catch (IOException e) {
            }
        }
        return false;
    }

    private void ensureParentDirectoriesExist() throws IOException {
        if (!hasParents) {
            Path parent = file.getParent();
            if (parent != null && !Files.isDirectory(parent)) {
                try {
                    Files.createDirectories(parent);
                } catch (IOException e) {
                    throw new IOException("could not create parent directories", e);
                }
            }
            hasParents = true;
        }
    }

    /**
     * Check if two file objects are equal.
     * Two RegularFileObjects are equal if the absolute paths of the underlying
     * files are equal.
     */
    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;

        if (!(other instanceof RegularFileObject))
            return false;

        RegularFileObject o = (RegularFileObject) other;
        return getAbsoluteFile().equals(o.getAbsoluteFile());
    }

    @Override
    public int hashCode() {
        return getAbsoluteFile().hashCode();
    }

    private Path getAbsoluteFile() {
        Path absFile = (absFileRef == null ? null : absFileRef.get());
        if (absFile == null) {
            absFile = file.toAbsolutePath();
            absFileRef = new SoftReference<>(absFile);
        }
        return absFile;
    }
}
