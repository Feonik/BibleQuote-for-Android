/*
 * Copyright (C) 2011 Scripture Software (http://scripturesoftware.org/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.BibleQuote.modules;

import com.BibleQuote.exceptions.FileAccessException;
import com.BibleQuote.utils.FsUtils;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author Yakushev Vladimir, Sergey Ursul
 */
public class FsModule extends Module {

	private static final long serialVersionUID = -660821372799486761L;

	/**
	 * modulePath is a directory path or an archive path with a name
	 */
	public final String modulePath;
	public final String versificationFileName;
	public final String stylesCssFileName;

	/**
	 * Имя ini-файла (раскладка в названии файла может быть произвольной)
	 */
	public final String iniFileName;

	public final Boolean isArchive;


	public FsModule(String iniFilePath) {

		modulePath = iniFilePath.substring(0, iniFilePath.lastIndexOf(File.separator));
		iniFileName = iniFilePath.substring(iniFilePath.lastIndexOf(File.separator) + 1);
		isArchive = modulePath.toLowerCase().endsWith(".zip");


		// Таблица версификации должна быть в корне модуля
		// (там же, где и bibleqt.ini) с именем файла "versmap*.xml",
		// такой файл должен быть только один.

		// Файл с описанием стилей должен быть в корне модуля
		// (там же, где и bibleqt.ini) с именем файла "styles*.css",
		// такой файл должен быть только один.

		final String sVersMapNameStart = "versmap";
		final String sVersMapNameEnd = ".xml";

		final String sStylesCssNameStart = "styles";
		final String sStylesCssNameEnd = ".css";

		if (isArchive) {

			boolean isZipVersMap = false;
			String sZipVersMapName = null;

			boolean isZipStylesCss = false;
			String sZipStylesCssName = null;

			File zipFile = new File(modulePath);
			try {
				InputStream moduleStream = new FileInputStream(zipFile);
				ZipInputStream zStream = new ZipInputStream(moduleStream);
				ZipEntry entry;
				while ((entry = zStream.getNextEntry()) != null) {
					String entryName = entry.getName().toLowerCase();

					//if (entryName.contains(File.separator)) {
					//	entryName = entryName.substring(entryName.lastIndexOf(File.separator) + 1);
					//}

					// файл "versmap*.xml" должен быть только там же,
					// где и bibleqt.ini
					if (entryName.startsWith(sVersMapNameStart)
							&& entryName.endsWith(sVersMapNameEnd)) {

						isZipVersMap = !isZipVersMap;

						// файл "versmap*.xml" должен быть только один
						if (!isZipVersMap) {
							break;
						}

						sZipVersMapName = entryName;
					}

					// файл "styles*.css" должен быть только там же,
					// где и bibleqt.ini
					if (entryName.startsWith(sStylesCssNameStart)
							&& entryName.endsWith(sStylesCssNameEnd)) {

						isZipVersMap = !isZipVersMap;

						// файл "styles*.css" должен быть только один
						if (!isZipStylesCss) {
							break;
						}

						sZipStylesCssName = entryName;
					}
				}

			} catch (FileNotFoundException e) {
				// TODO разделить обработку для styles и versmap
				isZipVersMap = false;
				isZipStylesCss = false;

				// TODO заменить e.printStackTrace()
				//e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			} catch (IOException e) {
				// TODO разделить обработку для styles и versmap
				isZipVersMap = false;
				isZipStylesCss = false;

				// TODO заменить e.printStackTrace()
				//e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			}


			if (isZipVersMap) {
				versificationFileName = sZipVersMapName;
			} else {
				versificationFileName = null;
			}


			if (isZipStylesCss) {
				stylesCssFileName = sZipStylesCssName;
			} else {
				stylesCssFileName = null;
			}

		} else { //  if (!isArchive)

			File dirModule = new File(modulePath);


			FilenameFilter fnfVersMap = new FilenameFilter() {
				public boolean accept(File dir, String name) {
					name = name.toLowerCase();

					return name.startsWith(sVersMapNameStart)
							&& name.endsWith(sVersMapNameEnd);
				}
			};

			String[] saVersMapFileNames = dirModule.list(fnfVersMap);

			// файл "versmap*.xml" должен быть только один
			if (saVersMapFileNames.length == 1) {
				versificationFileName = saVersMapFileNames[0];
			} else {
				versificationFileName = null;
			}


			FilenameFilter fnfStylesCss = new FilenameFilter() {
				public boolean accept(File dir, String name) {
					name = name.toLowerCase();

					return name.startsWith(sStylesCssNameStart)
							&& name.endsWith(sStylesCssNameEnd);
				}
			};

			String[] saStylesCssFileNames = dirModule.list(fnfStylesCss);

			// файл "styles*.css" должен быть только один
			if (saStylesCssFileNames.length == 1) {
				stylesCssFileName = saStylesCssFileNames[0];
			} else {
				stylesCssFileName = null;
			}
		}


		versificationMap = null;
	}

	@Override
	public String getDataSourceID() {
		return this.modulePath + File.separator + this.iniFileName;
	}

	@Override
	public String getID() {
		return ShortName.toUpperCase();
	}

	@Override
	public VersificationMap getVersificationMap() {

		if (versificationMap == null) {

			BufferedReader brVersificationFile = null;

			if (versificationFileName != null) {
				try {

					if (isArchive) {

						brVersificationFile = FsUtils.getTextFileReaderFromZipArchive(
								  modulePath, versificationFileName, "UTF-8");

					} else {

						brVersificationFile = new BufferedReader(new FileReader(modulePath + File.separator
								  + versificationFileName));
					}

				} catch (FileNotFoundException e) {
					brVersificationFile = null;
					// TODO заменить e.printStackTrace()
					//e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
				} catch (FileAccessException e) {
					brVersificationFile = null;
					// TODO заменить e.printStackTrace()
					//e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
				}
			}

			versificationMap = new VersificationMap(brVersificationFile);
		}

		return versificationMap;
	}


	@Override
	public String getStyleCss() {

		if (styleCss == null) {

			BufferedReader brStyleCssFile = null;

			if (stylesCssFileName != null) {
				try {

					if (isArchive) {

						brStyleCssFile = FsUtils.getTextFileReaderFromZipArchive(
								modulePath, stylesCssFileName, "UTF-8");

					} else {

						brStyleCssFile = new BufferedReader(new FileReader(modulePath + File.separator
								+ stylesCssFileName));
					}

				} catch (FileNotFoundException e) {
					brStyleCssFile = null;
					// TODO заменить e.printStackTrace()
					//e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
				} catch (FileAccessException e) {
					brStyleCssFile = null;
					// TODO заменить e.printStackTrace()
					//e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
				}
			}


			if (brStyleCssFile != null) {

				try {

					String str;
					// TODO StringBuilder(200) - size ?
					StringBuilder sbBuf = new StringBuilder(200);

					while (null != (str = brStyleCssFile.readLine())) {
						sbBuf.append(str);
						sbBuf.append("\r\n");
					}

					styleCss = sbBuf.toString();


				} catch (IOException e) {
					styleCss = null;

					// TODO заменить e.printStackTrace()
					e.printStackTrace();
				}
			}
		}

		return (styleCss == null) ? "" : styleCss;
	}
}
