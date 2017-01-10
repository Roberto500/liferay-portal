/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portal.tools.soy.builder.util;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Andrea Di Giorgi
 * @author Gregory Amerson
 */
public class FileTestUtil {

	public static void copy(File sourceFile, File destinationFile)
		throws IOException {

		if (sourceFile.isFile()) {
			FileOutputStream fileOutputStream = new FileOutputStream(
				destinationFile);

			try {
				_copy(new FileInputStream(sourceFile), fileOutputStream);
			}
			finally {
				fileOutputStream.close();
			}
		}
		else if (sourceFile.isDirectory()) {
			if (!destinationFile.exists() && !destinationFile.mkdirs()) {
				throw new IOException(
					"Could not create directory " + destinationFile);
			}

			if (!destinationFile.isDirectory()) {
				throw new IllegalArgumentException(
					"target directory for a directory must be a directory: " +
						destinationFile);
			}

			if (_isParentOf(sourceFile, destinationFile)) {
				throw new IllegalArgumentException(
					"target directory can not be child of source directory.");
			}

			File[] files = sourceFile.listFiles();

			for (File file : files) {
				copy(file, new File(destinationFile, file.getName()));
			}
		}
		else {
			throw new FileNotFoundException(
				"During copy: " + sourceFile.toString());
		}
	}

	public static void delete(File file) throws IOException {
		file = file.getAbsoluteFile();

		if (!file.exists()) {
			return;
		}

		if (file.getParentFile() == null) {
			throw new IllegalArgumentException(
				"Cannot recursively delete root for safety reasons");
		}

		boolean wasDeleted = true;

		if (file.isDirectory()) {
			File[] files = file.listFiles();

			for (File sub : files) {
				try {
					delete(sub);
				}
				catch (IOException ioe) {
					wasDeleted = false;
				}
			}
		}

		boolean deleted = file.delete();

		if (!deleted || !wasDeleted) {
			throw new IOException("Failed to delete " + file.getAbsoluteFile());
		}
	}

	private static void _copy(InputStream inputStream, DataOutput dataOutput)
		throws IOException {

		byte[] buffer = new byte[4096 * 16];

		try {
			int size = inputStream.read(buffer);

			while (size > 0) {
				dataOutput.write(buffer, 0, size);

				size = inputStream.read(buffer);
			}
		}
		finally {
			inputStream.close();
		}
	}

	private static void _copy(
			InputStream inputStream, OutputStream outputStream)
		throws IOException {

		DataOutputStream dos = new DataOutputStream(outputStream);

		_copy(inputStream, (DataOutput)dos);

		outputStream.flush();
	}

	private static boolean _isParentOf(File a, File b) {
		if ((a == null) || (b == null)) {
			return false;
		}

		if (!a.isDirectory()) {
			return false;
		}

		if (a.equals(b.getParentFile())) {
			return true;
		}

		return _isParentOf(a, b.getParentFile());
	}

}