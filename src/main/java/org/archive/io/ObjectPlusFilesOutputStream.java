/*
 *  This file is part of the Heritrix web crawler (crawler.archive.org).
 *
 *  Licensed to the Internet Archive (IA) by one or more individual 
 *  contributors. 
 *
 *  The IA licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.archive.io;

import java.io.*;
import java.nio.file.Files;
import java.util.LinkedList;

import org.archive.util.FileUtils;


/**
 * Enhanced ObjectOutputStream which maintains (a stack of) auxiliary
 * directories and offers convenience methods for serialized objects
 * to save their related disk files alongside their serialized version.
 *
 * @author gojomo
 */
public class ObjectPlusFilesOutputStream extends ObjectOutputStream {
    protected LinkedList<File> auxiliaryDirectoryStack = new LinkedList<File>();

    /**
     * Constructor
     *
     * @param out
     * @param topDirectory
     * @throws java.io.IOException
     */
    public ObjectPlusFilesOutputStream(OutputStream out, File topDirectory) throws IOException {
        super(out);
        auxiliaryDirectoryStack.addFirst(topDirectory);
    }

    /**
     * Add another subdirectory for any file-capture needs during the
     * current serialization.
     *
     * @param dir
     */
    public void pushAuxiliaryDirectory(String dir) {
        auxiliaryDirectoryStack.addFirst(new File(getAuxiliaryDirectory(),dir));
    }

    /**
     * Remove the top subdirectory.
     *
     */
    public void popAuxiliaryDirectory() {
        auxiliaryDirectoryStack.removeFirst();
    }

    /**
     * Return the current auxiliary directory for storing
     * files associated with serialized objects.
     *
     * @return Auxillary directory.
     */
    public File getAuxiliaryDirectory() {
        return (File)auxiliaryDirectoryStack.getFirst();
    }

    /**
     * Store a snapshot of an object's supporting file to the
     * current auxiliary directory. Should only be used for
     * files which are strictly appended-to, because it tries
     * to use a "hard link" where possible (meaning that
     * future edits to the original file's contents will
     * also affect the snapshot).
     *
     * Remembers current file extent to allow a future restore
     * to ignore subsequent appended data.
     *
     * @param file
     * @throws IOException
     */
    public void snapshotAppendOnlyFile(File file) throws IOException {
        // write filename
        String name = file.getName();
        writeUTF(name);
        // write current file length
        writeLong(file.length());
        File auxDir = getAuxiliaryDirectory();
        if(!auxDir.exists()) {
            FileUtils.ensureWriteableDirectory(auxDir);
        }
        File destination = new File(auxDir,name);
        hardlinkOrCopy(file, destination);
    }

	/**
     * Create a backup of this given file, first by trying a "hard
     * link", then by using a copy if hard linking is unavailable
     * (either because it is unsupported or the origin and checkpoint
     * directories are on different volumes).
     *
	 * @param file
	 * @param destination
     * @throws IOException
	 */
	private void hardlinkOrCopy(File file, File destination) throws IOException {
        try {
            Files.createLink(destination.toPath(), file.toPath());
        } catch (UnsupportedEncodingException e) {
            FileUtils.copyFile(file,destination);
        }
	}
}
