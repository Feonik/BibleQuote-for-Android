package com.BibleQuote.controllers;

import com.BibleQuote.dal.LibraryUnitOfWork;
import com.BibleQuote.dal.repository.IBookRepository;
import com.BibleQuote.dal.repository.IChapterRepository;
import com.BibleQuote.exceptions.BookNotFoundException;
import com.BibleQuote.modules.*;
import com.BibleQuote.utils.StringProc;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FsChapterController implements IChapterController {
	//private final String TAG = "FsChapterController";

	private IBookRepository<FsModule, FsBook> bRepository;
	private IChapterRepository<FsBook> chRepository;


	public FsChapterController(LibraryUnitOfWork unit) {
		bRepository = unit.getBookRepository();
		chRepository = unit.getChapterRepository();
	}


	public ArrayList<Chapter> getChapterList(Book book) throws BookNotFoundException {
		book = getValidBook(book);
		ArrayList<Chapter> chapterList = (ArrayList<Chapter>) chRepository.getChapters((FsBook) book);
		if (chapterList.size() == 0) {
			chapterList = (ArrayList<Chapter>) chRepository.loadChapters((FsBook) book);
		}
		return chapterList;
	}


	public Chapter getChapter(Book book, Integer chapterNumber) throws BookNotFoundException {
		book = getValidBook(book);
		Chapter chapter = chRepository.getChapterByNumber((FsBook) book, chapterNumber);
		if (chapter == null) {
			chapter = chRepository.loadChapter((FsBook) book, chapterNumber);
		}
		return chapter;
	}


	public ArrayList<Integer> getVerseNumbers(Book book, Integer chapterNumber) throws BookNotFoundException {
		book = getValidBook(book);
		Chapter chapter = chRepository.getChapterByNumber((FsBook) book, chapterNumber);
		if (chapter == null) {
			chapter = chRepository.loadChapter((FsBook) book, chapterNumber);
		}
		return chapter.getVerseNumbers();
	}


	public String getChapterHTMLView(Chapter chapter) {
		if (chapter == null) {
			return "";
		}
		Module currModule = chapter.getBook().getModule();

		ArrayList<Verse> verses = chapter.getVerseList();
		StringBuilder chapterHTML = new StringBuilder();
		for (int verse = 1; verse <= verses.size(); verse++) {
			String verseText = verses.get(verse - 1).getText();

			if (currModule.containsStrong) {
				// убираем номера Стронга
				verseText = verseText.replaceAll("\\s(\\d)+", "");
			}

			verseText = StringProc.stripTags(verseText, currModule.HtmlFilter);
			verseText = verseText.replaceAll("<a\\s+?href=\"verse\\s\\d+?\">(\\d+?)</a>", "<b>$1</b>");

			StringBuilder sbNewVerseText = new StringBuilder();

			if (currModule.isBible) {



				//verseText = "<p><cite style='display: inline-block; vertical-align: top; font-style: normal; padding: 0.25em 0.25em'><br>25</cite> <cite style='display: inline-block; vertical-align: text-bottom; font-style: normal; padding: 0.25em 0.25em'>ϐίβλος<br>Книга<br>c<br>976</cite><cite style='display: inline-block; vertical-align: text-bottom; font-style: normal; padding: 0.25em 0.25em'>γενέσεως<br>родословия<br>ngfs<br>1078</cite><cite style='display: inline-block; vertical-align: text-bottom; font-style: normal; padding: 0.25em 0.25em'>ἰησοῦ<br>Иисуса<br>ngms<br>2424</cite><cite style='display: inline-block; vertical-align: text-bottom; font-style: normal; padding: 0.25em 0.25em'>χριστοῦ<br>Христа,<br>ngms<br>5547</cite><cite style='display: inline-block; vertical-align: text-bottom; font-style: normal; padding: 0.25em 0.25em'>υἱοῦ<br>Сына<br>ngms<br>5207</cite><cite style='display: inline-block; vertical-align: text-bottom; font-style: normal; padding: 0.25em 0.25em'>⌐δαβὶδ¬<br>Давидова,<br>tp<br>1138</cite><cite style='display: inline-block; vertical-align: text-bottom; font-style: normal; padding: 0.25em 0.25em'>υἱοῦ<br>Сына<br>ngms<br>5207</cite><cite style='display: inline-block; vertical-align: text-bottom; font-style: normal; padding: 0.25em 0.25em'>ἀβραάµ<br>Авраамова.<br>c<br>11</cite>";



				int iVerseTextRegionStart = 0;
				int iVerseTextRegionEnd = verseText.length();

				Matcher matcher = Pattern.compile("(^<[^/]+?>)*?(\\d+)(</(.)+?>){0,1}?\\s+").matcher(verseText);

				if (matcher.lookingAt()) {

					sbNewVerseText
							  .append(verseText.substring(iVerseTextRegionStart, matcher.start()))
							  .append((matcher.group(1) == null) ? "" : matcher.group(1))
							  .append("<b>")
							  .append((matcher.group(2) == null) ? "" : matcher.group(2))
							  .append("</b>")
							  .append((matcher.group(3) == null) ? "" : matcher.group(3))
							  .append(" ");

					iVerseTextRegionStart = matcher.end();
				}


				int iTagRegionStart = iVerseTextRegionStart;
				int iTagRegionEnd = iVerseTextRegionStart;
				int iMcrEnd = iVerseTextRegionStart;

				matcher.usePattern(Pattern.compile("(^|\\G|[^\\p{L}\\d])([\\p{L}\\d]+?)([^\\p{L}\\d]|$)"));

				if ((iTagRegionStart = verseText.indexOf("<", iVerseTextRegionStart)) != -1) {

					matcher.region(iVerseTextRegionStart, iTagRegionStart);
					while (matcher.find()) {
						sbNewVerseText
								  .append((matcher.group(1) == null) ? "" : matcher.group(1))
								  .append("<span>")
								  .append((matcher.group(2) == null) ? "" : matcher.group(2))
								  .append("</span>")
								  .append((matcher.group(3) == null) ? "" : matcher.group(3));
						iMcrEnd = matcher.end();
					}

					sbNewVerseText
							  .append(verseText.substring(iMcrEnd, iTagRegionStart));

					if ((iTagRegionEnd = verseText.indexOf(">", iTagRegionStart)) != -1) {
						sbNewVerseText
								  .append(verseText.substring(iTagRegionStart, iTagRegionEnd + 1));
					} else {
						return "";
					}


					while ((iTagRegionStart = verseText.indexOf("<", iTagRegionEnd)) != -1) {

						matcher.region(iTagRegionEnd + 1, iTagRegionStart);
						iMcrEnd = iTagRegionEnd + 1;
						while (matcher.find()) {
							sbNewVerseText
									  .append((matcher.group(1) == null) ? "" : matcher.group(1))
									  .append("<span>")
									  .append((matcher.group(2) == null) ? "" : matcher.group(2))
									  .append("</span>")
									  .append((matcher.group(3) == null) ? "" : matcher.group(3));
							iMcrEnd = matcher.end();
						}
						sbNewVerseText
								  .append(verseText.substring(iMcrEnd, iTagRegionStart));

						if ((iTagRegionEnd = verseText.indexOf(">", iTagRegionStart)) != -1) {
							sbNewVerseText
									  .append(verseText.substring(iTagRegionStart, iTagRegionEnd + 1));
						} else {
							return "";
						}
					}


					matcher.region(iTagRegionEnd + 1, iVerseTextRegionEnd);
					iMcrEnd = iTagRegionEnd + 1;
					while (matcher.find()) {
						sbNewVerseText
								  .append((matcher.group(1) == null) ? "" : matcher.group(1))
								  .append("<span>")
								  .append((matcher.group(2) == null) ? "" : matcher.group(2))
								  .append("</span>")
								  .append((matcher.group(3) == null) ? "" : matcher.group(3));
						iMcrEnd = matcher.end();
					}
					sbNewVerseText
							  .append(verseText.substring(iMcrEnd, iVerseTextRegionEnd));


				} else {
					matcher.region(iVerseTextRegionStart, iVerseTextRegionEnd);
					while (matcher.find()) {
						sbNewVerseText
								  .append((matcher.group(1) == null) ? "" : matcher.group(1))
								  .append("<span>")
								  .append((matcher.group(2) == null) ? "" : matcher.group(2))
								  .append("</span>")
								  .append((matcher.group(3) == null) ? "" : matcher.group(3));
						iMcrEnd = matcher.end();
					}
					sbNewVerseText
							  .append(verseText.substring(iMcrEnd, iVerseTextRegionEnd));
				}


//				verseText = verseText
//						.replaceAll("^(<[^/]+?>)*?(\\d+)(</(.)+?>){0,1}?\\s+",
//								"$1<b>$2</b>$3 ").replaceAll(
//								"null", "").replaceAll("([>\\s]\\b)([\\p{L}]+?)(\\b)",
//								"$1<span>$2</span>$3");
//								//"$1<span onDblClick='dblClickWord(this)'>$2</span>$3");
			}

			chapterHTML.append(
					  "<div id=\"verse_" + verse + "\" class=\"verse\">"
								 + sbNewVerseText.toString().replaceAll("<(/)*div(.*?)>", "<$1p$2>")
								 + "</div>"
								 + "\r\n");
		}

		return chapterHTML.toString();
	}


	private Book getValidBook(Book book) throws BookNotFoundException {
		String moduleID = null;
		String bookID = null;
		try {
			Module module = book.getModule();
			book = bRepository.getBookByID((FsModule) module, book.getID());
			if (book == null) {
				throw new BookNotFoundException(moduleID, bookID);
			}
		} catch (Exception e) {
			throw new BookNotFoundException(moduleID, bookID);
		}
		return book;
	}
}
