package com.BibleQuote.controllers;

import com.BibleQuote.exceptions.BookNotFoundException;
import com.BibleQuote.modules.Book;
import com.BibleQuote.modules.Chapter;
import com.BibleQuote.modules.Module;

import java.util.ArrayList;

public interface IChapterController {

	public ArrayList<Chapter> getChapterList(Book book) throws BookNotFoundException;

	public Chapter getChapter(Book book, Integer chapterNumber, Boolean isReload) throws BookNotFoundException;

	public boolean saveChapter(Chapter chapter);

	public ArrayList<Integer> getVerseNumbers(Book book, Integer chapterNumber) throws BookNotFoundException;

	public String getChapterHTMLView(Chapter chapter);

	public String getVerseTextHtmlBody(Module module, String sVerseText);
}
