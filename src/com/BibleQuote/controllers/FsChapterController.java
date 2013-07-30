package com.BibleQuote.controllers;

import com.BibleQuote.dal.LibraryUnitOfWork;
import com.BibleQuote.dal.repository.IBookRepository;
import com.BibleQuote.dal.repository.IChapterRepository;
import com.BibleQuote.exceptions.BookNotFoundException;
import com.BibleQuote.modules.*;
import com.BibleQuote.utils.StringProc;

import java.util.ArrayList;

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


	public Chapter getChapter(Book book, Integer chapterNumber, Boolean isReload) throws BookNotFoundException {
		book = getValidBook(book);
		Chapter chapter;

		if (isReload) {
			chapter = chRepository.loadChapter((FsBook) book, chapterNumber);
		} else {
			chapter = chRepository.getChapterByNumber((FsBook) book, chapterNumber);
			if (chapter == null) {
				chapter = chRepository.loadChapter((FsBook) book, chapterNumber);
			}
		}

		return chapter;
	}


	public boolean saveChapter(Chapter chapter) {
		return chRepository.saveChapter(chapter);
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
			chapterHTML.append("<div id=\"verse_")
					  .append(verse)
					  .append("\" class=\"verse\">")
					  .append(getVerseTextHtmlBody(currModule, verses.get(verse - 1).getText()))
					  .append("</div>")
					  .append("\r\n");
		}

		return chapterHTML.toString();
	}


	public String getVerseTextHtmlBody(Module module, String sVerseText) {

		if (module == null || sVerseText == null || sVerseText.length() == 0) {
			return "";
		}

		if (module.containsStrong) {
			// убираем номера Стронга
			sVerseText = sVerseText.replaceAll("\\s(\\d)+", "");
		}

		sVerseText = StringProc.stripTags(sVerseText, module.HtmlFilter);
		sVerseText = sVerseText.replaceAll("<a\\s+?href=\"verse\\s\\d+?\">(\\d+?)</a>", "<b>$1</b>");

		if (module.isBible) {
			sVerseText = sVerseText
					  .replaceAll("(^|\\n)(<[^/]+?>)*?(\\d+)(</(.)+?>){0,1}?\\s+", "$1$2<b>$3</b>$4 ")
					  .replaceAll("null", "");
		}

		return sVerseText.replaceAll("<(/)*div(.*?)>", "<$1p$2>");
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
